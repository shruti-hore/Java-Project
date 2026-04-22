package crypto.internal;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import java.security.PublicKey;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;

public class TeamKeyEnvelopeTest {

    private final TeamKeyEnvelope envelopeService = new TeamKeyEnvelope();
    private final KeyPairService keyPairService = new KeyPairService();

    @Test
    void shouldWrapAndUnwrapCorrectly() throws Exception {
        // Setup keys
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        
        PublicKey senderPub = keyPairService.loadPublicKey(sender.publicKeyBytes());
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] originalTeamKey = new byte[32];
        Arrays.fill(originalTeamKey, (byte) 0xA5);
        byte[] teamKeyToWrap = Arrays.copyOf(originalTeamKey, originalTeamKey.length);

        // Wrap
        byte[] envelope = envelopeService.wrap(teamKeyToWrap, recipientPub, sender.privateKey());
        
        // teamKeyToWrap should be zeroed
        assertThat(teamKeyToWrap).containsOnly((byte) 0);

        // Unwrap
        byte[] unwrapped = envelopeService.unwrap(envelope, senderPub, recipient.privateKey());
        
        assertThat(unwrapped).isEqualTo(originalTeamKey);
    }

    @Test
    void shouldThrowExceptionOnWrongRecipientKey() throws Exception {
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        X25519KeyPair intruder = keyPairService.generateKeyPair();
        
        PublicKey senderPub = keyPairService.loadPublicKey(sender.publicKeyBytes());
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        Arrays.fill(teamKey, (byte) 0xCC);

        byte[] envelope = envelopeService.wrap(teamKey, recipientPub, sender.privateKey());

        assertThatThrownBy(() -> envelopeService.unwrap(envelope, senderPub, intruder.privateKey()))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldThrowExceptionOnTamperedEnvelope() throws Exception {
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        
        PublicKey senderPub = keyPairService.loadPublicKey(sender.publicKeyBytes());
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        byte[] envelope = envelopeService.wrap(teamKey, recipientPub, sender.privateKey());

        envelope[envelope.length - 1] ^= 0x01; // Tamper with tag

        assertThatThrownBy(() -> envelopeService.unwrap(envelope, senderPub, recipient.privateKey()))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldProduceDifferentEnvelopesForSameInput() throws Exception {
        X25519KeyPair sender = keyPairService.generateKeyPair();
        X25519KeyPair recipient = keyPairService.generateKeyPair();
        
        PublicKey recipientPub = keyPairService.loadPublicKey(recipient.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        
        byte[] env1 = envelopeService.wrap(Arrays.copyOf(teamKey, 32), recipientPub, sender.privateKey());
        byte[] env2 = envelopeService.wrap(Arrays.copyOf(teamKey, 32), recipientPub, sender.privateKey());

        assertThat(env1).isNotEqualTo(env2);
    }

    @Test
    void shouldBeCommutative() throws Exception {
        // A wraps for B. B unwraps using A's public key.
        X25519KeyPair userA = keyPairService.generateKeyPair();
        X25519KeyPair userB = keyPairService.generateKeyPair();
        
        PublicKey pubA = keyPairService.loadPublicKey(userA.publicKeyBytes());
        PublicKey pubB = keyPairService.loadPublicKey(userB.publicKeyBytes());
        
        byte[] teamKey = new byte[32];
        Arrays.fill(teamKey, (byte) 0x77);

        // A -> B
        byte[] env = envelopeService.wrap(Arrays.copyOf(teamKey, 32), pubB, userA.privateKey());
        
        // B unwraps
        byte[] unwrapped = envelopeService.unwrap(env, pubA, userB.privateKey());
        
        assertThat(unwrapped).isEqualTo(teamKey);
    }
}
