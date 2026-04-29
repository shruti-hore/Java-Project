package com.project.snm.service;

import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.model.mongo.ContentBlob;
import com.project.snm.repository.ContentBlobRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BlobService {

    private final ContentBlobRepository contentBlobRepository;
    private final ServerEncryptionService encryptionService;

    public BlobService(ContentBlobRepository contentBlobRepository, ServerEncryptionService encryptionService) {
        this.contentBlobRepository = contentBlobRepository;
        this.encryptionService = encryptionService;
    }

    public ContentBlob saveBlob(BlobUploadRequest request) {
        // Temporarily disabled server-side encryption to fix task visibility issues
        // String doublyEncrypted = encryptionService.encrypt(request.getEncryptedContent());
        
        ContentBlob blob = ContentBlob.builder()
                .encryptedContent(request.getEncryptedContent())
                .contentHash(request.getContentHash())
                .createdAt(Instant.now())
                .build();

        return contentBlobRepository.save(blob);
    }

    public ContentBlob getBlob(String id) {
        ContentBlob blob = contentBlobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blob not found"));
        
        // Temporarily disabled server-side decryption
        // String clientCiphertext = encryptionService.decrypt(blob.getEncryptedContent());
        // blob.setEncryptedContent(clientCiphertext);
        return blob;
    }
}