# COMPLETE TECHNICAL DOCUMENTATION - SECURE TASK MANAGER

## 0. RECENT CHANGES (APRIL 30, 2026)

- Client module no longer accesses MongoDB directly; direct Mongo driver and MongoService were removed from ctm_module.
- Backend document API consolidated to a single controller; duplicate controller/service/DTO removed.
- Document API now includes GET latest by document UUID and DELETE by document UUID.
- BlobService now returns HTTP 404 for missing blobs instead of RuntimeException.
- Auth module cleanup: removed redundant BouncyCastle dependency and an unused auth-key derivation step in register.

Notes:

- The backend stores encrypted task blobs in MongoDB and document metadata in MySQL. The UI uses REST only.
- Spring Security requires JWT (Bearer token) for /documents endpoints.

## 1. PROJECT OVERVIEW

The Secure Task Manager is a multi-module Java application designed for collaborative task management with a strong emphasis on security and client-side encryption. It features a modern JavaFX-based desktop client that communicates with a Spring Boot backend, utilizing both MongoDB and MySQL for data storage.

---

# 2. TECHNICAL EXPLANATION

## 2.1 SYSTEM DESIGN

The system follows a distributed client-server model with a focus on data privacy.

- **User Perspective**: The user interacts with a JavaFX desktop application. They must log in or register, which involves generating cryptographic keys locally. Once authenticated, they select a "Workspace" (Team) and manage tasks within that context.
- **UI Perspective**: The UI is built using JavaFX without FXML (pure Java logic). It uses a `StackPane` based overlay system for modals and a `BorderPane` for the main layout.
- **Service Perspective**: Logic is decoupled into services. `TaskService` handles high-level task operations, while `EncryptedTaskService` manages the encryption/decryption of task payloads before they are sent to the network.
- **Database Perspective**:
  - **MySQL**: Stores user accounts, team memberships, and cryptographic envelopes (metadata).
  - **MongoDB**: Stores the actual encrypted task documents (blobs).

**Actual Data Flow (Example: Adding a Task):**

1. User fills out the task form in `DashboardUI`.
2. `DashboardUI` calls `taskService.addTask(newTask)`.
3. `TaskService` applies workflow rules (e.g., status validation) via `WorkflowService`.
4. `TaskService` passes the task to `EncryptedTaskService`.
5. `EncryptedTaskService` encrypts the task content using the team's shared key and a unique nonce.
6. `HttpAuthClient` sends the encrypted payload to the `snm_module` (Spring Boot).
7. The backend stores the payload in **MongoDB**.
8. Upon success, the UI updates its local `ObservableList`, which automatically reflects in the Kanban and Calendar views.

**Limitations**:

- Current tight coupling between `DashboardUI` and specific service implementations.
- Missing a formal "Controller" layer in some parts, as `DashboardUI` handles both layout and some orchestration.

---

## 2.2 ARCHITECTURE

The project uses a **Layered Architecture (N-Tier)** with elements of **MVC** on the client side.

### Layer Mapping:

- **Presentation (UI) Layer**:
  - `DashboardUI.java`: Main application entry and orchestration.
  - `ui.views.*`: `DashboardView.java`, `MyTasksView.java`, `CalendarView.java`, `WorkspaceView.java`.
  - `SidebarView.java`: Navigation control.
- **Application/Service Layer**:
  - `TaskService.java`: High-level business logic.
  - `EncryptedTaskService.java`: Cryptographic orchestration.
  - `SyncManager.java`: Manages background synchronization and conflict resolution.
- **Model Layer**:
  - `client.model.Task.java`: The core data entity.
  - `client.model.ProjectItem.java`: Abstract base class.
  - `model.Team.java`: Represents a shared workspace.
- **Data Access Layer**:
  - `HttpAuthClient.java`: Handles all HTTP communication with the backend.
  - `LocalCache.java`: Stores documents locally for offline access/performance.
  - Backend stores encrypted blobs in MongoDB; the client does not connect to MongoDB directly.

### Responsibilities:

- **UI Layer**: Pure layout and event handling. Uses `Platform.runLater()` to update nodes from background threads.
- **Service Layer**: Business rules and state management. Never touches JavaFX nodes.
- **Data Layer**: Network and database persistence.

### Text-Based Architecture Diagram:

```text
[   JavaFX UI Layer   ] <---> [ Application Services ] <---> [ Security/Crypto ]
       |                            |                           |
[ DashboardUI / Views ]     [ TaskService / Sync ]      [ CryptoAdapter ]
       |                            |                           |
       +----------------------------+---------------------------+
                                    |
                         [ HttpAuthClient (REST) ]
                                    |
                         [ Spring Boot Backend ]
                         /                   \
                 [ MySQL ]               [ MongoDB ]
```

---

## 2.3 TECHNOLOGIES USED

| Technology                 | Reason for Use in THIS Project                                                                                                                       |
| :------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Java 21**                | Used for modern language features (Records, Sealed Classes) and robust concurrency support (Virtual Threads).                                        |
| **JavaFX**                 | Chosen over web technologies for native desktop performance, local hardware access (for crypto keys), and superior control over window management.   |
| **MongoDB**                | Used as a "Document Store" for task blobs. Its schema-less nature allows for storing diverse encrypted payloads without altering database structure. |
| **MySQL**                  | Used for relational data (Users, Teams, Permissions) where ACID compliance and strict relationships are critical.                                    |
| **Jackson**                | Used for high-speed JSON serialization/deserialization in the `HttpAuthClient` and backend controllers.                                              |
| **Sodium/internal Crypto** | Provides the primitives for X25519 (key exchange) and ChaCha20-Poly1305 (encryption).                                                                |

---

## 2.4 KEY FUNCTIONALITIES

