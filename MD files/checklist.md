# Implementation Checklist — T2 Module

## 1. COMPLETED FEATURES (CURRENT STATE)

### UI Layer

- Dashboard UI implemented with Advanced Dark Theme (JavaFX)
- Top Bar:
    - Real-time search
    - Profile & Notifications placeholders
- Sidebar navigation (No emojis):
    - Dashboard
    - My Tasks
    - Categories (placeholder)
    - Calendar (placeholder)
    - Settings (placeholder)
    - Logout (placeholder)
- Kanban board with Drag and Drop (JavaFX Dragboard API)
- Smart Deadline Indicators:
    - Red (Overdue)
    - Orange (Due Today)
    - Blue (In Progress)
    - Green (Done)
- Task statistics:
    - Total
    - Done (Completed)
    - Due Soon (within 48-72 hours)
- Analytics Panel placeholder (footer)
- Confirmation dialogs for critical actions (Delete)
- Empty state messages for columns

---

### Task Operations

- Add Task
  - Input validation implemented
  - Stored in MongoDB

- Edit Task
  - Updates title, description, deadline

- Delete Task
  - Removes task from database

- Update Status
  - DEADLINE → IN_PROGRESS → DONE

---

### Data Handling

- MongoDB integration via `MongoService`
- Task model implemented
- ObservableList used for UI updates
- Tasks sorted by deadline

---

## 2. PARTIALLY IMPLEMENTED

- Clean Architecture: Full separation between UI and Data layer (via TaskService)
- Notes field exists in model but not used in UI

---

## 3. NOT IMPLEMENTED (CRITICAL)

---

### 3.1 Cryptography Integration

Status: Not started

To implement:

- Create `CryptoFacade`
- Integrate:
  - AES-256-GCM encryption
  - AAD handling
  - Padding logic
- Replace all MongoService calls with encrypted flow

---

### 3.2 SQLite Local Cache

Status: Not started

To implement:

Tables:
- cached_docs
- pending_ops
- nonce_counters
- team_keys

Responsibilities:
- Store encrypted blobs
- Maintain nonce counters
- Enable offline support

---

### 3.3 Nonce Counter System

Status: Not started

To implement:

- Atomic counter per document
- Stored in SQLite
- Used to generate deterministic nonce

---

### 3.4 Replace MongoService (MAJOR CHANGE)

Current:

UI → MongoService → MongoDB


Target:

UI → CryptoFacade → REST API → MongoDB


Actions:
- Remove plaintext storage
- Implement API client layer

---

### 3.5 Task Encryption Flow

To implement:

- Convert Task → JSON
- Pad to 256 bytes
- Encrypt
- Send encrypted payload

---

### 3.6 Task Decryption Flow

To implement:

- Fetch blob
- Decrypt using team key
- Render UI

---

### 3.7 Conflict Resolution UI

Status: Not started

To implement:

- Side-by-side comparison
- Buttons:
  - Keep mine
  - Keep theirs
  - Merge

---

### 3.8 Invite Flow + Fingerprint

Status: Not started

To implement:

- Fetch public key
- Generate fingerprint (6 words)
- Verify with user
- Encrypt team key
- Send invite

---

### 3.9 Member Management Panel

Status: Not started

To implement:
- Show team members
- Show roles
- Remove member
- Display fingerprints

---

### 3.10 Profile / Settings Screen

Status: Not started

To implement:
- Show user fingerprint
- Password change flow

---

### 3.11 WebSocket Integration

Status: Not started

To implement:
- Listen for:
  - NEW_VERSION
  - CONFLICT
- Update UI dynamically

---

### 3.12 Offline Support

Status: Not started

To implement:
- Queue operations in SQLite
- Retry mechanism
- Sync indicator in UI

---

## 4. PRIORITY ORDER (RECOMMENDED)

1. CryptoFacade integration
2. SQLite + nonce counters
3. Replace MongoService
4. Encryption/decryption flow
5. Conflict UI
6. Invite + fingerprint
7. WebSocket sync
8. Offline support

---

## 5. CURRENT RISK AREAS

- Plaintext data exposure
- No encryption enforcement
- Tight coupling between UI and database
- No versioning or conflict handling
- No offline capability

---

## 6. FINAL GOAL

Transform current system from:

Functional UI + Plaintext Storage

Into:

Secure Zero-Knowledge Client:
- Encrypted data flow
- Offline-first architecture
- Conflict-resilient system
- Cryptographically verified collaboration