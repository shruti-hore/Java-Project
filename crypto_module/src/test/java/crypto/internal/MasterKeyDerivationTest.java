package crypto.internal;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class MasterKeyDerivationTest {

    private final MasterKeyDerivation derivation = new MasterKeyDerivation();

    @Test
    void shouldProduceDeterministicOutput() {
        char[] password = "securePassword123".toCharArray();
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        byte[] key1 = derivation.derive(password.clone(), salt);
        byte[] key2 = derivation.derive(password.clone(), salt);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1).hasSize(32);
    }

    @Test
    void shouldProduceDifferentOutputForDifferentPasswords() {
        byte[] salt = new byte[16];
        Arrays.fill(salt, (byte) 1);

        byte[] key1 = derivation.derive("password1".toCharArray(), salt);
        byte[] key2 = derivation.derive("password2".toCharArray(), salt);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldProduceDifferentOutputForDifferentSalts() {
        char[] password = "securePassword".toCharArray();
        byte[] salt1 = new byte[16];
        byte[] salt2 = new byte[16];
        salt2[0] = 1;

        byte[] key1 = derivation.derive(password.clone(), salt1);
        byte[] key2 = derivation.derive(password.clone(), salt2);

        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void shouldZeroPasswordAfterDerivation() {
        char[] password = "sensitivePassword".toCharArray();
        byte[] salt = new byte[16];

        derivation.derive(password, salt);

        char[] expectedZeroes = new char[password.length];
        Arrays.fill(expectedZeroes, (char) 0);

        assertThat(password).containsOnly((char) 0);
        assertThat(password).isEqualTo(expectedZeroes);
    }

    @Test
    void shouldThrowExceptionForInvalidSaltLength() {
        char[] password = "password".toCharArray();
        byte[] shortSalt = new byte[15];

        assertThatThrownBy(() -> derivation.derive(password, shortSalt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Salt must be exactly 16 bytes");
    }

    /**
     * Mandatory Adversarial Test: m=4096 parameters must fail.
     * Since parameters are hardcoded as constants in the implementation to prevent 
     * lowering, this test verifies that the hardcoded values meet the floor.
     */
    @Test
    void shouldVerifyArgon2idParametersMeetHardFloor() {
        // This is a meta-test to ensure the implementation hasn't lowered the floor.
        // In a real scenario, if parameters were passed in, we would test rejection.
        // Here we verify the internal constants are compliant.
        try {
            var fieldM = MasterKeyDerivation.class.getDeclaredField("MEMORY_COST");
            fieldM.setAccessible(true);
            int m = (int) fieldM.get(null);
            assertThat(m).isGreaterThanOrEqualTo(65536);

            var fieldT = MasterKeyDerivation.class.getDeclaredField("TIME_COST");
            fieldT.setAccessible(true);
            int t = (int) fieldT.get(null);
            assertThat(t).isGreaterThanOrEqualTo(3);

            var fieldP = MasterKeyDerivation.class.getDeclaredField("PARALLELISM");
            fieldP.setAccessible(true);
            int p = (int) fieldP.get(null);
            assertThat(p).isGreaterThanOrEqualTo(4);
        } catch (Exception e) {
            fail("Could not verify Argon2id parameters: " + e.getMessage());
        }
    }
}
