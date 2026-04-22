package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.Arrays;

/**
 * Task CRY-04: ECDH shared secret calculation.
 * Computes a secure shared secret between two parties using X25519.
 */
public class EcdhService {

    static {
        // Enforcement: Bouncy Castle provider must be explicitly added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Computes the SHA-256 hashed shared secret using X25519 ECDH.
     * 
     * @param myPrivateKey   Caller's X25519 private key.
     * @param theirPublicKey Counterparty's X25519 public key.
     * @return 32-byte hashed shared secret.
     * @throws IllegalArgumentException if keys are null.
     * @throws RuntimeException if the ECDH operation fails.
     */
    public byte[] computeSharedSecret(PrivateKey myPrivateKey, PublicKey theirPublicKey) {
        if (myPrivateKey == null || theirPublicKey == null) {
            throw new IllegalArgumentException("Keys must not be null");
        }

        byte[] rawSecret = null;
        try {
            // Enforcement: Algorithm must be X25519 via Bouncy Castle
            KeyAgreement agreement = KeyAgreement.getInstance("X25519", "BC");
            agreement.init(myPrivateKey);
            agreement.doPhase(theirPublicKey, true);
            
            rawSecret = agreement.generateSecret();
            
            // Enforcement: Raw ECDH output is not uniformly distributed - always hash it with SHA-256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(rawSecret);

        } catch (InvalidKeyException e) {
            // Propagate for mandatory adversarial tests (e.g., Ed25519 check)
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("X25519 ECDH not supported", e);
        } finally {
            // Enforcement: Raw shared secret zeroed immediately after hashing
            if (rawSecret != null) {
                Arrays.fill(rawSecret, (byte) 0);
            }
        }
    }
}
