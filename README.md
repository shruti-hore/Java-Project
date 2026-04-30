# Aegis Secure Vault

A zero-knowledge, end-to-end encrypted (E2EE) task management system.

## Rubric Evidence (Global)
- **SOLID (DIP)**: High-level modules depend on abstractions (Services) rather than low-level implementations.
- **Design Pattern (Facade)**: `HttpAuthClient` simplifies complex API interactions into a single interface.
- **Design Pattern (Singleton)**: `UserSession` manages global application state.

## Project Architecture
The system is divided into four specialized modules:

1.  **`crypto_module`**: Core security primitives (Argon2id, AES-GCM 256, X25519).
2.  **`auth_module`**: Identity management and secure session handling.
3.  **`ctm_module`**: JavaFX client application with offline SQLite caching and background sync.
4.  **`snm_module`**: Spring Boot backend serving as an encrypted blob vault (MongoDB) and metadata provider (MySQL).

## Quick Start
1.  **Backend**: Set `SPRING_DATASOURCE_PASSWORD` and run `mvn spring-boot:run` in `snm_module`.
2.  **Client**: Run `mvn javafx:run` in `ctm_module`.
