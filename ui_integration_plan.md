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
