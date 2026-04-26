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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String teamName;

    private String ownerUserId;   // UUID string of the creator (UserRecord.userId)

    @Column(unique = true)
    private String workspaceCode; // Short invite code for join-by-code

    private Instant createdAt;
}