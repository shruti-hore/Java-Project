````md
# Secure Task Manager: Updated Dashboard & MyTasks Architecture Design

This document defines the updated UI/UX architecture for the **Secure Task Manager (CTM Module - T2 Interface Layer)**.

The application now separates the interface into **two major workspaces**:

1. **Dashboard** → Overview, analytics, productivity summary
2. **My Tasks** → Full Kanban task management board

This aligns the application closer to a **modern SaaS productivity platform**, while preserving the existing JavaFX + service-layer integration.

---

# 1. High-Level UI Architecture

The UI is now divided into **navigation + workspace views**.

```text
+---------------------------------------------------------------+
|                        APPLICATION ROOT                        |
|                     (JavaFX BorderPane Root)                  |
+--------------------------+------------------------------------+
|                          |                                    |
|      LEFT SIDEBAR        |         MAIN WORKSPACE AREA        |
|                          |                                    |
| - Dashboard              |   Dashboard View / MyTasks View    |
| - My Tasks               |   (Dynamic center content swap)    |
| - Projects               |                                    |
| - Calendar               |                                    |
| - Settings               |                                    |
| - Logout                 |                                    |
+--------------------------+------------------------------------+
````

### Navigation Logic:

* Sidebar remains static
* Clicking a menu item replaces the **center panel**
* `root.setCenter(currentViewPane)`
* `Dashboard` and `MyTasks` are separate JavaFX panes

---

# 2. System Component Flow

This shows the interaction path from UI to persistence.

```text
+-----------------------------------------------------------+
|                     USER INTERFACE                        |
|        DashboardView / MyTasksView / JavaFX UI           |
+---------------------------+-------------------------------+
                            |
                     [User Action]
                            v
+-----------------------------------------------------------+
|                     SERVICE LAYER                         |
|            TaskService / MongoService Layer              |
| - Task validation                                         |
| - Sorting / filtering                                     |
| - Task state changes                                      |
+---------------------------+-------------------------------+
                            |
                    [Task Document]
                            v
+-----------------------------------------------------------+
|                      DATA LAYER                           |
|                 MongoDB task storage                      |
+-----------------------------------------------------------+
```

### Current Responsibility Split:

* **UI Layer:** Rendering + events
* **TaskService:** Business rules
* **MongoService:** Database CRUD
* **Crypto Module:** Future integration point before persistence

---

# 3. Sidebar Navigation Layout

The sidebar becomes the **global navigation panel**.

```text
+------------------------------+
|   PROFILE / USER INFO        |
|------------------------------|
|   Dashboard                  |
|   My Tasks                   |
|   Projects                   |
|   Calendar                   |
|------------------------------|
|   Inbox                      |
|   Settings                   |
|   Logout                     |
+------------------------------+
```

### Features:

* Active item highlighting
* Consistent icon spacing
* Rounded menu buttons
* Fixed width sidebar
* No content logic inside sidebar

### JavaFX Layout:

* `VBox sidebar`
* `Label` / `Button` menu items
* CSS-based active styling

---

# 4. Dashboard Section Design

The **Dashboard section** is the productivity overview page.

It is **not** used for direct task manipulation.

---

## 4.1 Dashboard Layout

```text
+--------------------------------------------------------------------------+
| TOP BAR: Dashboard   [ Search Bar ]                  [Date] [Month]      |
+--------------------------------------------------------------------------+
|                                                                          |
|   +---------------------------------------------------------------+      |
|   | Welcome to your Task Management Area                         |      |
|   | Subtitle text                                                |      |
|   | [Learn More]                              [Illustration]      |      |
|   +---------------------------------------------------------------+      |
|                                                                          |
|   +---------+ +---------+ +---------+ +---------+                       |
|   | Total   | | Progress| | Pending | | Complete|                       |
|   +---------+ +---------+ +---------+ +---------+                       |
|                                                                          |
|   +----------------------+ +----------------------+                     |
|   | Total Work Chart     | | Task Percentage      |                     |
|   +----------------------+ +----------------------+                     |
|                                                                          |
|   +----------------------+ +----------------------+                     |
|   | Work Progress Cards  | | Working Status       |                     |
|   +----------------------+ +----------------------+                     |
|                                                                          |
+--------------------------------------------------------------------------+
```

---

## 4.2 Dashboard Functional Components

### A. Welcome Card

Purpose:

* Greeting banner
* Summary CTA

Contains:

* Heading
* Subtitle
* Button
* Illustration placeholder

### B. Stats Cards

Displays:

* Total Tasks
* In Progress
* Pending
* Completed

Each card includes:

* Label
* Value
* Background theme color

### C. Analytics Section

Contains:

* **Line Chart:** workload trend
* **Donut Chart:** task percentage breakdown

### D. Progress Cards

Each card shows:

* Project name
* Progress %
* Start date
* End date

### E. Working Status

Displays:

* Overall completion %

---

## 4.3 Dashboard Data Sources

Dashboard metrics come from `TaskService`.

```text
TaskService
 ├── getTotalTasks()
 ├── getPendingTasks()
 ├── getCompletedTasks()
 ├── getInProgressTasks()
 └── getCompletionPercentage()
