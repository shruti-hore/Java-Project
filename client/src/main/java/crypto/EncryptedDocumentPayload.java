package crypto;

/**
 * PIPE-02 Fix [failure mode]: Payload record for transporting encrypted document blobs
 * and their required cryptographic metadata (nonce, AAD, counter).
 */
public record EncryptedDocumentPayload(
    String ciphertextBase64,
    String nonceBase64,
    String aadBase64,
    long   counterValue,
    int    versionSeq      // the version seq this was encrypted against - stored for conflict detection
) {}
