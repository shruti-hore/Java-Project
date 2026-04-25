# UI Integration Roadmap: Secure Auth & Workspaces

This document outlines the requirements and logic flow for integrating the `snm_module` backend into the frontend UI (JavaFX).

## 1. Authentication Flow (Login/Register Page)

### Registration
- **Input**: Email, Password.
- **Process**: 
    1. Generate a salt and Argon2id hash.
    2. Derived master key is used to encrypt the initial vault.
    3. **POST `/auth/register`**: Send `emailHmac`, `bcryptHash`, `publicKeyBase64`, `vaultBlobBase64`, and `saltBase64`.
- **Note**: The server never sees the raw password or master key.

### Two-Step Login Handshake
Because we use client-side key derivation, the UI must handle two distinct phases:
1.  **Phase 1 (Challenge)**: User enters email.
    - **API**: `POST /auth/login/challenge`.
    - **Result**: Server returns the user's `salt` and `vaultBlob`.
2.  **Phase 2 (Verify)**: User enters password.
    - **Action**: UI uses the `salt` + `password` to compute the master key locally (Argon2id).
    - **API**: `POST /auth/login/verify`.
    - **Result**: Server verifies the proof and returns a **JWT**.

## 2. Workspace Selection (Dashboard)

Upon successful login, the user lands on the **Workspace Dashboard**.

### Workspace Listing
- Display all workspaces the user is a member of.
- Each workspace should show basic metadata (Name, Owner, Last Synced).

### Selective Data Retrieval (Chunking)
- **Rule**: Do not retrieve the entire workspace at once.
- **Sequence**:
    1. **Metadata First**: Document names and structure.
    2. **Active Content**: Only the documents currently in view.
    3. **Background Sync**: Retrieve history and older versions in segments.

## 3. Team Management (Join/Create)

### Creating a Workspace
- Generates a unique **Workspace Code**.
- Creates the initial root document/collection on the server.

### Joining a Workspace
- **Action**: User enters a **Workspace Code**.
- **Process**:
    1. UI sends a join request to the server.
    2. The Workspace Owner must verify and accept the request.
    3. Once accepted, the Owner (or another member) must create a **Key Envelope** for the new user so they can decrypt the workspace data.

## 4. Security Integration

- **JWT Persistence**: The JWT must be stored securely in memory for the duration of the session.
- **Header Injection**: Every request to a non-auth endpoint must include:
  `Authorization: Bearer <your_jwt_token>`
- **Session Expiry**: UI should gracefully handle 401/403 responses by redirecting to the Login page.

---
> [!IMPORTANT]
> The cryptographic work (Argon2id, X25519) should be performed on a background thread to prevent UI freezing during login or key wrapping.

## 5. Existing UI Architecture (ctm_module Analysis)

The current UI in the `ctm_module` is built using **Programmatic JavaFX**, avoiding FXML files entirely. All layouts, components, and event bindings are constructed purely in Java code. 

**Key Characteristics & Conventions:**
1. **No FXML:** Views extend standard JavaFX Panes (like `BorderPane`, `VBox`) and assemble their children in the constructor.
2. **Hybrid Styling:** A mix of external CSS (`light_style.css` / `style.css`) and inline `-fx-*` styles.
3. **Application Shell (`DashboardUI.java`):** 
   - Uses a root `StackPane` (`mainStack`) to allow glass-pane overlays (like modal dialogs).
   - Contains a `BorderPane` (`mainRoot`) where the `SidebarView` is set to the left, and the center is swapped out dynamically based on navigation.

### Implemented Scenes & Views

Here is the complete map of the existing UI that we need to integrate with:

#### 1. The Login Scene (`DashboardUI.showLoginScreen()`)
Currently a simple VBox centered in the `mainStack`. It accepts Email and Password. 
**Integration Need:** This needs to be completely replaced/upgraded to handle the Two-Step Challenge-Verify handshake and Argon2id derivation on a background thread.

#### 2. The Main Shell & Sidebar (`SidebarView.java`)
Provides navigation routing via a callback. It swaps the `mainRoot` center pane between:
- `DASHBOARD`
- `KANBAN`
- `CALENDAR`
- `LOGOUT` (clears session and returns to Login Scene)

#### 3. Dashboard Analytics (`DashboardView.java`)
A `BorderPane` showing high-level stats (StatCards), Workload Analysis, and Recent Tasks.
**Integration Need:** The "Workspaces" list should ideally live here or in the Sidebar. When a user clicks a Workspace, we need to trigger the chunked downloading from the `SyncManager`.

