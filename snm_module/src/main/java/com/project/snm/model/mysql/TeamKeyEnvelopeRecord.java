package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_key_envelopes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"teamId", "userId"})
})
@Getter
@Setter
@NoArgsConstructor
public class TeamKeyEnvelopeRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String envelopeId;

    @Column(nullable = false)
    private String teamId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String envelopeBase64;  // wrapped team key blob — opaque
}
