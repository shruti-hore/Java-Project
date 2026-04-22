# System Workflow — Interface Module (T2)

## Overview

This document describes the current and future workflows of the Interface module, including how it interacts with users, backend systems, and (future) cryptographic components.

---

# 1. CURRENT WORKFLOW (IMPLEMENTED)

## 1.1 Task Creation Flow

User → UI Form → MongoDB

Steps:
1. User enters:
   - Title
   - Description
   - Deadline

2. UI creates Task object:

Task(taskId=null, title, description, deadline, completed=false, status="DEADLINE")


3. UI calls:

mongoService.addTask(task)


4. MongoDB stores plaintext document:

{
title,
description,
deadline,
completed,
status
}


5. UI updates local list and refreshes Kanban board

---

## 1.2 Task Retrieval Flow

MongoDB → UI → Kanban Board

Steps:
1. On startup:

mongoService.getTasks()


2. Documents converted into Task objects

3. Tasks stored in:

ObservableList<Task> taskList


4. UI renders tasks in:
- Deadline column
- In Progress column
- Done column

---

## 1.3 Task Update Flow

User Action → UI → MongoDB

### Status Update:
- Start → IN_PROGRESS
- Done → DONE

Calls:

mongoService.updateStatus(id, status)
mongoService.updateCompletion(id, true)


### Edit:

mongoService.updateTask(task)


### Delete:

mongoService.deleteTask(id)


---

## 1.4 Filtering and Search

- Filtering:
  - Based on completion status

- Search:
  - Matches title or description

Executed entirely in UI memory.

---

# 2. FUTURE WORKFLOW (REQUIRED ARCHITECTURE)

---

## 2.1 Secure Task Creation Flow

User → UI → Crypto → API → MongoDB

Steps:
1. User inputs task data

2. UI converts task → JSON

3. UI pads plaintext to 256 bytes

4. UI generates nonce using SQLite counter

5. UI calls Crypto module:

encrypt(plaintext, key, nonce, aad)


6. Encrypted payload sent to backend:

POST /documents/{uuid}/versions


7. MongoDB stores:

ciphertext, nonce, aad, key_version


---

## 2.2 Task Retrieval Flow (Secure)

Backend → UI → Crypto → Display

Steps:
1. UI fetches encrypted blob

2. UI retrieves team key from SQLite

3. UI decrypts:

decrypt(ciphertext, nonce, aad)


4. UI renders plaintext in memory only

---

## 2.3 Conflict Workflow

1. User submits update
2. Server detects version mismatch
3. Server returns:

HTTP 409 Conflict


4. UI:
- Fetches both versions
- Decrypts both
- Displays comparison

5. User chooses:
- Keep mine
- Keep theirs
- Merge

6. UI encrypts resolved version
7. Sends:

POST /documents/{uuid}/resolve


---

## 2.4 Invite Flow (Multi-User Interaction)

User A → UI → Backend → User B

Steps:
1. Manager enters email
2. UI fetches public key of invitee
3. UI generates fingerprint
4. Both users verify words
5. UI encrypts team key using ECDH
6. Sends invite to backend

---

## 2.5 Offline Workflow

If network unavailable:

1. Operation stored in:

pending_ops


2. UI continues working with cached data

3. Sync resumes when connection restored

---

## 2.6 Real-Time Sync Workflow

1. WebSocket receives:
- NEW_VERSION
- CONFLICT

2. UI:
- Fetches updated blob
- Decrypts
- Updates Kanban board

---

## Summary

Current workflow:
- UI → TaskService → MongoService → MongoDB (plaintext)
- Task movement via Drag and Drop
- Real-time UI refresh with Smart Deadline coloring

Target workflow:
- UI → Crypto → API → MongoDB (encrypted blobs)

The transition from current to target workflow is the core responsibility of T2.