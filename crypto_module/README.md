# CRY — Zero-Knowledge Cryptography Module

This module provides high-security cryptographic primitives for the application, following a zero-knowledge architecture. It is built using **Java 21** and **Bouncy Castle**.

## Core Functions

### 1. Master Key Derivation (Argon2id)
**Class:** `MasterKeyDerivation`
**Method:** `byte[] derive(char[] password, byte[] salt)`

Derives the primary 32-byte master key from a user's password. 
- **Parameters:**
    - `password`: The user's plaintext password.
    - `salt`: 16 random bytes (unique per user).
- **Security Constraints:**
    - Uses **Argon2id** with hard-floor parameters (`m=64MB, t=3, p=4`).
    - Password `char[]` is automatically zeroed in a `finally` block after derivation.
- **Use Case:** Initial user authentication and derivation of the base key material.
- **Why Argon2id** It is the standard for password hashing. Resistent to GPU cracking due to its memory heaviness. Not a problem since it is executed only one.

### 2. Sub-Key Derivation (HKDF-SHA256)
**Class:** `SubKeyDerivation`
**Method:** `byte[] derive(byte[] masterKey, String info)`

Derives context-specific sub-keys from the master key to ensure key isolation.
- **Parameters:**
    - `masterKey`: The 32-byte output from Argon2id.
    - `info`: Context label. Currently supports `"vault-key"` and `"auth-signing-key"`.
- **Security Constraints:**
    - Uses **HKDF-SHA256** (RFC 5869).
    - Prevents "taxonomy drift" by strictly validating info strings.
    - Uses **UTF-8** encoding for labels to ensure cross-platform consistency.
- **Use Case:** Creating isolated keys for specific tasks (e.g., one key for the vault, another for signing) so that a compromise in one context doesn't affect others.
- **Why HKDF-SHA256** Helps create strong master key, to the later create cryptographically separate derived keys.

### 3. X25519 Key Pair Generation
**Class:** `KeyPairService`
**Method:** `X25519KeyPair generateKeyPair()`

Generates a fresh random key pair for secure key agreement (Diffie-Hellman).
- **Parameters:**
    - No input for generation.
    - `rawBytes`: 32-byte array for loading existing public keys.
- **Security Constraints:**
    - Uses the **X25519** curve (RFC 7748).
    - Public keys are handled as **raw 32-byte arrays**, avoiding bulky DER/ASN.1 formats.
- **Use Case:** Generating the unique identity and encryption keys for users.
- **Why X25519** An elliptic curve designed specifically for key exchange — fast, small keys (32 bytes), and practically immune to implementation mistakes.

### 4. ECDH Shared Secret Calculation
**Class:** `EcdhService`
**Method:** `byte[] computeSharedSecret(PrivateKey myPrivateKey, PublicKey theirPublicKey)`

Computes a mutual 32-byte shared secret using a private key and a counterparty's public key.
- **Parameters:**
    - `myPrivateKey`: The caller's X25519 private key object.
    - `theirPublicKey`: The 32-byte public key of the other party.
- **Security Constraints:**
    - Raw ECDH output is **never returned**; it is immediately **SHA-256 hashed** to ensure uniform entropy.
    - The raw intermediate secret is zeroed in memory in a `finally` block.
- **Use Case:** The foundation for all peer-to-peer encryption. Two users can arrive at the same AES key by only knowing each other's public keys.
- **Why ECDH:** X25519 allows any two parties to derive the same shared secret from their respective key pairs — without ever transmitting the secret itself.

### 5. Key Vault Seal / Unseal (AES-256-GCM)
**Class:** `VaultService`
**Methods:**
- `byte[] seal(byte[] privateKeyBytes, byte[] vaultKey)`
- `byte[] unseal(byte[] vaultBlob, byte[] vaultKey)`

Protects sensitive private key material for database storage.
- **Parameters:**
    - `privateKeyBytes`: The raw private key bytes to protect.
    - `vaultKey`: 32-byte key derived via HKDF (info="vault-key").
    - `vaultBlob`: The output blob formatted as `nonce[12] + ciphertext + tag[16]`.
- **Security Constraints:**
    - Uses **AES-256-GCM** for authenticated encryption.
    - Each operation uses a fresh 12-byte **SecureRandom nonce** (prepended to output).
    - Plaintext `privateKeyBytes` are **zeroed** in a `finally` block after sealing.
    - Authentication failures throw `AEADBadTagException` (never swallowed).
- **Use Case:** Encrypting a user's private key before storing it in a database.
- **Why AES-GCM:** Provides "authenticated encryption"—if even a single bit of the encrypted vault is tampered with, the unseal operation will fail instead of returning garbage.

---

## Global Security Rules
- **Key Zeroing:** All `byte[]` arrays holding key material must be zeroed immediately after use.
- **No Log Leaks:** Plaintext, keys, and passwords are never logged, even at DEBUG level.
- **Zero Framework Deps:** No dependencies on Spring, JavaFX, or other heavy frameworks.
- **Provider:** All operations use the **Bouncy Castle (BC)** provider.
