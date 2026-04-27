package auth.service;

import auth.model.User;
import auth.repository.UserRepository;
import auth.session.SessionState;
import crypto.api.X25519KeyPair;

import javax.crypto.AEADBadTagException;
import java.security.SecureRandom;
import java.util.Arrays;

/*
 * SECURITY DESIGN:
 * - Password is never stored or transmitted
 * - Master key derived using Argon2
 * - Vault key derived using HKDF
 * - Private keys encrypted using AES-GCM
 * - Server stores only encrypted data (zero-knowledge)
 * - Session stores authKey for HMAC-based token signing
 */
public class AuthService 
{
    private final CryptoAdapter cryptoAdapter;
    private final UserRepository userRepository;

    public AuthService(CryptoAdapter cryptoAdapter, UserRepository userRepository) 
    {
        this.cryptoAdapter = cryptoAdapter;
        this.userRepository = userRepository;
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

        // Duplicate check
        if (userRepository.exists(email)) 
        {
            throw new RuntimeException("User already exists");
        }

        byte[] salt = generateSecureSalt();
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

            // Step 4: Generate key pair
            X25519KeyPair keyPair = cryptoAdapter.generateKeyPair();

            byte[] publicKeyBytes = cryptoAdapter.extractPublicKeyBytes(keyPair.publicKey());

            privateKeyBytes = keyPair.privateKeyBytes();

            // Step 5: Encrypt private key (vault)
            byte[] vaultBlob = cryptoAdapter.encryptVault(privateKeyBytes, vaultKey);

            // Step 6: Create user
            User user = new User();
            user.setEmail(email);
            user.setPublicKey(publicKeyBytes);
            user.setKeyVault(vaultBlob);
            user.setSalt(salt);

            // Save user
            userRepository.save(user);

            return user;

        } 
        catch (Exception e) 
        {
            throw new RuntimeException("Registration failed", e);
        } 
        finally 
        {
            // ===== CLEANUP =====
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
    public SessionState login(String email, char[] password)
            throws AEADBadTagException 
    {
        User storedUser = userRepository.findByEmail(email);

        if (storedUser == null) 
        {
            throw new IllegalArgumentException("Invalid email or user does not exist");
        }

        byte[] salt = storedUser.getSalt();
        byte[] vaultBlob = storedUser.getKeyVault();
        byte[] publicKeyBytes = storedUser.getPublicKey();

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

            // Step 5: Load private key
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
        }
    }

    // =========================
    // LOGOUT
    // =========================
    public void logout(SessionState session) 
    {
        if (session != null) 
        {
            session.zero();
        }
    }

    // =========================
    // TOKEN VALIDATION
    // =========================
    public boolean validateToken(String token, SessionState session) 
    {
        if (token == null || session == null) return false;

        TokenService tokenService = new TokenService();
        return tokenService.validateToken(token, session.getAuthSigningKey());
    }

    // =========================
    // UTIL
    // =========================
    private byte[] generateSecureSalt() 
    {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
