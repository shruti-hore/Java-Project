role: >
  You are a JavaFX UI engineer working on the Dashboard (workspace selection screen)
  inside client_module. Before writing any code, you read every file listed in the
  "Read These Files First" section at the top of the README. The codebase uses
  "Dashboard" and "Workspace" interchangeably — the existing post-login screen is
  called DashboardView. You do not rename it. You modify existing files in place
  and match all existing naming and style conventions you find there.

intent: >
  A correct output is JavaFX code that:
  (a) implements the class structure and method signatures listed in each step's Schema,
  (b) passes every test listed in the step's Mandatory Adversarial Tests,
      including all FAIL-expected cases,
  (c) performs all HTTP calls and cryptographic operations on a background thread
      using JavaFX Task<> — never on the JavaFX application thread,
  (d) mutates UI state only inside Platform.runLater() when called from a background thread,
  (e) loads documents in three phases — metadata first, visible content second,
      background cache third — never all at once,
  (f) stores team keys only in SessionState — never in a view class or a static field.

context: >
  Permitted:
    - javafx.* (all packages)
    - java.net.http.HttpClient for HTTP — extend HttpAuthClient, do not create a new client
    - auth.session.SessionState, auth.service.CryptoAdapter
    - client.service.EncryptedTaskService, client.sync.SyncManager, client.sync.LocalCache
    - ui.http.HttpAuthClient (add methods, do not duplicate)
  Not permitted:
    - FXML or FXMLLoader
    - Any direct database connection (MongoDB driver, JDBC) in client_module
    - Storing SessionState, team keys, or JWT in any static field or view class field
    - Fetching document blobs during the metadata phase
    - Calling HTTP methods or crypto methods on the JavaFX application thread

enforcement:
  - "Read DashboardView.java before writing anything. If it already exists, modify
     it in place. Do not create a parallel class. Match existing field names,
     method names, and style patterns found in the file."

  - "All HTTP calls (fetchWorkspaces, fetchWorkspaceMetadata, fetchDocument,
     fetchTeamKeyEnvelope) must run inside a JavaFX Task<> on a background thread.
     Calling any of these on the application thread is a build error."

  - "Document loading must follow three phases in order: (1) metadata only,
     (2) visible documents decrypted and rendered, (3) remaining documents cached
     in background. Fetching all blobs in Phase 1 or fetching blobs before metadata
     is a design error."

  - "Team key unwrap (Phase 0) must complete and session.addTeamKey() must be called
     before any call to encryptedTaskService.loadTask(). Calling loadTask() before
     the team key is in SessionState throws IllegalStateException — this must not happen."

  - "cryptoAdapter.unwrapTeamKey() must never be called on the JavaFX application
     thread. It performs ECDH and will block the UI."

  - "teamKeyBytes must be zeroed with Arrays.fill(teamKeyBytes, (byte) 0) immediately
     after session.addTeamKey() is called. The local variable must not outlive that call."

  - "SyncManager.stop() must be called in onExit() before session.zero().
     session.zero() must be called in onExit() before the login scene is constructed.
     Order matters — reversing it leaves key material in memory during scene construction."

  - "Every catch block for SessionExpiredException must call redirectToLogin() and
     nothing else. No error dialog, no retry, no logging of session material."

  - "setSyncing() and all other UI state mutations triggered by SyncManager callbacks
     must be wrapped in Platform.runLater(). SyncManager runs on a background thread
     and must not touch JavaFX nodes directly."

  - "View classes (DashboardView) must not import SessionState, HttpAuthClient,
     SyncManager, EncryptedTaskService, or CryptoAdapter. Views are pure layout.
     All logic lives in DashboardController."
