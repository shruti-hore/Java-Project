# Master Documentation

This document serves as the master documentation for the Java-Project repository. It outlines the overall architecture, describes all modules, and provides instructions for running the system.

## Module Overview

The repository consists of four primary modules designed to work together as a secure, collaborative task and document management system.

### 1. Cryptography Module (`crypto_module`)
**Purpose:** Provides zero-knowledge cryptographic primitives.
- **Security**: AES-256-GCM, Argon2id, HKDF-SHA256, X25519 ECDH.
- **Key Features**: Client-side encryption, secure key exchange via Team Key Envelopes, and strict memory zeroing.

### 2. Authentication Module (`auth_module`)
**Purpose:** Manages user identity and secure session state.
- **SessionState**: Stores ephemeral keys (Auth Key, X25519 Private Key) with `zero()` capability.
- **Two-Phase Login**: Orchestrates Challenge (retrieve salt/vault) and Verify (JWT issuance) flows.

### 3. Collaborative Task Module (`ctm_module`)
**Purpose:** JavaFX desktop client for secure task management.
- **Encrypted Storage**: Uses `EncryptedTaskService` to encrypt data before sending it to the backend.
- **SyncManager**: Handles real-time synchronization and conflict resolution via WebSockets.
- **Modern UI**: Feature-rich dashboard with Kanban boards, Calendar views, and workspace management.

### 4. Secure Node Module (`snm_module`)
**Purpose:** Spring Boot backend for managing encrypted blobs and relational metadata.
- **REST API**: Handles workspaces, memberships, and document versioning (all indexed by UUID strings).
- **Security**: Acts as a "blind" storage node—cannot decrypt user data.
- **Real-time**: WebSocket-based notifications for document updates.

---

## How to Run

### Prerequisites
- **Java**: JDK 21 or higher.
- **Maven**: 3.8.1 or higher.
- **Databases**:
  - **MySQL**: Relational storage for users, teams, and versions.
  - **MongoDB**: Storage for encrypted document blobs.
- **Environment Variables**:
  - `SPRING_DATASOURCE_PASSWORD`: Password for the MySQL database.

### 1. Backend Setup (`snm_module`)
1. Ensure MySQL and MongoDB are running.
2. Set the database password:
   ```powershell
   $env:SPRING_DATASOURCE_PASSWORD="your_mysql_password"
   ```
3. Run the Spring Boot application:
   ```powershell
   cd snm_module
   mvn spring-boot:run
   ```

### 2. Frontend Setup (`ctm_module`)
1. Ensure the backend is running at `http://localhost:8080`.
2. Launch the JavaFX application:
   ```powershell
   cd ctm_module
   mvn clean javafx:run
   ```

---

## System Architecture

The project employs a **Zero-Knowledge Architecture**:
1. **Encryption at Edge**: Data is encrypted in the `ctm_module` using keys derived in `auth_module`.
2. **Blind Backend**: `snm_module` stores and routes encrypted blobs but never sees plaintext.
3. **Identity**: Users are identified by their email, but all data access requires a valid JWT and a successfully unwrapped Team Key.

## Development Syntax
- **Commits**: Follow the module-specific tags:
  - `CRY-[type]`: Cryptography changes.
  - `AUTH-[type]`: Authentication changes.
  - `SNM-[type]`: Backend/Secure Node changes.
  - `CTM-[type]`: JavaFX Client changes.
- **Style**: Strict adherence to memory zeroing in `finally` blocks for all cryptographic material.
