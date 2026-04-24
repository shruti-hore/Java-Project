package com.project.snm.repository;

import com.project.snm.model.mysql.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentUuidOrderByVersionSeqAsc(String documentUuid);

    @Query("SELECT v FROM DocumentVersion v WHERE v.teamId = :teamId AND v.versionSeq = (SELECT MAX(v2.versionSeq) FROM DocumentVersion v2 WHERE v2.documentUuid = v.documentUuid)")
    List<DocumentVersion> findLatestVersionsByTeamId(Long teamId);
}