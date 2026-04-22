package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;

/**
 * Task CRY-06: AES-256-GCM Encryption Engine.
 * Provides core encryption and decryption for document blobs.
 */
public class EncryptionEngine {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int KEY_LENGTH = 32; // bytes
    private static final int NONCE_LENGTH = 12; // bytes
    private static final int AAD_LENGTH = 20; // bytes

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     *
     * @param plaintext Padded plaintext bytes.
     * @param teamKey   32-byte AES key.
     * @param nonce     12-byte deterministic nonce.
     * @param aad       20-byte associated data (doc UUID + version).
     * @return Ciphertext with 16-byte tag appended.
     */
    public byte[] encrypt(byte[] plaintext, byte[] teamKey, byte[] nonce, byte[] aad) {
        validateInputs(teamKey, nonce, aad);
        if (plaintext == null) {
            throw new IllegalArgumentException("Plaintext cannot be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(teamKey, "AES");
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            cipher.updateAAD(aad);
            
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        } finally {
            // Enforcement: Zeroing plaintext byte[]
            Arrays.fill(plaintext, (byte) 0);
            // Enforcement: Zero teamKey (AES key material) after use
            Arrays.fill(teamKey, (byte) 0);
        }
    }

    /**
     * Decrypts AES-256-GCM ciphertext.
     *
     * @param ciphertext Ciphertext with tag appended.
     * @param teamKey    32-byte AES key.
     * @param nonce      12-byte deterministic nonce.
     * @param aad        20-byte associated data.
     * @return Decrypted plaintext.
     * @throws javax.crypto.AEADBadTagException if authentication fails.
     */
    public byte[] decrypt(byte[] ciphertext, byte[] teamKey, byte[] nonce, byte[] aad) throws javax.crypto.AEADBadTagException {
        validateInputs(teamKey, nonce, aad);
        if (ciphertext == null) {
            throw new IllegalArgumentException("Ciphertext cannot be null");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(teamKey, "AES");
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            cipher.updateAAD(aad);
            
            return cipher.doFinal(ciphertext);
        } catch (javax.crypto.AEADBadTagException e) {
            // Enforcement: Decrypt failure must throw AEADBadTagException.
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof javax.crypto.AEADBadTagException) {
                throw (javax.crypto.AEADBadTagException) e.getCause();
            }
            throw new RuntimeException("Decryption failed", e);
        } finally {
            // Enforcement: Zero teamKey (AES key material) after use
            Arrays.fill(teamKey, (byte) 0);
        }
    }

    private void validateInputs(byte[] teamKey, byte[] nonce, byte[] aad) {
        if (teamKey == null || teamKey.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Team key must be exactly 32 bytes");
        }
        if (nonce == null || nonce.length != NONCE_LENGTH) {
            throw new IllegalArgumentException("Nonce must be exactly 12 bytes");
        }
        // Enforcement: Allow 20 bytes (documents) or 0 bytes (envelopes)
        if (aad == null || (aad.length != AAD_LENGTH && aad.length != 0)) {
            throw new IllegalArgumentException("AAD must be exactly 20 bytes or 0 bytes");
        }
    }
}
