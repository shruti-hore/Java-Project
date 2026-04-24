package crypto.internal;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class VaultServiceTest {

    private final VaultService vaultService = new VaultService();
    private final byte[] vaultKey = new byte[32]; // All zeros for test, but correct length

    public VaultServiceTest() {
        Arrays.fill(vaultKey, (byte) 0x42);
    }

    @Test
    void shouldSealAndUnsealCorrectly() throws AEADBadTagException {
        byte[] originalData = "secret-private-key-material".getBytes();
        byte[] dataToSeal = Arrays.copyOf(originalData, originalData.length);

        byte[] blob = vaultService.seal(dataToSeal, Arrays.copyOf(vaultKey, 32));
        
        // original dataToSeal should be zeroed
        assertThat(dataToSeal).containsOnly((byte) 0);

        byte[] unsealed = vaultService.unseal(blob, Arrays.copyOf(vaultKey, 32));
        assertThat(unsealed).isEqualTo(originalData);
    }

    @Test
    void shouldHaveCorrectBlobLength() {
        byte[] originalData = new byte[100];
        byte[] blob = vaultService.seal(originalData, Arrays.copyOf(vaultKey, 32));
        
        // Length = 12 (nonce) + 100 (plaintext) + 16 (GCM tag) = 128
        assertThat(blob).hasSize(12 + 100 + 16);
    }

    @Test
    void shouldThrowExceptionOnWrongKey() {
        byte[] originalData = "secret".getBytes();
        byte[] blob = vaultService.seal(originalData, Arrays.copyOf(vaultKey, 32));

        byte[] wrongKey = new byte[32];
        Arrays.fill(wrongKey, (byte) 0x99);

        assertThatThrownBy(() -> vaultService.unseal(blob, wrongKey))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnTamperedCiphertext() {
        byte[] originalData = "secret".getBytes();
        byte[] blob = vaultService.seal(originalData, Arrays.copyOf(vaultKey, 32));

        // Tamper with the ciphertext (starts at index 12)
        blob[15] ^= 0x01;

        assertThatThrownBy(() -> vaultService.unseal(blob, Arrays.copyOf(vaultKey, 32)))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnTamperedNonce() {
        byte[] originalData = "secret".getBytes();
        byte[] blob = vaultService.seal(originalData, Arrays.copyOf(vaultKey, 32));

        // Tamper with the nonce (indices 0-11)
        blob[5] ^= 0x01;

        assertThatThrownBy(() -> vaultService.unseal(blob, Arrays.copyOf(vaultKey, 32)))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldProduceDifferentBlobsForSameInput() {
        byte[] originalData = "secret".getBytes();
        
        byte[] blob1 = vaultService.seal(Arrays.copyOf(originalData, originalData.length), Arrays.copyOf(vaultKey, 32));
        byte[] blob2 = vaultService.seal(Arrays.copyOf(originalData, originalData.length), Arrays.copyOf(vaultKey, 32));

        assertThat(blob1).isNotEqualTo(blob2);
    }

    @Test
    void shouldThrowExceptionOnInvalidVaultKeyLength() {
        byte[] invalidKey = new byte[31];
        assertThatThrownBy(() -> vaultService.seal(new byte[10], invalidKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vault key must be exactly 32 bytes");
    }
}
