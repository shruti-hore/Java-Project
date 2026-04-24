package com.project.snm.websocket;

import com.project.snm.model.mysql.DocumentVersion;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SyncNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public SyncNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastDocumentUpdate(DocumentVersion version) {

        DocumentSyncMessage message = new DocumentSyncMessage(
                version.getDocumentUuid(),
                version.getVersionSeq(),
                "Document updated"
        );

        messagingTemplate.convertAndSend(
                "/topic/document/" + version.getDocumentUuid(),
                message
        );
    }
}