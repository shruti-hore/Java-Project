package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserRecord {
    @Id
    @GeneratedValue(generator = "uuid2")
    @org.hibernate.annotations.GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;              // Stored in plaintext. Used for lookup.
    
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String bcryptHash;

    @Column(nullable = false, length = 64)
    private String publicKeyBase64;    // Base64 of 32-byte X25519 public key

    @Column(nullable = false, columnDefinition = "TEXT")
    private String vaultBlobBase64;

    @Column(nullable = false, length = 32)
    private String saltBase64;         // Base64 of 16-byte Argon2id salt
}
