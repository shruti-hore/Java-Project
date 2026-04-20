import crypto.api.crypto_service;

public class CryptoAdapter
{
  private crypto_service cryptoService;
  
  public CryptoAdapter(crypto_service cryptoService)
  {
    this.cryptoService = cryptoService;
  }
  // Future methods will call crypto module functions
}
