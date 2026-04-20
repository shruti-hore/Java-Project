package crypto.internal;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class SubKeyDerivationTest {

    private final SubKeyDerivation derivation = new SubKeyDerivation();

    @Test
    void shouldProduceDifferentKeysForDifferentContexts() {
        byte[] masterKey = new byte[32];
        for (int i = 0; i < 32; i++) masterKey[i] = (byte) i;

        byte[] vaultKey = derivation.derive(masterKey, "vault-key");
        byte[] authKey = derivation.derive(masterKey, "auth-signing-key");

        assertThat(vaultKey).isNotEqualTo(authKey);
        assertThat(vaultKey).hasSize(32);
        assertThat(authKey).hasSize(32);
    }

    @Test
    void shouldBeDeterministic() {
        byte[] masterKey = new byte[32];
        String info = "vault-key";

        byte[] key1 = derivation.derive(masterKey, info);
        byte[] key2 = derivation.derive(masterKey, info);

        assertThat(key1).isEqualTo(key2);
    }

    @Test
    void shouldProduceDifferentKeysForDifferentMasterKeys() {
        byte[] masterKey1 = new byte[32];
        byte[] masterKey2 = new byte[32];
        masterKey2[0] = 1;
        String info = "vault-key";

        byte[] key1 = derivation.derive(masterKey1, info);
        byte[] key2 = derivation.derive(masterKey2, info);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldThrowExceptionForUnknownInfoStrings() {
        byte[] masterKey = new byte[32];

        // Underscore instead of hyphen
        assertThatThrownBy(() -> derivation.derive(masterKey, "vault_key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown HKDF context");

        // Empty string
        assertThatThrownBy(() -> derivation.derive(masterKey, ""))
                .isInstanceOf(IllegalArgumentException.class);

        // Null info
        assertThatThrownBy(() -> derivation.derive(masterKey, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidMasterKeyLength() {
        assertThatThrownBy(() -> derivation.derive(new byte[31], "vault-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Master key must be exactly 32 bytes");
    }
}
