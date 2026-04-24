package com.project.snm.service;

import com.project.snm.dto.CreateDocumentVersionRequest;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.repository.DocumentVersionRepository;
import com.project.snm.websocket.SyncNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final SyncNotificationService syncNotificationService;

    public DocumentVersionService(DocumentVersionRepository documentVersionRepository,
                                  SyncNotificationService syncNotificationService) {
        this.documentVersionRepository = documentVersionRepository;
        this.syncNotificationService = syncNotificationService;
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

        DocumentVersion savedVersion = documentVersionRepository.save(version);
        syncNotificationService.broadcastDocumentUpdate(savedVersion);
        return savedVersion;
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
        cleaned = cleaned.replace("{", "").replace("}", "").replace("\"", "");

        if (cleaned.isBlank()) {
            return clock;
        }

        String[] entries = cleaned.split(",");

        for (String entry : entries) {
            String[] pair = entry.split(":");
            if (pair.length == 2) {
                String key = pair[0].trim();
                Integer value = Integer.parseInt(pair[1].trim());
                clock.put(key, value);
            }
        }

        return clock;
    }

    private int compareClocks(Map<String, Integer> newClock, Map<String, Integer> oldClock) {
        boolean greater = false;
        boolean smaller = false;

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(newClock.keySet());
        allKeys.addAll(oldClock.keySet());

        for (String key : allKeys) {
            int newValue = newClock.getOrDefault(key, 0);
            int oldValue = oldClock.getOrDefault(key, 0);

            if (newValue > oldValue) {
                greater = true;
            } else if (newValue < oldValue) {
                smaller = true;
            }
        }

        if (!greater && !smaller) {
            return 0;   // duplicate
        }
        if (greater && !smaller) {
            return 1;   // valid newer version
        }
        if (!greater && smaller) {
            return -1;  // outdated
        }
        return 2;       // concurrent conflict
    }
}