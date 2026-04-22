package crypto.internal;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.util.Arrays;

/**
 * Task CRY-01: Argon2id Master Key Derivation.
 * Hard floor parameters: m=65536, t=3, p=4.
 */
public class MasterKeyDerivation {

    private static final int MEMORY_COST = 65536; // 64 MB
    private static final int TIME_COST = 3;
    private static final int PARALLELISM = 4;
    private static final int OUTPUT_LENGTH = 32;

    static {
        // Global Enforcement: Parameters may not be lowered below m=65536, t=3, p=4
        if (MEMORY_COST < 65536 || TIME_COST < 3 || PARALLELISM < 4) {
            throw new ExceptionInInitializerError("Argon2id parameters below hard floor");
        }
    }

    /**
     * Derives a 32-byte master key from a password and salt using Argon2id.
     *
     * @param password The user's plaintext password. Will be zeroed before return.
     * @param salt     16 random bytes.
     * @return 32-byte master key.
     * @throws IllegalArgumentException if salt is not 16 bytes.
     */
    public byte[] derive(char[] password, byte[] salt) {
        if (salt == null || salt.length != 16) {
            throw new IllegalArgumentException("Salt must be exactly 16 bytes");
        }

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(MEMORY_COST)
                .withIterations(TIME_COST)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] masterKey = new byte[OUTPUT_LENGTH];
        try {
            generator.generateBytes(password, masterKey);
            return masterKey;
        } finally {
            // Global Enforcement: Zeroing password char[]
            if (password != null) {
                Arrays.fill(password, (char) 0);
            }
        }
    }
}
