package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "document_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentUuid;

    private String teamId;   // FK → Team.id (UUID string)

    private Long versionSeq;

    private String blobId;

    private String nonceBase64;

    private String aadBase64;

    private String createdBy;  // FK → UserRecord.userId (UUID string)

    private Instant createdAt;

    @Column(length = 2000)
    private String vectorClockJson;
}