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


---

## Global Security Rules
- **Key Zeroing:** All `byte[]` arrays holding key material must be zeroed immediately after use.
- **No Log Leaks:** Plaintext, keys, and passwords are never logged, even at DEBUG level.
- **Zero Framework Deps:** No dependencies on Spring, JavaFX, or other heavy frameworks.
- **Provider:** All operations use the **Bouncy Castle (BC)** provider.
