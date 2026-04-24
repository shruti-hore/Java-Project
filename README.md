# Master Documentation

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
F o l d e r   P A T H   l i s t i n g   f o r   v o l u m e   P e r s o n a l 
 
D:.
│   .gitignore
│   forclaude.md
│   LICENSE
│   README.md
│   
├───auth_module
│   │   README.md
│   │   
│   └───src
│       └───main
│           └───java
│               └───auth
│                   ├───controller
│                   │       AuthController.java
│                   │
│                   ├───model
│                   │       User.java
│                   │
│                   ├───repository
│                   │       UserRepository.java
│                   │
│                   └───service
│                           AuthService.java
│                           CryptoAdapter.java
│
├───crypto_module
│   │   pom.xml
│   │   README.md
│   │
│   ├───Builder
│   │   │   agents.md
│   │   │   README.md
│   │   │   skills.md
│   │   │
│   │   ├───Analysis
│   │   │   │   howto.md
│   │   │   │   implementation_plan.md
│   │   │   │   Integration_Constraints.md
│   │   │   │   planAhead.md
│   │   │   │   Reader.md
│   │   │   │   Summary.md
│   │   │   │
│   │   │   └───splits
│   │   │           impl2.md
│   │   │           impl3.md
│   │   │           impl4.md
│   │   │
│   │   └───Cry_prompts
│   │       ├───Cry1
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry2
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry3
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry4
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry5
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry6
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry7
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       ├───Cry8
│   │       │       agents.md
│   │       │       README.md
│   │       │       skills.md
│   │       │
│   │       └───Cry9
│   │               agents.md
│   │               README.md
│   │               skills.md
│   │
│   ├───src
│   │   ├───main
│   │   │   ├───java
│   │   │   │   └───crypto
│   │   │   │       ├───api
│   │   │   │       │       CryptoService.java
│   │   │   │       │
│   │   │   │       └───internal
│   │   │   │               CryptoDemonstration.java
│   │   │   │               CryptoOperationException.java
│   │   │   │               CryptoServiceImpl.java
│   │   │   │               EcdhService.java
│   │   │   │               EncryptionEngine.java
│   │   │   │               FingerprintService.java
│   │   │   │               KeyPairService.java
│   │   │   │               MasterKeyDerivation.java
│   │   │   │               NonceBuilder.java
│   │   │   │               PaddingUtil.java
│   │   │   │               SubKeyDerivation.java
│   │   │   │               TeamKeyEnvelope.java
│   │   │   │               VaultService.java
│   │   │   │               X25519KeyPair.java
│   │   │   │
│   │   │   └───resources
│   │   │           bip39-english.txt
│   │   │
│   │   └───test
│   │       └───java
│   │           └───crypto
│   │               └───internal
│   │                       EcdhServiceTest.java
│   │                       EncryptionEngineTest.java
│   │                       FingerprintServiceTest.java
│   │                       KeyPairServiceTest.java
│   │                       MasterKeyDerivationTest.java
│   │                       PaddingUtilTest.java
│   │                       SubKeyDerivationTest.java
│   │                       TeamKeyEnvelopeTest.java
│   │                       VaultServiceTest.java
│   │
│   └───target
│       ├───classes
│       │   │   bip39-english.txt
│       │   │
│       │   └───crypto
│       │       ├───api
│       │       │       CryptoService.class
│       │       │
│       │       └───internal
│       │               CryptoDemonstration.class
│       │               CryptoOperationException.class
│       │               CryptoServiceImpl.class
│       │               EcdhService.class
│       │               EncryptionEngine.class
│       │               FingerprintService.class
│       │               KeyPairService.class
│       │               MasterKeyDerivation.class
│       │               NonceBuilder.class
│       │               PaddingUtil.class
│       │               SubKeyDerivation.class
│       │               TeamKeyEnvelope.class
│       │               VaultService.class
│       │               X25519KeyPair.class
│       │
│       ├───generated-sources
│       │   └───annotations
│       ├───maven-status
│       │   └───maven-compiler-plugin
│       │       └───compile
│       │           └───default-compile
│       │                   createdFiles.lst
│       │                   inputFiles.lst
│       │
│       └───test-classes
├───ctm_module
│   │   README.md
│   │
│   ├───lib
│   │       bson-5.6.5.jar
│   │       mongodb-driver-core-5.6.5.jar
│   │       mongodb-driver-sync-5.6.5.jar
│   │
│   ├───MD files
│   │       checklist.md
│   │       design.md
│   │       design1.md
│   │       summary.md
│   │       workflow.md
│   │
│   ├───model
│   │       Project.java
│   │       ProjectItem.class
│   │       ProjectItem.java
│   │       Task.class
│   │       Task.java
│   │       Team.java
│   │       User.java
│   │       WorkflowRule.java
│   │
│   ├───resources
│   │       light_style.css
│   │       style.css
│   │
│   ├───service
│   │       MongoService.class
│   │       MongoService.java
│   │       TaskService.class
│   │       TaskService.java
│   │       TestMongo.class
│   │       TestMongo.java
│   │       WorkflowService.java
│   │
│   ├───ui
│   │   │   DashboardUI.java
│   │   │
│   │   ├───components
│   │   │       StatCard.java
│   │   │       TaskCard.java
│   │   │
│   │   └───views
│   │           CalendarView.java
│   │           DashboardView.java
│   │           MyTasksView.java
│   │           SidebarView.java
│   │
│   └───utils
│           UserSession.java
│           ValidationUtils.java
│
├───javafx-sdk-26
│   └───lib
│           javafx.properties
│
└───snm_module
    │   .gitignore
    │   mvnw
    │   mvnw.cmd
    │   pom.xml
    │
    ├───.mvn
    │   └───wrapper
    │           maven-wrapper.properties
    │
    └───src
        ├───main
        │   ├───java
        │   │   └───com
        │   │       └───project
        │   │           └───snm
        │   │               │   SnmBackendApplication.java
        │   │               │
        │   │               ├───config
        │   │               │       WebSocketConfig.java
        │   │               │
        │   │               ├───controller
        │   │               │       BlobController.java
        │   │               │       DocumentVersionController.java
        │   │               │       SyncController.java
        │   │               │       TeamController.java
        │   │               │       TestController.java
        │   │               │
        │   │               ├───dto
        │   │               │       BlobUploadRequest.java
        │   │               │       CreateDocumentVersionRequest.java
        │   │               │       CreateTeamRequest.java
        │   │               │
        │   │               ├───model
        │   │               │   ├───mongo
        │   │               │   │       ContentBlob.java
        │   │               │   │
        │   │               │   └───mysql
        │   │               │           DocumentVersion.java
        │   │               │           Team.java
        │   │               │           TeamMember.java
        │   │               │
        │   │               ├───repository
        │   │               │       ContentBlobRepository.java
        │   │               │       DocumentVersionRepository.java
        │   │               │       TeamMemberRepository.java
        │   │               │       TeamRepository.java
        │   │               │
        │   │               ├───service
        │   │               │       BlobService.java
        │   │               │       DocumentVersionService.java
        │   │               │       SyncService.java
        │   │               │       TeamService.java
        │   │               │
        │   │               └───websocket
        │   │                       DocumentSyncMessage.java
        │   │                       SyncNotificationService.java
        │   │
        │   └───resources
        │           application.yaml
        │
        └───test
            └───java
                └───com
                    └───project
                        └───snm
                                SnmBackendApplicationTests.java
```

---

# Repository Contents

## 1. README.md, agents.md, skills.md
### README.md
# CRY — Zero-Knowledge Cryptography Module

**Core failure modes:** Nonce reuse · Silent GCM tag failure · Key material surviving in heap · Wrong derivation order · Argon2id parameters lowered silently · Padding skipped · BIP39 word index overflow · ECDH commutativity not verified

---

## Module Contract

This module is a standalone Maven module with zero Spring Boot and zero JavaFX dependencies.
It exposes a typed Java interface. All other modules call this interface. Nothing outside this
module performs cryptographic operations. If a PR touches key material outside this module,
reject it.


**Output artifact:** `crypto-1.0-SNAPSHOT.jar`
**Test runner:** JUnit 5 + AssertJ. Every task has mandatory adversarial tests — listed per task.

---

## Dependency Constraint

The only permitted external crypto dependency is Bouncy Castle:

```xml
<dependency>
  <groupId>org.bouncycastle</groupId>
  <artifactId>bcprov-jdk18on</artifactId>
  <version>1.78.1</version>
</dependency>
```

No Google Tink. No HashiCorp Vault client. No Apache Commons Codec for crypto operations.
JCE + Bouncy Castle only.

---

## Global Enforcement Rules — Apply to Every Task

| Rule | Constraint |
|---|---|
| Key material zeroing | Every `byte[]` holding a key, password, or plaintext must be zeroed with `Arrays.fill(b, (byte) 0)` in a `finally` block immediately after use |
| GCM tag failure | A failed AES-GCM authentication tag must throw `AEADBadTagException`. It must never be caught and swallowed silently |
| No plaintext logging | No key bytes, no plaintext content, no password characters may appear in any log statement — not even at DEBUG level |
| No framework imports | `import org.springframework.*` and `import javafx.*` are forbidden in this module |
| Argon2id floor | Parameters may not be lowered below m=65536, t=3, p=4 under any condition including test environments |
| Deterministic nonce | Document encryption nonces are counter-based only. `SecureRandom` is forbidden for document nonces |

---

## Task Index

| ID | Name | Output class | Day |
|---|---|---|---|
| CRY-01 | Maven module scaffold + Argon2id | `MasterKeyDerivation.java` | Day 1 |
| CRY-02 | HKDF sub-key derivation | `SubKeyDerivation.java` | Day 2 |
| CRY-03 | X25519 key pair generation | `KeyPairService.java` | Day 2 |
| CRY-04 | ECDH shared secret | `EcdhService.java` | Day 2 |
| CRY-05 | Key vault seal / unseal | `VaultService.java` | Day 3 |
| CRY-06 | AES-256-GCM encryption engine | `EncryptionEngine.java` | Day 4 |
| CRY-07 | Team key envelope wrap / unwrap | `TeamKeyEnvelope.java` | Day 4 |
| CRY-08 | Payload padding | `PaddingUtil.java` | Day 4 |
| CRY-09 | BIP39 fingerprint generation | `FingerprintService.java` | Day 5 |

Each task below is self-contained. To split: copy the task section into its own README.md,
keep the Global Enforcement Rules section, and delete everything else.

---
---

# Task CRY-01 — Maven module scaffold + Argon2id key derivation

**Core failure modes:** Wrong module structure prevents integration · Argon2id parameters lowered in tests · Salt not stored alongside output · Output array length wrong (not 32 bytes) · SecureRandom salt not used

---

## Your Input

None. This task creates structure from scratch.

At runtime the skill receives:
```
password : char[]   — user's plaintext password, never stored
salt     : byte[]   — 16 random bytes, generated once per user at registration, stored in DB
```

## Your Output File
```
src/main/java/MasterKeyDerivation.java
src/test/java/MasterKeyDerivationTest.java
pom.xml
```

## Run Command
```bash
mvn test -Dtest=MasterKeyDerivationTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Parameter | Value | Rule |
|---|---|---|
| Algorithm | Argon2id | Not Argon2i, not Argon2d, not PBKDF2 |
| Memory cost `m` | 65536 (64 MB) | Hard floor — never lower |
| Time cost `t` | 3 | Hard floor — never lower |
| Parallelism `p` | 4 | Hard floor — never lower |
| Salt length | 16 bytes | Generated with `SecureRandom` at registration |
| Output length | 32 bytes | Exactly 32 — this becomes the AES-256 master key |
| Salt storage | Returned from derive() for caller to store | Not stored inside this class |

**Method signature to implement:**
```java
public byte[] derive(char[] password, byte[] salt)
```
Password array must be zeroed before method returns in all code paths.

---

## Skills to Define in skills.md
- `setup_crypto_module` — creates Maven pom.xml with correct Bouncy Castle dependency and zero framework deps
- `derive_master_key` — char[] password + byte[] salt → byte[32] master key via Argon2id

---

## What Will Fail From the Naive Prompt
Run `"Implement Argon2id key derivation in Java"` without this README first. Then look for:
1. PBKDF2 or BCrypt used instead of Argon2id
2. Parameters set to `m=4096, t=1` (Bouncy Castle defaults) instead of the specified values
3. Password char[] not zeroed after derivation
4. Output array is 16 bytes (AES-128) not 32 bytes (AES-256)
5. Salt generated inside the method instead of passed in — makes it impossible to reproduce for login

---

## Mandatory Adversarial Tests
```
PASS: same password + same salt → identical 32-byte output (deterministic)
PASS: different password, same salt → different output
PASS: same password, different salt → different output
PASS: output is exactly 32 bytes
PASS: after derive() returns, password char[] is all zeroes
FAIL expected: m=4096 parameters → test must assert parameters before calling BC
```

## Commit Formula
```
CRY-01 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-02 — HKDF sub-key derivation

**Core failure modes:** Same info string produces same key as another context · Output length wrong · Master key not zeroed after use · Info string encoding inconsistent (UTF-8 vs platform default)

---

## Your Input File
```
src/main/java/MasterKeyDerivation.java  (from CRY-01, must exist)
```

At runtime the skill receives:
```
masterKey : byte[32]  — output of CRY-01
info      : String    — context label, must be one of the allowed values below
```

## Your Output File
```
src/main/java/SubKeyDerivation.java
src/test/java/SubKeyDerivationTest.java
```

## Run Command
```bash
mvn test -Dtest=SubKeyDerivationTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Parameter | Value | Rule |
|---|---|---|
| Algorithm | HKDF-SHA256 (RFC 5869) | Via Bouncy Castle HKDFBytesGenerator |
| Input key material | masterKey byte[32] | Output of Argon2id from CRY-01 |
| Salt | None (zero-length) | HKDF salt is omitted — the master key is already high-entropy |
| Output length | 32 bytes | Always 32 |
| Allowed info strings | `"vault-key"` · `"auth-signing-key"` | Exact UTF-8 strings — no other values permitted |

**Method signature to implement:**
```java
public byte[] derive(byte[] masterKey, String info)
```

**Allowed info values must be validated inside the method:**
```java
if (!info.equals("vault-key") && !info.equals("auth-signing-key")) {
    throw new IllegalArgumentException("Unknown HKDF context: " + info);
}
```

---

## Skills to Define in skills.md
- `derive_subkey` — byte[32] master key + info string → byte[32] context-specific key via HKDF-SHA256

---

## What Will Fail From the Naive Prompt
Run `"Derive a sub-key from a master key using HKDF in Java"` without this README. Then look for:
1. HMAC-SHA256 used directly instead of proper HKDF expand step
2. Info string encoded with platform default charset instead of UTF-8
3. `"vault_key"` (underscore) accepted instead of `"vault-key"` (hyphen) — taxonomy drift
4. No validation of info string — any string accepted silently
5. masterKey zeroed inside this method, breaking the caller's reference

---

## Mandatory Adversarial Tests
```
PASS: info="vault-key" and info="auth-signing-key" produce different 32-byte outputs
PASS: same masterKey + same info → identical output (deterministic)
PASS: different masterKey, same info → different output
PASS: output is exactly 32 bytes
FAIL expected: info="vault_key" → IllegalArgumentException thrown
FAIL expected: info="" → IllegalArgumentException thrown
```

## Commit Formula
```
CRY-02 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-03 — X25519 key pair generation

**Core failure modes:** Wrong curve (Ed25519 generated instead of X25519) · Private key returned as raw bytes without format wrapper · Public key not extractable as raw 32-byte array · Key pair not reproducible from saved private key bytes

---

## Your Input File

None. Generates fresh key material.

At runtime:
```
No input — generates a fresh random X25519 key pair
```

## Your Output File
```
src/main/java/KeyPairService.java
src/test/java/KeyPairServiceTest.java
```

## Run Command
```bash
mvn test -Dtest=KeyPairServiceTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Field | Constraint | Rule |
|---|---|---|
| Curve | X25519 | Not Ed25519, not P-256, not RSA |
| Provider | Bouncy Castle | `Security.addProvider(new BouncyCastleProvider())` |
| Public key format | Raw 32-byte array, base64 for storage | Must be extractable as `byte[32]` |
| Private key format | Wrapped in `byte[]`, held in RAM only | Never serialized to disk in plaintext |
| Output type | `X25519KeyPair` record or class | Holds `publicKeyBytes byte[]` and `privateKey PrivateKey` |

**Method signature to implement:**
```java
public X25519KeyPair generateKeyPair()
public byte[] extractPublicKeyBytes(PublicKey publicKey)
public PublicKey loadPublicKey(byte[] rawBytes)
```

---

## Skills to Define in skills.md
- `generate_x25519_keypair` — no input → X25519KeyPair (public key bytes + private key object)
- `load_public_key` — byte[32] raw public key bytes → X25519 PublicKey object for use in ECDH

---

## What Will Fail From the Naive Prompt
Run `"Generate an X25519 key pair in Java"` without this README. Then look for:
1. Ed25519 key pair generated (different curve — incompatible with ECDH)
2. KeyPairGenerator initialized without Bouncy Castle provider — fails silently on some JVMs
3. Public key returned as SubjectPublicKeyInfo-encoded bytes (DER format, 44 bytes) not raw 32 bytes
4. No `loadPublicKey()` method — receiving side cannot reconstruct from stored base64

---

## Mandatory Adversarial Tests
```
PASS: generateKeyPair() returns non-null public and private key
PASS: extractPublicKeyBytes() returns exactly 32 bytes
PASS: loadPublicKey(extractPublicKeyBytes(pub)) reconstructs usable PublicKey
PASS: two calls to generateKeyPair() produce different key pairs
FAIL expected: loadPublicKey(new byte[31]) → IllegalArgumentException (wrong length)
FAIL expected: loadPublicKey(new byte[32] all zeroes) → exception (invalid point)
```

## Commit Formula
```
CRY-03 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-04 — ECDH shared secret

**Core failure modes:** ECDH not commutative in implementation · Shared secret used directly as key without hashing · Wrong key type passed without error · Shared secret array not zeroed after use

---

## Your Input File
```
src/main/java/KeyPairService.java  (from CRY-03, must exist)
```

At runtime the skill receives:
```
privateKey : PrivateKey  — caller's X25519 private key (from CRY-03)
publicKey  : PublicKey   — counterparty's X25519 public key (from CRY-03)
```

## Your Output File
```
src/main/java/EcdhService.java
src/test/java/EcdhServiceTest.java
```

## Run Command
```bash
mvn test -Dtest=EcdhServiceTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Field | Constraint | Rule |
|---|---|---|
| Algorithm | X25519 ECDH | Via `KeyAgreement.getInstance("X25519", "BC")` |
| Shared secret post-processing | SHA-256 hash of raw ECDH output | Raw ECDH output is not uniformly distributed — always hash it |
| Output | byte[32] | The SHA-256 of the raw shared secret |
| Commutativity | ECDH(A_priv, B_pub) == ECDH(B_priv, A_pub) | Must be verified in tests |
| Raw shared secret | Zeroed immediately after SHA-256 | Never returned or stored |

