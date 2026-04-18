package com.zero.crypto;

import com.zero.crypto.internal.Argon2idHandler;
import com.zero.crypto.util.MemoryUtil;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class Argon2idAdversarialTest {

    private final Argon2idHandler handler = new Argon2idHandler();

    @Test
    void testConsistency() {
        char[] pwd = "correct-horse-battery-staple".toCharArray();
        byte[] salt = new byte[16]; // Fixed salt
        
        byte[] key1 = handler.deriveMasterKey(pwd, salt);
        byte[] key2 = handler.deriveMasterKey(pwd, salt);

        assertArrayEquals(key1, key2, "Same input must produce same output");
        assertEquals(32, key1.length, "Output must be exactly 32 bytes");
        
        MemoryUtil.wipe(key1);
        MemoryUtil.wipe(key2);
    }

    @Test
    void testSaltSensitivity() {
        char[] pwd = "password123".toCharArray();
        byte[] salt1 = "salt_alpha______".getBytes();
        byte[] salt2 = "salt_beta_______".getBytes();

        byte[] key1 = handler.deriveMasterKey(pwd, salt1);
        byte[] key2 = handler.deriveMasterKey(pwd, salt2);

        assertFalse(Arrays.equals(key1, key2), "Different salts must produce different keys");
        
        MemoryUtil.wipe(key1);
        MemoryUtil.wipe(key2);
    }

    @Test
    void testAdversarialNullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            handler.deriveMasterKey(null, new byte[16]);
        });
    }
}