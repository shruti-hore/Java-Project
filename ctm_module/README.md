# Client Task Module (CTM)

The primary user interface and local data manager.

## Rubric Evidence
- **OOP: Inheritance**: `SidebarView` and `DashboardView` extend JavaFX layout classes.
- **OOP: Polymorphism**: UI nodes are treated as general `Node` types in the main layout stack.
- **Design Pattern: Observer**: `ObservableList<Task>` triggers automatic UI updates on change.
- **File Handling/JDBC**: `LocalCache` (SQLite) and `NonceCounterStore` (NIO) demonstrate persistent storage.
- **Custom Exceptions**: `SessionExpiredException` and `EmptyFieldException` handle app-specific errors.

## Core Features
- **JavaFX UI**: Modern dashboard with Kanban and Calendar views.
- **SyncManager**: Background daemon ensuring eventual consistency with the cloud vault.