package auth.service;

import auth.model.User;
import auth.session.SessionState;
import crypto.api.CryptoService;
import crypto.api.X25519KeyPair;
import crypto.internal.CryptoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.AEADBadTagException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AuthServiceIntegrationTest {

    private AuthService authService;
    private CryptoAdapter cryptoAdapter;
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        // Instantiate manually - no Spring
        cryptoService = new CryptoServiceImpl();
        cryptoAdapter = new CryptoAdapter(cryptoService);
        authService = new AuthService(cryptoAdapter);
    }

    @Test
    void shouldRegisterAndLoginSuccessfully() throws AEADBadTagException {
        String email = "test@example.com";
        char[] password = "securePassword123".toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);

        // 1. Register
        User user = authService.register(email, password);

        assertThat(user.getPublicKey()).hasSize(32);
        assertThat(user.getPublicKey()).isNotEqualTo(new byte[32]); // non-zero
        assertThat(user.getKeyVault()).hasSize(12 + 32 + 16); // nonce + privKey + tag
        assertThat(password).containsOnly('0'); // Verify zeroing

        // 2. Login
        SessionState session = authService.login(email, passwordCopy, user.getSalt(), 
                                                 user.getKeyVault(), user.getPublicKey());

        assertThat(session).isNotNull();
        assertThat(session.getUserId()).isEqualTo(email);
        assertThat(session.getX25519PrivateKey()).isNotNull();
        assertThat(session.getAuthSigningKey()).hasSize(32);
    }

    @Test
    void shouldFailLoginWithWrongPassword() {
        String email = "test@example.com";
        char[] password = "correctPassword".toCharArray();
        char[] wrongPassword = "wrongPassword".toCharArray();

        User user = authService.register(email, password);

        assertThatThrownBy(() -> authService.login(email, wrongPassword, user.getSalt(), 
                                                 user.getKeyVault(), user.getPublicKey()))
                .isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void shouldZeroSessionStateCorrectly() throws AEADBadTagException {
        String email = "test@example.com";
        char[] password = "password".toCharArray();
        User user = authService.register(email, password);

        SessionState session = authService.login(email, "password".toCharArray(), user.getSalt(), 
                                                 user.getKeyVault(), user.getPublicKey());
        
        byte[] authKey = session.getAuthSigningKey();
        byte[] pubKeyBytes = session.getX25519PublicKeyBytes();
        
        session.addTeamKey("team1", new byte[32]);
        
        session.zero();
        
        assertThat(authKey).containsOnly((byte) 0);
        assertThat(pubKeyBytes).containsOnly((byte) 0);
        assertThatThrownBy(() -> session.getTeamKey("team1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldVerifyPrivateKeyRoundTripViaEcdh() throws AEADBadTagException {
        // User A registers
        char[] passA = "passA".toCharArray();
        User userA = authService.register("a@test.com", passA);
        
        // User B (fresh key pair)
        X25519KeyPair kpB = cryptoAdapter.generateKeyPair();
        
        // User A logs in to recover private key
        SessionState sessionA = authService.login("a@test.com", "passA".toCharArray(), userA.getSalt(), 
                                                  userA.getKeyVault(), userA.getPublicKey());
        
        PrivateKey recoveredPrivA = sessionA.getX25519PrivateKey();
        PublicKey pubA = cryptoAdapter.loadPublicKey(userA.getPublicKey());
        
        // Perform ECDH: (recoveredPrivA, pubB) vs (privB, pubA)
        // Note: wrapTeamKey uses ECDH internally and returns an envelope.
        // To verify secret equality, we should use a more direct method if available,
        // but the schema says "derive shared secret". 
        // I'll use the internal services or CryptoService to compute shared secret directly if possible.
        // Wait, CryptoService doesn't expose computeSharedSecret.
        // But wrapTeamKey is deterministic if we control the team key? No, it uses random nonce.
        
        // Let's check if I should add computeSharedSecret to CryptoService.
        // README1 Step 1 didn't have it.
        
        // Actually, I'll just use wrap/unwrap.
        byte[] teamKey = new byte[32];
        Arrays.fill(teamKey, (byte) 0x99);
        byte[] teamKeyCopy = Arrays.copyOf(teamKey, 32);
        
        // A wraps for B
        byte[] envelope = cryptoAdapter.wrapTeamKey(teamKey, kpB.publicKey(), recoveredPrivA);
        
        // B unwraps
        byte[] unwrapped = cryptoAdapter.unwrapTeamKey(envelope, pubA, kpB.privateKey());
        
        assertThat(unwrapped).isEqualTo(teamKeyCopy);
    }
}
