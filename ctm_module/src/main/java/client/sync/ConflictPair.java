package client.sync;

import client.crypto.EncryptedDocumentPayload;

/**
 * WIRE-FIX: Represents a conflict between local and remote versions of a document.
 * Uses a record to match the accessor patterns expected by SyncManager.
 */
public record ConflictPair(
    String docUuid, 
    String teamId, 
    EncryptedDocumentPayload localPayload, 
    String serverBlobRef, 
    byte[] teamKey
) {}
