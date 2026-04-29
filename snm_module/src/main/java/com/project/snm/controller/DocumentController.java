package com.project.snm.controller;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;
import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.dto.ConflictResponse;
import com.project.snm.dto.DocumentVersionResponse;
import com.project.snm.dto.DocumentVersionSubmission;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.repository.DocumentVersionRepository;
import com.project.snm.service.BlobService;
import com.project.snm.websocket.SyncNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PIPE-04 Fix [failure mode]: Server Document Write Endpoint with conflict detection.
 * Ensures nonce and AAD are stored alongside ciphertext. 
 * Implements optimistic concurrency control via expectedVersionSeq.
 */
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentVersionRepository documentVersionRepository;
    private final BlobService blobService;
    private final SyncNotificationService syncNotificationService;
    private final ObjectMapper objectMapper;

    @PostMapping("/{documentUuid}/versions")
    @Transactional
    public ResponseEntity<?> createVersion(
            @PathVariable String documentUuid,
            @RequestBody DocumentVersionSubmission submission
    ) throws StreamReadException, tools.jackson.core.JacksonException {
        // 1. Load current DocumentVersion for documentUuid
        List<DocumentVersion> versions = documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
        DocumentVersion current = versions.isEmpty() ? null : versions.get(versions.size() - 1);
        long currentVersionSeq = current == null ? 0L : current.getVersionSeq();

        // 2. Conflict check
        if (submission.getExpectedVersionSeq() != currentVersionSeq) {
            // Store the incoming ciphertext blob in MongoDB first
            BlobUploadRequest incomingBlobReq = new BlobUploadRequest();
            incomingBlobReq.setEncryptedContent(submission.getCiphertextBase64());
            // No hash provided in submission, we can use a placeholder or compute it
            String incomingBlobRef = blobService.saveBlob(incomingBlobReq).getId();
            
            ConflictResponse conflictResponse = new ConflictResponse(
                    current == null ? null : current.getBlobId(),
                    incomingBlobRef
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictResponse);
        }

        // 3. Store ciphertext blob in MongoDB
        BlobUploadRequest blobReq = new BlobUploadRequest();
        blobReq.setEncryptedContent(submission.getCiphertextBase64());
        String blobRef = blobService.saveBlob(blobReq).getId();

        // 4. Update vector clock — principal name is userId (UUID string)
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Map<String, Integer> clock = submission.getVectorClock();
        if (clock == null) clock = new HashMap<>();
        clock.put(userId, clock.getOrDefault(userId, 0) + 1);

        // 5. Create new DocumentVersion in MySQL
        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocumentUuid(documentUuid);
        newVersion.setTeamId(submission.getTeamId());
        newVersion.setVersionSeq(currentVersionSeq + 1);
        newVersion.setBlobId(blobRef);
        newVersion.setNonceBase64(submission.getNonceBase64());
        newVersion.setAadBase64(submission.getAadBase64());
        newVersion.setVectorClockJson(objectMapper.writeValueAsString(clock));
        newVersion.setCreatedBy(userId);
        newVersion.setCreatedAt(Instant.now());

        DocumentVersion saved = documentVersionRepository.save(newVersion);

        // 6. Broadcast via SyncNotificationService (happens after commit due to @Transactional)
        syncNotificationService.broadcastDocumentUpdate(saved);

        Map<String, Object> response = new HashMap<>();
        response.put("versionSeq", saved.getVersionSeq());
        response.put("blobRef", saved.getBlobId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documentUuid}/versions/latest")
    public ResponseEntity<DocumentVersionResponse> getLatestVersion(@PathVariable String documentUuid) throws tools.jackson.core.JacksonException {
        List<DocumentVersion> versions = documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
        if (versions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        DocumentVersion latest = versions.get(versions.size() - 1);
        return ResponseEntity.ok(mapToResponse(latest));
    }

    @GetMapping("/{documentUuid}")
    public ResponseEntity<DocumentVersionResponse> getLatestDocument(@PathVariable String documentUuid) throws tools.jackson.core.JacksonException {
        return getLatestVersion(documentUuid);
    }

    @DeleteMapping("/{documentUuid}")
    @Transactional
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentUuid) {
        documentVersionRepository.deleteByDocumentUuid(documentUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teams/{teamId}/documents")
    public ResponseEntity<List<Map<String, Object>>> getTeamDocuments(@PathVariable String teamId) {
        List<DocumentVersion> latests = documentVersionRepository.findLatestVersionsByTeamId(teamId);
        
        List<Map<String, Object>> responses = latests.stream()
                .map(v -> {
                    try {
                        Map<String, Object> docMap = new HashMap<>();
                        docMap.put("id", v.getDocumentUuid());
                        docMap.put("latestPayload", mapToResponse(v));
                        return docMap;
                    } catch (tools.jackson.core.JacksonException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
                
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/versions/by-blob/{blobId}")
    public ResponseEntity<DocumentVersionResponse> getVersionByBlob(@PathVariable String blobId) throws tools.jackson.core.JacksonException {
        DocumentVersion version = documentVersionRepository.findAll().stream()
                .filter(v -> v.getBlobId().equals(blobId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Version not found for blob: " + blobId));
                
        return ResponseEntity.ok(mapToResponse(version));
    }

    private DocumentVersionResponse mapToResponse(DocumentVersion version) throws tools.jackson.core.JacksonException {
        String ciphertext = blobService.getBlob(version.getBlobId()).getEncryptedContent();
        
        return DocumentVersionResponse.builder()
                .versionSeq(version.getVersionSeq())
                .ciphertextBase64(ciphertext)
                .nonceBase64(version.getNonceBase64())
                .aadBase64(version.getAadBase64())
                .vectorClock(objectMapper.readValue(version.getVectorClockJson(), Map.class))
                .build();
    }
}
