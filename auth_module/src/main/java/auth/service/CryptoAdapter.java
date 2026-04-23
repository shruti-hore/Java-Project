import crypto.api.crypto_service;

public class CryptoAdapter
{
  private crypto_service cryptoService;
  
  public CryptoAdapter(crypto_service cryptoService)
  {
    this.cryptoService = cryptoService;
  }

  public byte[] deriveMasterKey(char[] password, byte[] salt)
  {
    // call crypto module
    return cryptoService.deriveMasterKey(password, salt);; // placeholder
  }

  public byte[] deriveVaultKey(byte[] masterKey)
  {
    return cryptoService.deriveSubKey(masterKey, "vault-key"); // placeholder
  }

  public byte[] deriveAuthKey(byte[] masterKey)
  {
    return cryptoService.deriveSubKey(masterKey, "auth-signing-key");
  }

  // Future integration points (to be implemented when exposed in CryptoService)

  public byte[][] generateKeyPair()
  {
    throw new UnsupportedOperationException("KeyPair generation not exposed yet"); // [public, private]
  }

  public byte[] encryptVault(byte[] privateKey, byte[] vaultKey)
  {
    throw new UnsupportedOperationException("Vault encryption not exposed yet"); // placeholder
  }
}