```

Dashboard must remain **read-only analytics view**.

---

# 5. My Tasks Section Design

The **My Tasks section** contains the **interactive Kanban board**.

This is where:

* Add Task
* Edit Task
* Delete Task
* Move Task through statuses

---

## 5.1 My Tasks Layout

```text
+--------------------------------------------------------------------------+
| TOP BAR: My Tasks                 [Search Tasks]   [+ Add Task]          |
+--------------------------------------------------------------------------+
|                                                                          |
|   +----------------+ +----------------+ +----------------+              |
|   | To Do          | | In Progress    | | Done           |              |
|   |----------------| |----------------| |----------------|              |
|   | Task Card      | | Task Card      | | Task Card      |              |
|   | Task Card      | | Task Card      | | Task Card      |              |
|   |                | |                | |                |              |
|   +----------------+ +----------------+ +----------------+              |
|                                                                          |
+--------------------------------------------------------------------------+
```

---

## 5.2 Kanban Workflow State Machine

```text
      +-----------+
      |   TO DO   |
      +-----------+
            |
            v
      +-----------+
      |IN PROGRESS|
      +-----------+
            |
            v
      +-----------+
      |   DONE    |
      +-----------+
```

Task transitions:

* `TO_DO -> IN_PROGRESS`
* `IN_PROGRESS -> DONE`

---

## 5.3 Task Card Layout

```text
+-----------------------------------+
| Task Title                        |
| Task description                  |
| Due: YYYY-MM-DD                   |
| Priority: High                    |
|                                   |
| [Start] [Edit] [Delete]           |
+-----------------------------------+
```

Buttons change by state:

### TO_DO:

* Start
* Edit
* Delete

### IN_PROGRESS:

* Done
* Edit
* Delete

### DONE:

* Edit
* Delete

---

# 6. View Switching Architecture

The `DashboardUI` root pane swaps views based on sidebar selection.

```text
Sidebar Click Event
      |
      v
+---------------------------+
| switch(viewName)          |
|  Dashboard -> dashboardUI |
|  MyTasks   -> kanbanUI    |
+---------------------------+
      |
      v
root.setCenter(selectedPane)
```

---

# 7. Code Component Structure

The UI should now be modularized:

```text
ui/
 ├── DashboardUI.java
 ├── views/
 │   ├── DashboardView.java
 │   ├── MyTasksView.java
 │   ├── SidebarView.java
 │
 ├── components/
 │   ├── StatCard.java
 │   ├── TaskCard.java
 │   ├── ProgressCard.java
```

---

# 8. Integration With Existing Services

The existing backend flow remains unchanged.

```text
MyTasksView
   |
   v
TaskService
   |
   v
MongoService
   |
   v
MongoDB
```

Dashboard view also consumes `TaskService`.

---

# 9. Current Scope vs Future Scope

---

## Implement Now

### Dashboard:

* Welcome card
* Stats cards
* Placeholder charts
* Progress cards

### My Tasks:

* Kanban board
* Task cards
* Search
* Add/Edit/Delete
* Status transitions

---

## Implement Later

### T2 Future:

* Calendar view
* Notifications
* Team task assignment
* Conflict merge UI
* Fingerprint UI
* Offline sync state

### T3 Integration:

* Encrypt before `MongoService.addTask()`
* Decrypt after `MongoService.getTasks()`

---

# 10. Implementation Priority

---

## Phase 1

Refactor into:

* `DashboardView`
* `MyTasksView`
* `SidebarView`

---

## Phase 2

Build Dashboard:

* Welcome card
* Stats cards
* Charts placeholders

---

## Phase 3

Move Kanban logic into `MyTasksView`

---

## Phase 4

Add routing:

```java
root.setCenter(dashboardView);
root.setCenter(myTasksView);
```

---

## Phase 5

Prepare service hooks for crypto integration

---

# 11. Final Architectural Separation

```text
Dashboard
 └── Analytics + Overview only

My Tasks
 └── CRUD + Kanban only
```

This gives the project:

* Better modularity
* Better maintainability
* Cleaner separation of concerns
* Resume-level SaaS dashboard architecture
* Easy future integration with cryptography module

```
```
