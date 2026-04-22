package crypto.internal;

import org.junit.jupiter.api.Test;
import java.security.PublicKey;
import static org.assertj.core.api.Assertions.*;

public class KeyPairServiceTest {

    private final KeyPairService keyPairService = new KeyPairService();

    @Test
    void shouldGenerateValidKeyPair() {
        X25519KeyPair kp = keyPairService.generateKeyPair();
        
        assertThat(kp).isNotNull();
        assertThat(kp.publicKeyBytes()).hasSize(32);
        assertThat(kp.privateKey()).isNotNull();
        assertThat(kp.privateKey().getAlgorithm()).isEqualTo("X25519");
    }

    @Test
    void shouldExtractAndLoadPublicKey() {
        X25519KeyPair kp = keyPairService.generateKeyPair();
        byte[] rawBytes = kp.publicKeyBytes();
        
        PublicKey loadedKey = keyPairService.loadPublicKey(rawBytes);
        
        assertThat(loadedKey).isNotNull();
        assertThat(loadedKey.getAlgorithm()).isEqualTo("X25519");
        
        // Verify that extracting from the loaded key gives the same bytes
        byte[] reExtracted = keyPairService.extractPublicKeyBytes(loadedKey);
        assertThat(reExtracted).isEqualTo(rawBytes);
    }

    @Test
    void shouldProduceDifferentKeyPairsOnEachCall() {
        X25519KeyPair kp1 = keyPairService.generateKeyPair();
        X25519KeyPair kp2 = keyPairService.generateKeyPair();
        
        assertThat(kp1.publicKeyBytes()).isNotEqualTo(kp2.publicKeyBytes());
    }

    @Test
    void shouldFailForIncorrectKeyLength() {
        byte[] shortKey = new byte[31];
        
        assertThatThrownBy(() -> keyPairService.loadPublicKey(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly 32 bytes");
    }

    @Test
    void shouldFailForInvalidKeyPoints() {
        // An all-zero key is often invalid for X25519
        byte[] invalidKey = new byte[32];
        
        assertThatThrownBy(() -> keyPairService.loadPublicKey(invalidKey))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
