# ARCHITECTURE & SYSTEM DESIGN REQUIREMENTS

## 1. System Vision: Zero-Knowledge Collaboration
The project is a secure, collaborative task manager where the server is "blind" to user data.
- **Client-Side Encryption**: All sensitive data (titles, descriptions, deadlines) is encrypted using AES-256-GCM before transmission.
- **Zero-Knowledge Backend**: The backend (`snm_module`) stores only metadata and opaque blobs. It never sees raw task content or master keys.
- **Secure Key Exchange**: Team keys are shared via X25519-encrypted envelopes.

## 2. Technical Rubric & Design Patterns

### 2.1 OOPS Principles
- **Encapsulation**: Private fields with public getters/setters (e.g., `Task` class).
- **Inheritance**: `Task` extends `ProjectItem` (abstract base class).
- **Polymorphism**: `Task` overrides `getDetails()` to provide task-specific summaries.
- **Abstraction**: `ProjectItem` defines the contract for all manageable entities.

### 2.2 Architectural Layers
- **UI Layer**: JavaFX (No FXML). Pure programmatic layout. Responsiveness via `Platform.runLater()`.
- **Service Layer**: Decoupled business logic (`TaskService`) and crypto orchestration (`EncryptedTaskService`).
- **Data Layer**: REST communication via `HttpAuthClient`. Local persistence via `LocalCache` (SQLite).
- **Backend**: Spring Boot with MySQL (Metadata/Auth) and MongoDB (Encrypted Blobs).

### 2.3 Exception Handling
- **Custom Exceptions**: Use specific classes for validation (`InvalidEmailException`, `WeakPasswordException`) and session management (`SessionExpiredException`).
- **Global Handling**: UI must catch these to provide user-friendly feedback without crashing.

## 3. Core Data Flows

### 3.1 Task CRUD
1. **Create/Update**: UI Form → `TaskService` (Rules) → `EncryptedTaskService` (Crypto) → `HttpAuthClient` (POST) → Backend (Mongo).
2. **Read**: `HttpAuthClient` (GET) → `EncryptedTaskService` (Decrypt) → `ObservableList` → UI Refresh.
3. **Delete**: UI Action → `HttpAuthClient` (DELETE) → Backend.

### 3.2 Selective Data Retrieval (Chunking)
- **Phase 1**: Fetch Metadata (Names/IDs).
- **Phase 2**: Fetch Active Content (Visible tasks).
- **Phase 3**: Background Sync (History/Cache).

## 4. Technology Stack
- **Java 21**: Records, Sealed Classes, Virtual Threads.
- **JavaFX 21**: Native desktop UI.
- **Spring Boot**: Backend REST API.
- **MongoDB**: Blob storage.
- **MySQL**: Relational metadata.
- **Sodium/internal Crypto**: Argon2id, ChaCha20-Poly1305/AES-GCM, X25519.
