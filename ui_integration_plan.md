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
2. **Hybrid Styling:** The UI uses a mix of external CSS (`style.css` which defines a sleek dark theme with classes like `.root`, `.task-card`) and inline `-fx-*` styles for dynamic or specific element tweaks.
3. **Component Reusability:** Repeated UI elements are extracted into helper methods (e.g., `createWelcomeCard()`, `createTopBar()`) or separate classes (e.g., `StatCard`).
4. **Theme Colors:** 
   - Backgrounds: `#0d1117` (Main), `#161b22` (Panels/Cards)
   - Accents: `#4f46e5` (Indigo), `#58a6ff` (Light Blue)
   - Text: `white` (Primary), `#8b949e` (Secondary)

### Example Structure (`DashboardView` snippet)

To ensure the new Login and Conflict Resolution scenes match the existing UI developer's style, follow this pattern:

```java
package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

// 1. Extend a standard layout Pane
public class LoginView extends BorderPane {

    // 2. Build the UI in the constructor
    public LoginView() {
        // Apply global style class defined in style.css
        getStyleClass().add("root");

        // Use VBox/HBox for layout
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(40));

        // 3. Delegate to helper methods for modularity
        centerContent.getChildren().add(createLoginCard());

        setCenter(centerContent);
    }

    private VBox createLoginCard() {
        VBox card = new VBox(15);
        // Reuse existing CSS classes where possible
        card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 12; -fx-padding: 30; -fx-border-color: #30363d; -fx-border-width: 1;");
        card.setMaxWidth(400);

        Label title = new Label("Secure Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        // Inline styling for specific component looks
        emailField.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 6;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Master Password");
        passwordField.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 6;");

        Button loginBtn = new Button("Login / Register");
        loginBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 6;");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, emailField, passwordField, loginBtn);
        return card;
    }
}
```

## 6. Next Steps for Implementation
1. **Create `LoginView.java`**: Implement the Zero-Knowledge Challenge-Verify handshake UI using the programmatic layout style shown above.
2. **Update `DashboardView.java`**: Refactor the constructor to load the workspace list first, and implement the chunked fetching logic when a workspace is selected.
3. **Build `ConflictDialog.java`**: Create a custom JavaFX Stage/Dialog using programmatic layout to present `ConflictPair` data to the user.
4. **Wire `SyncManager`**: Attach the `SyncManager` start/stop lifecycle to the main JavaFX Application lifecycle and handle the `Platform.runLater` callbacks.
