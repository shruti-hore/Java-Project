# UI Integration — Part 5: Dashboard (Workspace Selection Screen)

## Read These Files First

Before writing a single line of code, locate and read the following files in the project. The codebase uses "Dashboard" and "Workspace" interchangeably in places — the existing screen that loads after login is called `DashboardView`. Do not rename it. Work with whatever class names and method names already exist.

```
Search for and read:
  - DashboardView.java          (the existing post-login screen)
  - DashboardController.java    (if it exists — may be empty or partial)
  - style.css                   (theme classes: .root, .task-card, etc.)
  - HttpAuthClient.java         (the HTTP client built in Part 4, Step 1)
  - SessionState.java           (holds JWT, private key, team keys)
  - EncryptedTaskService.java   (the encrypted document read path)
  - SyncManager.java            (background sync — start/stop lifecycle)

Search for any file containing:
  - "workspace" (case-insensitive)
  - "DashboardView"
  - "TeamKey"
  - "getTeamKey"

This tells you what already exists before you build anything.
```

**Naming rule:** If `DashboardView.java` already exists, modify it in place. Do not create a parallel `WorkspaceDashboardView.java`. Match all existing method names, field names, and style patterns found in the file.

---

**Core failure modes:** All workspace documents fetched on load — blocks UI and defeats chunked loading design · `SessionState` passed into view class — views must never hold key material directly · Team key unwrapped on the JavaFX thread — UI freezes during ECDH · SyncManager started before SessionState is fully populated · Workspace list rendered before HTTP response returns — empty list flash

---

## Style Contract

Match exactly. Do not introduce new colors or layout patterns.

| Token | Value |
|---|---|
| Background (main) | `#0d1117` |
| Background (card) | `#161b22` |
| Border | `#30363d` |
| Accent (primary) | `#4f46e5` |
| Accent (secondary) | `#58a6ff` |
| Text (primary) | `white` |
| Text (secondary) | `#8b949e` |
| Card radius | `12px` |

No FXML. All layout is programmatic. Helper methods for repeated components.

---

## Step Index

| Step | Name | Output |
|---|---|---|
| 1 | Workspace list HTTP client methods | `HttpAuthClient.java` (add methods) |
| 2 | `DashboardView` — workspace list layout | `DashboardView.java` (modify in place) |
| 3 | `DashboardController` — fetch and render workspace list | `DashboardController.java` |
| 4 | Chunked document loading on workspace click | `DashboardController.java` |
| 5 | Team key unwrap on workspace open | `DashboardController.java` |
| 6 | SyncManager lifecycle wiring | `DashboardController.java` |
| 7 | Sync status indicator | `DashboardView.java` + `DashboardController.java` |
| 8 | Session expiry and 401 handling | `DashboardController.java` |

---

---

# Step 1 — Workspace List HTTP Methods

**Before starting:** Read `HttpAuthClient.java` in full. Add to the existing class — do not create a new HTTP client.

**Core failure modes:** New HTTP client instantiated instead of reusing `HttpAuthClient` — JWT header not attached · Workspace metadata decoded as plaintext task content — it is metadata only, not encrypted

---

## Your Input File
```
ui/http/HttpAuthClient.java     (existing — read it first)
```

## Your Output File
```
ui/http/HttpAuthClient.java     (add methods to existing class)
```

---

## Schema — Add These Methods

```java
// Returns list of workspaces the authenticated user belongs to
// GET /teams/mine
// Each WorkspaceSummary contains: teamId, name, ownerUserId, lastSyncedAt
public List<WorkspaceSummary> fetchWorkspaces() throws IOException

// Returns document metadata for a workspace — no blobs, no ciphertext
// GET /teams/{teamId}/documents/metadata
// Each DocumentMeta contains: documentUuid, versionSeq, lastModifiedAt
public List<DocumentMeta> fetchWorkspaceMetadata(String teamId) throws IOException

// Returns one encrypted document blob — called per document, lazily
// GET /documents/{documentUuid}/versions/latest
// Returns EncryptedDocumentPayload (ciphertextBase64, nonceBase64, aadBase64, versionSeq)
public EncryptedDocumentPayload fetchDocument(String documentUuid) throws IOException

// Returns the team key envelope for the authenticated user in this team
// GET /teams/{teamId}/envelopes/me
public String fetchTeamKeyEnvelope(String teamId) throws IOException
```

