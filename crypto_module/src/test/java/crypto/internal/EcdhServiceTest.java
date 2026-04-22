package crypto.internal;

import org.junit.jupiter.api.Test;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import static org.assertj.core.api.Assertions.*;

public class EcdhServiceTest {

    private final EcdhService ecdhService = new EcdhService();
    private final KeyPairService keyPairService = new KeyPairService();

    @Test
    void shouldVerifyCommutativity() {
        X25519KeyPair alice = keyPairService.generateKeyPair();
        X25519KeyPair bob = keyPairService.generateKeyPair();

        // Alice computes: Alice_priv + Bob_pub
        byte[] aliceSecret = ecdhService.computeSharedSecret(alice.privateKey(), 
                keyPairService.loadPublicKey(bob.publicKeyBytes()));

        // Bob computes: Bob_priv + Alice_pub
        byte[] bobSecret = ecdhService.computeSharedSecret(bob.privateKey(), 
                keyPairService.loadPublicKey(alice.publicKeyBytes()));

        assertThat(aliceSecret).isEqualTo(bobSecret);
        assertThat(aliceSecret).hasSize(32);
    }

    @Test
    void shouldProduceDifferentSecretsForDifferentPairs() {
        X25519KeyPair alice = keyPairService.generateKeyPair();
        X25519KeyPair bob = keyPairService.generateKeyPair();
        X25519KeyPair charlie = keyPairService.generateKeyPair();

        byte[] secretAB = ecdhService.computeSharedSecret(alice.privateKey(), 
                keyPairService.loadPublicKey(bob.publicKeyBytes()));
        byte[] secretAC = ecdhService.computeSharedSecret(alice.privateKey(), 
                keyPairService.loadPublicKey(charlie.publicKeyBytes()));

        assertThat(secretAB).isNotEqualTo(secretAC);
    }

    @Test
    void shouldFailForNullInputs() {
        assertThatThrownBy(() -> ecdhService.computeSharedSecret(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailForWrongKeyType() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        
        // Ed25519 is for signatures, not ECDH. Should fail.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519", "BC");
        KeyPair edPair = kpg.generateKeyPair();
        
        X25519KeyPair alice = keyPairService.generateKeyPair();

        assertThatThrownBy(() -> ecdhService.computeSharedSecret(edPair.getPrivate(), 
                keyPairService.loadPublicKey(alice.publicKeyBytes())))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(InvalidKeyException.class);
    }
}
