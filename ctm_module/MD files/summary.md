# Collaborative Task Module (CTM) - Technical Summary

This document provides a detailed overview of the core components, classes, and packages within the Collaborative Task Module.

---

## 1. Model Layer: `model/`
The `model` package contains the core data structures used throughout the application.

### Key Classes:
- **`Task`** (Inherits from `ProjectItem`): The central data model for the application, representing an individual work item.
  - **Fields**: `title`, `description`, `deadline`, `completed`, `status`, `priority`, `ownerEmail`, `notes`.
- **`User`**: Represents a system user (email, password, etc.).
- **`Team`** & **`Project`**: Structural models for grouping users and tasks.
- **`WorkflowRule`**: Used by the workflow engine to define conditional actions.

---

## 2. Service Layer: `service/`
The `service` package handles all database interactions and business logic.

### Key Classes:
- **`MongoService`**: Handles direct MongoDB operations (`mongodb-driver-sync`).
  - **Methods**: `addTask()`, `getTasks()`, `deleteTask()`, `updateStatus()`, `updateCompletion()`, `updateTask()` (Now fully implemented).
- **`TaskService`**: An abstraction layer over `MongoService` (Clean Architecture), which provides the data to the UI layer.
- **`WorkflowService`**: Manages business rules and automation logic for tasks.

---

## 3. Exceptions Layer: `exceptions/`
Custom exceptions for robust input validation and error handling, particularly during authentication and task creation.

### Key Classes:
- **`EmptyFieldException`**: Thrown when a required input field is blank.
- **`InvalidEmailException`**: Thrown when an email doesn't match the required regex pattern.
- **`WeakPasswordException`**: Thrown when a password fails complexity requirements.

---

## 4. Utilities: `utils/`
Helper classes for shared logic across the application.

### Key Classes:
- **`ValidationUtils`**: Contains static methods for validating emails (e.g., regex matching), passwords, and task fields (like date validation).
- **`UserSession`**: Manages the currently logged-in user's state (`getCurrentUserEmail()`, `login()`, `logout()`).

---

## 5. UI Layer: `ui/`
The UI is built using a modular view-based JavaFX architecture, separating the Dashboard analytics from the Kanban task management.

### Main Controller: `DashboardUI`
- **Role**: Entry point, handles authentication, initializes the app, and manages view switching.
- **Key Features**: Login screen with custom exception validation, and routing to Dashboard, Kanban, and Calendar views.

### Views: `ui/views/`
- **`LoginView`**: The login interface (managed dynamically or purely via layout).
- **`SidebarView`**: Static navigation panel for switching between Dashboard, My Tasks, Calendar, and Logout. Now includes a functional "Add New Task" button (hooked in DashboardUI).
- **`DashboardView`**: Read-only analytics workspace featuring stats cards and analytics charts. Now includes functional "Add Task" and "View Tasks" buttons in the welcome card.
- **`MyTasksView`**: Interactive Kanban board for task manipulation (Drag & Drop support, filtering).
- **`CalendarView`**: A view that renders tasks on a calendar layout based on their deadlines.

### Components: `ui/components/`
- **`StatCard`**: Styled container for displaying key performance metrics.
- **`TaskCard`**: Modular task representation with context-aware buttons (Edit/Delete) and drag-and-drop capabilities.

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

### Target Architecture:
The UI module must evolve into an **Encryption-Orchestrating Client** where all task data is encrypted before leaving the UI and decrypted only inside UI memory.