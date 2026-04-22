package com.project.snm.service;

import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {

    private final DocumentVersionRepository documentVersionRepository;

    public SyncService(DocumentVersionRepository documentVersionRepository) {
        this.documentVersionRepository = documentVersionRepository;
    }

    public DocumentVersion getLatestVersion(String documentUuid) {
        List<DocumentVersion> versions =
                documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);

        if (versions.isEmpty()) {
            throw new RuntimeException("No versions found for document");
        }

        return versions.get(versions.size() - 1);
    }

    public List<DocumentVersion> getAllVersions(String documentUuid) {
        return documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
    }
}