**Method signature to implement:**
```java
public byte[] computeSharedSecret(PrivateKey myPrivateKey, PublicKey theirPublicKey)
```

---

## Skills to Define in skills.md
- `perform_ecdh` — X25519 PrivateKey + X25519 PublicKey → byte[32] shared secret (SHA-256 of raw ECDH output)

---

## What Will Fail From the Naive Prompt
Run `"Perform X25519 ECDH in Java"` without this README. Then look for:
1. Raw ECDH output returned without SHA-256 post-processing — not uniformly random, unsafe as key
2. No commutativity test — asymmetric implementations exist and are silently wrong
3. Wrong algorithm string (`"ECDH"` instead of `"X25519"`) — falls back to P-256 on some JVMs
4. Shared secret byte[] not zeroed after hashing

---

## Mandatory Adversarial Tests
```
PASS: ECDH(A_priv, B_pub) equals ECDH(B_priv, A_pub) — commutativity
PASS: output is exactly 32 bytes
PASS: different key pairs → different shared secrets
FAIL expected: passing Ed25519 PrivateKey → InvalidKeyException
FAIL expected: null public key → NullPointerException or IllegalArgumentException
```

## Commit Formula
```
CRY-04 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-05 — Key vault seal and unseal

**Core failure modes:** Wrong key used silently succeeds · GCM tag failure swallowed · Vault nonce not random (must be SecureRandom here — this is one-off, not document encryption) · Private key bytes not zeroed after sealing

---

## Your Input File
```
src/main/java/SubKeyDerivation.java  (from CRY-02)
```

At runtime the seal skill receives:
```
privateKeyBytes : byte[]  — raw private key material to protect
vaultKey        : byte[]  — 32-byte key derived via HKDF info="vault-key"
```

At runtime the unseal skill receives:
```
vaultBlob : byte[]  — the sealed output from seal()
vaultKey  : byte[]  — same 32-byte key used during sealing
```

## Your Output File
```
src/main/java/VaultService.java
src/test/java/VaultServiceTest.java
```

## Run Command
```bash
mvn test -Dtest=VaultServiceTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Field | Constraint | Rule |
|---|---|---|
| Encryption | AES-256-GCM | Same algorithm as document encryption |
| Nonce | 12 bytes, SecureRandom | Vault is sealed once per registration — random nonce is correct here |
| Nonce storage | Prepended to ciphertext in output blob | `vaultBlob = nonce[12] + ciphertext + gcmTag[16]` |
| AAD | None for vault | The vault has no document context to bind to |
| GCM tag failure | Must throw `AEADBadTagException` | Never catch and return null |
| Output format | `byte[]` — caller converts to base64 for DB storage | This class does not handle base64 |

**Method signatures to implement:**
```java
public byte[] seal(byte[] privateKeyBytes, byte[] vaultKey)
public byte[] unseal(byte[] vaultBlob, byte[] vaultKey)
```

---

## Skills to Define in skills.md
- `seal_vault` — byte[] private key + byte[32] vault key → byte[] sealed blob (nonce prepended)
- `unseal_vault` — byte[] sealed blob + byte[32] vault key → byte[] private key, or throws on wrong key

---

## What Will Fail From the Naive Prompt
Run `"Encrypt a private key using AES-GCM in Java"` without this README. Then look for:
1. GCM authentication failure caught and `null` returned — tampered vault appears to decrypt successfully
2. Nonce hardcoded or zeroed — all vaults have the same nonce, XOR attack possible
3. Nonce not prepended to output — unseal() has no way to recover it
4. privateKeyBytes not zeroed after seal() — key material lingers in heap

---

## Mandatory Adversarial Tests
```
PASS: seal() then unseal() with same key → identical original bytes
PASS: output blob length = 12 (nonce) + plaintext.length + 16 (GCM tag)
FAIL expected: unseal() with wrong vaultKey → AEADBadTagException
FAIL expected: unseal() with 1 byte of ciphertext tampered → AEADBadTagException
FAIL expected: unseal() with nonce bytes tampered → AEADBadTagException
PASS: two seal() calls on same input produce different blobs (nonce is random)
```

## Commit Formula
```
CRY-05 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-06 — AES-256-GCM encryption engine with counter nonce

**Core failure modes:** Counter nonce not incremented before use · AAD not passed into GCM cipher · GCM tag failure silently returns garbage · Wrong nonce construction order · Nonce counter not persisted between calls

---

## Your Input File

This task defines the core encryption engine used for all task content.
The nonce counter is managed by the caller (SQLite layer in T2) and passed in.

At runtime the encrypt skill receives:
```
plaintext    : byte[]   — already padded to 256-byte boundary (CRY-08 handles padding)
teamKey      : byte[]   — 32-byte AES team key
nonce        : byte[]   — 12 bytes, constructed by caller (see nonce schema below)
aad          : byte[]   — doc_uuid_bytes (16) + version_seq_bytes (4) = 20 bytes total
```

At runtime the decrypt skill receives:
```
ciphertext   : byte[]   — AES-GCM ciphertext + appended 16-byte GCM tag
teamKey      : byte[]   — same 32-byte team key used to encrypt
nonce        : byte[]   — same 12-byte nonce used to encrypt
aad          : byte[]   — same 20-byte AAD used to encrypt
```

## Your Output File
```
src/main/java/EncryptionEngine.java
src/main/java/NonceBuilder.java
src/test/java/EncryptionEngineTest.java
```

## Run Command
```bash
mvn test -Dtest=EncryptionEngineTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Field | Constraint | Rule |
|---|---|---|
| Algorithm | AES/GCM/NoPadding | GCM tag length = 128 bits (16 bytes) |
| Key | byte[32] | AES-256 only — never AES-128 |
| Nonce | byte[12] | Exactly 12 bytes — standard GCM nonce |
| AAD | byte[20] — doc_uuid (16 bytes) + version_seq (4 bytes, big-endian int) | Mandatory — never omit |
| GCM tag | Appended to ciphertext in output | Caller does not handle it separately |
| Decrypt failure | Throw `AEADBadTagException` | Never swallow, never return null |

**Nonce construction (implemented in NonceBuilder.java):**
```
nonce[0..1]  = team_key_version  (short, big-endian)
nonce[2..5]  = first 4 bytes of doc UUID  (from UUID's most-significant bytes)
nonce[6..11] = counter_value  (6-byte big-endian long, provided by caller from SQLite)
```

**Method signatures to implement:**
```java
// EncryptionEngine.java
public byte[] encrypt(byte[] plaintext, byte[] teamKey, byte[] nonce, byte[] aad)
public byte[] decrypt(byte[] ciphertext, byte[] teamKey, byte[] nonce, byte[] aad)

// NonceBuilder.java
public byte[] build(short teamKeyVersion, UUID docUuid, long counterValue)
```

---

## Skills to Define in skills.md
- `encrypt_blob` — padded plaintext + team key + nonce + AAD → AES-GCM ciphertext (tag appended)
- `decrypt_blob` — ciphertext + team key + nonce + AAD → plaintext, or throws on auth failure
- `build_nonce` — team key version + doc UUID + counter value → byte[12] deterministic nonce

---

## What Will Fail From the Naive Prompt
Run `"Encrypt data with AES-GCM in Java"` without this README. Then look for:
1. AAD not passed to cipher — no binding between ciphertext and its document slot
2. GCM tag failure caught and `null` or empty array returned
3. Random nonce generated inside encrypt() — counter-based nonce requirement violated
4. Key validated as non-null but not checked for exactly 32 bytes — AES-128 silently used
5. Nonce fields in wrong byte order — nonce construction differs per call, looks correct but breaks

---

## Mandatory Adversarial Tests
```
PASS: encrypt() then decrypt() with same key/nonce/aad → identical original plaintext
FAIL expected: decrypt() with 1 byte of ciphertext tampered → AEADBadTagException
FAIL expected: decrypt() with wrong teamKey → AEADBadTagException
FAIL expected: decrypt() with different AAD (different doc UUID) → AEADBadTagException
FAIL expected: decrypt() with different nonce → AEADBadTagException
PASS: NonceBuilder.build() with counter=0 and counter=1 → different nonces
PASS: nonce is exactly 12 bytes
FAIL expected: teamKey.length != 32 → IllegalArgumentException before cipher is initialized
```

## Commit Formula
```
CRY-06 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-07 — Team key envelope wrap and unwrap

**Core failure modes:** Team key not zeroed after wrapping · ECDH shared secret used as wrap key directly without HKDF · Unwrap with wrong private key silently returns garbage · Envelope format not self-contained (missing nonce)

---

## Your Input File
```
src/main/java/EcdhService.java      (from CRY-04)
src/main/java/EncryptionEngine.java  (from CRY-06)
src/main/java/SubKeyDerivation.java  (from CRY-02)
```

At runtime the wrap skill receives:
```
teamKey          : byte[]      — 32-byte AES team key to protect
recipientPubKey  : PublicKey   — invitee's X25519 public key
myPrivateKey     : PrivateKey  — inviting manager's X25519 private key
```

At runtime the unwrap skill receives:
```
envelope         : byte[]      — sealed blob from wrap()
senderPubKey     : PublicKey   — inviting manager's X25519 public key
myPrivateKey     : PrivateKey  — recipient's X25519 private key
```

## Your Output File
```
src/main/java/TeamKeyEnvelope.java
src/test/java/TeamKeyEnvelopeTest.java
```

## Run Command
```bash
mvn test -Dtest=TeamKeyEnvelopeTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Step | Operation | Constraint |
|---|---|---|
| 1. Shared secret | `EcdhService.computeSharedSecret(myPrivKey, recipientPubKey)` | byte[32] SHA-256 output |
| 2. Wrap key | `SubKeyDerivation.derive(sharedSecret, "team-key-wrap")` | HKDF with new info string `"team-key-wrap"` |
| 3. Nonce | 12 bytes, SecureRandom | One-off operation — random nonce correct here |
| 4. Wrap | `EncryptionEngine.encrypt(teamKey, wrapKey, nonce, new byte[0])` | AAD is empty byte array for envelopes |
| 5. Envelope format | `nonce[12] + ciphertext_with_tag` | Self-contained blob — unwrap extracts nonce from prefix |
| Zeroing | sharedSecret, wrapKey zeroed in finally | Before method returns in all paths |

**Method signatures to implement:**
```java
public byte[] wrap(byte[] teamKey, PublicKey recipientPubKey, PrivateKey myPrivateKey)
public byte[] unwrap(byte[] envelope, PublicKey senderPubKey, PrivateKey myPrivateKey)
```

---

## Skills to Define in skills.md
- `wrap_team_key` — AES team key + recipient X25519 public key + sender private key → sealed envelope byte[]
- `unwrap_team_key` — sealed envelope + sender X25519 public key + recipient private key → AES team key byte[], or throws

---

## What Will Fail From the Naive Prompt
Run `"Encrypt an AES key using ECDH and AES-GCM in Java"` without this README. Then look for:
1. ECDH shared secret used directly as AES key without HKDF — not uniformly distributed enough
2. Nonce not prepended to envelope — unwrap() has no way to recover it
3. Wrong private key used during unwrap returns garbage bytes instead of throwing
4. `"team-key-wrap"` HKDF info string not used — derives same key as vault, reuse attack

---

## Mandatory Adversarial Tests
```
PASS: wrap() then unwrap() with correct key pair → identical original teamKey bytes
FAIL expected: unwrap() with wrong recipient private key → AEADBadTagException (via EncryptionEngine)
FAIL expected: unwrap() with 1 byte of envelope tampered → AEADBadTagException
PASS: two wrap() calls on same teamKey → different envelopes (random nonce)
PASS: ECDH commutativity preserved — wrap by A→B, unwrap by B using A's pubkey succeeds
PASS: after wrap(), teamKey parameter byte array is zeroed (verify in test via reference)
```

## Commit Formula
```
CRY-07 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-08 — Payload padding

**Core failure modes:** Padding not applied before encrypt call · Unpad incorrectly strips valid trailing bytes · 256-byte boundary calculated wrong (off by one) · Empty input handled incorrectly · Padding marker byte chosen unsafely

---

## Your Input File

No upstream crypto dependency. Pure byte manipulation.

At runtime the pad skill receives:
```
plaintext : byte[]  — raw serialized task content JSON bytes, any length
```

At runtime the unpad skill receives:
```
padded : byte[]  — output of pad(), must be multiple of 256
```

## Your Output File
```
src/main/java/PaddingUtil.java
src/test/java/PaddingUtilTest.java
```

## Run Command
```bash
mvn test -Dtest=PaddingUtilTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Field | Constraint | Rule |
|---|---|---|
| Block size | 256 bytes | Never configurable — hardcoded constant |
| Padding scheme | PKCS#7-style length prefix | Append `(256 - (len % 256))` bytes, each equal to the pad length |
| Minimum output | 256 bytes | Input of 0 bytes → output of 256 bytes (all padding) |
| Maximum overhead | 255 bytes | Input of 256 bytes → output of 512 bytes (full extra block) |
| Output length | Always a multiple of 256 | Server rejects blobs not divisible by 256 |
| Unpad safety | Validate pad byte value before stripping | Reject if pad value > 256 or > array length |

**Method signatures to implement:**
```java
public byte[] pad(byte[] plaintext)
public byte[] unpad(byte[] padded)
```

---

## Skills to Define in skills.md
- `pad_payload` — byte[] plaintext → byte[] padded to nearest 256-byte boundary
- `unpad_payload` — byte[] padded → byte[] original plaintext, or throws if padding is invalid

---

## What Will Fail From the Naive Prompt
Run `"Pad a byte array to a block boundary in Java"` without this README. Then look for:
1. Block size set to 16 (AES block size) not 256 — padding too small, size analysis attack restored
2. PKCS#7 with block size 256 incorrectly treats pad length as a single byte — value > 255 overflows
3. Unpad strips bytes without validating the pad value — invalid padding accepted silently
4. Input exactly 256 bytes returns 256 bytes with no padding — breaks unpad (no pad marker present)
   (correct: always add a full extra block when input is already aligned)

---

## Mandatory Adversarial Tests
```
PASS: pad(new byte[0]).length == 256
PASS: pad(new byte[1]).length == 256
PASS: pad(new byte[255]).length == 256
PASS: pad(new byte[256]).length == 512   ← full extra block added
PASS: pad(new byte[257]).length == 512
PASS: unpad(pad(input)) equals input for input lengths 0..512
PASS: output is always divisible by 256
FAIL expected: unpad(new byte[255]) → IllegalArgumentException (not 256-aligned)
FAIL expected: unpad(new byte[256] with last byte = (byte)0) → IllegalArgumentException (invalid pad)
```

## Commit Formula
```
CRY-08 Fix [failure mode]: [why it failed] → [what you changed]
```

---
---

# Task CRY-09 — BIP39 fingerprint generation

**Core failure modes:** Non-deterministic output for same inputs · Word index overflows 2048 · Key ordering not normalized (A+B ≠ B+A) · BIP39 wordlist not shipped as a resource · Output word count wrong

---

## Your Input File

```
src/main/resources/bip39-english.txt
```

