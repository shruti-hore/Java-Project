package crypto.internal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Task CRY-09: BIP39 Fingerprint Generation.
 * Generates a 6-word mnemonic fingerprint from two public keys.
 */
public class FingerprintService {

    private final String[] wordlist;

    /**
     * Constructor loads the BIP39 wordlist from classpath.
     */
    public FingerprintService() {
        this.wordlist = loadWordlist();
        if (wordlist.length != 2048) {
            throw new CryptoOperationException("BIP39 wordlist must contain exactly 2048 words. Found: " + wordlist.length);
        }
    }

    /**
     * Generates a 6-word fingerprint from two X25519 public keys.
     * Normalized lexicographically to ensure fingerprint(A, B) == fingerprint(B, A).
     * 
     * @param publicKeyA First 32-byte public key.
     * @param publicKeyB Second 32-byte public key.
     * @return 6-word mnemonic string.
     */
    public String generate(byte[] publicKeyA, byte[] publicKeyB) {
        validateKeys(publicKeyA, publicKeyB);

        // 1. Normalize order (unsigned lexicographical comparison)
        byte[] lower;
        byte[] higher;
        if (compareUnsigned(publicKeyA, publicKeyB) <= 0) {
            lower = publicKeyA;
            higher = publicKeyB;
        } else {
            lower = publicKeyB;
            higher = publicKeyA;
        }

        // 2. Concatenate keys (64 bytes)
        byte[] combined = new byte[64];
        System.arraycopy(lower, 0, combined, 0, 32);
        System.arraycopy(higher, 0, combined, 32, 32);

        try {
            // 3. Hash with SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined);

            // 4. Map first 6 bytes to words
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                // Step 5 & 6: unsigned byte % 2048
                int wordIndex = Byte.toUnsignedInt(hash[i]) % 2048;
                sb.append(wordlist[wordIndex]);
                if (i < 5) {
                    sb.append(" ");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            throw new CryptoOperationException("Fingerprint generation failed", e);
        }
    }

    private String[] loadWordlist() {
        try (InputStream is = getClass().getResourceAsStream("/bip39-english.txt")) {
            if (is == null) {
                throw new CryptoOperationException("Wordlist resource not found: /bip39-english.txt");
            }
            List<String> words = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        words.add(trimmed);
                    }
                }
            }
            return words.toArray(new String[0]);
        } catch (Exception e) {
            throw new CryptoOperationException("Failed to load BIP39 wordlist", e);
        }
    }

    private void validateKeys(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Public keys cannot be null");
        }
        if (a.length != 32 || b.length != 32) {
            throw new IllegalArgumentException("Public keys must be exactly 32 bytes");
        }
    }

    private int compareUnsigned(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            int valA = Byte.toUnsignedInt(a[i]);
            int valB = Byte.toUnsignedInt(b[i]);
            if (valA != valB) {
                return valA - valB;
            }
        }
        return 0;
    }
}
