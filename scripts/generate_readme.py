import os
import subprocess

base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
readme_path = os.path.join(base_dir, "README.md")
tree_path = os.path.join(base_dir, "tree.txt")
forclaude_path = os.path.join(base_dir, "forclaude.md")

# Generate tree.txt using windows tree command
subprocess.run(f"tree \"{base_dir}\" /F /A > \"{tree_path}\"", shell=True)

readme_content = """# Master Documentation

This document serves as the master documentation for the Java-Project repository. It outlines the overall architecture, describes all modules and their respective codes, provides the project file structure, and includes the complete code and configuration contents for AI modeling purposes.

## Module Overview

The repository consists of three primary modules designed to work together to provide a secure, collaborative task and document management system.

### 1. Cryptography Module (`crypto_module`)
**Purpose:** Provides robust, zero-knowledge cryptographic primitives for secure data handling.
**Key Components:**
- **EncryptionEngine & VaultService**: Implements AES-GCM for secure data encryption and decryption.
- **KeyPairService & X25519KeyPair**: Manages X25519 elliptic curve key pairs for secure key exchange.
- **EcdhService**: Handles Elliptic Curve Diffie-Hellman (ECDH) shared secret derivation.
- **MasterKeyDerivation & SubKeyDerivation**: Uses Argon2id and HKDF for strong key derivation and context separation.
- **TeamKeyEnvelope**: Secures sharing of symmetric keys among team members.
- **FingerprintService**: Implements BIP39 deterministic fingerprints for public keys.

### 2. Collaborative Task Management (`ctm_module`)
**Purpose:** A Java client application for managing projects and tasks, utilizing MongoDB for data storage.
**Key Components:**
- **Models**: `Project`, `Task`, `User`, `Team`, `WorkflowRule` represent the core data entities.
- **Services**: `MongoService` handles MongoDB connections. `TaskService` and `WorkflowService` manage business logic.
- **UI Views**: Provides Swing/JavaFX interfaces like `DashboardView`, `CalendarView`, `MyTasksView`, and components like `StatCard` and `TaskCard`.

### 3. Secure Note Management Backend (`snm_module`)
**Purpose:** A Spring Boot backend service for managing documents, teams, and real-time synchronization.
**Key Components:**
- **Controllers**: `BlobController`, `DocumentVersionController`, `SyncController`, `TeamController` expose REST APIs.
- **Services**: `BlobService` (MongoDB) for unstructured content, `DocumentVersionService` and `TeamService` (MySQL) for relational data.
- **WebSocket**: `SyncNotificationService` handles real-time document synchronization updates across clients.
- **Repositories**: Spring Data JPA and Mongo repositories for database operations.

## File Structure

```text
"""

with open(readme_path, "w", encoding="utf-8") as f:
    f.write(readme_content)
    
    # Append tree
    if os.path.exists(tree_path):
        with open(tree_path, "r", encoding="utf-8", errors="replace") as t:
            f.write(t.read())
            
    f.write("```\n\n")
    f.write("---\n\n")
    
    # Append forclaude.md
    if os.path.exists(forclaude_path):
        with open(forclaude_path, "r", encoding="utf-8", errors="replace") as fc:
            f.write(fc.read())

if os.path.exists(tree_path):
    os.remove(tree_path)
