package crypto.api;

public interface CryptoService {

    /**
     * Derives a 32-byte master key from the user's password and database salt.
     * * @param password The user's plaintext password.
     * @param salt The Argon2id salt retrieved from the database.
     * @return 32-byte Master Key.
     */
    byte[] deriveMasterKey(char[] password, byte[] salt);

    /**
     * Derives a context-specific sub-key from the master key.
     * @param masterKey The 32-byte master key.
     * @param info The context label (e.g. "vault-key").
     * @return 32-byte Sub-Key.
     */
    byte[] deriveSubKey(byte[] masterKey, String info);

}