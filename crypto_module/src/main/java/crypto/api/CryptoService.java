package crypto.api;

public interface CryptoService {

    /**
     * Derives a 32-byte master key from the user's password and database salt.
     * * @param password The user's plaintext password.
     * @param salt The Argon2id salt retrieved from the database.
     * @return 32-byte Master Key.
     */
    byte[] deriveMasterKey(char[] password, byte[] salt);

}