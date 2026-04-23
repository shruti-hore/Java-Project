package com.project.snm.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.project.snm.dto.CreateDocumentVersionRequest;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.repository.DocumentVersionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;

    public DocumentVersionService(DocumentVersionRepository documentVersionRepository) {
        this.documentVersionRepository = documentVersionRepository;
    }

    public DocumentVersion createVersion(String docId, CreateDocumentVersionRequest request) {

        List<DocumentVersion> existingVersions =
                documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(docId);

        if (!existingVersions.isEmpty()) {
            DocumentVersion last = existingVersions.get(existingVersions.size() - 1);

            Map<String, Integer> lastClock = parseVectorClock(last.getVectorClockJson());
            Map<String, Integer> newClock = parseVectorClock(request.getVectorClockJson());

            int result = compareClocks(newClock, lastClock);

            if (result == 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate version detected");
            } else if (result == -1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Outdated version rejected");
            } else if (result == 2) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Conflict detected (parallel updates)");
            }
        }

        long nextVersion = existingVersions.size() + 1L;

        DocumentVersion version = new DocumentVersion();
        version.setDocumentUuid(docId);
        version.setTeamId(request.getTeamId());
        version.setVersionSeq(nextVersion);
        version.setBlobId(request.getBlobId());
        version.setCreatedBy(request.getCreatedBy());
        version.setCreatedAt(Instant.now());
        version.setVectorClockJson(request.getVectorClockJson());

        return documentVersionRepository.save(version);
    }

    public List<DocumentVersion> getVersions(String documentUuid) {
        return documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
    }

    private Map<String, Integer> parseVectorClock(String json) {
        Map<String, Integer> clock = new HashMap<>();

        if (json == null || json.isBlank()) {
            return clock;
        }

        String cleaned = json.trim();

        if (cleaned.startsWith("{")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("}")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        if (cleaned.isBlank()) {
            return clock;
        }

        String[] entries = cleaned.split(",");

        for (String entry : entries) {
            String[] pair = entry.split(":");

            if (pair.length != 2) {
                throw new RuntimeException("Invalid vector clock format");
            }

            String key = pair[0].trim().replace("\"", "");
            Integer value = Integer.parseInt(pair[1].trim().replace("\"", ""));

            clock.put(key, value);
        }

        return clock;
    }

    private int compareClocks(Map<String, Integer> a, Map<String, Integer> b) {
        boolean aGreater = false;
        boolean bGreater = false;

        Set<String> keys = new HashSet<>();
        keys.addAll(a.keySet());
        keys.addAll(b.keySet());

        for (String key : keys) {
            int av = a.getOrDefault(key, 0);
            int bv = b.getOrDefault(key, 0);

            if (av > bv) {
                aGreater = true;
            }
            if (bv > av) {
                bGreater = true;
            }
        }

        if (aGreater && !bGreater) {
            return 1;
        }
        if (!aGreater && bGreater) {
            return -1;
        }
        if (!aGreater && !bGreater) {
            return 0;
        }
        return 2;
    }
}