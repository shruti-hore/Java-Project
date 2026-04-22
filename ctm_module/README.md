# Collaborative Task Module (CTM)

## Overview
The **Collaborative Task Module (CTM)** is the core task management engine for the Secure Team Task Manager. It provides a robust interface for creating, tracking, and managing tasks with real-time status updates and persistent storage.

## Key Features
- **Dashboard Workspace**: High-level productivity overview with analytics charts, statistics cards, and a welcome summary.
- **My Tasks Workspace**: Fully interactive Kanban board for task manipulation (Add/Edit/Delete) with Drag & Drop support.
- **Modular Navigation**: Integrated sidebar for seamless switching between different functional workspaces.
- **MongoDB Persistence**: Seamless data storage using MongoDB, ensuring task data is preserved across sessions.
- **Real-time Search**: Instant task filtering by title in the My Tasks view.
- **Secure Validations**: Integrated safety checks for task dates (no past dates) and user emails (@gmail.com requirement).

## Responsibilities
- **Modular UI Orchestration**: Managing navigation and view-switching between separate Dashboard and My Tasks panels.
- **Task Lifecycle Management**: Handling creation, editing, status transitions (DEADLINE, IN_PROGRESS, DONE), and deletion.
- **Data Persistence**: Interfacing with MongoDB via `MongoService` for CRUD operations and document mapping.
- **Business Logic Integration**: Applying workflow rules and validations before persistence.

## Structure
- `ui/views/` → Primary workspace panels (`DashboardView`, `MyTasksView`, `SidebarView`)
- `ui/components/` → Reusable UI elements (`TaskCard`, `StatCard`)
- `model/` → Data classes (e.g., `Task.java`, `User.java`)
- `service/` → Business logic (`TaskService.java`) and database interaction (`MongoService.java`)
- `utils/` → Security validations and session management
- `lib/` → External dependencies (MongoDB Driver, JavaFX)

## Tech Stack
- **Frontend**: JavaFX (with custom CSS styling)
- **Backend**: Java
- **Database**: MongoDB (via `mongodb-driver-sync`)