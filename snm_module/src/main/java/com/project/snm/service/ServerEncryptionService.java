package com.project.snm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ServerEncryptionService {

    private final byte[] serverKey;

    public ServerEncryptionService(@Value("${server.encryption-key:default-32-byte-long-key-for-aes-256}") String key) {
        // Ensure 32 bytes for AES-256
        byte[] k = key.getBytes();
        this.serverKey = new byte[32];
        System.arraycopy(k, 0, this.serverKey, 0, Math.min(k.length, 32));
    }

    public String encrypt(String plaintext) {
        try {
            byte[] nonce = new byte[12];
            new SecureRandom().nextBytes(nonce);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(serverKey, "AES"), spec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            byte[] combined = new byte[nonce.length + ciphertext.length];
            System.arraycopy(nonce, 0, combined, 0, nonce.length);
            System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Server-side encryption failed", e);
        }
    }

    public String decrypt(String base64) {
        try {
            byte[] combined = Base64.getDecoder().decode(base64);
            byte[] nonce = new byte[12];
            System.arraycopy(combined, 0, nonce, 0, 12);
            byte[] ciphertext = new byte[combined.length - 12];
            System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(serverKey, "AES"), spec);
            
            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new RuntimeException("Server-side decryption failed", e);
        }
    }
}
