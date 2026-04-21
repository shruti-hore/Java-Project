# Collaborative Task Module (CTM)

## Overview
The **Collaborative Task Module (CTM)** is the core task management engine for the Secure Team Task Manager. It provides a robust interface for creating, tracking, and managing tasks with real-time status updates and persistent storage.

## Key Features
- **Kanban Workflow**: Visualize tasks across three stages: *Deadline*, *In Progress*, and *Done*.
- **MongoDB Persistence**: Seamless data storage using MongoDB, ensuring task data is preserved across sessions.
- **Dynamic Filtering & Search**: Quickly find tasks via the search bar or filter by *All*, *Completed*, or *Pending* status.
- **Task Statistics**: Real-time dashboard stats showing total, completed, and pending task counts.
- **Responsive UI**: Modern JavaFX interface featuring a sleek sidebar, hover effects, and status-coded task cards.

## Responsibilities
- **Task Lifecycle Management**: Handling creation, editing, status transitions (DEADLINE, IN_PROGRESS, DONE), and deletion of tasks.
- **Data Persistence**: Interfacing with MongoDB via `MongoService` for CRUD operations and document mapping.
- **UI State Coordination**: Synchronizing the Kanban board and task lists with the underlying database.
- **Search & Filter Logic**: Implementing local search and status-based filtering for improved UX.

## Structure
- `model` → Data classes (e.g., `Task.java` defining task attributes and status)
- `service` → Business logic and database interaction (`MongoService.java`)
- `ui` → JavaFX components and dashboard layout (`DashboardUI.java`)
- `utils` → Helper functions for data formatting and validation
- `dto` → Data transfer objects for internal communication

## Tech Stack
- **Frontend**: JavaFX (with custom CSS styling)
- **Backend**: Java
- **Database**: MongoDB (via `mongodb-driver-sync`)