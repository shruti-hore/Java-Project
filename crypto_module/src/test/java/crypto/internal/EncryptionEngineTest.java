package crypto.internal;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import java.util.Arrays;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

public class EncryptionEngineTest {

    private final EncryptionEngine engine = new EncryptionEngine();
    private final NonceBuilder nonceBuilder = new NonceBuilder();
    private final byte[] teamKey = new byte[32];
    private final byte[] aad = new byte[20];
    private final UUID docUuid = UUID.randomUUID();

    public EncryptionEngineTest() {
        Arrays.fill(teamKey, (byte) 0x11);
        Arrays.fill(aad, (byte) 0x22);
    }

    @Test
    void shouldEncryptAndDecryptCorrectly() throws AEADBadTagException {
        byte[] original = "this-is-padded-to-exactly-32-byt".getBytes(); // 32 bytes
        byte[] plaintext = Arrays.copyOf(original, original.length);
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 100L);

        byte[] ciphertext = engine.encrypt(plaintext, teamKey, nonce, aad);
        
        // Plaintext should be zeroed
        assertThat(plaintext).containsOnly((byte) 0);

        byte[] decrypted = engine.decrypt(ciphertext, teamKey, nonce, aad);
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void shouldThrowExceptionOnTamperedCiphertext() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        ciphertext[5] ^= 0x01; // Tamper

        assertThatThrownBy(() -> engine.decrypt(ciphertext, teamKey, nonce, aad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnWrongKey() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        byte[] wrongKey = new byte[32];
        Arrays.fill(wrongKey, (byte) 0x99);

        assertThatThrownBy(() -> engine.decrypt(ciphertext, wrongKey, nonce, aad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnWrongAAD() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        byte[] wrongAad = new byte[20];
        Arrays.fill(wrongAad, (byte) 0x33);

        assertThatThrownBy(() -> engine.decrypt(ciphertext, teamKey, nonce, wrongAad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnWrongNonce() {
        byte[] original = "padded-content-256".getBytes();
        byte[] nonce = nonceBuilder.build((short) 1, docUuid, 1L);
        byte[] ciphertext = engine.encrypt(original, teamKey, nonce, aad);

        byte[] wrongNonce = nonceBuilder.build((short) 1, docUuid, 2L);

        assertThatThrownBy(() -> engine.decrypt(ciphertext, teamKey, wrongNonce, aad))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void nonceBuilderShouldProduceDifferentNoncesForDifferentCounters() {
        byte[] n1 = nonceBuilder.build((short) 1, docUuid, 0L);
        byte[] n2 = nonceBuilder.build((short) 1, docUuid, 1L);

        assertThat(n1).isNotEqualTo(n2);
        assertThat(n1).hasSize(12);
    }

    @Test
    void shouldValidateKeyLength() {
        byte[] shortKey = new byte[31];
        byte[] nonce = new byte[12];
        assertThatThrownBy(() -> engine.encrypt(new byte[16], shortKey, nonce, aad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team key must be exactly 32 bytes");
    }
}
