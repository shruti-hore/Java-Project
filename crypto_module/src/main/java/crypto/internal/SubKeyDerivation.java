package crypto.internal;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import java.nio.charset.StandardCharsets;

/**
 * Task CRY-02: HKDF sub-key derivation.
 * Derives context-specific keys from a master key using HKDF-SHA256.
 */
public class SubKeyDerivation {

    private static final int OUTPUT_LENGTH = 32;

    /**
     * Derives a 32-byte sub-key from the master key.
     *
     * @param masterKey 32-byte master key from Argon2id.
     * @param info      Context label (e.g., "vault-key", "auth-signing-key").
     * @return 32-byte derived sub-key.
     * @throws IllegalArgumentException if input is invalid or info is unknown.
     */
    public byte[] derive(byte[] masterKey, String info) {
        // Enforcement: Validate input length
        if (masterKey == null || masterKey.length != 32) {
            throw new IllegalArgumentException("Master key must be exactly 32 bytes");
        }

        // Enforcement: Validate info string
        if (!"vault-key".equals(info) && !"auth-signing-key".equals(info)) {
            throw new IllegalArgumentException("Unknown HKDF context: " + info);
        }

        // HKDF-SHA256 (RFC 5869)
        // Salt is null (zero-length) as master key is already high-entropy
        HKDFParameters params = new HKDFParameters(
                masterKey, 
                null, 
                info.getBytes(StandardCharsets.UTF_8)
        );

        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA256Digest());
        generator.init(params);

        byte[] subKey = new byte[OUTPUT_LENGTH];
        generator.generateBytes(subKey, 0, OUTPUT_LENGTH);

        // Note: masterKey is NOT zeroed here as it is managed by the caller
        return subKey;
    }
}
