import auth.model.User;
import java.security.SecureRandom;

public class AuthService
{
  private CryptoAdapter cryptoAdapter;

  public AuthService(CryptoAdapter cryptoAdapter)
  {
    this.cryptoAdapter = cryptoAdapter;
  }

  public User register(String email, char[] password)
  {
    byte[] salt = generateSalt();

    // REAL CALL
    byte[] masterKey = cryptoAdapter.deriveMasterKey(password, salt);

    // REAL CALL
    byte[] vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

    // Placeholder until crypto exposes more APIs
    byte[] publicKey = new byte[32];
    byte[] encryptedVault = new byte[0];

    // Create user object
    User user = new User();
    user.setEmail(email);
    user.setPublicKey(publicKey);
    user.setKeyVault(encryptedVault);
    user.setSalt(salt);

    // Cleanup sensitive data
    zeroArray(masterKey);

    return user;
  }

  private byte[] generateSalt()
  {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return salt;
  }

  private void zeroArray(byte[] arr)
  {
    if (arr != null)
    {
      for (int i = 0; i < arr.length; i++)
        arr[i] = 0;
    }
  }
}
