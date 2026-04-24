package crypto.api;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;
import javax.crypto.AEADBadTagException;

public interface CryptoService {

    /**
     * Derives a 32-byte master key from the user's password and database salt.
     * @param password The user's plaintext password.
     * @param salt The Argon2id salt retrieved from the database.
     * @return 32-byte Master Key.
     */
    byte[] deriveMasterKey(char[] password, byte[] salt);

    /**
     * Derives a context-specific sub-key from the master key.
     * @param masterKey The 32-byte master key.
     * @param info The context label (e.g. "vault-key").
     * @return 32-byte Sub-Key.
     */
    byte[] deriveSubKey(byte[] masterKey, String info);

    X25519KeyPair generateKeyPair();

    byte[] extractPublicKeyBytes(PublicKey publicKey);
    PublicKey loadPublicKey(byte[] rawBytes);
    PrivateKey loadPrivateKey(byte[] rawBytes);

    byte[] sealVault(byte[] privateKeyBytes, byte[] vaultKey);
    byte[] unsealVault(byte[] vaultBlob, byte[] vaultKey) throws AEADBadTagException;

    byte[] wrapTeamKey(byte[] teamKey, PublicKey recipientPublicKey, PrivateKey myPrivateKey);
    byte[] unwrapTeamKey(byte[] envelope, PublicKey senderPublicKey, PrivateKey myPrivateKey) throws AEADBadTagException;

    byte[] encryptDocument(byte[] paddedPlaintext, byte[] teamKey, byte[] nonce, byte[] aad);
    byte[] decryptDocument(byte[] ciphertext, byte[] teamKey, byte[] nonce, byte[] aad) throws AEADBadTagException;

    byte[] buildNonce(short teamKeyVersion, UUID docUuid, long counterValue);

    byte[] pad(byte[] plaintext);
    byte[] unpad(byte[] padded);

    String generateFingerprint(byte[] publicKeyA, byte[] publicKeyB);
}