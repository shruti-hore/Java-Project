package com.project.snm.repository;

import com.project.snm.model.mysql.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentUuidOrderByVersionSeqAsc(String documentUuid);
}