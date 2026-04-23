package com.project.snm.dto;

import lombok.Data;

@Data
public class BlobUploadRequest {
    private String encryptedContent;
    private String contentHash;
}