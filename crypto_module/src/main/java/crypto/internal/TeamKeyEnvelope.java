package crypto.internal;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Task CRY-07: Team Key Envelope Wrap and Unwrap.
 * Protects an AES team key using ECDH shared secrets and HKDF wrap keys.
 */
public class TeamKeyEnvelope {

    private final EcdhService ecdhService = new EcdhService();
    private final SubKeyDerivation subKeyDerivation = new SubKeyDerivation();
    private final EncryptionEngine encryptionEngine = new EncryptionEngine();
    
    private static final int NONCE_LENGTH = 12;
    private static final String WRAP_INFO = "team-key-wrap";

    /**
     * Wraps a team key for a recipient.
     * 
     * @param teamKey         32-byte team key to protect.
     * @param recipientPubKey Recipient's X25519 public key.
     * @param myPrivateKey    Sender's X25519 private key.
     * @return Sealed envelope: nonce[12] + ciphertext.
     */
    public byte[] wrap(byte[] teamKey, PublicKey recipientPubKey, PrivateKey myPrivateKey) {
        if (teamKey == null || teamKey.length != 32) {
            throw new IllegalArgumentException("Team key must be 32 bytes");
        }

        byte[] sharedSecret = null;
        byte[] wrapKey = null;
        byte[] nonce = new byte[NONCE_LENGTH];
        
        try {
            // 1. Compute Shared Secret
            sharedSecret = ecdhService.computeSharedSecret(myPrivateKey, recipientPubKey);
            
            // 2. Derive Wrap Key via HKDF
            wrapKey = subKeyDerivation.derive(sharedSecret, WRAP_INFO);
            
            // 3. Generate random nonce
            new SecureRandom().nextBytes(nonce);
            
            // 4. Encrypt teamKey (AAD is empty for envelopes)
            byte[] ciphertext = encryptionEngine.encrypt(teamKey, wrapKey, nonce, new byte[0]);
            
            // 5. Construct envelope: nonce + ciphertext
            byte[] envelope = new byte[NONCE_LENGTH + ciphertext.length];
            System.arraycopy(nonce, 0, envelope, 0, NONCE_LENGTH);
            System.arraycopy(ciphertext, 0, envelope, NONCE_LENGTH, ciphertext.length);
            
            return envelope;
            
        } finally {
            // Enforcement: Zeroing all sensitive material
            if (sharedSecret != null) Arrays.fill(sharedSecret, (byte) 0);
            if (wrapKey != null) Arrays.fill(wrapKey, (byte) 0);
            if (teamKey != null) Arrays.fill(teamKey, (byte) 0);
        }
    }

    /**
     * Unwraps a team key envelope.
     * 
     * @param envelope     Sealed envelope from wrap().
     * @param senderPubKey Sender's X25519 public key.
     * @param myPrivateKey Recipient's X25519 private key.
     * @return Original 32-byte team key.
     * @throws javax.crypto.AEADBadTagException if authentication fails.
     */
    public byte[] unwrap(byte[] envelope, PublicKey senderPubKey, PrivateKey myPrivateKey) throws javax.crypto.AEADBadTagException {
        if (envelope == null || envelope.length < NONCE_LENGTH + 16) {
            throw new IllegalArgumentException("Invalid envelope length");
        }

        byte[] sharedSecret = null;
        byte[] wrapKey = null;
        
        try {
            // 1. Compute Shared Secret
            sharedSecret = ecdhService.computeSharedSecret(myPrivateKey, senderPubKey);
            
            // 2. Derive Wrap Key via HKDF
            wrapKey = subKeyDerivation.derive(sharedSecret, WRAP_INFO);
            
            // 3. Extract nonce and ciphertext
            byte[] nonce = Arrays.copyOfRange(envelope, 0, NONCE_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(envelope, NONCE_LENGTH, envelope.length);
            
            // 4. Decrypt (AAD is empty)
            return encryptionEngine.decrypt(ciphertext, wrapKey, nonce, new byte[0]);
            
        } finally {
            // Enforcement: Zeroing intermediate keys
            if (sharedSecret != null) Arrays.fill(sharedSecret, (byte) 0);
            if (wrapKey != null) Arrays.fill(wrapKey, (byte) 0);
        }
    }
}