Define `WorkspaceSummary` and `DocumentMeta` as inner records. All methods call `authorizedRequest()` — never construct a raw request without the JWT header. All methods throw `IOException` on non-200; throw `SessionExpiredException` (a simple `RuntimeException` subclass) specifically on 401.

---

## Mandatory Adversarial Tests
```
PASS: fetchWorkspaces() request includes Authorization header
FAIL expected: fetchWorkspaces() called before setJwt() → request still includes header
      (verify JWT is null → SessionExpiredException, not a silent empty list)
PASS: fetchDocument() makes one HTTP call per document UUID, not a batch fetch
FAIL expected: any method receives 401 → SessionExpiredException thrown
```

## Commit Formula
```
DASH-01 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 2 — `DashboardView` Workspace List Layout

**Before starting:** Read `DashboardView.java` in full. Identify what already exists — top bar, welcome card, any existing layout structure. Modify in place. Do not rebuild from scratch.

**Core failure modes:** Workspace cards built with hardcoded workspace names — must be dynamically populated · `SessionState` stored as a field on the view — views must be dumb layout only · Workspace list area absent — controller has nowhere to render into

---

## Your Input File
```
ui/views/DashboardView.java     (existing — read before modifying)
style.css                       (read to find reusable class names)
```

## Your Output File
```
ui/views/DashboardView.java     (modify in place)
```

---

## Schema — Add or Refactor These Elements

The view must expose these references to the controller. If any already exist under different names, keep the existing names and add getters.

```java
// Exposed to controller — add getters for all
private final VBox workspaceListContainer;   // populated dynamically by controller
private final Button createWorkspaceButton;
private final Button joinWorkspaceButton;
private final Label syncStatusLabel;         // "Syncing..." indicator (Step 7)
private final ProgressIndicator syncSpinner; // small, top-right corner
```

**Layout addition — workspace list section:**
```
VBox workspaceSection
  ├─ HBox header
  │    ├─ Label "Your Workspaces" (white, 18px, bold)
  │    └─ HBox buttons (right-aligned)
  │         ├─ Button "Join" (secondary style, #30363d bg)
  │         └─ Button "+ New" (accent #4f46e5)
  └─ VBox workspaceListContainer (spacing: 10)
       └─ (populated by controller)
```

**Workspace card helper method** — called by controller, not by the view itself. Expose it as a public factory:
```java
public VBox createWorkspaceCard(String name, String ownerUserId, String lastSynced)
// Returns a styled card the controller can add to workspaceListContainer
// Card style: background #161b22, radius 12, border #30363d
// Shows name (white, 15px bold), owner (secondary, 12px), last synced (secondary, 11px)
// Entire card is clickable — returns it with an onMouseClicked slot open for controller to bind
```

No event handlers inside the view. No HTTP calls. No reference to `SessionState`, `HttpAuthClient`, or any service class.

---

## Mandatory Adversarial Tests
```
PASS: DashboardView() constructs without throwing
PASS: getWorkspaceListContainer(), getCreateWorkspaceButton(), getJoinWorkspaceButton(),
      getSyncStatusLabel(), getSyncSpinner() all return non-null
PASS: workspaceListContainer is empty at construction
PASS: createWorkspaceCard() returns a VBox with non-null onMouseClicked property slot
PASS: no import of SessionState, HttpAuthClient, SyncManager, or EncryptedTaskService
```

## Commit Formula
```
DASH-02 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 3 — `DashboardController` — Fetch and Render Workspace List

**Before starting:** Read `DashboardController.java` if it exists. If partially implemented, extend it. Do not replace existing working logic.

**Core failure modes:** `fetchWorkspaces()` called on JavaFX application thread — HTTP blocks UI · Workspace list rendered before response returns — empty flash then repopulate · Error from HTTP shown as unhandled exception dialog instead of inline message

---

## Your Input File
```
ui/views/DashboardView.java         (from Step 2)
ui/http/HttpAuthClient.java         (from Step 1)
auth/session/SessionState.java
```

## Your Output File
```
ui/controllers/DashboardController.java     (create or modify in place)
```

---

## Schema

```java
public class DashboardController {

    private final DashboardView view;
    private final SessionState session;
    private final HttpAuthClient httpClient;

    public DashboardController(DashboardView view, SessionState session,
                                HttpAuthClient httpClient)

    public void initialize()
    // Called once after scene is set.
    // 1. Binds createWorkspaceButton → handleCreateWorkspace() (Step 1 of Part 6)
    // 2. Binds joinWorkspaceButton → handleJoinWorkspace() (Step 2 of Part 6)
    // 3. Calls loadWorkspaceList()
    // 4. Starts SyncManager (Step 6)
}
```

**`loadWorkspaceList()`:**
```
1. Show loading state: workspaceListContainer shows a ProgressIndicator centered
2. Run on background thread (Task<List<WorkspaceSummary>>):
   a. workspaces ← httpClient.fetchWorkspaces()
3. On success (Platform.runLater):
   a. workspaceListContainer.getChildren().clear()
   b. For each WorkspaceSummary:
      - card ← view.createWorkspaceCard(ws.name(), ws.ownerUserId(), ws.lastSyncedAt())
      - card.setOnMouseClicked → handleWorkspaceSelected(ws.teamId())
      - workspaceListContainer.getChildren().add(card)
4. On SessionExpiredException → redirectToLogin()
5. On IOException → show inline error label in workspaceListContainer:
   "Could not load workspaces. Check your connection."
```

---

## Mandatory Adversarial Tests
```
PASS: loadWorkspaceList() runs HTTP call on background thread, not FX thread
PASS: workspace cards are populated after HTTP response, not before
PASS: clicking a workspace card calls handleWorkspaceSelected() with correct teamId
FAIL expected: SessionExpiredException → redirectToLogin() called, not exception dialog
PASS: IOException → inline error label shown inside workspaceListContainer
```

## Commit Formula
```
DASH-03 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 4 — Chunked Document Loading on Workspace Click

**Core failure modes:** All documents fetched at once — defeats chunked loading design · Blob ciphertext fetched during metadata phase — metadata and content fetched in wrong order · Documents not currently in view fetched eagerly — background sync fetches everything immediately

---

## Your Input File
```
ui/controllers/DashboardController.java     (from Step 3)
ui/http/HttpAuthClient.java                 (from Step 1)
client/service/EncryptedTaskService.java
```

---

## Schema — `handleWorkspaceSelected(String teamId)`

Three-phase load. Each phase is a separate background `Task`. Phase 2 starts only after Phase 1 completes. Phase 3 runs as a background-only operation and does not block the UI.

**Phase 1 — Metadata (triggers immediately on click):**
```
Background Task:
  metadata ← httpClient.fetchWorkspaceMetadata(teamId)
Platform.runLater:
  Render document name stubs into the Kanban board (placeholders — no content yet)
  Start Phase 2
```

**Phase 2 — Active content (documents currently in view):**
```
Background Task:
  For each document currently visible in the Kanban viewport:
    payload ← httpClient.fetchDocument(documentUuid)
    fields ← encryptedTaskService.loadTask(documentUuid, teamId, payload)
Platform.runLater (per document, not batched):
  Replace placeholder stub with real task card content
```

**Phase 3 — Background sync (remaining documents not in view):**
```
Background Task (low priority, daemon):
  For each document NOT in the current viewport:
    payload ← httpClient.fetchDocument(documentUuid)
    localCache.cacheDocument(documentUuid, teamId, versionSeq, fields)
  // Updates local cache only — does not update UI unless document comes into view
```

"Currently in view" means the documents whose Kanban column is visible on screen. Use the existing Kanban board's visible column list to determine this — do not invent a new viewport concept.

---

## Mandatory Adversarial Tests
```
PASS: Phase 1 completes before Phase 2 starts
PASS: Phase 2 fetches only documents in the visible Kanban columns
PASS: Phase 3 runs on a background thread and does not update UI nodes directly
PASS: clicking a second workspace while Phase 3 is running cancels Phase 3's Task
FAIL expected: encryptedTaskService.loadTask() called during Phase 1 — must not happen
```

## Commit Formula
```
DASH-04 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 5 — Team Key Unwrap on Workspace Open

**Core failure modes:** Team key unwrap happens on JavaFX thread — ECDH blocks UI · Unwrapped team key stored in the view — must live only in `SessionState` · `session.getTeamKey()` called before unwrap completes — `IllegalStateException` from `SessionState`

---

## Your Input File
```
ui/controllers/DashboardController.java     (from Steps 3–4)
auth/session/SessionState.java
auth/service/CryptoAdapter.java
ui/http/HttpAuthClient.java
```

---

## Schema — Insert Before Phase 1 in `handleWorkspaceSelected()`

Team key unwrap must complete before any document decryption is attempted. Insert this as Phase 0, before the three-phase load.

**Phase 0 — Team key unwrap:**
```
if session.hasTeamKey(teamId):
    skip — already unwrapped in this session, proceed to Phase 1

Background Task:
  envelopeBase64 ← httpClient.fetchTeamKeyEnvelope(teamId)
  envelopeBytes ← Base64.decode(envelopeBase64)
  // Need the team owner's public key for ECDH — fetch it or use cached value
  ownerPublicKeyBytes ← httpClient.fetchOwnerPublicKey(teamId)
  ownerPublicKey ← cryptoAdapter.loadPublicKey(ownerPublicKeyBytes)
  teamKeyBytes ← cryptoAdapter.unwrapTeamKey(envelopeBytes, ownerPublicKey,
                                               session.getX25519PrivateKey())
Platform.runLater:
  session.addTeamKey(teamId, teamKeyBytes)
  zero teamKeyBytes local variable
  proceed to Phase 1
```

Add `hasTeamKey(String teamId)` to `SessionState` if it doesn't already exist:
```java
public boolean hasTeamKey(String teamId) {
    return teamKeys.containsKey(teamId);
}
```

Add to `HttpAuthClient`:
```java
// GET /teams/{teamId}/owner-public-key
public byte[] fetchOwnerPublicKey(String teamId) throws IOException
```

---

## Mandatory Adversarial Tests
```
PASS: second click on same workspace skips Phase 0 — no duplicate unwrap
PASS: cryptoAdapter.unwrapTeamKey() never called on JavaFX application thread
PASS: teamKeyBytes local variable zeroed after session.addTeamKey()
PASS: Phase 1 does not start until session.hasTeamKey(teamId) returns true
FAIL expected: unwrapTeamKey() throws AEADBadTagException (corrupted envelope)
      → show inline error "Could not unlock workspace.", do not proceed to Phase 1
```

## Commit Formula
```
DASH-05 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 6 — SyncManager Lifecycle Wiring

**Before starting:** Read `SyncManager.java` in full. It already has `start()` and `stop()` methods. Wire them — do not reimplement.

**Core failure modes:** `SyncManager.start()` called before `SessionState` has any team keys — first sync attempt fails · `SyncManager.stop()` never called on scene exit — background thread runs after logout · `conflictCallback` not set before `start()` — conflict events swallowed silently

---

## Your Input File
```
ui/controllers/DashboardController.java     (from Steps 3–5)
client/sync/SyncManager.java                (read it first)
```

---

## Schema — Add to `DashboardController.initialize()`

```java
private SyncManager syncManager;

// In initialize(), after loadWorkspaceList():
syncManager = new SyncManager(
    localCache,
    conflictResolver,
    encryptedTaskService,
    conflictPair -> Platform.runLater(() -> handleConflict(conflictPair))
);
syncManager.start();
```

**`handleConflict(ConflictPair pair)`** — placeholder for now, will be implemented fully in the Conflict Dialog part:
```java
private void handleConflict(ConflictPair pair) {
    // TODO: replaced by ConflictDialog in Part 7
    view.getSyncStatusLabel().setText("Conflict detected — resolution required.");
}
```

**On scene exit / logout:**
```java
public void onExit() {
    syncManager.stop();
    session.zero();
    httpClient.setJwt(null);
}
```

`onExit()` must be called by whoever triggers the scene transition away from Dashboard — either a logout button handler or a `SessionExpiredException` redirect. Wire the logout button to call `onExit()` before switching scenes.

---

## Mandatory Adversarial Tests
```
PASS: SyncManager.start() is called during initialize()
PASS: SyncManager.stop() is called during onExit() before session.zero()
PASS: conflictCallback is set before start() — not null at first sync cycle
PASS: onExit() calls session.zero() and sets JWT to null
FAIL expected: onExit() called twice → SyncManager.stop() handles double-stop gracefully
      (ScheduledExecutorService.shutdown() is idempotent — verify no exception thrown)
```

## Commit Formula
```
DASH-06 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 7 — Sync Status Indicator

**Core failure modes:** Sync spinner updated from background thread directly — `IllegalStateException` · Spinner left spinning after sync completes — user thinks sync is still running · Status label not cleared after successful sync — stale "Syncing..." persists

---

## Your Input File
```
ui/views/DashboardView.java             (from Step 2 — syncStatusLabel, syncSpinner)
ui/controllers/DashboardController.java (from Step 6)
client/sync/SyncManager.java            (add callbacks)
```

---

## Schema

Add two callbacks to `SyncManager` construction — `onSyncStart` and `onSyncComplete`:

```java
syncManager = new SyncManager(
    localCache,
    conflictResolver,
    encryptedTaskService,
    conflictPair -> Platform.runLater(() -> handleConflict(conflictPair)),
    () -> Platform.runLater(() -> setSyncing(true)),    // onSyncStart
    () -> Platform.runLater(() -> setSyncing(false))    // onSyncComplete
);
```

Add to `SyncManager` — call these at the start and end of `drainAndSync()`:
```java
private final Runnable onSyncStart;
private final Runnable onSyncComplete;
```

**`setSyncing(boolean syncing)` in `DashboardController`:**
```java
private void setSyncing(boolean syncing) {
    view.getSyncSpinner().setVisible(syncing);
    view.getSyncStatusLabel().setText(syncing ? "Syncing..." : "");
    view.getSyncStatusLabel().setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
}
```

Spinner is small — `setPrefSize(14, 14)`. Positioned in the top-right of the dashboard top bar, not in the workspace list area.

---

## Mandatory Adversarial Tests
```
PASS: setSyncing(true) → spinner visible, label shows "Syncing..."
PASS: setSyncing(false) → spinner hidden, label cleared
PASS: setSyncing() always called inside Platform.runLater() — never from background thread directly
PASS: if drainAndSync() finds an empty queue, onSyncStart and onSyncComplete still both fire
```

## Commit Formula
```
DASH-07 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Step 8 — Session Expiry and 401 Handling

**Core failure modes:** `SessionExpiredException` thrown from background thread causes unhandled exception dialog · Redirect to login happens before `onExit()` is called — session not zeroed, JWT not cleared · Login scene constructed on background thread — `IllegalStateException`

---

## Your Input File
```
ui/controllers/DashboardController.java     (from Steps 3–7)
ui/views/LoginView.java
ui/controllers/LoginController.java
```

---

## Schema — `redirectToLogin()`

```java
private void redirectToLogin() {
    Platform.runLater(() -> {
        onExit();   // stops SyncManager, zeroes session, clears JWT
        Stage stage = (Stage) view.getScene().getWindow();
        LoginView loginView = new LoginView();
        LoginController loginController = new LoginController(
            loginView, httpClient, cryptoAdapter
        );
        loginController.initialize();
        Scene scene = new Scene(loginView);
        scene.getStylesheets().add(
            getClass().getResource("/style.css").toExternalForm()
        );
        stage.setScene(scene);
    });
}
```

Every `catch (SessionExpiredException e)` block in `DashboardController` — across all steps — calls `redirectToLogin()`. No other handling. No error dialog. No retry.

Add a global catch in `DashboardController` for any uncaught `SessionExpiredException` that escapes a Task's `setOnFailed` handler:
```java
Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
    if (e instanceof SessionExpiredException) redirectToLogin();
});
```

---

## Mandatory Adversarial Tests
```
PASS: SessionExpiredException from any background task → redirectToLogin() called
PASS: redirectToLogin() calls onExit() before constructing LoginView
PASS: LoginView constructed inside Platform.runLater(), not on background thread
PASS: after redirect, session.zero() has been called — no team keys remain in SessionState
FAIL expected: 401 response from any HttpAuthClient method → SessionExpiredException,
      not IOException, not silent empty result
```

## Commit Formula
```
DASH-08 Fix [failure mode]: [why it failed] → [what you changed]
```
