package crypto.internal;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Task CRY-06: Deterministic Nonce Builder.
 * Constructs a 12-byte GCM nonce from context fields.
 */
public class NonceBuilder {

    /**
     * Constructs a 12-byte deterministic nonce.
     * Nonce layout:
     * [0..1]  = teamKeyVersion (2 bytes, big-endian)
     * [2..5]  = first 4 bytes of docUuid (4 bytes)
     * [6..11] = counterValue (6 bytes, big-endian)
     *
     * @param teamKeyVersion Short version of the team key.
     * @param docUuid        UUID of the document.
     * @param counterValue   6-byte counter value (long).
     * @return 12-byte nonce array.
     */
    public byte[] build(short teamKeyVersion, UUID docUuid, long counterValue) {
        if (docUuid == null) {
            throw new IllegalArgumentException("Document UUID cannot be null");
        }
        
        byte[] nonce = new byte[12];
        ByteBuffer buffer = ByteBuffer.wrap(nonce);

        // 1. team_key_version (short, 2 bytes)
        buffer.putShort(teamKeyVersion);

        // 2. first 4 bytes of doc UUID (4 bytes)
        // UUID.getMostSignificantBits() returns a long; we take the first 4 bytes.
        long msb = docUuid.getMostSignificantBits();
        buffer.putInt((int) (msb >>> 32));

        // 3. counter_value (6 bytes from big-endian long)
        // We take the last 6 bytes of the long.
        buffer.put((byte) (counterValue >>> 40));
        buffer.put((byte) (counterValue >>> 32));
        buffer.put((byte) (counterValue >>> 24));
        buffer.put((byte) (counterValue >>> 16));
        buffer.put((byte) (counterValue >>> 8));
        buffer.put((byte) (counterValue & 0xFF));

        return nonce;
    }
}
