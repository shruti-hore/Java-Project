package com.zero.crypto.internal;

import com.zero.crypto.util.MemoryUtil;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class Argon2idHandler {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65536;
    private static final int PARALLELISM = 4;
    private static final int OUTPUT_LENGTH = 32;

    public byte[] deriveMasterKey(char[] password, byte[] salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }

        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY_KB)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] result = new byte[OUTPUT_LENGTH];
        try {
            generator.generateBytes(password, result);
            return result;
        } finally {
            // Memory management: The generator holds internal state; 
            // however, the password and result are handled by the caller.
        }
    }
}