package com.project.snm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * User entity class mapping to the 'users' table.
 * Simplified for Phase 1 rebuild.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "bcrypt_hash", nullable = false)
    private String bcryptHash;

    @Column(nullable = false)
    private String salt;

    @Column(name = "vault_blob", nullable = false, columnDefinition = "TEXT")
    private String vaultBlob;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