### 1. Add/Edit/Delete Task

- **Logic**: Handled via `showAddTaskDialog` and `showEditDialog` in `DashboardUI`. These create a modal overlay.
- **Methods**: `taskService.addTask()`, `taskService.updateTask()`, `taskService.deleteTask()`.
- **Handling**: UI collects inputs; `TaskService` orchestrates the save via the encrypted pipeline.

### 2. Status Change Workflow

- **Logic**: Tasks follow a strict progression: `DEADLINE` → `IN_PROGRESS` → `DONE`.
- **Methods**: `taskService.markInProgress()`, `taskService.markDone()`.
- **Handling**: In Kanban (`MyTasksView`), drag-and-drop or single-click actions trigger these status updates, which are then persisted via `EncryptedTaskService`.

### 3. Kanban Board Logic

- **Logic**: The `MyTasksView` iterates through the `ObservableList<Task>` and separates items into three distinct columns based on the `status` field.
- **Handling**: UI Layer. Uses a `FlowPane` or `VBox` per column to organize task cards.

### 4. Global Calendar Sync

- **Logic**: Aggregates tasks from ALL user-joined teams. It performs background ECDH key unwrapping if team keys are not already in `SessionState`.
- **Methods**: `DashboardUI.loadGlobalCalendar()`.
- **Handling**: Dashboard logic.

---

# 3. CLASS DIAGRAM & OOPS IMPLEMENTATION

## 3.1 CLASS DIAGRAM

```text
+-----------------------+           +-----------------------+
|   <<Abstract>>        |           |         Task          |
|    ProjectItem        | <|--------|-----------------------|
|-----------------------|           | - description: String |
| # id: String          |           | - deadline: String    |
| # title: String       |           | - completed: boolean  |
| # status: String      |           | - priority: String    |
|-----------------------|           | - teamId: String      |
| + getDetails(): String|           |-----------------------|
| + getters/setters     |           | + getDetails(): String|
+-----------------------+           +-----------------------+
           ^                                    |
           | (Association)                      | (Uses)
           |                                    v
+-----------------------+           +-----------------------+
|     DashboardUI       | --------> |     TaskService       |
|-----------------------|           |-----------------------|
| - taskList: List<Task>|           | - encryptedService    |
| - taskService: Service|           | - workflow: Service   |
+-----------------------+           +-----------------------+
                                                |
                                                v
                                    +-----------------------+
                                    |    HttpAuthClient     |
                                    |-----------------------|
                                    | + REST calls (JWT)    |
                                    +-----------------------+
```

---

## 3.2 OOPS IMPLEMENTATION DETAILS

1. **Encapsulation**:
   - Variables like `description`, `deadline`, and `status` in the `Task` class are `private` or `protected`. Access is strictly controlled through public getters and setters, ensuring data integrity (e.g., status validation).
2. **Inheritance**:
   - `Task` extends `ProjectItem`. This allows shared attributes (ID, Title) to be defined once, promoting code reuse.
3. **Polymorphism**:
   - **Method Overriding**: The `Task` class overrides the abstract method `getDetails()` from `ProjectItem` to return a task-specific summary string including the deadline.
4. **Abstraction**:
   - `ProjectItem` is an **abstract class**. You cannot instantiate a generic "item"; you must instantiate a concrete implementation like `Task`. This defines a blueprint for all future manageable items (e.g., subtasks or projects).

---

## 3.3 CUSTOM EXCEPTION HANDLING

The project implements a robust custom exception hierarchy in the `exceptions` package.

- **Implemented Exceptions**:
  - `EmptyFieldException`: Thrown when mandatory fields (like email or password) are left blank.
  - `InvalidEmailException`: Thrown when an email does not match the required regex pattern.
  - `WeakPasswordException`: Thrown when a password does not meet complexity requirements.
  - `SessionExpiredException`: A critical exception caught by controllers to trigger an automatic redirect to the login screen.

**Example Usage (ValidationUtils):**

```java
public static void validateEmail(String email) throws InvalidEmailException {
    if (!email.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$")) {
        throw new InvalidEmailException("Invalid email format");
    }
}
```

---

## 3.4 CRUD IMPLEMENTATION

### MongoDB Implementation (Persistence)

All persistence operations go through the backend REST API. The backend stores encrypted blobs in MongoDB:

- **Create**: `POST /documents/{documentUuid}/versions` stores ciphertext in MongoDB and metadata in MySQL.
- **Read**: `GET /documents/teams/{teamId}/documents` or `GET /documents/{documentUuid}/versions/latest` returns ciphertext for client-side decryption.
- **Delete**: `DELETE /documents/{documentUuid}` removes metadata (Mongo blobs remain unless cleanup is added).

### JavaFX Implementation (Interaction)

The frontend uses the **Observer Pattern** via `ObservableList` to reflect CRUD changes:

- **Create/Update**: Triggered by the "Save" button in the Task dialog. The event handler calls `taskService.addTask()`, then adds the object to the `taskList`.
- **Delete**: Triggered by the delete icon in the Kanban board. It calls `taskService.deleteTask()` and removes the item from the `taskList`.
- **Refresh**: Since the UI binds to the `ObservableList`, calling `myTasksView.refresh()` or simply mutating the list updates the view instantly.

**Data Flow Example (User Story: Deleting a Task):**

1. User clicks the "Delete" icon on a task card.
2. UI triggers `handleDelete(task)`.
3. `TaskService.deleteTask(id)` is called.
4. `EncryptedTaskService` issues a DELETE request via `HttpAuthClient`.
5. Backend removes the document from MongoDB.
6. Upon HTTP 200, the UI removes the task from the `taskList` (Observable).
7. The Kanban board visually updates as the card disappears.
