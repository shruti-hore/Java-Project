# Secure Network Module (SNM)

The cloud backbone for encrypted data persistence.

## Rubric Evidence
- **SOLID: SRP/OCP**: Uses the Controller-Service-Repository pattern, allowing for new entities without modifying existing business logic.
- **JDBC/Persistence**: Hibernate/JPA integration for MySQL metadata and MongoDB for binary blobs.

## Infrastructure
- **MySQL**: Stores non-sensitive metadata (teams, users, workspace codes).
- **MongoDB**: The "Vault" storing encrypted document versions as binary blobs.
