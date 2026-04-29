package auth.service;

import auth.model.User;
import crypto.api.X25519KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;
import auth.session.SessionState;
import javax.crypto.AEADBadTagException;

public class AuthService {

    private final CryptoAdapter cryptoAdapter;

    public AuthService(CryptoAdapter cryptoAdapter) {
        this.cryptoAdapter = cryptoAdapter;
    }

    public User register(String email, char[] password) {
        byte[] salt = generateSalt();
        byte[] masterKey = null;
        byte[] vaultKey = null;
        X25519KeyPair keyPair = null;
        byte[] privateKeyBytes = null;
        byte[] publicKeyBytes = null;
        byte[] vaultBlob = null;

        try {
            // Step 1: Master Key
            masterKey = cryptoAdapter.deriveMasterKey(password, salt);

            // Step 2: Vault Key
            vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

            // Step 4: Key Pair
            keyPair = cryptoAdapter.generateKeyPair();
            publicKeyBytes = cryptoAdapter.extractPublicKeyBytes(keyPair.publicKey());
            privateKeyBytes = keyPair.privateKeyBytes();

            // Step 5: Vault Encryption
            vaultBlob = cryptoAdapter.encryptVault(privateKeyBytes, vaultKey);

            // Create user object
            User user = new User();
            user.setEmail(email);
            user.setPublicKey(publicKeyBytes);
            user.setKeyVault(vaultBlob);
            user.setSalt(salt);

            return user;

        } catch (Exception e) {
            throw new RuntimeException("Registration failed", e);
        } finally {
            // ✅ Secure cleanup (VERY IMPORTANT)
            if (masterKey != null) Arrays.fill(masterKey, (byte) 0);
            if (vaultKey != null) Arrays.fill(vaultKey, (byte) 0);
            if (privateKeyBytes != null) Arrays.fill(privateKeyBytes, (byte) 0);
            if (password != null) Arrays.fill(password, '0');
        }
    }

    public SessionState login(String userId, String username, char[] password, byte[] salt,
                              byte[] vaultBlob, byte[] publicKeyBytes, String jwt)
            throws AEADBadTagException {
        
        byte[] masterKey = null;
        byte[] vaultKey = null;
        byte[] authKey = null;
        byte[] privateKeyBytes = null;
        java.security.PrivateKey privateKey = null;

        try {
            // Step 1: Master Key
            masterKey = cryptoAdapter.deriveMasterKey(password, salt);

            // Step 2: Vault Key
            vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

            // Step 3: Auth Signing Key
            authKey = cryptoAdapter.deriveAuthKey(masterKey);

            // Step 4: Decrypt Vault
            privateKeyBytes = cryptoAdapter.decryptVault(vaultBlob, vaultKey);
            // AEADBadTagException propagates if password is wrong

            // Step 5: Load Private Key
            privateKey = cryptoAdapter.loadPrivateKey(privateKeyBytes);

            // Step 6: Create SessionState
            SessionState session = new SessionState(userId, username, jwt, authKey, privateKey, publicKeyBytes);
            session.setX25519PublicKey(cryptoAdapter.loadPublicKey(publicKeyBytes));
            return session;

        } finally {
            // Step 7: Zero sensitive material
            if (masterKey != null) Arrays.fill(masterKey, (byte) 0);
            if (vaultKey != null) Arrays.fill(vaultKey, (byte) 0);
            if (privateKeyBytes != null) Arrays.fill(privateKeyBytes, (byte) 0);
            // authKey is NOT zeroed here as it is stored in SessionState
        }
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
