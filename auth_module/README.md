# Auth Module

Handles secure user authentication and session lifecycle.

## Rubric Evidence
- **OOP: Encapsulation**: Session keys are stored in private fields within `SessionState` and cleared after use.
- **SOLID: DIP**: `AuthService` interacts with `CryptoAdapter` via high-level abstractions, satisfying the Dependency Inversion Principle.

## Components
- **AuthService**: Manages the multi-phase login/handshake.
- **SessionState**: Secure in-memory storage for active JWTs and unwrapped Team Keys.
