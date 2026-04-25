package client.sync;

/**
 * PIPE-07 Fix [failure mode]: PendingOperation record for tracking offline writes.
 */
public record PendingOperation(
    long id,
    String docUuid,
    String teamId,
    String ciphertextBase64,
    String nonceBase64,
    String aadBase64,
    int versionSeq,
    String vectorClockJson,
    int retryCount
) {}
