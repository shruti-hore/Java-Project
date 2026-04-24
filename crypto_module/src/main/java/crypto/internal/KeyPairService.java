package crypto.internal;

import crypto.api.X25519KeyPair;
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
            byte[] privBytes = extractPrivateKeyBytes(kp.getPrivate());
            return new X25519KeyPair(pubBytes, kp.getPublic(), kp.getPrivate(), privBytes);
            
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptoOperationException("X25519 KeyPair generation failed", e);
        }
    }

    /**
     * Extracts raw 32-byte private key material from an X25519 PrivateKey.
     * 
     * @param privateKey The X25519 private key object.
     * @return 32-byte raw private key.
     */
    private byte[] extractPrivateKeyBytes(PrivateKey privateKey) {
        try {
            byte[] encoded = privateKey.getEncoded();
            org.bouncycastle.asn1.pkcs.PrivateKeyInfo pki = org.bouncycastle.asn1.pkcs.PrivateKeyInfo.getInstance(encoded);
            return org.bouncycastle.asn1.ASN1OctetString.getInstance(pki.parsePrivateKey()).getOctets();
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Could not extract raw bytes from private key", e);
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

    /**
     * Reconstructs an X25519 PrivateKey from its raw 32-byte representation.
     * 
     * @param rawBytes 32-byte raw private key.
     * @return PrivateKey object.
     */
    public PrivateKey loadPrivateKey(byte[] rawBytes) {
        if (rawBytes == null || rawBytes.length != 32) {
            throw new IllegalArgumentException("X25519 private key must be exactly 32 bytes");
        }
        try {
            // PKCS#8 header for X25519 (1.3.101.110)
            byte[] pkcs8Header = {
                0x30, 0x2e, 0x02, 0x01, 0x00, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x6e, 0x04, 0x22, 0x04, 0x20
            };
            byte[] encoded = new byte[pkcs8Header.length + rawBytes.length];
            System.arraycopy(pkcs8Header, 0, encoded, 0, pkcs8Header.length);
            System.arraycopy(rawBytes, 0, encoded, pkcs8Header.length, rawBytes.length);

            KeyFactory kf = KeyFactory.getInstance("X25519", "BC");
            return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(encoded));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | java.security.spec.InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid X25519 private key material", e);
        }
    }
}
