package crypto;

import auth.service.CryptoAdapter;
import crypto.api.CryptoService;
import crypto.internal.CryptoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.AEADBadTagException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentCryptoServiceTest {

    private DocumentCryptoService service;
    private CryptoService cryptoService;
    private CryptoAdapter cryptoAdapter;
    private NonceCounterStore counterStore;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        cryptoService = new CryptoServiceImpl();
        cryptoAdapter = new CryptoAdapter(cryptoService);
        counterStore = new NonceCounterStore(tempDir.resolve("nonce.store"));
        service = new DocumentCryptoService(cryptoAdapter, counterStore);
    }

    @Test
    void testRoundTrip() throws Exception {
        String docUuid = UUID.randomUUID().toString();
        byte[] teamKey = new byte[32]; // dummy key
        Map<String, Object> fields = new HashMap<>();
        fields.put("title", "Test Task");
        fields.put("priority", 1);

        EncryptedDocumentPayload payload = service.encrypt(docUuid, (short) 1, 0, teamKey, fields);
        Map<String, Object> decrypted = service.decrypt(docUuid, teamKey, payload);

        assertEquals(fields, decrypted);
    }

    @Test
    void testDistinctNonces() throws Exception {
        String docUuid = UUID.randomUUID().toString();
        byte[] teamKey = new byte[32];
        Map<String, Object> fields = new HashMap<>();
        fields.put("test", "data");

        String nonce1 = service.encrypt(docUuid, (short) 1, 0, teamKey, fields).nonceBase64();
        String nonce2 = service.encrypt(docUuid, (short) 1, 1, teamKey, fields).nonceBase64();
        String nonce3 = service.encrypt(docUuid, (short) 1, 2, teamKey, fields).nonceBase64();

        assertNotEquals(nonce1, nonce2);
        assertNotEquals(nonce2, nonce3);
        assertNotEquals(nonce1, nonce3);
    }

    @Test
    void testFlippedByteThrowsException() throws Exception {
        String docUuid = UUID.randomUUID().toString();
        byte[] teamKey = new byte[32];
        Map<String, Object> fields = Map.of("secret", "message");

        EncryptedDocumentPayload payload = service.encrypt(docUuid, (short) 1, 0, teamKey, fields);
        
        byte[] ciphertext = Base64.getDecoder().decode(payload.ciphertextBase64());
        ciphertext[0] ^= 1; // flip one bit
        
        EncryptedDocumentPayload tamperedPayload = new EncryptedDocumentPayload(
                Base64.getEncoder().encodeToString(ciphertext),
                payload.nonceBase64(),
                payload.aadBase64(),
                payload.counterValue(),
                payload.versionSeq()
        );

        assertThrows(AEADBadTagException.class, () -> service.decrypt(docUuid, teamKey, tamperedPayload));
    }

    @Test
    void testWrongKeyThrowsException() throws Exception {
        String docUuid = UUID.randomUUID().toString();
        byte[] teamKey = new byte[32];
        byte[] wrongKey = new byte[32];
        wrongKey[0] = 1;
        
        Map<String, Object> fields = Map.of("secret", "message");

        EncryptedDocumentPayload payload = service.encrypt(docUuid, (short) 1, 0, teamKey, fields);

        assertThrows(AEADBadTagException.class, () -> service.decrypt(docUuid, wrongKey, payload));
    }

    @Test
    void testSwappedAadThrowsException() throws Exception {
        String docUuidA = UUID.randomUUID().toString();
        String docUuidB = UUID.randomUUID().toString();
        byte[] teamKey = new byte[32];
        
        Map<String, Object> fields = Map.of("doc", "A");

        EncryptedDocumentPayload payloadA = service.encrypt(docUuidA, (short) 1, 0, teamKey, fields);
        EncryptedDocumentPayload payloadB = service.encrypt(docUuidB, (short) 1, 0, teamKey, fields);

        // Try to decrypt Doc A's ciphertext using Doc B's AAD
        EncryptedDocumentPayload tamperedPayload = new EncryptedDocumentPayload(
                payloadA.ciphertextBase64(),
                payloadA.nonceBase64(),
                payloadB.aadBase64(),
                payloadA.counterValue(),
                payloadA.versionSeq()
        );

        assertThrows(AEADBadTagException.class, () -> service.decrypt(docUuidA, teamKey, tamperedPayload));
    }

    @Test
    void testCiphertextPaddingAndOverhead() throws Exception {
        String docUuid = UUID.randomUUID().toString();
        byte[] teamKey = new byte[32];
        Map<String, Object> fields = Map.of("k", "v");

        EncryptedDocumentPayload payload = service.encrypt(docUuid, (short) 1, 0, teamKey, fields);
        byte[] ciphertext = Base64.getDecoder().decode(payload.ciphertextBase64());
        
        // Ciphertext should be padded to 256 bytes multiple + 16 bytes GCM tag
        // Our padding logic should ensure the plaintext is a multiple of 256.
        // So ciphertext length = paddedPlaintextLength + 16.
        assertTrue(ciphertext.length > 16);
        assertEquals(16, ciphertext.length % 256, "Ciphertext length must be (multiple of 256) + 16 bytes overhead");
    }
}
