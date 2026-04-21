package crypto.internal;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class FingerprintServiceTest {

    private final FingerprintService service = new FingerprintService();

    @Test
    void shouldGenerateCommutativeFingerprints() {
        byte[] keyA = new byte[32];
        byte[] keyB = new byte[32];
        Arrays.fill(keyA, (byte) 1);
        Arrays.fill(keyB, (byte) 2);

        String fp1 = service.generate(keyA, keyB);
        String fp2 = service.generate(keyB, keyA);

        assertThat(fp1).isEqualTo(fp2);
        assertThat(fp1.split(" ")).hasSize(6);
    }

    @Test
    void shouldHandleSameKeys() {
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 0xAA);

        String fp = service.generate(key, key);
        assertThat(fp.split(" ")).hasSize(6);
    }

    @Test
    void shouldProduceDifferentFingerprintsForDifferentKeys() {
        byte[] key1 = new byte[32];
        byte[] key2 = new byte[32];
        byte[] key3 = new byte[32];
        Arrays.fill(key1, (byte) 1);
        Arrays.fill(key2, (byte) 2);
        Arrays.fill(key3, (byte) 3);

        String fp12 = service.generate(key1, key2);
        String fp13 = service.generate(key1, key3);

        assertThat(fp12).isNotEqualTo(fp13);
    }

    @Test
    void shouldFailOnInvalidKeyLengths() {
        byte[] valid = new byte[32];
        byte[] invalid = new byte[31];

        assertThatThrownBy(() -> service.generate(valid, invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void shouldFailOnNullKeys() {
        byte[] valid = new byte[32];
        assertThatThrownBy(() -> service.generate(valid, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldLoadWordlistCorrectly() {
        // This implicitly checks the wordlist length in constructor
        assertThatNoException().isThrownBy(FingerprintService::new);
    }
}
