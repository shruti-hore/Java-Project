# IMPLEMENTATION & WIRING GUIDE

## 1. Core Mission
Wire the secure cryptographic engines (`HttpAuthClient`, `CryptoAdapter`, `SessionState`, `EncryptedTaskService`, `SyncManager`) into the existing `DashboardUI` chassis without modifying the visual layout or CSS.

## 2. Secure Wiring Roadmap (Part 7)

### Step 1: Dependency Injection
- Replace legacy `TaskService`, `AuthService`, and `TeamService` in `DashboardUI`.
- Initialize `httpClient` and `cryptoAdapter` in `start()`.

### Step 2: Two-Phase Auth
- **Phase 1 (Challenge)**: Fetch salt/vault blob from server using email HMAC.
- **Phase 2 (Verify)**: Derive master key locally (Argon2id), unseal vault, and verify proof for JWT.
- **Enforcement**: Must use a background `Task<>` for KDF and network calls.

### Step 3: Workspace Selection
- Redirect `TeamService` to fetch workspaces via HTTP.
- Implement Phase 0 (Team Key Unwrap) before entering a workspace.

### Step 4: Secure CRUD Redirects
- Redirect `TaskService.addTask()`, `updateTask()`, and `deleteTask()` to the `EncryptedTaskService`.
- **Constraint**: `MongoService` must be completely removed from the client modules.

### Step 5: Sync & Lifecycle
- Start `SyncManager` only after a team key is confirmed in `SessionState`.
- Ensure `SyncManager.stop()` and `sessionState.zero()` are called in the correct order during logout.

## 3. Mandatory Implementation Patterns

### 3.1 Background Operations
- **Network/Crypto**: All calls to `httpClient`, `cryptoAdapter`, and `encryptedTaskService` MUST be wrapped in a JavaFX `Task<>`.
- **UI Updates**: All mutations to UI nodes (e.g., `label.setText()`) triggered from background tasks MUST be wrapped in `Platform.runLater()`.

### 3.2 Security Hygiene
- **Memory Zeroing**: Passwords (`char[]`) and Team Keys (`byte[]`) must be explicitly zeroed using `Arrays.fill()` in a `finally` block immediately after use.
- **Stateless Views**: View classes must not hold key material or session state.

## 4. Adversarial Testing Checklist
- [ ] UI remains responsive during login (Argon2id on background thread).
- [ ] Mismatched password on login triggers `AEADBadTagException` and a friendly error.
- [ ] Logout clears all key material from RAM (`sessionState == null`).
- [ ] Moving a task in Kanban triggers an immediate local refresh via `TaskService` (Local-Aware fetch).
