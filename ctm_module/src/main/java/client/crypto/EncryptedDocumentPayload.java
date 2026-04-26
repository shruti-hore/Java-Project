package client.crypto;

/**
 * Stub: Represents an encrypted document payload for future crypto integration.
 */
public class EncryptedDocumentPayload {
    private byte[] ciphertext;
    private byte[] nonce;
    private long versionSeq;

    public EncryptedDocumentPayload() {}

    public EncryptedDocumentPayload(byte[] ciphertext, byte[] nonce, long versionSeq) {
        this.ciphertext = ciphertext;
        this.nonce = nonce;
        this.versionSeq = versionSeq;
    }

    public byte[] getCiphertext() { return ciphertext; }
    public byte[] getNonce() { return nonce; }
    public long getVersionSeq() { return versionSeq; }
}
