import auth.model.User;

public class AuthService
{
  private CryptoAdapter cryptoAdapter;

  public AuthService(CryptoAdapter cryptoAdapter)
  {
    this.cryptoAdapter = cryptoAdapter;
  }

  public User register(String email, char[] password)
  {
    // Step 1: Generate salt
    byte[] salt = generateSalt();

    // Step 2: Derive master key (via crypto module)
    byte[] masterKey = cryptoAdapter.deriveMasterKey(password, salt);

    // Step 3: Derive vault key
    byte[] vaultKey = cryptoAdapter.deriveVaultKey(masterKey);

    // Step 4: Generate key pair
    byte[][] keyPair = cryptoAdapter.generateKeyPair();

    byte[] publicKey = keyPair[0];
    byte[] privateKey = keyPair[1];

    // Step 5: Encrypt private key (vault)
    byte[] encryptedVault = cryptoAdapter.encryptVault(privateKey, vaultKey);

    // Step 6: Create user object
    User user = new User();
    user.setEmail(email);
    user.setPublicKey(publicKey);
    user.setKeyVault(encryptedVault);
    user.setSalt(salt);

    // Cleanup sensitive data
    zeroArray(masterKey);
    zeroArray(privateKey);

    return user;
  }

  private byte[] generateSalt()
  {
    return new byte[16]; // placeholder
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
