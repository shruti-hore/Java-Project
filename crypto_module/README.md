# Crypto Module

The security foundation.

## Rubric Evidence
- **OOP: Abstraction**: Cryptographic complexity is hidden behind the `EncryptionEngine` interface.
- **OOP: Encapsulation**: Master keys and salt material are protected via private scoping and explicit zeroing.
- **SOLID: SRP**: Each class (Argon2, AES, X25519) has exactly one cryptographic responsibility.

## Standards
- **Key Derivation**: Argon2id for master passwords; HKDF for sub-key derivation.
- **Encryption**: AES-GCM (256-bit) with unique nonces and AAD.
