package com.project.snm.dto;

import lombok.Data;

@Data
public class CreateDocumentVersionRequest {
    private String teamId;
    private String blobId;
    private String createdBy;
    private String vectorClockJson;
}