#### 4. Kanban / Task Management (`MyTasksView.java`)
Displays tasks in columns (TODO, IN_PROGRESS, DONE). It heavily uses custom `TaskCard` components.
**Integration Need:** The data feeding into this view (`ObservableList<Task>`) needs to be backed by our new `EncryptedTaskService` instead of the legacy plaintext `TaskService`.

#### 5. Calendar (`CalendarView.java`)
A grid displaying tasks by date. Same data source as the Kanban view.

#### 6. Modal Overlays (`DashboardUI.showOverlay()`)
`DashboardUI` includes built-in methods for `showAddTaskDialog()` and `showEditDialog()`. These create a dark semi-transparent glass pane over the `mainStack` and center a VBox with a form.
**Integration Need:** We should reuse this exact `showOverlay(Node)` mechanism to display the **Conflict Resolution Dialog** (when `SyncManager` detects a 409 Conflict).

### Example Structure (`LoginView` rewrite blueprint)

To ensure the new Auth logic matches the UI developer's style, we should extract the Login logic into its own class following this pattern:

```java
package ui.views;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView extends VBox {
    public LoginView(Runnable onLoginSuccess) {
        setAlignment(Pos.CENTER);
        setMaxSize(400, 480);
        setStyle("-fx-background-color: white; -fx-padding: 50; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");

        Label title = new Label("SECURE TASKER");
        title.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 32px; -fx-font-weight: bold;");

        // ... Add TextField/PasswordField matching DashboardUI exactly ...

        Button loginBtn = new Button("SIGN IN");
        loginBtn.getStyleClass().add("button-primary");
        
        loginBtn.setOnAction(e -> {
            // TODO: Move Argon2id and Key wrapping to a background Task here!
            // On success: Platform.runLater(onLoginSuccess);
        });

        getChildren().addAll(title, loginBtn);
    }
}
```
## 6. Implementation Action Plan (What Needs to be Built)

Now that the backend, cryptography (Tasks 1 & 2), and secure document pipeline (Task 3) are complete, here is the exact checklist of what must be implemented in the JavaFX UI:

### Step 1: The Secure Login/Registration View
- **Replace `DashboardUI.showLoginScreen()`**: Instead of a simple mock VBox, create `ui.views.LoginView` and `ui.views.RegisterView`.
- **Implement Registration**: Gather Email/Password, call `CryptoAdapter.deriveMasterKey()` (Argon2id) on a JavaFX background `Task`, generate `X25519KeyPair`, encrypt the initial vault, and POST to `/auth/register`.
- **Implement Challenge-Verify Login**:
  - Phase 1: POST email to `/auth/login/challenge`. Retrieve Salt and VaultBlob.
  - Phase 2: Run Argon2id on a background `Task` using the Salt + Password. Unseal the VaultBlob using the derived key. POST the verification proof to `/auth/login/verify`.
- **Session State**: On success, populate `SessionState` with the JWT, the user's private key, and the unsealed Vault containing Team Keys.

### Step 2: Workspace / Dashboard Hookup
- **Refactor `initializeDashboard()`**: Currently, it loads all tasks via `taskService.getAllTasks()`. This must be changed to first fetch the list of *Workspaces* (Teams) from `/documents/teams/{teamId}/documents`.
- **Chunked Loading**: Add logic to `DashboardView` so that when a Workspace is clicked, it fetches the first chunk of encrypted document blobs.
- **Join/Create Workspace UI**: Add a button in the Sidebar or Dashboard to "Create Workspace" (generates a new `TeamKey` and saves it to the vault) or "Join Workspace" (prompts for a code).

### Step 3: Wire the Encryption Pipeline to the Kanban Board
- **Replace `TaskService`**: In `MyTasksView.java`, `DashboardUI.java`, and `CalendarView.java`, replace the legacy `TaskService` with the new `EncryptedTaskService`.
- **Data Flow**: When `showAddTaskDialog()` or `showEditDialog()` is saved, the UI must pass the raw fields to `EncryptedTaskService`, which handles AES-GCM encryption and AAD binding before sending to the server.

### Step 4: The SyncManager & Conflict Resolution
- **Lifecycle Hook**: Start the `SyncManager` daemon thread when the application starts (or right after login), and cleanly `stop()` it in the `Application.stop()` method.
- **Conflict UI**: Create a new class `ui.components.ConflictDialog` (reusing the `DashboardUI.showOverlay()` mechanism). When the `SyncManager` invokes its `Platform.runLater()` callback with a `ConflictPair`, pop up this dialog to show "Local Edit" vs "Server Edit" and allow the user to select which version to keep.
