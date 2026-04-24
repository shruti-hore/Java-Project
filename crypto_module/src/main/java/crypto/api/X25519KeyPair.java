package crypto.api;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Container for X25519 key material.
 * 
 * @param publicKeyBytes  Raw 32-byte public key.
 * @param publicKey       The public key object.
 * @param privateKey      The private key object.
 * @param privateKeyBytes Raw 32-byte private key material.
 */
public record X25519KeyPair(byte[] publicKeyBytes, PublicKey publicKey, PrivateKey privateKey, byte[] privateKeyBytes) {
}
