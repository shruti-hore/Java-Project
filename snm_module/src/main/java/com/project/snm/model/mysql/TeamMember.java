package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "team_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teamId", "userId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String teamId;   // FK → Team.id (UUID string)

    @Column(nullable = false)
    private String userId;   // FK → UserRecord.userId (UUID string)

    private String role;

    private Instant joinedAt;
}