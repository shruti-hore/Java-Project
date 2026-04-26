# Collaborative Task Module (CTM) - Technical Summary

The Collaborative Task Module is a secure JavaFX client designed for zero-knowledge project and task management. It integrates with the `auth_module` and `crypto_module` to ensure all data is encrypted before leaving the client.

---

## 1. Model Layer: `client.model/`
The model package uses String UUIDs for all identifiers to ensure compatibility with the Secure Node (SNM) backend.
- **`Task`**: Represents a work item with fields for title, status, priority, and encrypted content references.
- **`Team`**: Represents a collaborative workspace.
- **`DocumentVersion`**: Tracks the state of a document at a specific point in time, including nonces and AAD for GCM decryption.

---

## 2. Service Layer: `service/` & `client.service/`
Business logic has been transitioned from plaintext storage to an encryption-orchestrated architecture.
- **`EncryptedTaskService`**: The core data orchestrator. It handles the encryption of task blobs and communication with the SNM backend via HTTP.
- **`TaskService`**: Provides a high-level API for the UI, wrapping the `EncryptedTaskService`.
- **`SyncManager`**: Manages the local cache (`LocalCache`) and real-time synchronization with the server using WebSockets and conflict resolution logic.
- **`TeamService`**: Handles workspace discovery and team-key management.

---

## 3. UI Layer: `ui/`
The UI follows a modular view-based architecture with rich aesthetics and responsive layouts.
- **`DashboardUI`**: The main entry point. Orchestrates the two-phase login flow (Challenge/Verify) and manages the transition between workspace selection and the main application.
- **`DashboardView`**: Analytics-focused view showing project statistics and team workloads.
- **`MyTasksView`**: A full Kanban implementation with drag-and-drop task status updates.
- **`CalendarView`**: Renders tasks chronologically based on deadlines.
- **`WorkspaceView`**: A selection screen for choosing or creating secure workspaces.

---

## 4. Security Architecture (Implemented)
The architectural gaps identified in previous versions have been resolved:
- **Client-Side Encryption**: All task content is encrypted using AES-256-GCM before transmission.
- **Zero-Knowledge**: The backend (`snm_module`) only stores opaque blobs and metadata.
- **Secure Sessions**: Master keys are derived via Argon2id and stored in the ephemeral `SessionState`.
- **Team Security**: Workspace-specific keys are shared via X25519-encrypted envelopes, ensuring only team members can decrypt content.

---

## 5. Build and Execution
- **Module Path**: `ctm_module`
- **Build Command**: `mvn clean install`
- **Run Command**: `mvn javafx:run`
- **Requirements**: Java 21, JavaFX 21, and a running `snm_module` backend.