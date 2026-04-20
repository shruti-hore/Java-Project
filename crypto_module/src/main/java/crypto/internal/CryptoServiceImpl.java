package crypto.internal;

import crypto.api.CryptoService;

/**
 * Implementation of CryptoService using internal components.
 */
public class CryptoServiceImpl implements CryptoService {
    
    private final MasterKeyDerivation masterDerivation = new MasterKeyDerivation();
    private final SubKeyDerivation subDerivation = new SubKeyDerivation();

    @Override
    public byte[] deriveMasterKey(char[] password, byte[] salt) {
        return masterDerivation.derive(password, salt);
    }

    @Override
    public byte[] deriveSubKey(byte[] masterKey, String info) {
        return subDerivation.derive(masterKey, info);
    }
}
