package com.project.snm.dto;

import lombok.Data;

@Data
public class CreateDocumentVersionRequest {
    private Long teamId;
    private String blobId;
    private Long createdBy;
    private String vectorClockJson;
}