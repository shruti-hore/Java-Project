package client.crypto;

/**
 * WIRE-FIX: Secure document payload.
 * Matches the record pattern used in the cryptographic pipeline.
 */
public record EncryptedDocumentPayload(
    String ciphertextBase64,
    String nonceBase64,
    String aadBase64,
    long counter,
    int versionSeq
) {}
