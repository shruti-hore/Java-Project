package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * Task CRY-05: Key Vault Seal and Unseal.
 * Protects private key material using AES-256-GCM.
 */
public class VaultService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int NONCE_LENGTH = 12; // in bytes
    private static final int KEY_LENGTH = 32; // in bytes

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Seals a private key using AES-256-GCM with a random nonce.
     *
     * @param privateKeyBytes Raw private key material to protect.
     * @param vaultKey        32-byte key derived via HKDF.
     * @return Sealed output: nonce[12] + ciphertext + gcmTag[16].
     * @throws IllegalArgumentException if vaultKey is not 32 bytes or privateKeyBytes is null.
     */
    public byte[] seal(byte[] privateKeyBytes, byte[] vaultKey) {
        if (privateKeyBytes == null) {
            throw new IllegalArgumentException("Private key bytes cannot be null");
        }
        if (vaultKey == null || vaultKey.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Vault key must be exactly 32 bytes");
        }

        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[NONCE_LENGTH];
        random.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(vaultKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] ciphertext = cipher.doFinal(privateKeyBytes);
            
            byte[] vaultBlob = new byte[NONCE_LENGTH + ciphertext.length];
            System.arraycopy(nonce, 0, vaultBlob, 0, NONCE_LENGTH);
            System.arraycopy(ciphertext, 0, vaultBlob, NONCE_LENGTH, ciphertext.length);
            
            return vaultBlob;
        } catch (Exception e) {
            throw new RuntimeException("Vault seal failed", e);
        } finally {
            // Enforcement: privateKeyBytes not zeroed after seal() is a failure mode
            Arrays.fill(privateKeyBytes, (byte) 0);
        }
    }

    /**
     * Unseals a sealed blob to recover the original private key.
     *
     * @param vaultBlob Sealed output from seal().
     * @param vaultKey  32-byte key used during sealing.
     * @return Recovered raw private key bytes.
     * @throws IllegalArgumentException if vaultKey is not 32 bytes or vaultBlob is too short.
     * @throws javax.crypto.AEADBadTagException if authentication fails.
     */
    public byte[] unseal(byte[] vaultBlob, byte[] vaultKey) throws javax.crypto.AEADBadTagException {
        if (vaultBlob == null || vaultBlob.length < NONCE_LENGTH + 16) {
            throw new IllegalArgumentException("Vault blob is too short or null");
        }
        if (vaultKey == null || vaultKey.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Vault key must be exactly 32 bytes");
        }

        byte[] nonce = Arrays.copyOfRange(vaultBlob, 0, NONCE_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(vaultBlob, NONCE_LENGTH, vaultBlob.length);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(vaultKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            return cipher.doFinal(ciphertext);
        } catch (javax.crypto.AEADBadTagException e) {
            // Enforcement: GCM tag failure must throw AEADBadTagException.
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Vault unseal failed", e);
        }
    }
}
