package crypto.internal;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Container for X25519 key material.
 * 
 * @param publicKeyBytes Raw 32-byte public key.
 * @param privateKey      The private key object (held in RAM).
 */
public record X25519KeyPair(byte[] publicKeyBytes, PrivateKey privateKey) {
}
