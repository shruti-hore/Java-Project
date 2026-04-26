package crypto.internal;

import crypto.api.CryptoService;
import crypto.api.X25519KeyPair;

import javax.crypto.AEADBadTagException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

/**
 * Implementation of CryptoService using internal components.
 */
public class CryptoServiceImpl implements CryptoService {
    
    private final MasterKeyDerivation masterDerivation = new MasterKeyDerivation();
    private final SubKeyDerivation subDerivation = new SubKeyDerivation();
    private final KeyPairService keyPairService = new KeyPairService();
    private final VaultService vaultService = new VaultService();
    private final TeamKeyEnvelope teamKeyEnvelope = new TeamKeyEnvelope();
    private final EncryptionEngine encryptionEngine = new EncryptionEngine();
    private final NonceBuilder nonceBuilder = new NonceBuilder();
    private final PaddingUtil paddingUtil = new PaddingUtil();
    private final FingerprintService fingerprintService = new FingerprintService();

    @Override
    public byte[] deriveMasterKey(char[] password, byte[] salt) {
        return masterDerivation.derive(password, salt);
    }

    @Override
    public byte[] deriveSubKey(byte[] masterKey, String info) {
        return subDerivation.derive(masterKey, info);
    }

    @Override
    public X25519KeyPair generateKeyPair() {
        return keyPairService.generateKeyPair();
    }

    @Override
    public byte[] extractPublicKeyBytes(PublicKey publicKey) {
        return keyPairService.extractPublicKeyBytes(publicKey);
    }

    @Override
    public PublicKey loadPublicKey(byte[] rawBytes) {
        return keyPairService.loadPublicKey(rawBytes);
    }

    @Override
    public PrivateKey loadPrivateKey(byte[] rawBytes) {
        return keyPairService.loadPrivateKey(rawBytes);
    }

    @Override
    public byte[] sealVault(byte[] privateKeyBytes, byte[] vaultKey) {
        return vaultService.seal(privateKeyBytes, vaultKey);
    }

    @Override
    public byte[] unsealVault(byte[] vaultBlob, byte[] vaultKey) throws AEADBadTagException {
        return vaultService.unseal(vaultBlob, vaultKey);
    }

    @Override
    public byte[] wrapTeamKey(byte[] teamKey, PublicKey recipientPublicKey, PrivateKey myPrivateKey) {
        return teamKeyEnvelope.wrap(teamKey, recipientPublicKey, myPrivateKey);
    }

    @Override
    public byte[] unwrapTeamKey(byte[] envelope, PublicKey senderPublicKey, PrivateKey myPrivateKey) throws AEADBadTagException {
        return teamKeyEnvelope.unwrap(envelope, senderPublicKey, myPrivateKey);
    }

    @Override
    public byte[] encryptDocument(byte[] paddedPlaintext, byte[] teamKey, byte[] nonce, byte[] aad) {
        return encryptionEngine.encrypt(paddedPlaintext, teamKey, nonce, aad);
    }

    @Override
    public byte[] decryptDocument(byte[] ciphertext, byte[] teamKey, byte[] nonce, byte[] aad) throws AEADBadTagException {
        return encryptionEngine.decrypt(ciphertext, teamKey, nonce, aad);
    }

    @Override
    public byte[] buildNonce(short teamKeyVersion, UUID docUuid, long counterValue) {
        return nonceBuilder.build(teamKeyVersion, docUuid, counterValue);
    }

    @Override
    public byte[] pad(byte[] plaintext) {
        return paddingUtil.pad(plaintext);
    }

    @Override
    public byte[] unpad(byte[] padded) {
        return paddingUtil.unpad(padded);
    }

    @Override
    public String generateFingerprint(byte[] publicKeyA, byte[] publicKeyB) {
        return fingerprintService.generate(publicKeyA, publicKeyB);
    }

    @Override
    public String computeStableEmailHash(String email) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.toLowerCase().trim().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String computeEmailHmac(String email, byte[] authSigningKey) {
        org.bouncycastle.crypto.macs.HMac hmac = new org.bouncycastle.crypto.macs.HMac(new org.bouncycastle.crypto.digests.SHA256Digest());
        hmac.init(new org.bouncycastle.crypto.params.KeyParameter(authSigningKey));
        byte[] emailBytes = email.toLowerCase().trim().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        hmac.update(emailBytes, 0, emailBytes.length);
        byte[] result = new byte[hmac.getMacSize()];
        hmac.doFinal(result, 0);
        return java.util.Base64.getEncoder().encodeToString(result);
    }

    @Override
    public String computeMasterKeyProof(byte[] masterKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(masterKey);
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
