public class User
{
  private String email;
  private byte[] publicKey;
  private byte[] keyVault;
  private byte[] salt;

  public void setEmail(String email)
  {
    this.email = email;
  }

  public void setPublicKey(byte[] publicKey)
  {
    this.publicKey = publicKey;
  }

  public void setKeyVault(byte[] keyVault)
  {
    this.keyVault = keyVault;
  }

  public void setSalt(byte[] salt)
  {
    this.salt = salt;
  }
}
