# Master Documentation

This document serves as the master documentation for the Java-Project repository. It outlines the overall architecture, describes all modules and their respective codes, provides a high-level overview of the entire system, and serves as a starting point for developers.

## Module Overview

The repository consists of four primary modules designed to work together to provide a secure, collaborative task and document management system.

### 1. Cryptography Module (`crypto_module`)
**Purpose:** Provides robust, zero-knowledge cryptographic primitives for secure data handling.
**Key Components:**
- **EncryptionEngine & VaultService**: Implements AES-GCM for secure data encryption and decryption.
- **KeyPairService & X25519KeyPair**: Manages X25519 elliptic curve key pairs for secure key exchange.
- **EcdhService**: Handles Elliptic Curve Diffie-Hellman (ECDH) shared secret derivation.
- **MasterKeyDerivation & SubKeyDerivation**: Uses Argon2id and HKDF for strong key derivation and context separation.
- **TeamKeyEnvelope**: Secures sharing of symmetric keys among team members.
- **FingerprintService**: Implements BIP39 deterministic fingerprints for public keys.

### 2. Authentication Module (`auth_module`)
**Purpose:** Acts as the secure bridge between the client applications and the raw cryptography module, managing user identity and secure session state.
**Key Components:**
- **AuthService**: Handles user registration and login workflows, orchestrating secure key derivation, and ensuring strict memory zeroing (cleaning up byte arrays in `finally` blocks).
- **SessionState**: Manages the runtime cryptographic state of an authenticated user (e.g., Auth Signing Key, X25519 Private Key) with defensive `zero()` capabilities.
- **CryptoAdapter**: Isolates the `crypto_module` API from the rest of the application.

### 3. Collaborative Task Management (`ctm_module`)
**Purpose:** A JavaFX client application for managing projects and tasks, utilizing MongoDB for data storage.
**Key Components:**
- **Models**: `Project`, `Task`, `User`, `Team`, `WorkflowRule` represent the core data entities.
- **Services**: `MongoService` handles MongoDB connections. `TaskService` and `WorkflowService` manage business logic.
- **UI Views**: Provides JavaFX interfaces like `DashboardView`, `CalendarView`, `MyTasksView`, and components like `StatCard` and `TaskCard`.

### 4. Secure Note Management Backend (`snm_module`)
**Purpose:** A Spring Boot backend service for managing documents, teams, and real-time synchronization.
**Key Components:**
- **Controllers**: `BlobController`, `DocumentVersionController`, `SyncController`, `TeamController` expose REST APIs.
- **Services**: `BlobService` (MongoDB) for unstructured content, `DocumentVersionService` and `TeamService` (MySQL) for relational data.
- **WebSocket**: `SyncNotificationService` handles real-time document synchronization updates across clients.
- **Repositories**: Spring Data JPA and Mongo repositories for database operations.

## System Architecture & Module Interactions

The project employs a modular architecture to enforce separation of concerns and maintain a strong security posture:

1. **Client Tier (`ctm_module` & `auth_module`)**: 
   The `ctm_module` acts as the primary user interface. It manages task states, workflow rules, and direct UI interactions. The `auth_module` handles the client-side authentication flows, deriving session keys from passwords using the `crypto_module` and keeping sensitive data securely in memory.

2. **Security Tier (`crypto_module`)**:
   Isolated from network or framework dependencies. It performs all AES-GCM encryption, ECDH key agreements, Argon2id/HKDF derivations, and strict memory zeroing. It ensures "Zero-Knowledge" properties by encrypting content before it ever hits the network.

3. **Backend & Synchronization Tier (`snm_module`)**:
   The central Spring Boot backend. It provides REST APIs for team sharing, document versioning, and encrypted blob storage. It utilizes WebSockets to push real-time sync notifications to connected clients. *Note: The backend has no knowledge of the plaintext content or the encryption keys. It only routes and stores ciphertext.*

## Technology Stack

- **Frontend / Client UI**: Java, JavaFX
- **Backend Service**: Java, Spring Boot, Spring Data JPA, WebSockets
- **Cryptography**: Bouncy Castle (JCE)
- **Databases**: 
  - **MongoDB**: For flexible storage of encrypted document blobs and task data.
  - **MySQL**: For relational mapping of users, teams, and document version histories.
- **Build Tools**: Maven

## Security Posture

- **Zero-Knowledge Architecture**: The server cannot read user documents. All encryption is performed client-side using AES-256-GCM.
- **Memory Safety**: Cryptographic keys and passwords are systematically zeroed (`Arrays.fill()`) immediately after use in `finally` blocks.
- **Strong Key Derivation**: Argon2id is used for master key derivation, and HKDF-SHA256 for context-specific subkeys.
- **Secure Key Exchange**: X25519 Elliptic Curve Diffie-Hellman (ECDH) is used for sharing team keys securely among users.
- **Authenticated Encryption**: Any tampered ciphertext is immediately rejected via strict `AEADBadTagException` handling.

## Getting Started

*(Instructions for building and running each module can be found in their respective directories.)*

- **Build all modules**: Run `mvn clean install` from the respective module directories.
- **Run the backend**: Start the `SnmBackendApplication` inside `snm_module`.
- **Run the client**: Launch the UI from `ctm_module` (Ensure JavaFX SDK is properly configured).
