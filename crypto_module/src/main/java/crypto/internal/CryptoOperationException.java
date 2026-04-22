package crypto.internal;

/**
 * Base exception for all unrecoverable cryptographic operation failures.
 * This replaces generic RuntimeException throws across the module.
 */
public class CryptoOperationException extends RuntimeException {
    
    public CryptoOperationException(String message) {
        super(message);
    }

    public CryptoOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
