package com.project.snm.service;

import com.project.snm.dto.BlobUploadRequest;
import com.project.snm.model.mongo.ContentBlob;
import com.project.snm.repository.ContentBlobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class BlobService {

    private final ContentBlobRepository contentBlobRepository;

    public BlobService(ContentBlobRepository contentBlobRepository) {
        this.contentBlobRepository = contentBlobRepository;
    }

    public ContentBlob saveBlob(BlobUploadRequest request) {
        ContentBlob blob = ContentBlob.builder()
                .encryptedContent(request.getEncryptedContent())
                .contentHash(request.getContentHash())
                .createdAt(Instant.now())
                .build();

        return contentBlobRepository.save(blob);
    }

    public ContentBlob getBlob(String id) {
        return contentBlobRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blob not found"));
    }
}