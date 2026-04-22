### Secure Task Manager: Dashboard Architecture & Design

Following the visual inspiration of the "Advanced" Dark Theme, this specification outlines the structural components and functional logic for the Dashboard module.

---

#### 🧠 System Component Diagram
This diagram illustrates the high-level flow from the user interaction to the persistent storage layer.

```text
+-----------------------------------------------------------+
|                      USER INTERFACE                       |
|          (JavaFX: DashboardUI.java / FXML / CSS)          |
+----------------------------+------------------------------+
                             |
                  [Task Event: Add/Edit/Move]
                             v
+-----------------------------------------------------------+
|                      SERVICE LAYER                        |
|           (MongoService.java / TaskService.java)          |
|  - Validates Logic       - Maps ObjectId <-> String       |
|  - Sorts by Deadline     - Prepares Encryption (Future)   |
+----------------------------+------------------------------+
                             |
                  [BSON / Encrypted Blob]
                             v
+-----------------------------------------------------------+
|                      DATA LAYER                           |
|          (MongoDB: content_blobs collection)              |
+-----------------------------------------------------------+
```


---

#### 🖥️ UI Layout Specification
The dashboard is divided into four primary functional zones to maximize productivity and visibility.

```text
+---------------------------------------------------------------------------------+
|  SIDEBAR          |  TOP BAR: Search [__________]  Profile (👤)  Notifications (🔔) |
+-------------------+-------------------------------------------------------------+
|                   |                                                             |
|  [🏠] Dashboard   |   WELCOME BACK, USER!                                       |
|  [✅] My Tasks    |   Stats: [ 85 Total ]  [ 54 Done ]  [ 12 Due Soon ]         |
|  [🏷️] Categories  |  ---------------------------------------------------------  |
|  [📅] Calendar    |                                                             |
|                   |   KANBAN BOARD                                              |
|  [⚙️] Settings    |   +----------+          +-------------+          +--------+     |
|  [🔓] Logout      |   | DEADLINE |          | IN PROGRESS |          |  DONE  |     |
|                   |   +----------+          +-------------+          +--------+     |
|                   |   | [Task A] |          |  [Task B]   |          | [Task C]|    |
|                   |   | [Task D] |          |             |          |         |    |
|                   |   +----------+          +-------------+          +--------+     |
|                   |                                                             |
|                   |   ANALYTICS PANEL                                           |
|                   |   [ (O) % Completion ]  [ |||| % Workload Distribution ]    |
+-------------------+-------------------------------------------------------------+
```


---

#### 🔁 Task State Machine
As the "Productivity Architect," you manage the transition of tasks through these defined states.

```text
      [ USER INPUT ]
            |
            v
    +----------------+       (Move)       +----------------+
    |    DEADLINE    | -----------------> |  IN PROGRESS   |
    | (Status: New)  |                    | (Status: Active)|
    +----------------+                    +-------+--------+
            |                                     |
            |            (Complete)               |
            +-------------------------------------+
                               |
                               v
                       +----------------+
                       |      DONE      |
                       | (Status: Comp) |
                       +----------------+
```


---

#### 🛠️ Key Technical Features
* **Dynamic Sorting**: The system automatically triggers a `taskList.sort((a, b) -> a.getDeadline().compareTo(b.getDeadline()))` whenever a task is added or edited to ensure the "Deadline" column stays prioritized.
* **ID Handling**: The UI layer utilizes `String id`, while the `MongoService` performs the translation to `new ObjectId(id)` for database operations, ensuring a clean separation of concerns.
* **Real-time Analytics**: Head-less stats (Total, Completed, Pending) are recalculated on every `ObservableList` change to keep the dashboard header accurate.
* **Persistence**: All changes are synchronized with the MongoDB `updateTask` or `updateStatus` methods, using static imports for `Updates.set` to maintain clean, readable service code.