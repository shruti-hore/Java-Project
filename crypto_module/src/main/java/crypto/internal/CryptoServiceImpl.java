package crypto.internal;

import crypto.api.CryptoService;

/**
 * Implementation of CryptoService using internal components.
 */
public class CryptoServiceImpl implements CryptoService {
    
    private final MasterKeyDerivation derivation = new MasterKeyDerivation();

    @Override
    public byte[] deriveMasterKey(char[] password, byte[] salt) {
        return derivation.derive(password, salt);
    }
}
