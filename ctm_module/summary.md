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
    - `notes` (String): Additional notes (new feature).
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

## 3. UI Layer: [DashboardUI.java](file:///c:/Users/Shruti/CODES/Java-Project/ctm-module/ui/DashboardUI.java)
The `DashboardUI` class is the main JavaFX application providing the graphical user interface.

### Class: `DashboardUI`
*Inherits from `javafx.application.Application`*

- **Core Fields:**
    - `taskList`: An `ObservableList` used to synchronize UI updates with the underlying data.
    - `currentFilter`: Tracks the current view mode (ALL, COMPLETED, PENDING).
    - `searchText`: Stores the current search query for real-time filtering.
    - `deadlineColumn`, `inProgressColumn`, `doneColumn`: VBox containers for the Kanban board columns.
- **Main Logic Methods:**
    - `start(Stage stage)`: Initializes the UI layout, sets up the sidebar, search bar, statistics panel, and Kanban board.
    - `refreshTasks()`: The most critical logic method. It:
        1. Filters tasks based on `currentFilter` and `searchText`.
        2. Clears the Kanban columns.
        3. Creates "Task Cards" (styled VBoxes) for each task.
        4. Implements **Conditional Visibility** for buttons (e.g., only show "Start" in the Deadline column).
        5. Updates the Statistics dashboard.
    - `updateStats()`: Calculates counts for Total, Completed, and Pending tasks.
- **UI Helper Methods:**
    - `styleButton(Button btn, String color, String hoverColor, String textColor)`: Applies consistent CSS styling, hover animations, and cursor changes to buttons.
    - `highlightSidebar(Label selected, Label... others)`: Manages the visual state of the sidebar navigation.
    - `addHoverEffect(Label label)`: Adds interactive color changes to sidebar items.
    - `showError(String msg)`: Displays JavaFX Error Alerts for invalid user inputs.

---

## Summary of Recent Changes
- **UX Refactor**: Implemented `styleButton` helper for consistent interactive elements.
- **Kanban Logic**: Added logic to hide/show "Start" and "Done" buttons based on the task's current column.
- **Search & Filter**: Integrated real-time search functionality with status-based sidebar filtering.
- **Data Persistence**: Fully integrated with `MongoService` for all CRUD operations, including a fix for missing status fields.
