import auth.model.User;
import java.security.SecureRandom;

public class AuthService {

    private CryptoAdapter cryptoAdapter;

    public AuthService(CryptoAdapter cryptoAdapter) {
        this.cryptoAdapter = cryptoAdapter;
    }

    public User register(String email, char[] password) {

        byte[] salt = generateSalt();

        // Step 1: Master Key
        byte[] masterKey = cryptoAdapter.deriveMasterKey(password, salt);

        // Step 2: Vault Key
        byte[] vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

        // Step 3: Auth Signing Key (for future JWT)
        byte[] authKey = cryptoAdapter.deriveAuthKey(masterKey);

        // TODO: Replace with X25519 key pair generation from CryptoService
        byte[] publicKey = new byte[32];

        // TODO: Replace with AES-GCM vault encryption using CryptoService
        byte[] encryptedVault = new byte[0];

        // Create user object
        User user = new User();
        user.setEmail(email);
        user.setPublicKey(publicKey);
        user.setKeyVault(encryptedVault);
        user.setSalt(salt);

        // ✅ Secure cleanup (VERY IMPORTANT)
        zeroArray(masterKey);
        zeroArray(vaultKey);
        zeroArray(authKey);
        zeroCharArray(password);

        return user;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private void zeroArray(byte[] arr) {
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = 0;
            }
        }
    }

    private void zeroCharArray(char[] arr) {
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = 0;
            }
        }
    }
}
