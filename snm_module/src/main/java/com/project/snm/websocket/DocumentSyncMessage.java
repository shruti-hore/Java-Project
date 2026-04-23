package com.project.snm.websocket;

public class DocumentSyncMessage {

    private String documentId;
    private Long version;
    private String message;

    public DocumentSyncMessage() {
    }

    public DocumentSyncMessage(String documentId, Long version, String message) {
        this.documentId = documentId;
        this.version = version;
        this.message = message;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}