This file must be downloaded from the official BIP39 repository
(https://github.com/trezor/python-mnemonic/blob/master/src/mnemonic/wordlist/english.txt)
and committed to the project. It contains exactly 2048 words, one per line.

At runtime the skill receives:
```
publicKeyA : byte[32]  — first participant's X25519 public key raw bytes
publicKeyB : byte[32]  — second participant's X25519 public key raw bytes
```

## Your Output File
```
src/main/java/FingerprintService.java
src/test/java/FingerprintServiceTest.java
src/main/resources/bip39-english.txt
```

## Run Command
```bash
mvn test -Dtest=FingerprintServiceTest
```

---

## Schema — Your Enforcement Must Reference These Exactly

| Step | Operation | Constraint |
|---|---|---|
| 1. Normalize order | Lexicographic comparison of publicKeyA and publicKeyB as unsigned byte arrays | Lower key always first — ensures A+B and B+A produce same output |
| 2. Concatenate | `lower_key_bytes + higher_key_bytes` | 64 bytes total |
| 3. Hash | SHA-256 of 64 bytes | Standard JCE MessageDigest — no Bouncy Castle needed |
| 4. Slice | First 6 bytes of the 32-byte hash | Bytes 0–5 only |
| 5. Map | `word_index = Byte.toUnsignedInt(hashByte) % 2048` | `toUnsignedInt` prevents negative modulo |
| 6. Look up | Word at index from loaded BIP39 wordlist | Load wordlist once in constructor, not per call |
| Output | 6 words joined with single space | Exactly 6 words, no punctuation |

**Method signature to implement:**
```java
public String generate(byte[] publicKeyA, byte[] publicKeyB)
```

**Key normalization helper:**
```java
private int compareUnsigned(byte[] a, byte[] b) {
    for (int i = 0; i < a.length; i++) {
        int diff = Byte.toUnsignedInt(a[i]) - Byte.toUnsignedInt(b[i]);
        if (diff != 0) return diff;
    }
    return 0;
}
```

---

## Skills to Define in skills.md
- `generate_fingerprint` — byte[32] public key A + byte[32] public key B → String of 6 BIP39 words
- `load_bip39_wordlist` — reads `bip39-english.txt` from classpath → String[2048], called once in constructor

---

## What Will Fail From the Naive Prompt
Run `"Generate a 6-word fingerprint from two public keys using BIP39"` without this README. Then look for:
1. `byte % 2048` used instead of `Byte.toUnsignedInt(byte) % 2048` — negative bytes give negative modulo, ArrayIndexOutOfBoundsException
2. Key ordering not normalized — fingerprint(A, B) ≠ fingerprint(B, A), users cannot verify each other
3. Wordlist loaded from external URL at runtime instead of bundled as a classpath resource
4. Wordlist has 2047 or 2049 words — one line missing or extra blank line — index wraps wrong
5. Output uses dots or hyphens as separator — UI prompt says spaces, inconsistency causes confusion

---

## Mandatory Adversarial Tests
```
PASS: generate(A, B) equals generate(B, A) — commutativity
PASS: output contains exactly 6 words separated by single spaces
PASS: every word in output is present in the loaded BIP39 wordlist
PASS: generate(A, A) does not throw — same key both sides is valid (self-check)
PASS: different key pairs produce different fingerprints (with high probability)
PASS: wordlist loaded from classpath contains exactly 2048 entries
FAIL expected: generate(new byte[31], validKey) → IllegalArgumentException (wrong length)
FAIL expected: generate(null, validKey) → NullPointerException or IllegalArgumentException
```

## Commit Formula
```
CRY-09 Fix [failure mode]: [why it failed] → [what you changed]
```

### agents.md
# agents.md — CRY Cryptography Module
# INSTRUCTIONS:
#   1. Copy the task section you are working on from README.md into a standalone README.
#   2. Fill in the bracketed fields below by reading that task's README section.
#   3. The enforcement list must copy the exact values from the task's Schema table.
#   4. Delete these comments before committing.

role: >
  You are a Java cryptography engineer implementing one self-contained task inside the
  `com.zk.crypto` Maven module. Your operational boundary is exactly one class and its
  test class, as named in the task's Output File section. You do not modify any other
  class. You do not add Spring Boot or JavaFX imports. You do not implement anything
  not listed in the task's Schema or Skills sections.

intent: >
  A correct output is a Java class that:
  (a) implements the method signatures listed in the task's Schema section exactly as
      written — same method names, same parameter types, same return types,
  (b) passes every test case listed in the task's Mandatory Adversarial Tests section,
      including all FAIL-expected cases that must throw a specific exception,
  (c) contains no log statements that print key material, plaintext content, or
      password characters,
  (d) zeroes all byte[] arrays holding key material in finally blocks.
  Output is verifiable by running: `mvn -pl crypto-module test -Dtest=[TaskTestClass]`

context: >
  You are allowed to use:
    - The Java standard library (java.security.*, javax.crypto.*, java.util.*)
    - Bouncy Castle (org.bouncycastle.*), version pinned in pom.xml
    - Classes already built in this module (listed in the task's Input File section)
  You are NOT allowed to use:
    - Any Spring Boot, JavaFX, or web framework classes
    - Google Tink, HashiCorp Vault client, Apache Commons Codec for crypto operations
    - Any external network calls
    - Any file I/O except reading classpath resources (CRY-09 only)
  You derive all parameter values from the task's Schema table.
  You do not invent parameter values not present in the Schema.

enforcement:
  # --- Fill these in from the task's Schema table and Global Enforcement Rules ---
  # Copy exact values. Do not paraphrase. Each rule must be independently testable.

  - "[FILL IN from Schema: Algorithm line — e.g. Algorithm must be Argon2id via Bouncy Castle, not PBKDF2 or BCrypt]"

  - "[FILL IN from Schema: Key/output length — e.g. Output must be exactly 32 bytes. Throw IllegalArgumentException if input key is not 32 bytes]"

  - "[FILL IN from Schema: Nonce rule — e.g. Nonce must be constructed via NonceBuilder.build(), not SecureRandom, for document encryption]"

  - "[FILL IN from Schema: AAD rule — e.g. AAD must be passed as doc_uuid_bytes (16) + version_seq_bytes (4). Empty AAD is a build error]"

  - "GCM authentication tag failure must throw AEADBadTagException. Catching this exception and returning null or an empty array is a build error."

  - "Every byte[] variable named key, masterKey, vaultKey, teamKey, sharedSecret, wrapKey, or password must be zeroed with Arrays.fill(b, (byte) 0) in a finally block before the method returns."

  - "No import statement may reference org.springframework.*, javafx.*, com.google.crypto.tink.*, or any HTTP client library."

  - "[FILL IN from Mandatory Adversarial Tests: the FAIL-expected case most likely to be skipped — e.g. unwrap() with wrong private key must throw, not return garbage]"

  - "If the task's Input File section lists upstream classes as dependencies, those classes must be imported and called — do not reimplement their logic inline."

  - "[FILL IN: one task-specific rule that is unique to this task and not covered by the globals above]"

### skills.md
# skills.md — CRY Cryptography Module
# INSTRUCTIONS:
#   Copy the skill names from the task's "Skills to Define" section in README.md.
#   Fill in one entry per skill. Each skill maps to one method in the output class.
#   Do not add skills not listed in the README — extra skills cause scope creep.
#   Delete these comments before committing.

skills:
  - name: "[FILL IN: skill name from README — e.g. derive_master_key]"
    description: >
      [FILL IN: one sentence from the README's Skills list — copy it verbatim.
       e.g. Derives a 32-byte AES-256 master key from a user password and salt using Argon2id.]
    input:
      - name: "[FILL IN: param 1 name]"
        type: "[FILL IN: Java type — e.g. char[], byte[], PublicKey, String]"
        constraint: "[FILL IN: exact constraint from Schema — e.g. must not be null, must be exactly 32 bytes]"
      - name: "[FILL IN: param 2 name, if any]"
        type: "[FILL IN: Java type]"
        constraint: "[FILL IN: constraint]"
    output:
      type: "[FILL IN: Java return type — e.g. byte[], String, X25519KeyPair]"
      guarantee: "[FILL IN: what the caller can rely on — e.g. always 32 bytes, always 6 space-separated words]"
    error_handling:
      invalid_input: "[FILL IN: what to throw on bad input — e.g. IllegalArgumentException if salt.length != 16]"
      crypto_failure: "[FILL IN: what to throw on crypto failure — e.g. AEADBadTagException propagated from GCM, never caught]"
      zeroing: "[FILL IN: which parameters are zeroed and when — e.g. password char[] zeroed in finally before return]"

  - name: "[FILL IN: second skill name from README — e.g. load_bip39_wordlist. Delete this entry if the task only has one skill.]"
    description: >
      [FILL IN: one sentence.]
    input:
      - name: "[FILL IN]"
        type: "[FILL IN]"
        constraint: "[FILL IN]"
    output:
      type: "[FILL IN]"
      guarantee: "[FILL IN]"
    error_handling:
      invalid_input: "[FILL IN]"
      crypto_failure: "[FILL IN — write N/A if this skill has no crypto operations]"
      zeroing: "[FILL IN — write N/A if this skill handles no key material]"

# --- Shared constraints that apply to every skill in this module ---
# Do not remove this section. It applies to all skills above.

global_skill_constraints:
  - No skill may log key bytes, plaintext content, or password characters at any log level.
  - No skill may import org.springframework.* or javafx.*.
  - Every skill that receives or produces a byte[] containing key material must zero it in a finally block.
  - Every skill that calls AES-GCM decrypt must let AEADBadTagException propagate — never catch it.
  - Method signatures must match the README Schema section exactly — same names, same types.


## 2. pom.xml Files
### Java-Project\crypto_module\pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.zero</groupId>
    <artifactId>crypto-module</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk18on</artifactId>
            <version>1.78.1</version>
        </dependency>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.25.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```
### Java-Project\snm_module\pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.project</groupId>
    <artifactId>snm-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>snm-backend</name>
    <description>snm-backend</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <executions>
                    <execution>
                        <id>default-compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                        <configuration>
                            <annotationProcessorPaths>
                                <path>
                                    <groupId>org.projectlombok</groupId>
                                    <artifactId>lombok</artifactId>
                                </path>
                            </annotationProcessorPaths>
                        </configuration>
                    </execution>
                    <execution>
                        <id>default-testCompile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>testCompile</goal>
                        </goals>
                        <configuration>
                            <annotationProcessorPaths>
                                <path>
                                    <groupId>org.projectlombok</groupId>
                                    <artifactId>lombok</artifactId>
                                </path>
                            </annotationProcessorPaths>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>

```

## 3. Java Files
### Java-Project\auth_module\src\main\java\auth\controller\AuthController.java
```java
public class AuthController
{
}

```
### Java-Project\auth_module\src\main\java\auth\model\User.java
```java
public class User
{
  private String email;
  private byte[] publicKey;
  private byte[] keyVault;
  private byte[] salt;

  public void setEmail(String email)
  {
    this.email = email;
  }

  public void setPublicKey(byte[] publicKey)
  {
    this.publicKey = publicKey;
  }

  public void setKeyVault(byte[] keyVault)
  {
    this.keyVault = keyVault;
  }

  public void setSalt(byte[] salt)
  {
    this.salt = salt;
  }
}

```
### Java-Project\auth_module\src\main\java\auth\repository\UserRepository.java
```java
public class UserRepository
{
}

```
### Java-Project\auth_module\src\main\java\auth\service\AuthService.java
```java
import auth.model.User;
import java.security.SecureRandom;

public class AuthService {

    private CryptoAdapter cryptoAdapter;

    public AuthService(CryptoAdapter cryptoAdapter) {
        this.cryptoAdapter = cryptoAdapter;
    }

    public User register(String email, char[] password) {

        byte[] salt = generateSalt();

        // Step 1: Master Key
        byte[] masterKey = cryptoAdapter.deriveMasterKey(password, salt);

        // Step 2: Vault Key
        byte[] vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

        // Step 3: Auth Signing Key (for future JWT)
        byte[] authKey = cryptoAdapter.deriveAuthKey(masterKey);

        // TODO: Replace with X25519 key pair generation from CryptoService
        byte[] publicKey = new byte[32];

        // TODO: Replace with AES-GCM vault encryption using CryptoService
        byte[] encryptedVault = new byte[0];

        // Create user object
        User user = new User();
        user.setEmail(email);
        user.setPublicKey(publicKey);
        user.setKeyVault(encryptedVault);
        user.setSalt(salt);

        // ✅ Secure cleanup (VERY IMPORTANT)
        zeroArray(masterKey);
        zeroArray(vaultKey);
        zeroArray(authKey);
        zeroCharArray(password);

        return user;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private void zeroArray(byte[] arr) {
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = 0;
            }
        }
    }

    private void zeroCharArray(char[] arr) {
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = 0;
            }
        }
    }
}

```
### Java-Project\auth_module\src\main\java\auth\service\CryptoAdapter.java
```java
import crypto.api.crypto_service;

public class CryptoAdapter
{
  private crypto_service cryptoService;
  
  public CryptoAdapter(crypto_service cryptoService)
  {
    this.cryptoService = cryptoService;
  }

  public byte[] deriveMasterKey(char[] password, byte[] salt)
  {
    // call crypto module
    return cryptoService.deriveMasterKey(password, salt);; // placeholder
  }

  public byte[] deriveVaultKey(byte[] masterKey)
  {
    return cryptoService.deriveSubKey(masterKey, "vault-key"); // placeholder
  }

  public byte[] deriveAuthKey(byte[] masterKey)
  {
    return cryptoService.deriveSubKey(masterKey, "auth-signing-key");
  }

  // Future integration points (to be implemented when exposed in CryptoService)

  public byte[][] generateKeyPair()
  {
    throw new UnsupportedOperationException("KeyPair generation not exposed yet"); // [public, private]
  }

  public byte[] encryptVault(byte[] privateKey, byte[] vaultKey)
  {
    throw new UnsupportedOperationException("Vault encryption not exposed yet"); // placeholder
  }
}

```
### Java-Project\crypto_module\src\main\java\crypto\api\CryptoService.java
```java
package crypto.api;

public interface CryptoService {

    /**
     * Derives a 32-byte master key from the user's password and database salt.
     * * @param password The user's plaintext password.
     * @param salt The Argon2id salt retrieved from the database.
     * @return 32-byte Master Key.
     */
    byte[] deriveMasterKey(char[] password, byte[] salt);

    /**
     * Derives a context-specific sub-key from the master key.
     * @param masterKey The 32-byte master key.
     * @param info The context label (e.g. "vault-key").
     * @return 32-byte Sub-Key.
     */
    byte[] deriveSubKey(byte[] masterKey, String info);

}
```
### Java-Project\crypto_module\src\main\java\crypto\internal\CryptoDemonstration.java
```java
package crypto.internal;

import java.security.PublicKey;
import java.util.HexFormat;

/**
 * Manual demonstration tester for SubKeyDerivation and KeyPairService.
 */
public class CryptoDemonstration {

    public static void main(String[] args) {
        System.out.println("=== CRYPTO MODULE DEMONSTRATION ===\n");

        // 1. Sub-Key Derivation Demo
        demoSubKeyDerivation();

        System.out.println("\n-----------------------------------\n");

        // 2. X25519 Key Pair Demo
        demoKeyPairService();

        System.out.println("\n-----------------------------------\n");

        // 3. ECDH Shared Secret Demo (Preview of CRY-04)
        demoSharedSecret();
    }

    private static void demoSubKeyDerivation() {
        System.out.println("[TASK CRY-02] Sub-Key Derivation (HKDF-SHA256)");
        SubKeyDerivation derivation = new SubKeyDerivation();
        
        // Simulating a 32-byte master key from Argon2id
        byte[] mockMasterKey = new byte[32];
        for (int i = 0; i < 32; i++) mockMasterKey[i] = (byte) (i + 1);

        System.out.println("Master Key (Mock): " + HexFormat.of().formatHex(mockMasterKey));

        // Derive different keys
        byte[] vaultKey = derivation.derive(mockMasterKey, "vault-key");
        byte[] authKey = derivation.derive(mockMasterKey, "auth-signing-key");

        System.out.println("Derived Vault Key: " + HexFormat.of().formatHex(vaultKey));
        System.out.println("Derived Auth Key:  " + HexFormat.of().formatHex(authKey));
        System.out.println("Result: Keys are unique for each context string.");
    }

    private static void demoKeyPairService() {
        System.out.println("[TASK CRY-03] X25519 Key Pair Generation");
        KeyPairService service = new KeyPairService();

        // Generate
        X25519KeyPair kp = service.generateKeyPair();
        System.out.println("Generated X25519 Key Pair.");
        System.out.println("Public Key (Raw 32-bytes): " + HexFormat.of().formatHex(kp.publicKeyBytes()));

        // Demonstrate loading the public key object from raw bytes
        PublicKey loadedKey = service.loadPublicKey(kp.publicKeyBytes());
        System.out.println("Re-loaded Public Key Algorithm: " + loadedKey.getAlgorithm());
        
        // Demonstrate extracting bytes back from a PublicKey object
        byte[] reExtracted = service.extractPublicKeyBytes(loadedKey);
        System.out.println("Re-extracted Bytes Match:      " + (HexFormat.of().formatHex(reExtracted).equals(HexFormat.of().formatHex(kp.publicKeyBytes()))));
    }

    private static void demoSharedSecret() {
        System.out.println("[PREVIEW CRY-04] ECDH Shared Secret Calculation");
        KeyPairService service = new KeyPairService();
        EcdhService ecdhService = new EcdhService();

        // Create two parties
        X25519KeyPair alice = service.generateKeyPair();
        X25519KeyPair bob   = service.generateKeyPair();

        System.out.println("Alice and Bob generated their own key pairs.");

        try {
            // Enforcement: Use EcdhService which applies SHA-256 post-processing
            // and zeros the raw intermediate secret — matches the production code path.
            byte[] aliceSecret = ecdhService.computeSharedSecret(
                    alice.privateKey(), service.loadPublicKey(bob.publicKeyBytes()));

            byte[] bobSecret = ecdhService.computeSharedSecret(
                    bob.privateKey(), service.loadPublicKey(alice.publicKeyBytes()));

            System.out.println("Alice's Shared Secret (SHA-256): " + HexFormat.of().formatHex(aliceSecret));
            System.out.println("Bob's Shared Secret   (SHA-256): " + HexFormat.of().formatHex(bobSecret));
            System.out.println("Secrets Match:                   " + java.util.Arrays.equals(aliceSecret, bobSecret));
            System.out.println("Result: Both parties arrived at the same SHA-256 post-processed secret.");
        } catch (Exception e) {
            System.err.println("ECDH Demo Failed: " + e.getMessage());
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\CryptoOperationException.java
```java
package crypto.internal;

/**
 * Base exception for all unrecoverable cryptographic operation failures.
 * This replaces generic RuntimeException throws across the module.
 */
public class CryptoOperationException extends RuntimeException {
    
    public CryptoOperationException(String message) {
        super(message);
    }

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\CryptoServiceImpl.java
```java
package crypto.internal;

import crypto.api.CryptoService;

/**
 * Implementation of CryptoService using internal components.
 */
public class CryptoServiceImpl implements CryptoService {
    
    private final MasterKeyDerivation masterDerivation = new MasterKeyDerivation();
    private final SubKeyDerivation subDerivation = new SubKeyDerivation();

    @Override
    public byte[] deriveMasterKey(char[] password, byte[] salt) {
        return masterDerivation.derive(password, salt);
    }

    @Override
    public byte[] deriveSubKey(byte[] masterKey, String info) {
        return subDerivation.derive(masterKey, info);
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\EcdhService.java
```java
package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.Arrays;

/**
 * Task CRY-04: ECDH shared secret calculation.
 * Computes a secure shared secret between two parties using X25519.
 */
public class EcdhService {

    static {
        // Enforcement: Bouncy Castle provider must be explicitly added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Computes the SHA-256 hashed shared secret using X25519 ECDH.
     * 
     * @param myPrivateKey   Caller's X25519 private key.
     * @param theirPublicKey Counterparty's X25519 public key.
     * @return 32-byte hashed shared secret.
     * @throws IllegalArgumentException if keys are null.
     * @throws CryptoOperationException if the ECDH operation fails.
     */
    public byte[] computeSharedSecret(PrivateKey myPrivateKey, PublicKey theirPublicKey) {
        if (myPrivateKey == null || theirPublicKey == null) {
            throw new IllegalArgumentException("Keys must not be null");
        }

        byte[] rawSecret = null;
        try {
            // Enforcement: Algorithm must be X25519 via Bouncy Castle
            KeyAgreement agreement = KeyAgreement.getInstance("X25519", "BC");
            agreement.init(myPrivateKey);
            agreement.doPhase(theirPublicKey, true);
            
            rawSecret = agreement.generateSecret();
            
            // Enforcement: Raw ECDH output is not uniformly distributed - always hash it with SHA-256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(rawSecret);

        } catch (InvalidKeyException e) {
            // Propagate for mandatory adversarial tests (e.g., Ed25519 check)
            throw new CryptoOperationException("Invalid key for ECDH", e);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptoOperationException("X25519 ECDH not supported", e);
        } finally {
            // Enforcement: Raw shared secret zeroed immediately after hashing
            if (rawSecret != null) {
                Arrays.fill(rawSecret, (byte) 0);
            }
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\EncryptionEngine.java
```java
package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;

/**
 * Task CRY-06: AES-256-GCM Encryption Engine.
 * Provides core encryption and decryption for document blobs.
 */
public class EncryptionEngine {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int KEY_LENGTH = 32; // bytes
    private static final int NONCE_LENGTH = 12; // bytes
    private static final int AAD_LENGTH = 20; // bytes

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     *
     * @param plaintext Padded plaintext bytes.
     * @param teamKey   32-byte AES key.
     * @param nonce     12-byte deterministic nonce.
     * @param aad       20-byte associated data (doc UUID + version).
     * @return Ciphertext with 16-byte tag appended.
     */
    public byte[] encrypt(byte[] plaintext, byte[] teamKey, byte[] nonce, byte[] aad) {
        validateInputs(teamKey, nonce, aad);
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(teamKey, "AES");
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            cipher.updateAAD(aad);
            
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new CryptoOperationException("Encryption failed", e);
        } finally {
            // Enforcement: Zeroing plaintext byte[]
            Arrays.fill(plaintext, (byte) 0);
            // Enforcement: Zero teamKey (AES key material) after use
            Arrays.fill(teamKey, (byte) 0);
        }
    }

    /**
     * Decrypts AES-256-GCM ciphertext.
     *
     * @param ciphertext Ciphertext with tag appended.
     * @param teamKey    32-byte AES key.
     * @param nonce      12-byte deterministic nonce.
     * @param aad        20-byte associated data.
     * @return Decrypted plaintext.
     * @throws javax.crypto.AEADBadTagException if authentication fails.
     */
    public byte[] decrypt(byte[] ciphertext, byte[] teamKey, byte[] nonce, byte[] aad) throws javax.crypto.AEADBadTagException {
        validateInputs(teamKey, nonce, aad);
        if (ciphertext == null) {
            throw new IllegalArgumentException("Ciphertext cannot be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(teamKey, "AES");
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            cipher.updateAAD(aad);
            
            return cipher.doFinal(ciphertext);
        } catch (javax.crypto.AEADBadTagException e) {
            // Enforcement: Decrypt failure must throw AEADBadTagException.
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof javax.crypto.AEADBadTagException) {
                throw (javax.crypto.AEADBadTagException) e.getCause();
            }
            throw new CryptoOperationException("Decryption failed", e);
        } finally {
            // Enforcement: Zero teamKey (AES key material) after use
            Arrays.fill(teamKey, (byte) 0);
        }
    }

    private void validateInputs(byte[] teamKey, byte[] nonce, byte[] aad) {
        if (teamKey == null || teamKey.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Team key must be exactly 32 bytes");
        }
        if (nonce == null || nonce.length != NONCE_LENGTH) {
            throw new IllegalArgumentException("Nonce must be exactly 12 bytes");
        }
        // Enforcement: Allow 20 bytes (documents) or 0 bytes (envelopes)
        if (aad == null || (aad.length != AAD_LENGTH && aad.length != 0)) {
            throw new IllegalArgumentException("AAD must be exactly 20 bytes or 0 bytes");
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\FingerprintService.java
```java
package crypto.internal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Task CRY-09: BIP39 Fingerprint Generation.
 * Generates a 6-word mnemonic fingerprint from two public keys.
 */
public class FingerprintService {

    private final String[] wordlist;

    /**
     * Constructor loads the BIP39 wordlist from classpath.
     */
    public FingerprintService() {
        this.wordlist = loadWordlist();
        if (wordlist.length != 2048) {
            throw new CryptoOperationException("BIP39 wordlist must contain exactly 2048 words. Found: " + wordlist.length);
        }
    }

    /**
     * Generates a 6-word fingerprint from two X25519 public keys.
     * Normalized lexicographically to ensure fingerprint(A, B) == fingerprint(B, A).
     * 
     * @param publicKeyA First 32-byte public key.
     * @param publicKeyB Second 32-byte public key.
     * @return 6-word mnemonic string.
     */
    public String generate(byte[] publicKeyA, byte[] publicKeyB) {
        validateKeys(publicKeyA, publicKeyB);

        // 1. Normalize order (unsigned lexicographical comparison)
        byte[] lower;
        byte[] higher;
        if (compareUnsigned(publicKeyA, publicKeyB) <= 0) {
            lower = publicKeyA;
            higher = publicKeyB;
        } else {
            lower = publicKeyB;
            higher = publicKeyA;
        }

        // 2. Concatenate keys (64 bytes)
        byte[] combined = new byte[64];
        System.arraycopy(lower, 0, combined, 0, 32);
        System.arraycopy(higher, 0, combined, 32, 32);

        try {
            // 3. Hash with SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined);

            // 4. Map first 6 bytes to words
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                // Step 5 & 6: unsigned byte % 2048
                int wordIndex = Byte.toUnsignedInt(hash[i]) % 2048;
                sb.append(wordlist[wordIndex]);
                if (i < 5) {
                    sb.append(" ");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            throw new CryptoOperationException("Fingerprint generation failed", e);
        }
    }

    private String[] loadWordlist() {
        try (InputStream is = getClass().getResourceAsStream("/bip39-english.txt")) {
            if (is == null) {
                throw new CryptoOperationException("Wordlist resource not found: /bip39-english.txt");
            }
            List<String> words = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        words.add(trimmed);
                    }
                }
            }
            return words.toArray(new String[0]);
        } catch (Exception e) {
            throw new CryptoOperationException("Failed to load BIP39 wordlist", e);
        }
    }

    private void validateKeys(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Public keys cannot be null");
        }
        if (a.length != 32 || b.length != 32) {
            throw new IllegalArgumentException("Public keys must be exactly 32 bytes");
        }
    }

    private int compareUnsigned(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            int valA = Byte.toUnsignedInt(a[i]);
            int valB = Byte.toUnsignedInt(b[i]);
            if (valA != valB) {
                return valA - valB;
            }
        }
        return 0;
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\KeyPairService.java
```java
package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;
import org.bouncycastle.jcajce.spec.RawEncodedKeySpec;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * Task CRY-03: X25519 Key Pair Generation.
 * Provides services to generate and manage X25519 keys for ECDH.
 */
public class KeyPairService {

    static {
        // Enforcement: Bouncy Castle provider must be explicitly added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generates a fresh random X25519 key pair.
     * 
     * @return X25519KeyPair containing the 32-byte public key and the PrivateKey object.
     */
    public X25519KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519", "BC");
            KeyPair kp = kpg.generateKeyPair();
            
            byte[] pubBytes = extractPublicKeyBytes(kp.getPublic());
            return new X25519KeyPair(pubBytes, kp.getPrivate());
            
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptoOperationException("X25519 KeyPair generation failed", e);
        }
    }

    /**
     * Extracts the raw 32-byte array from an X25519 public key.
     * 
     * @param publicKey The X25519 public key object.
     * @return 32-byte raw public key.
     * @throws IllegalArgumentException if the key is not X25519.
     */
    public byte[] extractPublicKeyBytes(PublicKey publicKey) {
        // Enforcement: Must be extractable as raw 32-byte array, not DER format
        if (publicKey instanceof XDHPublicKey xdhKey) {
            return xdhKey.getUEncoding();
        }
        throw new IllegalArgumentException("Provided key is not a valid X25519 public key");
    }

    /**
     * Reconstructs an X25519 PublicKey from its raw 32-byte representation.
     * 
     * @param rawBytes 32-byte raw public key.
     * @return PublicKey object for use in ECDH.
     * @throws IllegalArgumentException if bytes are invalid or wrong length.
     */
    public PublicKey loadPublicKey(byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length != 32) {
            throw new IllegalArgumentException("X25519 public key must be exactly 32 bytes");
        }

        // Enforcement: Reject all-zero public keys (invalid point)
        boolean allZero = true;
        for (byte b : rawBytes) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            throw new IllegalArgumentException("Invalid X25519 public key: all zeros");
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("X25519", "BC");
            return kf.generatePublic(new RawEncodedKeySpec(rawBytes));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid X25519 public key material", e);
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\MasterKeyDerivation.java
```java
package crypto.internal;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.util.Arrays;

/**
 * Task CRY-01: Argon2id Master Key Derivation.
 * Hard floor parameters: m=65536, t=3, p=4.
 */
public class MasterKeyDerivation {

    private static final int MEMORY_COST = 65536; // 64 MB
    private static final int TIME_COST = 3;
    private static final int PARALLELISM = 4;
    private static final int OUTPUT_LENGTH = 32;

    static {
        // Global Enforcement: Parameters may not be lowered below m=65536, t=3, p=4
        if (MEMORY_COST < 65536 || TIME_COST < 3 || PARALLELISM < 4) {
            throw new ExceptionInInitializerError("Argon2id parameters below hard floor");
        }
    }

    /**
     * Derives a 32-byte master key from a password and salt using Argon2id.
     *
     * @param password The user's plaintext password. Will be zeroed before return.
     * @param salt     16 random bytes.
     * @return 32-byte master key.
     * @throws IllegalArgumentException if salt is not 16 bytes.
     */
    public byte[] derive(char[] password, byte[] salt) {
        if (salt == null || salt.length != 16) {
            throw new IllegalArgumentException("Salt must be exactly 16 bytes");
        }

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(MEMORY_COST)
                .withIterations(TIME_COST)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] masterKey = new byte[OUTPUT_LENGTH];
        try {
            generator.generateBytes(password, masterKey);
            return masterKey;
        } finally {
            // Global Enforcement: Zeroing password char[]
            if (password != null) {
                Arrays.fill(password, (char) 0);
            }
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\NonceBuilder.java
```java
package crypto.internal;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Task CRY-06: Deterministic Nonce Builder.
 * Constructs a 12-byte GCM nonce from context fields.
 */
public class NonceBuilder {

    /**
     * Constructs a 12-byte deterministic nonce.
     * Nonce layout:
     * [0..1]  = teamKeyVersion (2 bytes, big-endian)
     * [2..5]  = first 4 bytes of docUuid (4 bytes)
     * [6..11] = counterValue (6 bytes, big-endian)
     *
     * @param teamKeyVersion Short version of the team key.
     * @param docUuid        UUID of the document.
     * @param counterValue   6-byte counter value (long).
     * @return 12-byte nonce array.
     */
    public byte[] build(short teamKeyVersion, UUID docUuid, long counterValue) {
        if (docUuid == null) {
            throw new IllegalArgumentException("Document UUID cannot be null");
        }
        
        byte[] nonce = new byte[12];
        ByteBuffer buffer = ByteBuffer.wrap(nonce);

        // 1. team_key_version (short, 2 bytes)
        buffer.putShort(teamKeyVersion);

        // 2. first 4 bytes of doc UUID (4 bytes)
        // UUID.getMostSignificantBits() returns a long; we take the first 4 bytes.
        long msb = docUuid.getMostSignificantBits();
        buffer.putInt((int) (msb >>> 32));

        // 3. counter_value (6 bytes from big-endian long)
        // We take the last 6 bytes of the long.
        buffer.put((byte) (counterValue >>> 40));
        buffer.put((byte) (counterValue >>> 32));
        buffer.put((byte) (counterValue >>> 24));
        buffer.put((byte) (counterValue >>> 16));
        buffer.put((byte) (counterValue >>> 8));
        buffer.put((byte) (counterValue & 0xFF));

        return nonce;
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\PaddingUtil.java
```java
package crypto.internal;

import java.util.Arrays;

/**
 * Task CRY-08: Payload Padding.
 * Implements 256-byte block alignment using a PKCS#7-style scheme.
 */
public class PaddingUtil {

    private static final int BLOCK_SIZE = 256;

    /**
     * Pads a byte array to the nearest 256-byte boundary.
     * Always adds at least one byte of padding. If input is already block-aligned,
     * a full extra block (256 bytes) is added.
     *
     * @param plaintext Original data.
     * @return Padded data, length is a multiple of 256.
     */
    public byte[] pad(byte[] plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }

        int padLength = BLOCK_SIZE - (plaintext.length % BLOCK_SIZE);
        // Note: if len % 256 == 0, padLength will be 256.
        
        byte[] padded = new byte[plaintext.length + padLength];
        System.arraycopy(plaintext, 0, padded, 0, plaintext.length);
        
        // Fill padding bytes with the length value.
        // For padLength = 256, (byte)256 is 0.
        byte padValue = (byte) (padLength & 0xFF);
        for (int i = plaintext.length; i < padded.length; i++) {
            padded[i] = padValue;
        }
        
        return padded;
    }

    /**
     * Removes PKCS#7-style padding from a 256-byte aligned array.
     *
     * @param padded Padded data.
     * @return Original plaintext.
     * @throws IllegalArgumentException if padding is invalid or alignment is wrong.
     */
    public byte[] unpad(byte[] padded) {
        if (padded == null) {
            throw new IllegalArgumentException("Padded data cannot be null");
        }
        if (padded.length == 0 || padded.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Invalid alignment: length must be a multiple of 256");
        }

        // Read last byte to determine padding length
        int lastByte = padded[padded.length - 1] & 0xFF;
        int effectivePadLength = (lastByte == 0) ? 256 : lastByte;
        
        if (effectivePadLength > padded.length) {
            throw new IllegalArgumentException("Padding length exceeds array size");
        }

        // Validate all padding bytes
        for (int i = padded.length - effectivePadLength; i < padded.length; i++) {
            if ((padded[i] & 0xFF) != lastByte) {
                throw new IllegalArgumentException("Invalid padding pattern");
            }
        }

        return Arrays.copyOfRange(padded, 0, padded.length - effectivePadLength);
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\SubKeyDerivation.java
```java
package crypto.internal;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import java.nio.charset.StandardCharsets;

/**
 * Task CRY-02: HKDF sub-key derivation.
 * Derives context-specific keys from a master key using HKDF-SHA256.
 */
public class SubKeyDerivation {

    private static final int OUTPUT_LENGTH = 32;

    /**
     * Derives a 32-byte sub-key from the master key.
     *
     * @param masterKey 32-byte master key from Argon2id.
     * @param info      Context label (e.g., "vault-key", "auth-signing-key").
     * @return 32-byte derived sub-key.
     * @throws IllegalArgumentException if input is invalid or info is unknown.
     * @apiNote The {@code masterKey} is intentionally <strong>not</strong> zeroed inside this
     *          method. Multiple sub-keys (e.g., "vault-key", "auth-signing-key", "team-key-wrap")
     *          are typically derived from the same master key in sequence; zeroing it here would
     *          corrupt subsequent derivation calls. The <strong>caller is responsible</strong> for
     *          zeroing {@code masterKey} via {@code Arrays.fill(masterKey, (byte) 0)} once all
     *          required sub-keys have been derived.
     */
    public byte[] derive(byte[] masterKey, String info) {
        // Enforcement: Validate input length
        if (masterKey == null || masterKey.length != 32) {
            throw new IllegalArgumentException("Master key must be exactly 32 bytes");
        }

        // Enforcement: Validate info string
        if (!"vault-key".equals(info) && 
            !"auth-signing-key".equals(info) && 
            !"team-key-wrap".equals(info)) {
            throw new IllegalArgumentException("Unknown HKDF context: " + info);
        }

        // HKDF-SHA256 (RFC 5869)
        // Salt is null (zero-length) as master key is already high-entropy
        HKDFParameters params = new HKDFParameters(
                masterKey, 
                null, 
                info.getBytes(StandardCharsets.UTF_8)
        );

        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA256Digest());
        generator.init(params);

        byte[] subKey = new byte[OUTPUT_LENGTH];
        generator.generateBytes(subKey, 0, OUTPUT_LENGTH);

        // Caller-managed: masterKey is NOT zeroed here — see @apiNote in Javadoc above.
        return subKey;
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\TeamKeyEnvelope.java
```java
package crypto.internal;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Task CRY-07: Team Key Envelope Wrap and Unwrap.
 * Protects an AES team key using ECDH shared secrets and HKDF wrap keys.
 */
public class TeamKeyEnvelope {

    private final EcdhService ecdhService = new EcdhService();
    private final SubKeyDerivation subKeyDerivation = new SubKeyDerivation();
    private final EncryptionEngine encryptionEngine = new EncryptionEngine();
    
    private static final int NONCE_LENGTH = 12;
    private static final String WRAP_INFO = "team-key-wrap";

    /**
     * Wraps a team key for a recipient.
     * 
     * @param teamKey         32-byte team key to protect.
     * @param recipientPubKey Recipient's X25519 public key.
     * @param myPrivateKey    Sender's X25519 private key.
     * @return Sealed envelope: nonce[12] + ciphertext.
     */
    public byte[] wrap(byte[] teamKey, PublicKey recipientPubKey, PrivateKey myPrivateKey) {
        if (teamKey == null || teamKey.length != 32) {
            throw new IllegalArgumentException("Team key must be 32 bytes");
        }

        byte[] sharedSecret = null;
        byte[] wrapKey = null;
        byte[] nonce = new byte[NONCE_LENGTH];
        
        try {
            // 1. Compute Shared Secret
            sharedSecret = ecdhService.computeSharedSecret(myPrivateKey, recipientPubKey);
            
            // 2. Derive Wrap Key via HKDF
            wrapKey = subKeyDerivation.derive(sharedSecret, WRAP_INFO);
            
            // 3. Generate random nonce
            new SecureRandom().nextBytes(nonce);
            
            // 4. Encrypt teamKey (AAD is empty for envelopes)
            byte[] ciphertext = encryptionEngine.encrypt(teamKey, wrapKey, nonce, new byte[0]);
            
            // 5. Construct envelope: nonce + ciphertext
            byte[] envelope = new byte[NONCE_LENGTH + ciphertext.length];
            System.arraycopy(nonce, 0, envelope, 0, NONCE_LENGTH);
            System.arraycopy(ciphertext, 0, envelope, NONCE_LENGTH, ciphertext.length);
            
            return envelope;
            
        } finally {
            // Enforcement: Zeroing all sensitive material
            if (sharedSecret != null) Arrays.fill(sharedSecret, (byte) 0);
            if (wrapKey != null) Arrays.fill(wrapKey, (byte) 0);
            if (teamKey != null) Arrays.fill(teamKey, (byte) 0);
        }
    }

    /**
     * Unwraps a team key envelope.
     * 
     * @param envelope     Sealed envelope from wrap().
     * @param senderPubKey Sender's X25519 public key.
     * @param myPrivateKey Recipient's X25519 private key.
     * @return Original 32-byte team key.
     * @throws javax.crypto.AEADBadTagException if authentication fails.
     */
    public byte[] unwrap(byte[] envelope, PublicKey senderPubKey, PrivateKey myPrivateKey) throws javax.crypto.AEADBadTagException {
        if (envelope == null || envelope.length < NONCE_LENGTH + 16) {
            throw new IllegalArgumentException("Invalid envelope length");
        }

        byte[] sharedSecret = null;
        byte[] wrapKey = null;
        
        try {
            // 1. Compute Shared Secret
            sharedSecret = ecdhService.computeSharedSecret(myPrivateKey, senderPubKey);
            
            // 2. Derive Wrap Key via HKDF
            wrapKey = subKeyDerivation.derive(sharedSecret, WRAP_INFO);
            
            // 3. Extract nonce and ciphertext
            byte[] nonce = Arrays.copyOfRange(envelope, 0, NONCE_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(envelope, NONCE_LENGTH, envelope.length);
            
            // 4. Decrypt (AAD is empty)
            return encryptionEngine.decrypt(ciphertext, wrapKey, nonce, new byte[0]);
            
        } finally {
            // Enforcement: Zeroing intermediate keys
            if (sharedSecret != null) Arrays.fill(sharedSecret, (byte) 0);
            if (wrapKey != null) Arrays.fill(wrapKey, (byte) 0);
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\VaultService.java
```java
package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * Task CRY-05: Key Vault Seal and Unseal.
 * Protects private key material using AES-256-GCM.
 */
public class VaultService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int NONCE_LENGTH = 12; // in bytes
    private static final int KEY_LENGTH = 32; // in bytes

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Seals a private key using AES-256-GCM with a random nonce.
     *
     * @param privateKeyBytes Raw private key material to protect.
     * @param vaultKey        32-byte key derived via HKDF.
     * @return Sealed output: nonce[12] + ciphertext + gcmTag[16].
     * @throws IllegalArgumentException if vaultKey is not 32 bytes or privateKeyBytes is null.
     */
    public byte[] seal(byte[] privateKeyBytes, byte[] vaultKey) {
        if (privateKeyBytes == null) {
            throw new IllegalArgumentException("Private key bytes cannot be null");
        }
        if (vaultKey == null || vaultKey.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Vault key must be exactly 32 bytes");
        }

        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[NONCE_LENGTH];
        random.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(vaultKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] ciphertext = cipher.doFinal(privateKeyBytes);
            
            byte[] vaultBlob = new byte[NONCE_LENGTH + ciphertext.length];
            System.arraycopy(nonce, 0, vaultBlob, 0, NONCE_LENGTH);
            System.arraycopy(ciphertext, 0, vaultBlob, NONCE_LENGTH, ciphertext.length);
            
            return vaultBlob;
        } catch (Exception e) {
            throw new CryptoOperationException("Vault seal failed", e);
        } finally {
            // Enforcement: privateKeyBytes not zeroed after seal() is a failure mode
            Arrays.fill(privateKeyBytes, (byte) 0);
            // Enforcement: Zero vaultKey (AES key material) after use
            Arrays.fill(vaultKey, (byte) 0);
        }
    }

    /**
     * Unseals a sealed blob to recover the original private key.
     *
     * @param vaultBlob Sealed output from seal().
     * @param vaultKey  32-byte key used during sealing.
     * @return Recovered raw private key bytes.
     * @throws IllegalArgumentException if vaultKey is not 32 bytes or vaultBlob is too short.
     * @throws javax.crypto.AEADBadTagException if authentication fails.
     */
    public byte[] unseal(byte[] vaultBlob, byte[] vaultKey) throws javax.crypto.AEADBadTagException {
        if (vaultBlob == null || vaultBlob.length < NONCE_LENGTH + 16) {
            throw new IllegalArgumentException("Vault blob is too short or null");
        }
        if (vaultKey == null || vaultKey.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Vault key must be exactly 32 bytes");
        }

        byte[] nonce = Arrays.copyOfRange(vaultBlob, 0, NONCE_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(vaultBlob, NONCE_LENGTH, vaultBlob.length);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(vaultKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            return cipher.doFinal(ciphertext);
        } catch (javax.crypto.AEADBadTagException e) {
            // Enforcement: GCM tag failure must throw AEADBadTagException.
            throw e;
        } catch (Exception e) {
            throw new CryptoOperationException("Vault unseal failed", e);
        } finally {
            // Enforcement: Zero vaultKey (AES key material) after use
            Arrays.fill(vaultKey, (byte) 0);
        }
    }
}

```
### Java-Project\crypto_module\src\main\java\crypto\internal\X25519KeyPair.java
```java
package crypto.internal;

import java.security.PrivateKey;

/**
 * Container for X25519 key material.
 * 
 * @param publicKeyBytes Raw 32-byte public key.
 * @param privateKey      The private key object (held in RAM).
 */
public record X25519KeyPair(byte[] publicKeyBytes, PrivateKey privateKey) {
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\EcdhServiceTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import static org.assertj.core.api.Assertions.*;

public class EcdhServiceTest {

    private final EcdhService ecdhService = new EcdhService();
    private final KeyPairService keyPairService = new KeyPairService();

    @Test
    void shouldVerifyCommutativity() {
        X25519KeyPair alice = keyPairService.generateKeyPair();
        X25519KeyPair bob = keyPairService.generateKeyPair();

        // Alice computes: Alice_priv + Bob_pub
        byte[] aliceSecret = ecdhService.computeSharedSecret(alice.privateKey(), 
                keyPairService.loadPublicKey(bob.publicKeyBytes()));

        // Bob computes: Bob_priv + Alice_pub
        byte[] bobSecret = ecdhService.computeSharedSecret(bob.privateKey(), 
                keyPairService.loadPublicKey(alice.publicKeyBytes()));

        assertThat(aliceSecret).isEqualTo(bobSecret);
        assertThat(aliceSecret).hasSize(32);
    }

    @Test
    void shouldProduceDifferentSecretsForDifferentPairs() {
        X25519KeyPair alice = keyPairService.generateKeyPair();
        X25519KeyPair bob = keyPairService.generateKeyPair();
        X25519KeyPair charlie = keyPairService.generateKeyPair();

        byte[] secretAB = ecdhService.computeSharedSecret(alice.privateKey(), 
                keyPairService.loadPublicKey(bob.publicKeyBytes()));
        byte[] secretAC = ecdhService.computeSharedSecret(alice.privateKey(), 
                keyPairService.loadPublicKey(charlie.publicKeyBytes()));

        assertThat(secretAB).isNotEqualTo(secretAC);
    }

    @Test
    void shouldFailForNullInputs() {
        assertThatThrownBy(() -> ecdhService.computeSharedSecret(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailForWrongKeyType() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        
        // Ed25519 is for signatures, not ECDH. Should fail.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519", "BC");
        KeyPair edPair = kpg.generateKeyPair();
        
        X25519KeyPair alice = keyPairService.generateKeyPair();

        assertThatThrownBy(() -> ecdhService.computeSharedSecret(edPair.getPrivate(), 
                keyPairService.loadPublicKey(alice.publicKeyBytes())))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(InvalidKeyException.class);
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\EncryptionEngineTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import java.util.Arrays;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

public class EncryptionEngineTest {

    private final EncryptionEngine engine = new EncryptionEngine();
    private final NonceBuilder nonceBuilder = new NonceBuilder();
    private final byte[] teamKey = new byte[32];
    private final byte[] aad = new byte[20];
    private final UUID docUuid = UUID.randomUUID();

    public EncryptionEngineTest() {
        Arrays.fill(teamKey, (byte) 0x11);
        Arrays.fill(aad, (byte) 0x22);
    }

    @Test
    void shouldEncryptAndDecryptCorrectly() throws AEADBadTagException {
        byte[] original = "this-is-padded-to-exactly-32-byt".getBytes(); // 32 bytes
        byte[] plaintext = Arrays.copyOf(original, original.length);
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 100L);

        byte[] ciphertext = engine.encrypt(plaintext, teamKey, nonce, aad);
        
        // Plaintext should be zeroed
        assertThat(plaintext).containsOnly((byte) 0);

        byte[] decrypted = engine.decrypt(ciphertext, teamKey, nonce, aad);
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void shouldThrowExceptionOnTamperedCiphertext() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        ciphertext[5] ^= 0x01; // Tamper

        assertThatThrownBy(() -> engine.decrypt(ciphertext, teamKey, nonce, aad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnWrongKey() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        byte[] wrongKey = new byte[32];
        Arrays.fill(wrongKey, (byte) 0x99);

        assertThatThrownBy(() -> engine.decrypt(ciphertext, wrongKey, nonce, aad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnWrongAAD() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        byte[] wrongAad = new byte[20];
        Arrays.fill(wrongAad, (byte) 0x33);

        assertThatThrownBy(() -> engine.decrypt(ciphertext, teamKey, nonce, wrongAad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnWrongNonce() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        byte[] wrongNonce = nonceBuilder.build((short) 1, docUuid, 2L);

        assertThatThrownBy(() -> engine.decrypt(ciphertext, teamKey, wrongNonce, aad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void nonceBuilderShouldProduceDifferentNoncesForDifferentCounters() {
        byte[] n1 = nonceBuilder.build((short) 1, docUuid, 0L);
        byte[] n2 = nonceBuilder.build((short) 1, docUuid, 1L);

        assertThat(n1).isNotEqualTo(n2);
        assertThat(n1).hasSize(12);
    }

    @Test
    void shouldValidateKeyLength() {
        byte[] shortKey = new byte[31];
        byte[] nonce = new byte[12];
        assertThatThrownBy(() -> engine.encrypt(new byte[16], shortKey, nonce, aad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team key must be exactly 32 bytes");
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\FingerprintServiceTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class FingerprintServiceTest {

    private final FingerprintService service = new FingerprintService();

    @Test
    void shouldGenerateCommutativeFingerprints() {
        byte[] keyA = new byte[32];
        byte[] keyB = new byte[32];
        Arrays.fill(keyA, (byte) 1);
        Arrays.fill(keyB, (byte) 2);

        String fp1 = service.generate(keyA, keyB);
        String fp2 = service.generate(keyB, keyA);

        assertThat(fp1).isEqualTo(fp2);
        assertThat(fp1.split(" ")).hasSize(6);
    }

    @Test
    void shouldHandleSameKeys() {
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 0xAA);

        String fp = service.generate(key, key);
        assertThat(fp.split(" ")).hasSize(6);
    }

    @Test
    void shouldProduceDifferentFingerprintsForDifferentKeys() {
        byte[] key1 = new byte[32];
        byte[] key2 = new byte[32];
        byte[] key3 = new byte[32];
        Arrays.fill(key1, (byte) 1);
        Arrays.fill(key2, (byte) 2);
        Arrays.fill(key3, (byte) 3);

        String fp12 = service.generate(key1, key2);
        String fp13 = service.generate(key1, key3);

        assertThat(fp12).isNotEqualTo(fp13);
    }

    @Test
    void shouldFailOnInvalidKeyLengths() {
        byte[] valid = new byte[32];
        byte[] invalid = new byte[31];

        assertThatThrownBy(() -> service.generate(valid, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void shouldFailOnNullKeys() {
        byte[] valid = new byte[32];
        assertThatThrownBy(() -> service.generate(valid, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldLoadWordlistCorrectly() {
        // This implicitly checks the wordlist length in constructor
        assertThatNoException().isThrownBy(FingerprintService::new);
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\KeyPairServiceTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import java.security.PublicKey;
import static org.assertj.core.api.Assertions.*;

public class KeyPairServiceTest {

    private final KeyPairService keyPairService = new KeyPairService();

    @Test
    void shouldGenerateValidKeyPair() {
        X25519KeyPair kp = keyPairService.generateKeyPair();
        
        assertThat(kp).isNotNull();
        assertThat(kp.publicKeyBytes()).hasSize(32);
        assertThat(kp.privateKey()).isNotNull();
        assertThat(kp.privateKey().getAlgorithm()).isEqualTo("X25519");
    }

    @Test
    void shouldExtractAndLoadPublicKey() {
        X25519KeyPair kp = keyPairService.generateKeyPair();
        byte[] rawBytes = kp.publicKeyBytes();
        
        PublicKey loadedKey = keyPairService.loadPublicKey(rawBytes);
        
        assertThat(loadedKey).isNotNull();
        assertThat(loadedKey.getAlgorithm()).isEqualTo("X25519");
        
        // Verify that extracting from the loaded key gives the same bytes
        byte[] reExtracted = keyPairService.extractPublicKeyBytes(loadedKey);
        assertThat(reExtracted).isEqualTo(rawBytes);
    }

    @Test
    void shouldProduceDifferentKeyPairsOnEachCall() {
        X25519KeyPair kp1 = keyPairService.generateKeyPair();
        X25519KeyPair kp2 = keyPairService.generateKeyPair();
        
        assertThat(kp1.publicKeyBytes()).isNotEqualTo(kp2.publicKeyBytes());
    }

    @Test
    void shouldFailForIncorrectKeyLength() {
        byte[] shortKey = new byte[31];
        
        assertThatThrownBy(() -> keyPairService.loadPublicKey(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly 32 bytes");
    }

    @Test
    void shouldFailForInvalidKeyPoints() {
        // An all-zero key is often invalid for X25519
        byte[] invalidKey = new byte[32];
        
        assertThatThrownBy(() -> keyPairService.loadPublicKey(invalidKey))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\MasterKeyDerivationTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class MasterKeyDerivationTest {

    private final MasterKeyDerivation derivation = new MasterKeyDerivation();

    @Test
    void shouldProduceDeterministicOutput() {
        char[] password = "securePassword123".toCharArray();
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        byte[] key1 = derivation.derive(password.clone(), salt);
        byte[] key2 = derivation.derive(password.clone(), salt);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1).hasSize(32);
    }

    @Test
    void shouldProduceDifferentOutputForDifferentPasswords() {
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        byte[] key1 = derivation.derive("password1".toCharArray(), salt);
        byte[] key2 = derivation.derive("password2".toCharArray(), salt);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldProduceDifferentOutputForDifferentSalts() {
        char[] password = "securePassword".toCharArray();
        byte[] salt1 = new byte[16];
        byte[] salt2 = new byte[16];
        salt2[0] = 1;

        byte[] key1 = derivation.derive(password.clone(), salt1);
        byte[] key2 = derivation.derive(password.clone(), salt2);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldZeroPasswordAfterDerivation() {
        char[] password = "sensitivePassword".toCharArray();
        byte[] salt = new byte[16];

        derivation.derive(password, salt);

        char[] expectedZeroes = new char[password.length];
        Arrays.fill(expectedZeroes, (char) 0);

        assertThat(password).containsOnly((char) 0);
        assertThat(password).isEqualTo(expectedZeroes);
    }

    @Test
    void shouldThrowExceptionForInvalidSaltLength() {
        char[] password = "password".toCharArray();
        byte[] shortSalt = new byte[15];

        assertThatThrownBy(() -> derivation.derive(password, shortSalt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Salt must be exactly 16 bytes");
    }

    /**
     * Mandatory Adversarial Test: m=4096 parameters must fail.
     * Since parameters are hardcoded as constants in the implementation to prevent 
     * lowering, this test verifies that the hardcoded values meet the floor.
     */
    @Test
    void shouldVerifyArgon2idParametersMeetHardFloor() {
        // This is a meta-test to ensure the implementation hasn't lowered the floor.
        // In a real scenario, if parameters were passed in, we would test rejection.
        // Here we verify the internal constants are compliant.
        try {
            var fieldM = MasterKeyDerivation.class.getDeclaredField("MEMORY_COST");
            fieldM.setAccessible(true);
            int m = (int) fieldM.get(null);
            assertThat(m).isGreaterThanOrEqualTo(65536);

            var fieldT = MasterKeyDerivation.class.getDeclaredField("TIME_COST");
            fieldT.setAccessible(true);
            int t = (int) fieldT.get(null);
            assertThat(t).isGreaterThanOrEqualTo(3);

            var fieldP = MasterKeyDerivation.class.getDeclaredField("PARALLELISM");
            fieldP.setAccessible(true);
            int p = (int) fieldP.get(null);
            assertThat(p).isGreaterThanOrEqualTo(4);
        } catch (Exception e) {
            fail("Could not verify Argon2id parameters: " + e.getMessage());
        }
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\PaddingUtilTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class PaddingUtilTest {

    private final PaddingUtil util = new PaddingUtil();

    @Test
    void shouldPadEmptyInputToBlockSize() {
        byte[] padded = util.pad(new byte[0]);
        assertThat(padded).hasSize(256);
        assertThat(padded).containsOnly((byte) 0); // 256 bytes of 256 (0)
    }

    @Test
    void shouldPadOneByteToBlockSize() {
        byte[] padded = util.pad(new byte[1]);
        assertThat(padded).hasSize(256);
        assertThat(padded[255]).isEqualTo((byte) 255);
    }

    @Test
    void shouldPad255BytesToBlockSize() {
        byte[] padded = util.pad(new byte[255]);
        assertThat(padded).hasSize(256);
        assertThat(padded[255]).isEqualTo((byte) 1);
    }

    @Test
    void shouldAddFullBlockIfAlreadyAligned() {
        byte[] input = new byte[256];
        byte[] padded = util.pad(input);
        assertThat(padded).hasSize(512);
        // Last 256 bytes should all be (byte)0
        for (int i = 256; i < 512; i++) {
            assertThat(padded[i]).isEqualTo((byte) 0);
        }
    }

    @Test
    void shouldPad257BytesToTwoBlocks() {
        byte[] padded = util.pad(new byte[257]);
        assertThat(padded).hasSize(512);
        assertThat(padded[511] & 0xFF).isEqualTo(512 - 257); // 255
    }

    @Test
    void roundtripShouldWorkForVariousLengths() {
        for (int i = 0; i <= 512; i++) {
            byte[] input = new byte[i];
            Arrays.fill(input, (byte) 0xAA);
            byte[] padded = util.pad(input);
            assertThat(padded.length % 256).isZero();
            byte[] unpadded = util.unpad(padded);
            assertThat(unpadded).isEqualTo(input);
        }
    }

    @Test
    void unpadShouldFailOnIncorrectAlignment() {
        assertThatThrownBy(() -> util.unpad(new byte[255]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("alignment");
    }

    @Test
    void unpadShouldFailOnInvalidPaddingPattern() {
        byte[] padded = util.pad(new byte[10]);
        padded[padded.length - 2] ^= 0x01; // Tamper with one padding byte
        
        assertThatThrownBy(() -> util.unpad(padded))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pattern");
    }

    @Test
    void unpadShouldFailOnZeroLastByteIfPatternIsInvalid() {
        // This is a tricky one. If last byte is 0 (256), then all 256 bytes must be 0.
        // If we provide a 256-byte array where the last byte is 0 but others are not, it should fail.
        byte[] badPad = new byte[256];
        badPad[0] = 1; // Not 0
        badPad[255] = 0;
        
        assertThatThrownBy(() -> util.unpad(badPad))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void unpadShouldAcceptAllZerosAs256BytePad() {
        byte[] allZeros = new byte[256];
        byte[] unpadded = util.unpad(allZeros);
        assertThat(unpadded).isEmpty();
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\SubKeyDerivationTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class SubKeyDerivationTest {

    private final SubKeyDerivation derivation = new SubKeyDerivation();

    @Test
    void shouldProduceDifferentKeysForDifferentContexts() {
        byte[] masterKey = new byte[32];
        for (int i = 0; i < 32; i++) masterKey[i] = (byte) i;

        byte[] vaultKey = derivation.derive(masterKey, "vault-key");
        byte[] authKey = derivation.derive(masterKey, "auth-signing-key");

        assertThat(vaultKey).isNotEqualTo(authKey);
        assertThat(vaultKey).hasSize(32);
        assertThat(authKey).hasSize(32);
    }

    @Test
    void shouldBeDeterministic() {
        byte[] masterKey = new byte[32];
        String info = "vault-key";

        byte[] key1 = derivation.derive(masterKey, info);
        byte[] key2 = derivation.derive(masterKey, info);

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldProduceDifferentKeysForDifferentMasterKeys() {
        byte[] masterKey1 = new byte[32];
        byte[] masterKey2 = new byte[32];
        masterKey2[0] = 1;
        String info = "vault-key";

        byte[] key1 = derivation.derive(masterKey1, info);
        byte[] key2 = derivation.derive(masterKey2, info);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldThrowExceptionForUnknownInfoStrings() {
        byte[] masterKey = new byte[32];

        // Underscore instead of hyphen
        assertThatThrownBy(() -> derivation.derive(masterKey, "vault_key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown HKDF context");

        // Empty string
        assertThatThrownBy(() -> derivation.derive(masterKey, ""))
                .isInstanceOf(IllegalArgumentException.class);

        // Null info
        assertThatThrownBy(() -> derivation.derive(masterKey, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidMasterKeyLength() {
        assertThatThrownBy(() -> derivation.derive(new byte[31], "vault-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Master key must be exactly 32 bytes");
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\TeamKeyEnvelopeTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import java.security.PublicKey;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class TeamKeyEnvelopeTest {

    private final TeamKeyEnvelope envelopeService = new TeamKeyEnvelope();
    private final KeyPairService keyPairService = new KeyPairService();

    @Test
    void shouldWrapAndUnwrapCorrectly() throws Exception {
        // Setup keys
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        
        PublicKey senderPub = keyPairService.loadPublicKey(sender.publicKeyBytes());
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] originalTeamKey = new byte[32];
        Arrays.fill(originalTeamKey, (byte) 0xA5);
        byte[] teamKeyToWrap = Arrays.copyOf(originalTeamKey, originalTeamKey.length);

        // Wrap
        byte[] envelope = envelopeService.wrap(teamKeyToWrap, recipientPub, sender.privateKey());
        
        // teamKeyToWrap should be zeroed
        assertThat(teamKeyToWrap).containsOnly((byte) 0);

        // Unwrap
        byte[] unwrapped = envelopeService.unwrap(envelope, senderPub, recipient.privateKey());
        
        assertThat(unwrapped).isEqualTo(originalTeamKey);
    }

    @Test
    void shouldThrowExceptionOnWrongRecipientKey() throws Exception {
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        X25519KeyPair intruder = keyPairService.generateKeyPair();
        
        PublicKey senderPub = keyPairService.loadPublicKey(sender.publicKeyBytes());
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        Arrays.fill(teamKey, (byte) 0xCC);

        byte[] envelope = envelopeService.wrap(teamKey, recipientPub, sender.privateKey());

        assertThatThrownBy(() -> envelopeService.unwrap(envelope, senderPub, intruder.privateKey()))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnTamperedEnvelope() throws Exception {
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        
        PublicKey senderPub = keyPairService.loadPublicKey(sender.publicKeyBytes());
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        byte[] envelope = envelopeService.wrap(teamKey, recipientPub, sender.privateKey());

        envelope[envelope.length - 1] ^= 0x01; // Tamper with tag

        assertThatThrownBy(() -> envelopeService.unwrap(envelope, senderPub, recipient.privateKey()))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldProduceDifferentEnvelopesForSameInput() throws Exception {
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        
        byte[] env1 = envelopeService.wrap(Arrays.copyOf(teamKey, 32), recipientPub, sender.privateKey());
        byte[] env2 = envelopeService.wrap(Arrays.copyOf(teamKey, 32), recipientPub, sender.privateKey());

        assertThat(env1).isNotEqualTo(env2);
    }

    @Test
    void shouldBeCommutative() throws Exception {
        // A wraps for B. B unwraps using A's public key.
        X25519KeyPair userA = keyPairService.generateKeyPair();
        X25519KeyPair userB = keyPairService.generateKeyPair();
        
        PublicKey pubA = keyPairService.loadPublicKey(userA.publicKeyBytes());
        PublicKey pubB = keyPairService.loadPublicKey(userB.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        Arrays.fill(teamKey, (byte) 0x77);

        // A -> B
        byte[] env = envelopeService.wrap(Arrays.copyOf(teamKey, 32), pubB, userA.privateKey());
        
        // B unwraps
        byte[] unwrapped = envelopeService.unwrap(env, pubA, userB.privateKey());
        
        assertThat(unwrapped).isEqualTo(teamKey);
    }
}

```
### Java-Project\crypto_module\src\test\java\crypto\internal\VaultServiceTest.java
```java
package crypto.internal;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class VaultServiceTest {

    private final VaultService vaultService = new VaultService();
    private final byte[] vaultKey = new byte[32]; // All zeros for test, but correct length

    public VaultServiceTest() {
        Arrays.fill(vaultKey, (byte) 0x42);
    }

    @Test
    void shouldSealAndUnsealCorrectly() throws AEADBadTagException {
        byte[] originalData = "secret-private-key-material".getBytes();
        byte[] dataToSeal = Arrays.copyOf(originalData, originalData.length);

        byte[] blob = vaultService.seal(dataToSeal, vaultKey);
        
        // original dataToSeal should be zeroed
        assertThat(dataToSeal).containsOnly((byte) 0);

        byte[] unsealed = vaultService.unseal(blob, vaultKey);
        assertThat(unsealed).isEqualTo(originalData);
    }

    @Test
    void shouldHaveCorrectBlobLength() {
        byte[] originalData = new byte[100];
        byte[] blob = vaultService.seal(originalData, vaultKey);
        
        // Length = 12 (nonce) + 100 (plaintext) + 16 (GCM tag) = 128
        assertThat(blob).hasSize(12 + 100 + 16);
    }

    @Test
    void shouldThrowExceptionOnWrongKey() {
        byte[] originalData = "secret".getBytes();
        byte[] blob = vaultService.seal(originalData, vaultKey);

        byte[] wrongKey = new byte[32];
        Arrays.fill(wrongKey, (byte) 0x99);

        assertThatThrownBy(() -> vaultService.unseal(blob, wrongKey))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnTamperedCiphertext() {
        byte[] originalData = "secret".getBytes();
        byte[] blob = vaultService.seal(originalData, vaultKey);

        // Tamper with the ciphertext (starts at index 12)
        blob[15] ^= 0x01;

        assertThatThrownBy(() -> vaultService.unseal(blob, vaultKey))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnTamperedNonce() {
        byte[] originalData = "secret".getBytes();
        byte[] blob = vaultService.seal(originalData, vaultKey);

        // Tamper with the nonce (indices 0-11)
        blob[5] ^= 0x01;

        assertThatThrownBy(() -> vaultService.unseal(blob, vaultKey))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldProduceDifferentBlobsForSameInput() {
        byte[] originalData = "secret".getBytes();
        
        byte[] blob1 = vaultService.seal(Arrays.copyOf(originalData, originalData.length), vaultKey);
        byte[] blob2 = vaultService.seal(Arrays.copyOf(originalData, originalData.length), vaultKey);

        assertThat(blob1).isNotEqualTo(blob2);
    }

    @Test
    void shouldThrowExceptionOnInvalidVaultKeyLength() {
        byte[] invalidKey = new byte[31];
        assertThatThrownBy(() -> vaultService.seal(new byte[10], invalidKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vault key must be exactly 32 bytes");
    }
}

```
### Java-Project\ctm_module\model\Project.java
```java
import java.util.*;
import model.Task;

public class Project {

    private String projectId;
    private String projectName;
    private List<Task> tasks;

    public Project(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getProjectName() {
        return projectName;
    }
}
```
### Java-Project\ctm_module\model\ProjectItem.java
```java
package model;

public abstract class ProjectItem {

    protected String id;
    protected String title;
    protected String status; // DEADLINE, IN_PROGRESS, DONE

    public ProjectItem(String id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Polymorphism entry point
    public abstract String getDetails();
}
```
### Java-Project\ctm_module\model\Task.java
```java
package model;

import org.bson.types.ObjectId;

public class Task extends ProjectItem {

    private String description;
    private String deadline;
    private boolean completed;
    private String priority;

    private String userId;
    private String teamId;

    public Task(String id, String title, String description,
                String deadline, boolean completed, String status, String priority, String userId, String teamId) {

        super(id, title, status);
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
        this.priority = priority;
        this.userId = userId;
        this.teamId = teamId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public boolean isCompleted() { return completed; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    @Override
    public String getDetails() {
        return title + " - " + description + " (Due: " + deadline + ")";
    }
}
```
### Java-Project\ctm_module\model\Team.java
```java
package model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String id;
    private String name;
    private String ownerId;
    private List<String> members;

    public Team(String id, String name, String ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.members = new ArrayList<>();
        this.members.add(ownerId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public List<String> getMembers() { return members; }
    public void addMember(String email) { if (!members.contains(email)) members.add(email); }
}

```
### Java-Project\ctm_module\model\User.java
```java
package model;

public class User {
    private String email;
    private String name;

    public User() {}

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

```
### Java-Project\ctm_module\model\WorkflowRule.java
```java
package model;

public class WorkflowRule {
    private String id;
    private String conditionType; // e.g., "DEADLINE_PASSED", "PRIORITY_EQUALS"
    private String conditionValue;
    private String actionType; // e.g., "SET_PRIORITY", "SET_STATUS"
    private String actionValue;

    public WorkflowRule(String id, String conditionType, String conditionValue, String actionType, String actionValue) {
        this.id = id;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.actionType = actionType;
        this.actionValue = actionValue;
    }

    public String getConditionType() { return conditionType; }
    public String getConditionValue() { return conditionValue; }
    public String getActionType() { return actionType; }
    public String getActionValue() { return actionValue; }
}

```
### Java-Project\ctm_module\service\MongoService.java
```java
package service;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.combine;
import com.mongodb.client.*;
import model.Task;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class MongoService {

    private final MongoCollection<Document> collection;
    private final MongoDatabase db;

    public MongoService() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        db = client.getDatabase("taskdb");
        collection = db.getCollection("tasks");
    }

    public String addTask(Task task) {
        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("deadline", task.getDeadline())
                .append("completed", task.isCompleted())
                .append("status", task.getStatus())
                .append("priority", task.getPriority())
                .append("userId", task.getUserId())
                .append("teamId", task.getTeamId());

        collection.insertOne(doc);
        return doc.getObjectId("_id").toString();
    }

    public List<Task> getTasks(String userId, String teamId) {
        List<Task> list = new ArrayList<>();
        List<Document> filters = new ArrayList<>();
        if (userId != null) filters.add(new Document("userId", userId));
        if (teamId != null) filters.add(new Document("teamId", teamId));
        
        Document query = filters.isEmpty() ? new Document() : new Document("$or", filters);

        for (Document doc : collection.find(query)) {
            ObjectId id = doc.getObjectId("_id");
            String title = doc.getString("title");
            String desc = doc.getString("description");
            String deadline = doc.getString("deadline");
            Boolean completed = doc.getBoolean("completed");
            String status = doc.getString("status");
            String priority = doc.getString("priority");
            String uId = doc.getString("userId");
            String tId = doc.getString("teamId");

            if (status == null) status = "DEADLINE";
            if (priority == null) priority = "Low";
            if (completed == null) completed = false;

            list.add(new Task(id.toString(), title, desc, deadline, completed, status, priority, uId, tId));
        }

        return list;
    }

    public void deleteTask(String id) {
        if (id == null) return;
        collection.deleteOne(eq("_id", new ObjectId(id)));
    }

    public void updateStatus(String id, String status) {
        if (id == null) return;
        collection.updateOne(
            eq("_id", new ObjectId(id)),
            set("status", status)
        );
    }

    public void updateCompletion(String id, boolean completed) {
        if (id == null) return;
        collection.updateOne(
            eq("_id", new ObjectId(id)),
            set("completed", completed)
        );
    }

    public void updateTask(Task t) {
        if (t.getId() == null) return;
        collection.updateOne(
            eq("_id", new ObjectId(t.getId())),
            combine(
                set("title", t.getTitle()),
                set("description", t.getDescription()),
                set("deadline", t.getDeadline()),
                set("status", t.getStatus()),
                set("completed", t.isCompleted()),
                set("priority", t.getPriority()),
                set("userId", t.getUserId()),
                set("teamId", t.getTeamId())
            )
        );
    }

    public void createTeam(model.Team team) {
        Document doc = new Document("name", team.getName())
                .append("ownerId", team.getOwnerId())
                .append("members", team.getMembers());
        
        MongoCollection<Document> teams = db.getCollection("teams");
        teams.insertOne(doc);
        team.setId(doc.getObjectId("_id").toString());
    }

    public void inviteToTeam(String teamId, String email) {
        MongoCollection<Document> teams = db.getCollection("teams");
        teams.updateOne(
            eq("_id", new ObjectId(teamId)),
            com.mongodb.client.model.Updates.addToSet("members", email)
        );
    }

    public List<model.Team> getTeamsForUser(String email) {
        List<model.Team> list = new ArrayList<>();
        MongoCollection<Document> teams = db.getCollection("teams");
        
        for (Document doc : teams.find(com.mongodb.client.model.Filters.in("members", email))) {
            model.Team t = new model.Team(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("ownerId")
            );
            List<String> m = (List<String>) doc.get("members");
            if (m != null) {
                for (String member : m) t.addMember(member);
            }
            list.add(t);
        }
        return list;
    }
}
```
### Java-Project\ctm_module\service\TaskService.java
```java
package service;
import java.util.List;
import model.Task;

public class TaskService {

    private MongoService mongo = new MongoService();
    private WorkflowService workflow = new WorkflowService();

    public List<Task> getAllTasks(String userId, String teamId) {
        return mongo.getTasks(userId, teamId);
    }

    public void addTask(Task t) {
        workflow.applyRules(t);
        String id = mongo.addTask(t);
        t.setId(id);
    }

    public void updateTask(Task t) {
        workflow.applyRules(t);
        mongo.updateTask(t);
    }

    public void deleteTask(String id) {
        mongo.deleteTask(id);
    }

    public void markInProgress(Task t) {
        t.setStatus("IN_PROGRESS");
        mongo.updateStatus(t.getId(), "IN_PROGRESS");
    }

    public void markDone(Task t) {
        updateStatus(t, "DONE");
    }

    public void updateStatus(Task t, String status) {
        t.setStatus(status);
        if (status.equals("DONE")) {
            t.setCompleted(true);
            mongo.updateCompletion(t.getId(), true);
        } else {
            t.setCompleted(false);
            mongo.updateCompletion(t.getId(), false);
        }
        mongo.updateStatus(t.getId(), status);
    }
}
```
### Java-Project\ctm_module\service\TestMongo.java
```java
package service;

import com.mongodb.client.*;
import org.bson.Document;

public class TestMongo {

    public static void main(String[] args) {

        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

        // Access database
        MongoDatabase database = mongoClient.getDatabase("task_manager");

        // Access collection
        MongoCollection<Document> collection = database.getCollection("tasks");

        System.out.println("Connected to MongoDB!");

        // ===== INSERT TEST =====
        Document task = new Document("title", "Test Task")
                .append("description", "MongoDB connection test")
                .append("deadline", "2026-04-25")
                .append("completed", false);

        collection.insertOne(task);
        System.out.println("Inserted test task!");

        // ===== FETCH TEST =====
        FindIterable<Document> tasks = collection.find();

        System.out.println("\nTasks in DB:");
        for (Document doc : tasks) {
            System.out.println(doc.toJson());
        }

        // Close connection
        mongoClient.close();
    }
}
```
### Java-Project\ctm_module\service\WorkflowService.java
```java
package service;

import model.Task;
import model.WorkflowRule;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class WorkflowService {
    private List<WorkflowRule> rules = new ArrayList<>();

    public WorkflowService() {
        // Sample rule: If deadline is today, set priority to High
        rules.add(new WorkflowRule("1", "DEADLINE_IS_TODAY", "", "SET_PRIORITY", "High"));
    }

    public void applyRules(Task task) {
        for (WorkflowRule rule : rules) {
            if (evaluateCondition(task, rule)) {
                executeAction(task, rule);
            }
        }
    }

    private boolean evaluateCondition(Task task, WorkflowRule rule) {
        switch (rule.getConditionType()) {
            case "DEADLINE_IS_TODAY":
                return task.getDeadline().equals(LocalDate.now().toString());
            case "PRIORITY_EQUALS":
                return task.getPriority().equals(rule.getConditionValue());
            default:
                return false;
        }
    }

    private void executeAction(Task task, WorkflowRule rule) {
        switch (rule.getActionType()) {
            case "SET_PRIORITY":
                task.setPriority(rule.getActionValue());
                break;
            case "SET_STATUS":
                task.setStatus(rule.getActionValue());
                break;
        }
    }
}

```
### Java-Project\ctm_module\ui\DashboardUI.java
```java
package ui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import java.io.File;
import java.time.LocalDate;
import model.Task;
import model.User;
import service.TaskService;
import ui.views.DashboardView;
import ui.views.MyTasksView;
import ui.views.SidebarView;
import utils.UserSession;
import utils.ValidationUtils;

public class DashboardUI extends Application {

    private StackPane mainStack;
    private BorderPane mainRoot;
    private TaskService taskService = new TaskService();
    private ObservableList<Task> taskList;
    
    private DashboardView dashboardView;
    private MyTasksView myTasksView;
    private ui.views.CalendarView calendarView;

    @Override
    public void start(Stage stage) {
        mainStack = new StackPane();
        Scene scene = new Scene(mainStack, 1200, 800);
        try {
            File cssFile = new File("resources/light_style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        stage.setScene(scene);
        stage.setTitle("Secure Task Manager");
        stage.centerOnScreen();
        stage.show();

        showLoginScreen();
    }

    private void showLoginScreen() {
        mainStack.setStyle("-fx-background-color: #f5f6fa;");
        
        VBox loginBox = new VBox(25);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxSize(400, 480);
        loginBox.setStyle("-fx-background-color: white; -fx-padding: 50; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");

        Label title = new Label("SECURE TASKER");
        title.setStyle("-fx-text-fill: #4f46e5; -fx-font-size: 32px; -fx-font-weight: bold;");

        VBox fields = new VBox(10);
        Label eLbl = new Label("EMAIL ADDRESS");
        eLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        Label pLbl = new Label("PASSWORD");
        pLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #6b7280;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #1f2937; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");
        fields.getChildren().addAll(eLbl, emailField, new Region(), pLbl, passField);

        Button loginBtn = new Button("SIGN IN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setPrefHeight(50);

        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            if (!ValidationUtils.isValidEmail(email)) {
                showError("Invalid Email! Must be a valid @gmail.com address.");
                return;
            }
            User user = new User();
            user.setEmail(email);
            UserSession.login(user);
            initializeDashboard();
        });

        loginBox.getChildren().addAll(title, new Label("Manage your tasks efficiently"), fields, loginBtn);
        mainStack.getChildren().add(loginBox);
    }

    private void initializeDashboard() {
        mainStack.getChildren().clear();
        String userEmail = UserSession.getCurrentUserEmail();
        
        taskList = FXCollections.observableArrayList(taskService.getAllTasks(userEmail, null));
        
        dashboardView = new DashboardView(taskList);
        myTasksView = new MyTasksView(taskService, taskList, this::handleEditAction, t -> {
            if (showConfirmation("Delete Task", "Are you sure you want to delete this task?")) {
                taskService.deleteTask(t.getId());
                taskList.remove(t);
                myTasksView.refresh();
            }
        });
        calendarView = new ui.views.CalendarView(taskList);
        
        SidebarView sidebar = new SidebarView(viewKey -> {
            switch(viewKey) {
                case "DASHBOARD": mainRoot.setCenter(dashboardView); break;
                case "KANBAN": mainRoot.setCenter(myTasksView); myTasksView.refresh(); break;
                case "CALENDAR": mainRoot.setCenter(calendarView); calendarView.refresh(); break;
                case "LOGOUT": UserSession.logout(); showLoginScreen(); break;
                default: showError("Module coming soon!");
            }
        });

        mainRoot = new BorderPane();
        mainRoot.setLeft(sidebar);
        mainRoot.setCenter(dashboardView);

        mainStack.getChildren().add(mainRoot);
    }

    private void handleEditAction(Task t) {
        if (t == null) showAddTaskDialog();
        else showEditDialog(t);
    }

    private void showAddTaskDialog() {
        TextField tIn = new TextField(); tIn.setPromptText("Title");
        TextField dIn = new TextField(); dIn.setPromptText("Description");
        DatePicker dateIn = new DatePicker(LocalDate.now());
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue("Low");
        pIn.setMaxWidth(Double.MAX_VALUE);
        
        Button save = new Button("Add Task");
        save.setStyle("-fx-background-color: #238636; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);
        
        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) { showError("Title cannot be empty!"); return; }
            if (!ValidationUtils.isFutureOrPresentDate(date)) { showError("Date cannot be in the past!"); return; }

            Task newTask = new Task(null, title, dIn.getText(), date, false, "DEADLINE", pIn.getValue(), UserSession.getCurrentUserEmail(), null);
            taskService.addTask(newTask);
            taskList.add(newTask);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("NEW TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle("-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
        layout.setMaxSize(400, 450);
        showOverlay(layout);
    }

    private void showEditDialog(Task t) {
        TextField tIn = new TextField(t.getTitle());
        TextField dIn = new TextField(t.getDescription());
        DatePicker dateIn = new DatePicker(LocalDate.parse(t.getDeadline()));
        ComboBox<String> pIn = new ComboBox<>(FXCollections.observableArrayList("High", "Medium", "Low"));
        pIn.setValue(t.getPriority());
        pIn.setMaxWidth(Double.MAX_VALUE);

        Button save = new Button("Save Changes");
        save.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8;");
        save.setMaxWidth(Double.MAX_VALUE);
        
        save.setOnAction(e -> {
            String title = tIn.getText();
            String date = dateIn.getValue().toString();
            if (!ValidationUtils.isValidTaskTitle(title)) { showError("Title cannot be empty!"); return; }
            if (!ValidationUtils.isFutureOrPresentDate(date)) { showError("Date cannot be in the past!"); return; }

            t.setTitle(title);
            t.setDescription(dIn.getText());
            t.setDeadline(date);
            t.setPriority(pIn.getValue());
            taskService.updateTask(t);
            myTasksView.refresh();
            hideOverlay();
        });

        VBox layout = new VBox(15, new Label("EDIT TASK"), tIn, dIn, dateIn, pIn, save);
        layout.setStyle("-fx-padding: 40; -fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 30, 0, 0, 10);");
        layout.setMaxSize(400, 450);
        showOverlay(layout);
    }

    private void showOverlay(Node content) {
        Region glassPane = new Region();
        glassPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
        glassPane.setOnMouseClicked(e -> hideOverlay());

        VBox container = new VBox(content);
        container.setAlignment(Pos.CENTER);
        container.setPickOnBounds(false);
        content.setEffect(new DropShadow(30, Color.BLACK));

        mainStack.getChildren().addAll(glassPane, container);
    }

    private void hideOverlay() {
        if (mainStack.getChildren().size() > 1) {
            mainStack.getChildren().remove(mainStack.getChildren().size() - 1);
            mainStack.getChildren().remove(mainStack.getChildren().size() - 1);
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void main(String[] args) { launch(); }
}
```
### Java-Project\ctm_module\ui\components\StatCard.java
```java
package ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class StatCard extends VBox {
    public StatCard(String label, String value, String color) {
        setSpacing(8);
        setPadding(new Insets(20));
        getStyleClass().add("stat-card");
        
        // Soft icon placeholder
        Region icon = new Region();
        icon.setPrefSize(40, 40);
        icon.setStyle("-fx-background-color: " + color + "; -fx-opacity: 0.1; -fx-background-radius: 10;");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add("stat-value");
        
        Label tagLbl = new Label(label.toUpperCase());
        tagLbl.getStyleClass().add("stat-label");

        getChildren().addAll(icon, valLbl, tagLbl);
        setPrefWidth(220);
    }
}

```
### Java-Project\ctm_module\ui\components\TaskCard.java
```java
package ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.*;
import model.Task;
import service.TaskService;
import java.util.function.Consumer;

public class TaskCard extends VBox {
    private Task task;
    private TaskService taskService;
    private Runnable onAction;
    private Consumer<Task> onEdit;
    private Consumer<Task> onDelete;

    public TaskCard(Task t, TaskService service, Runnable refresh, Consumer<Task> editAction, Consumer<Task> deleteAction) {
        this.task = t;
        this.taskService = service;
        this.onAction = refresh;
        this.onEdit = editAction;
        this.onDelete = deleteAction;

        setSpacing(12);
        setPadding(new javafx.geometry.Insets(20));
        getStyleClass().add("task-card");

        String priority = t.getPriority();
        if (priority == null) priority = "Low";
        
        Label pTag = new Label(priority.toUpperCase());
        pTag.getStyleClass().addAll("priority-tag", "priority-" + priority.toLowerCase());
        
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("task-title-bold");
        
        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("metadata-text");
        desc.setWrapText(true);
        
        Label deadline = new Label("📅 " + t.getDeadline());
        deadline.getStyleClass().add("metadata-text");

        HBox metaRow = new HBox(deadline);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        styleBtn(editBtn, "#34495e", "#2c3e50");
        styleBtn(deleteBtn, "#e74c3c", "#c0392b");
        editBtn.setPrefWidth(70);
        deleteBtn.setPrefWidth(70);

        HBox actions = new HBox(10, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        String status = t.getStatus();
        if (status == null) status = "DEADLINE";

        if (status.equals("DEADLINE")) {
            Button startBtn = new Button("Start");
            styleBtn(startBtn, "#3498db", "#2980b9");
            startBtn.setPrefWidth(80);
            startBtn.setOnAction(e -> { taskService.updateStatus(t, "IN_PROGRESS"); onAction.run(); });
            actions.getChildren().add(0, startBtn);
        } else if (status.equals("IN_PROGRESS")) {
            Button doneBtn = new Button("Done");
            styleBtn(doneBtn, "#2ecc71", "#27ae60");
            doneBtn.setPrefWidth(80);
            doneBtn.setOnAction(e -> { taskService.updateStatus(t, "DONE"); onAction.run(); });
            actions.getChildren().add(0, doneBtn);
        }

        editBtn.setOnAction(e -> onEdit.accept(t));
        deleteBtn.setOnAction(e -> {
            onDelete.accept(t);
        });

        getChildren().addAll(pTag, title, desc, metaRow, actions);

        // Status coloring
        String statusColor = "#e5e7eb";
        if (status.equals("DEADLINE")) statusColor = "#8b5cf6";
        else if (status.equals("IN_PROGRESS")) statusColor = "#f59e0b";
        else if (status.equals("DONE")) statusColor = "#10b981";
        
        setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                 "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4); " +
                 "-fx-border-color: transparent transparent transparent " + statusColor + "; -fx-border-width: 0 0 0 4;");

        // Drag support
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(t.getId());
            db.setContent(content);
            event.consume();
        });
    }

    private void styleBtn(Button btn, String color, String hoverColor) {
        String base = "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 15; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(base + "-fx-background-color: " + hoverColor + ";"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
}

```
### Java-Project\ctm_module\ui\views\CalendarView.java
```java
package ui.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import model.Task;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class CalendarView extends VBox {
    private YearMonth currentYearMonth;
    private List<Task> tasks;
    private GridPane calendarGrid;
    private Label monthLabel;

    public CalendarView(List<Task> tasks) {
        this.tasks = tasks;
        this.currentYearMonth = YearMonth.now();
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #0d1117;");

        HBox nav = new HBox(20);
        nav.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("CALENDAR");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button prev = new Button("←");
        Button next = new Button("→");
        styleNavBtn(prev);
        styleNavBtn(next);
        
        monthLabel = new Label();
        monthLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-min-width: 150px;");
        monthLabel.setAlignment(Pos.CENTER);

        prev.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); refresh(); });
        next.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); refresh(); });

        nav.getChildren().addAll(title, spacer, prev, monthLabel, next);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(calendarGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(nav, scroll);
        refresh();
    }

    private void styleNavBtn(Button btn) {
        btn.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 15; -fx-cursor: hand;");
    }

    public void refresh() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentYearMonth.getYear());

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        for (int i = 0; i < 7; i++) {
            Label dayLbl = new Label(days[i]);
            dayLbl.setStyle("-fx-text-fill: #8b949e; -fx-font-weight: bold; -fx-padding: 10;");
            calendarGrid.add(dayLbl, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (int i = 0; i < daysInMonth; i++) {
            int row = (i + dayOfWeek) / 7 + 1;
            int col = (i + dayOfWeek) % 7;
            
            LocalDate date = firstOfMonth.plusDays(i);
            VBox dayBox = createDayBox(date);
            calendarGrid.add(dayBox, col, row);
        }
    }

    private VBox createDayBox(LocalDate date) {
        VBox box = new VBox(5);
        box.setMinSize(140, 100);
        box.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #30363d; -fx-border-width: 1;");
        
        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setStyle("-fx-text-fill: " + (date.equals(LocalDate.now()) ? "#8b5cf6" : "white") + "; -fx-font-weight: bold;");
        
        box.getChildren().add(dateLbl);

        for (Task t : tasks) {
            if (t.getDeadline().equals(date.toString())) {
                Label taskLbl = new Label("• " + t.getTitle());
                String color = "#3498db";
                if ("DONE".equals(t.getStatus())) color = "#2ecc71";
                else if (date.isBefore(LocalDate.now())) color = "#e74c3c";
                
                taskLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
                box.getChildren().add(taskLbl);
            }
        }
        
        return box;
    }
}

```
### Java-Project\ctm_module\ui\views\DashboardView.java
```java
package ui.views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.geometry.Side;
import model.Task;
import ui.components.StatCard;
import java.time.LocalDate;

public class DashboardView extends BorderPane {
    private ObservableList<Task> taskList;

    public DashboardView(ObservableList<Task> tasks) {
        this.taskList = tasks;
        getStyleClass().add("root");

        // --- TOP BAR ---
        setTop(createTopBar());

        // --- CENTER CONTENT (Scrollable) ---
        VBox centerContent = new VBox(30);
        centerContent.setPadding(new Insets(30));
        
        centerContent.getChildren().addAll(
            createWelcomeCard(),
            createStatsRow(),
            createChartsRow(),
            createWorkProgressSection()
        );

        ScrollPane scrollPane = new ScrollPane(centerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        setCenter(scrollPane);

        // --- RIGHT PANEL ---
        setRight(createRightPanel());
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20, 30, 20, 30));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Dashboard");
        title.getStyleClass().add("text-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search anything...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 10; -fx-padding: 10 15; -fx-border-color: transparent;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label("March 2021");
        dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        Button dateSelector = new Button("📅");
        dateSelector.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 8; -fx-padding: 8;");

        topBar.getChildren().addAll(title, searchField, spacer, dateLabel, dateSelector);
        return topBar;
    }

    private VBox createWelcomeCard() {
        VBox welcome = new VBox(15);
        welcome.getStyleClass().add("welcome-card");
        
        Label title = new Label("Your Task Management Area");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        long pending = getCount("DEADLINE") + getCount("IN_PROGRESS");
        Label sub = new Label("You have " + pending + " tasks remaining. Keep pushing forward!");
        sub.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");
        
        Button learnMore = new Button("View Tasks");
        learnMore.setStyle("-fx-background-color: white; -fx-text-fill: #4f46e5; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 25;");
        
        welcome.getChildren().addAll(title, sub, learnMore);
        return welcome;
    }

    private HBox createStatsRow() {
        HBox row = new HBox(20);
        row.getChildren().addAll(
            new StatCard("Total Tasks", String.valueOf(taskList.size()), "#4f46e5"),
            new StatCard("In Progress", String.valueOf(getCount("IN_PROGRESS")), "#f59e0b"),
            new StatCard("Pending", String.valueOf(getCount("DEADLINE")), "#8b5cf6"),
            new StatCard("Completed", String.valueOf(getCount("DONE")), "#10b981")
        );
        return row;
    }

    private HBox createChartsRow() {
        HBox row = new HBox(25);
        row.setPrefHeight(350);

        // Line Chart Container
        VBox lineChartBox = new VBox(15);
        lineChartBox.getStyleClass().add("chart-container");
        HBox.setHgrow(lineChartBox, Priority.ALWAYS);
        
        Label chartTitle = new Label("Work Progress");
        chartTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Mock data for trends (since we don't have historical data in Mongo yet)
        series.getData().add(new XYChart.Data<>("Mon", 2));
        series.getData().add(new XYChart.Data<>("Tue", 5));
        series.getData().add(new XYChart.Data<>("Wed", 3));
        series.getData().add(new XYChart.Data<>("Thu", 8));
        series.getData().add(new XYChart.Data<>("Fri", 10));
        lineChart.getData().add(series);
        
        lineChartBox.getChildren().addAll(chartTitle, lineChart);

        // Donut Chart Container
        VBox pieChartBox = new VBox(15);
        pieChartBox.getStyleClass().add("chart-container");
        pieChartBox.setPrefWidth(300);
        
        Label pieTitle = new Label("Task Distribution");
        pieTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        PieChart pieChart = new PieChart();
        pieChart.getData().add(new PieChart.Data("To Do", getCount("DEADLINE")));
        pieChart.getData().add(new PieChart.Data("In Progress", getCount("IN_PROGRESS")));
        pieChart.getData().add(new PieChart.Data("Done", getCount("DONE")));
        pieChart.setLabelsVisible(false);
        pieChart.setLegendSide(Side.BOTTOM);
        
        pieChartBox.getChildren().addAll(pieTitle, pieChart);

        row.getChildren().addAll(lineChartBox, pieChartBox);
        return row;
    }

    private VBox createWorkProgressSection() {
        VBox section = new VBox(20);
        Label title = new Label("Recent Tasks");
        title.getStyleClass().add("text-title");
        
        HBox row = new HBox(20);
        
        int count = 0;
        for (Task t : taskList) {
            if (count >= 3) break;
            double progress = "DONE".equals(t.getStatus()) ? 1.0 : ("IN_PROGRESS".equals(t.getStatus()) ? 0.5 : 0.0);
            row.getChildren().add(createProgressCard(t.getTitle(), progress, "Start", t.getDeadline()));
            count++;
        }
        
        if (count == 0) {
            row.getChildren().add(new Label("No tasks found. Add a new task to get started!"));
        }
        
        section.getChildren().addAll(title, row);
        return section;
    }

    private VBox createProgressCard(String name, double progress, String start, String end) {
        VBox card = new VBox(12);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(280);
        
        Label nLabel = new Label(name);
        nLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        ProgressBar bar = new ProgressBar(progress);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        bar.setStyle("-fx-accent: #4f46e5;");
        
        HBox dates = new HBox();
        Label sLabel = new Label(start);
        Label eLabel = new Label(end);
        sLabel.getStyleClass().add("text-subtitle");
        eLabel.getStyleClass().add("text-subtitle");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        dates.getChildren().addAll(sLabel, spacer, eLabel);
        
        card.getChildren().addAll(nLabel, bar, dates);
        return card;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(30);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e1e4e8; -fx-border-width: 0 0 0 1;");

        // Simple Calendar Placeholder
        VBox calendar = new VBox(15);
        Label calTitle = new Label("Calendar");
        calTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        for (int i = 1; i <= 31; i++) {
            Label day = new Label(String.valueOf(i));
            day.setAlignment(Pos.CENTER);
            day.setPrefSize(30, 30);
            day.setStyle("-fx-background-color: #f5f6fa; -fx-background-radius: 5; -fx-font-size: 11px;");
            grid.add(day, (i-1)%7, (i-1)/7);
        }
        calendar.getChildren().addAll(calTitle, grid);

        // Upcoming Schedule
        VBox upcoming = new VBox(15);
        Label upTitle = new Label("Upcoming Schedule");
        upTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        
        VBox list = new VBox(10);
        int scheduled = 0;
        for (Task t : taskList) {
            if (scheduled >= 5) break;
            if ("DONE".equals(t.getStatus())) continue;
            
            String color = "#4f46e5"; // Default
            if (t.getDeadline().equals(LocalDate.now().toString())) color = "#ef4444";
            else if ("IN_PROGRESS".equals(t.getStatus())) color = "#f59e0b";

            list.getChildren().add(createScheduleItem(t.getTitle(), t.getDeadline(), color));
            scheduled++;
        }
        
        if (scheduled == 0) {
            list.getChildren().add(new Label("No upcoming tasks."));
        }

        upcoming.getChildren().addAll(upTitle, list);
        panel.getChildren().addAll(calendar, upcoming);
        return panel;
    }

    private HBox createScheduleItem(String title, String time, String color) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12;");
        
        Region dot = new Region();
        dot.setPrefSize(8, 8);
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
        
        VBox text = new VBox(2);
        Label tLbl = new Label(title);
        tLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
        Label timLbl = new Label(time);
        timLbl.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        
        text.getChildren().addAll(tLbl, timLbl);
        item.getChildren().addAll(dot, text);
        return item;
    }

    private long getCount(String status) {
        return taskList.stream().filter(t -> status.equals(t.getStatus())).count();
    }
}

```
### Java-Project\ctm_module\ui\views\MyTasksView.java
```java
package ui.views;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import model.Task;
import service.TaskService;
import ui.components.TaskCard;
import java.util.function.Consumer;

public class MyTasksView extends VBox {
    private TaskService taskService;
    private ObservableList<Task> taskList;
    private String searchText = "";
    private VBox deadlineColumn, inProgressColumn, doneColumn;
    private Consumer<Task> onEdit;
    private Consumer<Task> onDelete;

    public MyTasksView(TaskService service, ObservableList<Task> tasks, Consumer<Task> editAction, Consumer<Task> deleteAction) {
        this.taskService = service;
        this.taskList = tasks;
        this.onEdit = editAction;
        this.onDelete = deleteAction;

        setSpacing(30);
        setPadding(new Insets(30));
        setStyle("-fx-background-color: #f5f6fa;");

        // --- TOP BAR ---
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 0, 10, 0));

        Label title = new Label("TRACKING");
        title.setStyle("-fx-text-fill: #1f2937; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: white; -fx-text-fill: #1f2937; -fx-background-radius: 12; -fx-padding: 12 15; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");
        searchField.textProperty().addListener((obs, o, n) -> { searchText = n.toLowerCase(); refresh(); });

        Button addTaskBtn = new Button("+ Add Task");
        addTaskBtn.getStyleClass().add("button-primary");
        addTaskBtn.setPrefHeight(45);
        addTaskBtn.setOnAction(e -> onEdit.accept(null)); // null means new task

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(title, searchField, spacer, addTaskBtn);

        // --- KANBAN ---
        HBox kanban = new HBox(25);
        VBox.setVgrow(kanban, Priority.ALWAYS);

        deadlineColumn = new VBox(15);
        inProgressColumn = new VBox(15);
        doneColumn = new VBox(15);

        kanban.getChildren().addAll(
            createColumn("TO DO", "#8b5cf6", deadlineColumn, "DEADLINE"),
            createColumn("IN PROGRESS", "#f59e0b", inProgressColumn, "IN_PROGRESS"),
            createColumn("DONE", "#10b981", doneColumn, "DONE")
        );

        getChildren().addAll(topBar, kanban);
        refresh();
    }

    private VBox createColumn(String title, String color, VBox content, String status) {
        VBox col = new VBox(15);
        HBox.setHgrow(col, Priority.ALWAYS);

        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.setStyle("-fx-padding: 15; -fx-background-color: #f0f3f6; -fx-background-radius: 20;");
        content.setMinWidth(300);

        // Drag over column
        scroll.setOnDragOver(event -> {
            if (event.getGestureSource() != content && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        scroll.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                String taskId = db.getString();
                Task found = taskList.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
                if (found != null && !found.getStatus().equals(status)) {
                    taskService.updateStatus(found, status);
                    refresh();
                }
                event.setDropCompleted(true);
            }
            event.consume();
        });

        col.getChildren().addAll(lbl, scroll);
        return col;
    }

    public void refresh() {
        deadlineColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        doneColumn.getChildren().clear();

        for (Task t : taskList) {
            if (!searchText.isEmpty() && !t.getTitle().toLowerCase().contains(searchText)) continue;

            TaskCard card = new TaskCard(t, taskService, this::refresh, onEdit, onDelete);
            
            if ("DEADLINE".equals(t.getStatus())) deadlineColumn.getChildren().add(card);
            else if ("IN_PROGRESS".equals(t.getStatus())) inProgressColumn.getChildren().add(card);
            else if ("DONE".equals(t.getStatus())) doneColumn.getChildren().add(card);
        }
    }
}

```
### Java-Project\ctm_module\ui\views\SidebarView.java
```java
package ui.views;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class SidebarView extends VBox {
    private Consumer<String> onNavigate;
    private Label activeNav;

    public SidebarView(Consumer<String> navigateAction) {
        this.onNavigate = navigateAction;
        setPrefWidth(240);
        getStyleClass().add("sidebar");
        setPadding(new Insets(0));

        // Profile Section
        VBox profile = new VBox(10);
        profile.setAlignment(Pos.CENTER);
        profile.setPadding(new Insets(40, 20, 40, 20));
        
        Region avatar = new Region();
        avatar.setPrefSize(60, 60);
        avatar.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 30;");
        
        String userEmail = utils.UserSession.getCurrentUserEmail();
        String displayUser = (userEmail != null && userEmail.contains("@")) ? userEmail.split("@")[0] : "User";
        
        Label name = new Label(displayUser.toUpperCase());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
        Label role = new Label(userEmail != null ? userEmail : "");
        role.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 10px;");
        
        profile.getChildren().addAll(avatar, name, role);

        Label dashboardNav = createNavItem("Dashboard", "DASHBOARD");
        Label trackingNav = createNavItem("Tracking", "KANBAN");
        Label projectsNav = createNavItem("Projects", "PROJECTS");
        Label historyNav = createNavItem("Work History", "HISTORY");
        
        Label toolsHeader = new Label("TOOLS");
        toolsHeader.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 30 20 10 20;");
        
        Label inboxNav = createNavItem("Inbox", "INBOX");
        Label settingsNav = createNavItem("Settings", "SETTINGS");
        Label logoutNav = createNavItem("Logout", "LOGOUT");

        VBox navBox = new VBox(5, dashboardNav, trackingNav, projectsNav, historyNav, toolsHeader, inboxNav, settingsNav, logoutNav);
        navBox.setPadding(new Insets(0, 15, 0, 15));

        Button addTaskBtn = new Button("+ Add New Task");
        addTaskBtn.getStyleClass().add("button-primary");
        addTaskBtn.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(addTaskBtn, new Insets(40, 20, 20, 20));

        getChildren().addAll(profile, navBox, new Region(), addTaskBtn);
        VBox.setVgrow(getChildren().get(2), Priority.ALWAYS);

        selectNav(dashboardNav);
    }

    private Label createNavItem(String text, String viewKey) {
        Label nav = new Label(text);
        nav.getStyleClass().add("sidebar-nav-item");
        nav.setMaxWidth(Double.MAX_VALUE);
        
        nav.setOnMouseClicked(e -> {
            selectNav(nav);
            onNavigate.accept(viewKey);
        });
        
        return nav;
    }

    private void selectNav(Label nav) {
        if (activeNav != null) {
            activeNav.getStyleClass().remove("sidebar-nav-active");
        }
        activeNav = nav;
        activeNav.getStyleClass().add("sidebar-nav-active");
    }
}

```
### Java-Project\ctm_module\utils\UserSession.java
```java
package utils;

import model.User;

public class UserSession {
    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "Guest";
    }
}

```
### Java-Project\ctm_module\utils\ValidationUtils.java
```java
package utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidTaskTitle(String title) {
        return title != null && !title.trim().isEmpty();
    }

    public static boolean isFutureOrPresentDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return !date.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }
}

```
### Java-Project\snm_module\src\main\java\com\project\snm\SnmBackendApplication.java
```java
package com.project.snm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SnmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnmBackendApplication.class, args);
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\config\WebSocketConfig.java
```java
package com.project.snm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-sync")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\controller\BlobController.java
```java
package com.project.snm.controller;

import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.model.mongo.ContentBlob;
import com.project.snm.service.BlobService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/blobs")
public class BlobController {

    private final BlobService blobService;

    public BlobController(BlobService blobService) {
        this.blobService = blobService;
    }

    @PostMapping
    public ContentBlob uploadBlob(@RequestBody BlobUploadRequest request) {
        return blobService.saveBlob(request);
    }

    @GetMapping("/{id}")
    public ContentBlob getBlob(@PathVariable String id) {
        return blobService.getBlob(id);
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\controller\DocumentVersionController.java
```java
package com.project.snm.controller;

import com.project.snm.dto.CreateDocumentVersionRequest;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.service.DocumentVersionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }

    @PostMapping("/{uuid}/versions")
    public DocumentVersion createVersion(
            @PathVariable String uuid,
            @RequestBody CreateDocumentVersionRequest request
    ) {
        return documentVersionService.createVersion(uuid, request);
    }

    @GetMapping("/{uuid}/versions")
    public List<DocumentVersion> getVersions(@PathVariable String uuid) {
        return documentVersionService.getVersions(uuid);
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\controller\SyncController.java
```java
package com.project.snm.controller;

import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.service.SyncService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/{documentUuid}/latest")
    public DocumentVersion getLatestVersion(@PathVariable String documentUuid) {
        return syncService.getLatestVersion(documentUuid);
    }

    @GetMapping("/{documentUuid}/all")
    public List<DocumentVersion> getAllVersions(@PathVariable String documentUuid) {
        return syncService.getAllVersions(documentUuid);
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\controller\TeamController.java
```java
package com.project.snm.controller;

import com.project.snm.dto.CreateTeamRequest;
import com.project.snm.model.mysql.Team;
import com.project.snm.service.TeamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request);
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\controller\TestController.java
```java
package com.project.snm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "SNM Backend Running Successfully";
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\dto\BlobUploadRequest.java
```java
package com.project.snm.dto;

import lombok.Data;

@Data
public class BlobUploadRequest {
    private String encryptedContent;
    private String contentHash;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\dto\CreateDocumentVersionRequest.java
```java
package com.project.snm.dto;

import lombok.Data;

@Data
public class CreateDocumentVersionRequest {
    private Long teamId;
    private String blobId;
    private Long createdBy;
    private String vectorClockJson;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\dto\CreateTeamRequest.java
```java
package com.project.snm.dto;

import lombok.Data;

@Data
public class CreateTeamRequest {
    private String teamName;
    private Long createdBy;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\model\mongo\ContentBlob.java
```java
package com.project.snm.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "content_blobs")
public class ContentBlob {

    @Id
    private String id;

    private String encryptedContent;

    private String contentHash;

    private Instant createdAt;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\model\mysql\DocumentVersion.java
```java
package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "document_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentUuid;

    private Long teamId;

    private Long versionSeq;

    private String blobId;

    private Long createdBy;

    private Instant createdAt;

    @Column(length = 2000)
    private String vectorClockJson;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\model\mysql\Team.java
```java
package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;

    private Long createdBy;

    private Instant createdAt;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\model\mysql\TeamMember.java
```java
package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teamId;

    private Long userId;

    private String role;

    private Instant joinedAt;
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\repository\ContentBlobRepository.java
```java
package com.project.snm.repository;

import com.project.snm.model.mongo.ContentBlob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentBlobRepository extends MongoRepository<ContentBlob, String> {
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\repository\DocumentVersionRepository.java
```java
package com.project.snm.repository;

import com.project.snm.model.mysql.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentUuidOrderByVersionSeqAsc(String documentUuid);
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\repository\TeamMemberRepository.java
```java
package com.project.snm.repository;

import com.project.snm.model.mysql.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\repository\TeamRepository.java
```java
package com.project.snm.repository;

import com.project.snm.model.mysql.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\service\BlobService.java
```java
package com.project.snm.service;

import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.model.mongo.ContentBlob;
import com.project.snm.repository.ContentBlobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BlobService {

    private final ContentBlobRepository contentBlobRepository;

    public BlobService(ContentBlobRepository contentBlobRepository) {
        this.contentBlobRepository = contentBlobRepository;
    }

    public ContentBlob saveBlob(BlobUploadRequest request) {
        ContentBlob blob = ContentBlob.builder()
                .encryptedContent(request.getEncryptedContent())
                .contentHash(request.getContentHash())
                .createdAt(Instant.now())
                .build();

        return contentBlobRepository.save(blob);
    }

    public ContentBlob getBlob(String id) {
        return contentBlobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blob not found"));
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\service\DocumentVersionService.java
```java
package com.project.snm.service;

import com.project.snm.dto.CreateDocumentVersionRequest;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.repository.DocumentVersionRepository;
import com.project.snm.websocket.SyncNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final SyncNotificationService syncNotificationService;

    public DocumentVersionService(DocumentVersionRepository documentVersionRepository,
                                  SyncNotificationService syncNotificationService) {
        this.documentVersionRepository = documentVersionRepository;
        this.syncNotificationService = syncNotificationService;
    }

    public DocumentVersion createVersion(String docId, CreateDocumentVersionRequest request) {

        List<DocumentVersion> existingVersions =
                documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(docId);

        if (!existingVersions.isEmpty()) {
            DocumentVersion last = existingVersions.get(existingVersions.size() - 1);

            Map<String, Integer> lastClock = parseVectorClock(last.getVectorClockJson());
            Map<String, Integer> newClock = parseVectorClock(request.getVectorClockJson());

            int result = compareClocks(newClock, lastClock);

            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate version detected");
            } else if (result == -1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Outdated version rejected");
            } else if (result == 2) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict detected (parallel updates)");
            }
        }

        long nextVersion = existingVersions.size() + 1L;

        DocumentVersion version = new DocumentVersion();
        version.setDocumentUuid(docId);
        version.setTeamId(request.getTeamId());
        version.setVersionSeq(nextVersion);
        version.setBlobId(request.getBlobId());
        version.setCreatedBy(request.getCreatedBy());
        version.setCreatedAt(Instant.now());
        version.setVectorClockJson(request.getVectorClockJson());

        DocumentVersion savedVersion = documentVersionRepository.save(version);
        syncNotificationService.broadcastDocumentUpdate(savedVersion);
        return savedVersion;
    }

    public List<DocumentVersion> getVersions(String documentUuid) {
        return documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
    }

    private Map<String, Integer> parseVectorClock(String json) {
        Map<String, Integer> clock = new HashMap<>();

        if (json == null || json.isBlank()) {
            return clock;
        }

        String cleaned = json.trim();
        cleaned = cleaned.replace("{", "").replace("}", "").replace("\"", "");

        if (cleaned.isBlank()) {
            return clock;
        }

        String[] entries = cleaned.split(",");

        for (String entry : entries) {
            String[] pair = entry.split(":");
            if (pair.length == 2) {
                String key = pair[0].trim();
                Integer value = Integer.parseInt(pair[1].trim());
                clock.put(key, value);
            }
        }

        return clock;
    }

    private int compareClocks(Map<String, Integer> newClock, Map<String, Integer> oldClock) {
        boolean greater = false;
        boolean smaller = false;

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(newClock.keySet());
        allKeys.addAll(oldClock.keySet());

        for (String key : allKeys) {
            int newValue = newClock.getOrDefault(key, 0);
            int oldValue = oldClock.getOrDefault(key, 0);

            if (newValue > oldValue) {
                greater = true;
            } else if (newValue < oldValue) {
                smaller = true;
            }
        }

        if (!greater && !smaller) {
            return 0;   // duplicate
        }
        if (greater && !smaller) {
            return 1;   // valid newer version
        }
        if (!greater && smaller) {
            return -1;  // outdated
        }
        return 2;       // concurrent conflict
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\service\SyncService.java
```java
package com.project.snm.service;

import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {

    private final DocumentVersionRepository documentVersionRepository;

    public SyncService(DocumentVersionRepository documentVersionRepository) {
        this.documentVersionRepository = documentVersionRepository;
    }

    public DocumentVersion getLatestVersion(String documentUuid) {
        List<DocumentVersion> versions =
                documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);

        if (versions.isEmpty()) {
            throw new RuntimeException("No versions found for document");
        }

        return versions.get(versions.size() - 1);
    }

    public List<DocumentVersion> getAllVersions(String documentUuid) {
        return documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\service\TeamService.java
```java
package com.project.snm.service;

import com.project.snm.dto.CreateTeamRequest;
import com.project.snm.model.mysql.Team;
import com.project.snm.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team createTeam(CreateTeamRequest request) {
        Team team = new Team();
        team.setTeamName(request.getTeamName());
        team.setCreatedBy(request.getCreatedBy());
        team.setCreatedAt(Instant.now());

        return teamRepository.save(team);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\websocket\DocumentSyncMessage.java
```java
package com.project.snm.websocket;

public class DocumentSyncMessage {

    private String documentId;
    private Long version;
    private String message;

    public DocumentSyncMessage() {
    }

    public DocumentSyncMessage(String documentId, Long version, String message) {
        this.documentId = documentId;
        this.version = version;
        this.message = message;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
```
### Java-Project\snm_module\src\main\java\com\project\snm\websocket\SyncNotificationService.java
```java
package com.project.snm.websocket;

import com.project.snm.model.mysql.DocumentVersion;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SyncNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public SyncNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastDocumentUpdate(DocumentVersion version) {

        DocumentSyncMessage message = new DocumentSyncMessage(
                version.getDocumentUuid(),
                version.getVersionSeq(),
                "Document updated"
        );

        messagingTemplate.convertAndSend(
                "/topic/document/" + version.getDocumentUuid(),
                message
        );
    }
}
```
### Java-Project\snm_module\src\test\java\com\project\snm\SnmBackendApplicationTests.java
```java
package com.project.snm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SnmBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}

```

## 4. Flyway Migrations

## 5. Application Properties/YAML
### Java-Project\javafx-sdk-26\lib\javafx.properties
```yaml
javafx.version=26
javafx.runtime.version=26+27
javafx.runtime.build=27

```
### Java-Project\snm_module\.mvn\wrapper\maven-wrapper.properties
```yaml
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.14/apache-maven-3.9.14-bin.zip

```
### Java-Project\snm_module\src\main\resources\application.yaml
```yaml
spring:
  application:
    name: snm-backend

  data:
    mongodb:
      uri: mongodb://localhost:27017/snm-db

  datasource:
    url: jdbc:mysql://localhost:3306/snm_metadata
    username: root
    password:
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```
