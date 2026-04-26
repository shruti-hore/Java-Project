package client.service;

import client.crypto.EncryptedDocumentPayload;

/**
 * Stub: Will handle decrypting tasks from encrypted payloads. 
 * Crypto integration is deferred to a later phase.
 */
public class EncryptedTaskService {
    public client.model.Task loadTask(String documentUuid, String teamId, EncryptedDocumentPayload payload) {
        // Stub — real implementation will decrypt payload using team key from SessionState
        return null;
    }
}
