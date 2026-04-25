package com.project.snm.controller;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.dto.ConflictResponse;
import com.project.snm.dto.DocumentVersionResponse;
import com.project.snm.dto.DocumentVersionSubmission;
import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.model.mysql.UserRecord;
import com.project.snm.repository.DocumentVersionRepository;
import com.project.snm.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/{documentUuid}/versions")
    @Transactional
    public ResponseEntity<?> createVersion(
            @PathVariable String documentUuid,
            @RequestBody DocumentVersionSubmission submission
    ) throws JacksonException {
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

        // 4. Update vector clock
        String userEmailHmac = SecurityContextHolder.getContext().getAuthentication().getName();
        UserRecord user = userRepository.findByEmailHmac(userEmailHmac)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Integer> clock = submission.getVectorClock();
        if (clock == null) clock = new HashMap<>();
        clock.put(user.getUserId(), clock.getOrDefault(user.getUserId(), 0) + 1);

        // 5. Create new DocumentVersion in MySQL
        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocumentUuid(documentUuid);
        newVersion.setTeamId(submission.getTeamId());
        newVersion.setVersionSeq(currentVersionSeq + 1);
        newVersion.setBlobId(blobRef);
        newVersion.setNonceBase64(submission.getNonceBase64());
        newVersion.setAadBase64(submission.getAadBase64());
        newVersion.setVectorClockJson(objectMapper.writeValueAsString(clock));
        newVersion.setCreatedBy(user.getUserId() == null ? null : Long.parseLong("0")); // userId is UUID, createdBy is Long — using 0 as placeholder until schema aligns
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
    public ResponseEntity<DocumentVersionResponse> getLatestVersion(@PathVariable String documentUuid) throws JacksonException {
        List<DocumentVersion> versions = documentVersionRepository.findByDocumentUuidOrderByVersionSeqAsc(documentUuid);
        if (versions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        DocumentVersion latest = versions.get(versions.size() - 1);
        return ResponseEntity.ok(mapToResponse(latest));
    }

    @GetMapping("/teams/{teamId}/documents")
    public ResponseEntity<List<DocumentVersionResponse>> getTeamDocuments(@PathVariable Long teamId) {
        List<DocumentVersion> latests = documentVersionRepository.findLatestVersionsByTeamId(teamId);
        
        List<DocumentVersionResponse> responses = latests.stream()
                .map(v -> {
                    try {
                        return mapToResponse(v);
                    } catch (JacksonException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
                
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/versions/by-blob/{blobId}")
    public ResponseEntity<DocumentVersionResponse> getVersionByBlob(@PathVariable String blobId) throws JacksonException {
        DocumentVersion version = documentVersionRepository.findAll().stream()
                .filter(v -> v.getBlobId().equals(blobId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Version not found for blob: " + blobId));
                
        return ResponseEntity.ok(mapToResponse(version));
    }

    private DocumentVersionResponse mapToResponse(DocumentVersion version) throws JacksonException {
        String ciphertext = blobService.getBlob(version.getBlobId()).getEncryptedContent();
        
        return DocumentVersionResponse.builder()
                .documentUuid(version.getDocumentUuid())
                .versionSeq(version.getVersionSeq())
                .ciphertextBase64(ciphertext)
                .nonceBase64(version.getNonceBase64())
                .aadBase64(version.getAadBase64())
                .vectorClock(objectMapper.readValue(version.getVectorClockJson(), Map.class))
                .build();
    }
}
