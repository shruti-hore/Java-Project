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
    return new byte[32]; // placeholder
  }

  public byte[] deriveVaultKey(byte[] masterKey)
  {
    return new byte[32]; // placeholder
  }

  public byte[][] generateKeyPair()
  {
    return new byte[2][]; // [public, private]
  }

  public byte[] encryptVault(byte[] privateKey, byte[] vaultKey)
  {
    return new byte[0]; // placeholder
  }
}
