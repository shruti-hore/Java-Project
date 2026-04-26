You are a senior software architect and code reviewer.

Your task is to deeply analyze the provided Java project (full folder context) and generate a COMPLETE TECHNICAL DOCUMENTATION.

Do NOT give generic explanations. Everything must be derived from the actual codebase.

The goal is to produce a REPORT that can be directly used for:
- Academic submission
- Viva explanation
- Resume-level project documentation

--------------------------------------------------
OUTPUT FORMAT (STRICT)
--------------------------------------------------

# 2. TECHNICAL EXPLANATION

## 2.1 SYSTEM DESIGN
- Explain the system from a user → UI → service → database perspective
- Describe actual data flow using current code (DashboardUI, MongoService, Task, etc.)
- Include:
  - Event flow (Add, Edit, Delete, Status change)
  - How data moves between layers
  - Any current limitations (tight coupling, missing abstraction, etc.)

---

## 2.2 ARCHITECTURE
- Identify architecture style used:
  (Layered / MVC / Hybrid)

- Map actual files to layers:
  - UI Layer → (DashboardUI.java, etc.)
  - Service Layer → (MongoService.java, TaskService.java if exists)
  - Model Layer → (Task.java, ProjectItem.java)
  - Data Layer → MongoDB

- Explain:
  - Responsibilities of each layer
  - Dependency direction
  - Violations (if UI directly calls DB, mention it)

- Provide TEXT-BASED ARCHITECTURE DIAGRAM

---

## 2.3 TECHNOLOGIES USED
For EACH technology, explain:
- Why it is used in THIS project (not generic definition)

Include:
- Java (version if visible)
- JavaFX (UI handling)
- MongoDB (data storage)
- MongoDB Java Driver
- Any libraries (BSON, ObjectId, etc.)

Also mention:
- Why MongoDB instead of SQL
- Why JavaFX instead of web

---

## 2.4 KEY FUNCTIONALITIES
Extract from code (DO NOT GUESS):

Explain implementation of:
- Add Task
- Edit Task
- Delete Task
- Status Change (Deadline → In Progress → Done)
- Search functionality
- Filters (All / Completed / Pending)
- Sorting by deadline
- Kanban board logic (column separation)

For each:
- Explain logic
- Mention actual methods used
- Mention where it is handled (UI or Service)

---

# 3. CLASS DIAGRAM & OOPS IMPLEMENTATION

## 3.1 CLASS DIAGRAM
- Generate a TEXT-BASED UML diagram from actual classes
- Include:
  - Task
  - ProjectItem
  - MongoService
  - DashboardUI
  - Any other relevant classes

Format example:
Class Task
  - id : String
  - title : String
  - description : String
  - deadline : String
  - completed : boolean
  - status : String
  + getters/setters

Also show:
- Inheritance (Task extends ProjectItem)
- Associations (UI → Service → Model)

---

## 3.2 OOPS IMPLEMENTATION DETAILS

Explain how the project uses:

1. Encapsulation
   - Private variables + getters/setters

2. Inheritance
   - Task extends ProjectItem

3. Polymorphism
   - Method overriding (getDetails())

4. Abstraction
   - Where abstraction exists or is missing

Also mention:
- Any design issues or improvements needed

---

## 3.3 CUSTOM EXCEPTION HANDLING

Check code and answer:

- Are custom exceptions implemented?
  IF YES:
    - Explain structure
    - Where used

  IF NO:
    - Suggest where they SHOULD be used:
        - Invalid input
        - Database errors
        - Null ID
        - Validation failures

Provide example:
- Custom exception class design
- Where to integrate

---

## 3.4 CRUD IMPLEMENTATION (IMPORTANT)

Explain how CRUD is implemented using:

### MongoDB
- Create → insertOne()
- Read → find()
- Update → updateOne() with set()
- Delete → deleteOne()

Map each to:
- Actual methods in MongoService

### JavaFX (Frontend)
Explain:
- How UI triggers CRUD
- Event handlers (button actions)
- Data binding with ObservableList

### Data Flow Example:
User clicks "Add Task"
→ UI collects data
→ MongoService.addTask()
→ MongoDB stores document
→ UI refreshTasks()

---

--------------------------------------------------
EXTRA REQUIREMENTS
--------------------------------------------------

- DO NOT skip any section
- DO NOT give generic textbook definitions
- Base everything on actual code
- Be detailed but structured
- Use headings, bullet points, and diagrams

--------------------------------------------------
GOAL
--------------------------------------------------

Produce a COMPLETE technical documentation that:
- Clearly explains the system
- Matches actual implementation
- Can be directly submitted or used in viva