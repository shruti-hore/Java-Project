package auth.service;

import crypto.api.CryptoService;
import crypto.api.X25519KeyPair;

import javax.crypto.AEADBadTagException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

public class CryptoAdapter {
    
    private final CryptoService cryptoService;

    public CryptoAdapter(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public byte[] deriveMasterKey(char[] password, byte[] salt) {
        return cryptoService.deriveMasterKey(password, salt);
    }

    public byte[] deriveVaultKey(byte[] masterKey) {
        return cryptoService.deriveSubKey(masterKey, "vault-key");
    }

    public byte[] deriveAuthKey(byte[] masterKey) {
        return cryptoService.deriveSubKey(masterKey, "auth-signing-key");
    }

    public X25519KeyPair generateKeyPair() {
        return cryptoService.generateKeyPair();
    }

    public byte[] extractPublicKeyBytes(PublicKey publicKey) {
        return cryptoService.extractPublicKeyBytes(publicKey);
    }

    public PublicKey loadPublicKey(byte[] rawBytes) {
        return cryptoService.loadPublicKey(rawBytes);
    }

    public PrivateKey loadPrivateKey(byte[] rawBytes) {
        return cryptoService.loadPrivateKey(rawBytes);
    }

    public byte[] encryptVault(byte[] privateKeyBytes, byte[] vaultKey) {
        return cryptoService.sealVault(privateKeyBytes, vaultKey);
    }

    public byte[] decryptVault(byte[] vaultBlob, byte[] vaultKey) throws AEADBadTagException {
        return cryptoService.unsealVault(vaultBlob, vaultKey);
    }

    public byte[] wrapTeamKey(byte[] teamKey, PublicKey recipientPublicKey, PrivateKey myPrivateKey) {
        return cryptoService.wrapTeamKey(teamKey, recipientPublicKey, myPrivateKey);
    }

    public byte[] unwrapTeamKey(byte[] envelope, PublicKey senderPublicKey, PrivateKey myPrivateKey) throws AEADBadTagException {
        return cryptoService.unwrapTeamKey(envelope, senderPublicKey, myPrivateKey);
    }

    public byte[] encryptDocument(byte[] padded, byte[] teamKey, byte[] nonce, byte[] aad) {
        return cryptoService.encryptDocument(padded, teamKey, nonce, aad);
    }

    public byte[] decryptDocument(byte[] ciphertext, byte[] teamKey, byte[] nonce, byte[] aad) throws AEADBadTagException {
        return cryptoService.decryptDocument(ciphertext, teamKey, nonce, aad);
    }

    public byte[] buildNonce(short keyVersion, UUID docUuid, long counter) {
        return cryptoService.buildNonce(keyVersion, docUuid, counter);
    }

    public byte[] pad(byte[] plaintext) {
        return cryptoService.pad(plaintext);
    }

    public byte[] unpad(byte[] padded) {
        return cryptoService.unpad(padded);
    }

    public String generateFingerprint(byte[] publicKeyA, byte[] publicKeyB) {
        return cryptoService.generateFingerprint(publicKeyA, publicKeyB);
    }
}
