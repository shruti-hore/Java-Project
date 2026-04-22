# Collaborative Task Module (CTM) - Technical Summary

This document provides a detailed overview of the core components, classes, and methods within the Collaborative Task Module.

---

## 1. Model Layer: [Task.java](file:///c:/Users/Shruti/CODES/Java-Project/ctm-module/model/Task.java)
The `Task` class is the central data model for the application, representing an individual work item.

### Class: `Task`
*Inherits from `ProjectItem`*

- **Fields:**
    - `description` (String): Detailed task description.
    - `deadline` (String): Due date in string format (ISO-8601).
    - `completed` (boolean): Tracks if the task is finished.
    - `status` (String): Current Kanban state (DEADLINE, IN_PROGRESS, DONE).
- **Constructor:**
    - `Task(id, title, description, deadline, completed, status)`: Initializes a task with all core properties.
- **Key Methods:**
    - `getDetails()`: Overrides parent method to return a formatted string: `title - description (Due: deadline)`.
    - **Getters/Setters**: Standard accessors and mutators for all fields.

---

## 2. Service Layer: [MongoService.java](file:///c:/Users/Shruti/CODES/Java-Project/ctm-module/service/MongoService.java)
The `MongoService` class handles all database interactions using the MongoDB Java Driver.

### Class: `MongoService`
- **Fields:**
    - `collection`: A `MongoCollection<Document>` pointing to the `tasks` collection in the `taskdb` database.
- **Constructor:**
    - Establishes a connection to `mongodb://localhost:27017` and initializes the collection reference.
- **Key Methods:**
    - `addTask(Task task)`: Converts a `Task` object to a BSON `Document` and inserts it into MongoDB.
    - `getTasks()`: Fetches all documents from the collection, maps them back to `Task` objects (handling null statuses), and returns a `List<Task>`.
    - `deleteTask(String id)`: Removes a document from the database using its `ObjectId`.
    - `updateStatus(String id, String status)`: Updates only the `status` field of a task (e.g., to "IN_PROGRESS").
    - `updateCompletion(String id, boolean completed)`: Updates only the `completed` boolean flag.
    - `updateTask(Task t)`: Performs a comprehensive update of all task fields (Title, Description, Deadline, Status, Completed) for a specific ID.

---

## 3. UI Layer: [ui/](file:///c:/Users/Shruti/CODES/Java-Project/ctm_module/ui/)
The UI is built using a modular view-based architecture to separate the Dashboard analytics from the Kanban task management.

### Main Controller: `DashboardUI`
- **Role**: Entry point, handles authentication, and manages view switching.
- **Key Methods**:
    - `start(Stage stage)`: Initializes the application stack and shows the login screen.
    - `initializeDashboard()`: Sets up the main `BorderPane` with a `SidebarView` and `DashboardView`.
    - `handleEditAction(Task t)`: Centralized handler for adding or editing tasks.

### Views: `ui/views/`
- **`SidebarView`**: Static navigation panel for switching between Dashboard, My Tasks, Calendar, and Settings.
- **`DashboardView`**: Read-only analytics workspace featuring:
    - **Welcome Banner**: Dynamic greeting with pending task count.
    - **Stats Cards**: High-level overview of Total, In Progress, Pending, and Completed tasks.
    - **Analytics Charts**: Pie chart for distribution and Line chart for progress trends.
- **`MyTasksView`**: Interactive Kanban board for task manipulation:
    - **Real-time Search**: Filters tasks by title as you type.
    - **Kanban Columns**: Interactive columns (To Do, In Progress, Done) with Drag & Drop support.

### Components: `ui/components/`
- **`StatCard`**: Styled container for displaying key performance metrics.
- **`TaskCard`**: Modular task representation with context-aware buttons (Start/Done/Edit/Delete) and drag-and-drop capabilities.


---

## Summary of Recent Changes
- **UX Refactor**: Implemented `styleButton` helper for consistent interactive elements.
- **Kanban Logic**: Added logic to hide/show "Start" and "Done" buttons based on the task's current column.
- **Search & Filter**: Integrated real-time search functionality with status-based sidebar filtering.
- **Data Persistence**: Fully integrated with `MongoService` for all CRUD operations, including a fix for missing status fields.

# T2 Module Summary — Interface Layer (JavaFX)

## Overview

This module represents the **client-side interface (T2)** of the Zero-Knowledge Collaborative Task Manager. It is responsible for:

- Rendering the user interface using JavaFX
- Handling all user interactions
- Managing task visualization via a Kanban board
- Communicating with the backend (currently MongoDB directly)
- Acting as the integration layer for future cryptographic and connectivity modules

At present, the implementation reflects an **early-stage functional UI system**, not yet aligned with the zero-knowledge architecture.

---

## Current Architecture (Implemented)

### UI Layer (JavaFX)
- Main entry point: `DashboardUI`
- Displays:
  - Task creation form
  - Search functionality
  - Task statistics (total, completed, pending)
  - Sidebar filters (All / Completed / Pending)
  - Kanban board (Deadline / In Progress / Done)

### Data Layer (Current)
- Interaction via `TaskService` (Clean Architecture abstraction)
- Tasks are stored and retrieved in **plaintext** (MongoDB via `MongoService`)
- No encryption or abstraction layer exists yet

### Model Layer
- `Task` class extends `ProjectItem`
- Contains:
  - title
  - description
  - deadline
  - status
  - completion flag
  - notes (not yet used in UI)

---

## Functional Capabilities (Implemented)

- Create task
- Edit task with date picker
- Delete task with confirmation dialog
- Update task status via **Drag and Drop** (Deadline ↔ In Progress ↔ Done)
- Smart Deadline coloring (Overdue/Today/Upcoming)
- Search tasks (title)
- Filter tasks (Dashboard / My Tasks)
- Display tasks in Kanban board format with empty state messages
- Real-time UI refresh on actions

---

## Architectural Gap (Critical)

The current implementation **violates the core project principle**:

> "Server must be cryptographically blind to all task content"

### Issues:
- Task data is stored in plaintext in MongoDB
- No encryption before persistence
- No decryption layer
- No separation between UI and storage logic
- No offline support
- No conflict handling

---

## Target Architecture (To Be Implemented)

The UI module must evolve into:

### 1. Encryption-Orchestrating Client
- All task data encrypted before leaving the UI
- All blobs decrypted only inside UI memory

### 2. Layered Communication
Replace:

UI → MongoService → MongoDB


With:

UI → CryptoFacade → Connectivity API → MongoDB (opaque blobs)


### 3. Offline-First Client
- Introduce SQLite for:
  - caching encrypted blobs
  - nonce counters
  - pending operations queue

---

## Role of T2 in Final System

T2 will become:

- The **only place where plaintext exists**
- The **controller of encryption flow**
- The **handler of conflict resolution**
- The **manager of local state and synchronization**

---

## Current Status

| Area                     | Status        |
|--------------------------|--------------|
| JavaFX UI               | Implemented  |
| Kanban Board            | Implemented  |
| MongoDB Integration     | Implemented  |
| Encryption              | Not Started  |
| SQLite Cache            | Not Started  |
| WebSocket Sync          | Not Started  |
| Conflict Handling       | Not Started  |
| Invite Flow             | Not Started  |

---

## Conclusion

The current system is a **functional task manager UI**, but not yet a **secure zero-knowledge client**.

The next phase involves transforming this UI into a **secure orchestration layer** that integrates