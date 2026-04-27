package auth.service;

import auth.model.User;
import auth.session.SessionState;
import crypto.api.X25519KeyPair;

import javax.crypto.AEADBadTagException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AuthService 
{
    private final CryptoAdapter cryptoAdapter;

    public AuthService(CryptoAdapter cryptoAdapter) 
    {
        this.cryptoAdapter = cryptoAdapter;
    }

    // =========================
    // REGISTER
    // =========================
    public User register(String email, char[] password) 
    {
        if (email == null || password == null) 
        {
            throw new IllegalArgumentException("Email and password must not be null");
        }

        byte[] salt = generateSalt();
        byte[] masterKey = null;
        byte[] vaultKey = null;
        byte[] authKey = null;
        byte[] privateKeyBytes = null;

        try 
            {
            // Step 1: Master Key (Argon2)
            masterKey = cryptoAdapter.deriveMasterKey(password, salt);

            // Step 2: Vault Key (HKDF)
            vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

            // Step 3: Auth Signing Key (HKDF)
            authKey = cryptoAdapter.deriveAuthKey(masterKey);

            // Step 4: Generate X25519 key pair
            X25519KeyPair keyPair = cryptoAdapter.generateKeyPair();

            byte[] publicKeyBytes = cryptoAdapter.extractPublicKeyBytes(keyPair.publicKey());

            privateKeyBytes = keyPair.privateKeyBytes();

            // Step 5: Encrypt private key into vault (AES-GCM)
            byte[] vaultBlob = cryptoAdapter.encryptVault(privateKeyBytes, vaultKey);

            // Step 6: Build user object
            User user = new User();
            user.setEmail(email);
            user.setPublicKey(publicKeyBytes);
            user.setKeyVault(vaultBlob);
            user.setSalt(salt);

            return user;

        } 
        catch (Exception e) 
        {
            throw new RuntimeException("Registration failed", e);
        } 
        finally 
        {
            // ===== CRITICAL SECURITY CLEANUP =====
            if (masterKey != null) Arrays.fill(masterKey, (byte) 0);
            if (vaultKey != null) Arrays.fill(vaultKey, (byte) 0);
            if (authKey != null) Arrays.fill(authKey, (byte) 0);
            if (privateKeyBytes != null) Arrays.fill(privateKeyBytes, (byte) 0);
            if (password != null) Arrays.fill(password, '\0');
        }
    }

    // =========================
    // LOGIN
    // =========================
    public SessionState login(String email,
                              char[] password,
                              byte[] salt,
                              byte[] vaultBlob,
                              byte[] publicKeyBytes)
            throws AEADBadTagException 
    {
        byte[] masterKey = null;
        byte[] vaultKey = null;
        byte[] authKey = null;
        byte[] privateKeyBytes = null;

        try 
        {
            // Step 1: Master Key
            masterKey = cryptoAdapter.deriveMasterKey(password, salt);

            // Step 2: Vault Key
            vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

            // Step 3: Auth Signing Key
            authKey = cryptoAdapter.deriveAuthKey(masterKey);

            // Step 4: Decrypt vault
            privateKeyBytes = cryptoAdapter.decryptVault(vaultBlob, vaultKey);
            // Wrong password → AEADBadTagException

            // Step 5: Load private key object
            var privateKey = cryptoAdapter.loadPrivateKey(privateKeyBytes);

            // Step 6: Create session
            SessionState session = new SessionState(email, authKey, privateKey, publicKeyBytes);

            // Step 7: Generate token
            TokenService tokenService = new TokenService();
            String token = tokenService.generateToken(email, authKey);

            // Step 8: Attach token
            session.setToken(token);

            return session;

        } 
        finally 
        {
            // ===== CLEANUP =====
            if (masterKey != null) Arrays.fill(masterKey, (byte) 0);
            if (vaultKey != null) Arrays.fill(vaultKey, (byte) 0);
            if (privateKeyBytes != null) Arrays.fill(privateKeyBytes, (byte) 0);
            if (password != null) Arrays.fill(password, '\0');
            // authKey NOT cleared (stored in session)
        }
    }

    // =========================
    // UTIL
    // =========================
    private byte[] generateSalt() 
    {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
