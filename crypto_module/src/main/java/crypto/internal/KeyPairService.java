package crypto.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.interfaces.XDHPublicKey;
import org.bouncycastle.jcajce.spec.RawEncodedKeySpec;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * Task CRY-03: X25519 Key Pair Generation.
 * Provides services to generate and manage X25519 keys for ECDH.
 */
public class KeyPairService {

    static {
        // Enforcement: Bouncy Castle provider must be explicitly added
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Generates a fresh random X25519 key pair.
     * 
     * @return X25519KeyPair containing the 32-byte public key and the PrivateKey object.
     */
    public X25519KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519", "BC");
            KeyPair kp = kpg.generateKeyPair();
            
            byte[] pubBytes = extractPublicKeyBytes(kp.getPublic());
            return new X25519KeyPair(pubBytes, kp.getPrivate());
            
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptoOperationException("X25519 KeyPair generation failed", e);
        }
    }

    /**
     * Extracts the raw 32-byte array from an X25519 public key.
     * 
     * @param publicKey The X25519 public key object.
     * @return 32-byte raw public key.
     * @throws IllegalArgumentException if the key is not X25519.
     */
    public byte[] extractPublicKeyBytes(PublicKey publicKey) {
        // Enforcement: Must be extractable as raw 32-byte array, not DER format
        if (publicKey instanceof XDHPublicKey xdhKey) {
            return xdhKey.getUEncoding();
        }
        throw new IllegalArgumentException("Provided key is not a valid X25519 public key");
    }

    /**
     * Reconstructs an X25519 PublicKey from its raw 32-byte representation.
     * 
     * @param rawBytes 32-byte raw public key.
     * @return PublicKey object for use in ECDH.
     * @throws IllegalArgumentException if bytes are invalid or wrong length.
     */
    public PublicKey loadPublicKey(byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length != 32) {
            throw new IllegalArgumentException("X25519 public key must be exactly 32 bytes");
        }

        // Enforcement: Reject all-zero public keys (invalid point)
        boolean allZero = true;
        for (byte b : rawBytes) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            throw new IllegalArgumentException("Invalid X25519 public key: all zeros");
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("X25519", "BC");
            return kf.generatePublic(new RawEncodedKeySpec(rawBytes));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid X25519 public key material", e);
        }
    }
}
