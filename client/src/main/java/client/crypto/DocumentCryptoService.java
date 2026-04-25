package client.crypto;

import auth.service.CryptoAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.AEADBadTagException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * PIPE-02 Fix [AAD binding]: Every encrypt call passes docUuid (16) + versionSeq (4) as AAD
 * to bind the ciphertext to a specific document version.
 * PIPE-02 Fix [GCM failure propagation]: AEADBadTagException is never caught inside the pipeline.
 */
public class DocumentCryptoService {

    private final CryptoAdapter cryptoAdapter;
    private final NonceCounterStore counterStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentCryptoService(CryptoAdapter cryptoAdapter, NonceCounterStore counterStore) {
        this.cryptoAdapter = cryptoAdapter;
        this.counterStore = counterStore;
    }

    public EncryptedDocumentPayload encrypt(
        String docUuid,
        short  teamKeyVersion,
        int    versionSeq,      // current version - encoded into AAD
        byte[] teamKey,
        Map<String, Object> taskFields
    ) throws IOException {
        // 1. jsonBytes <- ObjectMapper.writeValueAsBytes(taskFields)
        byte[] jsonBytes = objectMapper.writeValueAsBytes(taskFields);
        
        // 2. padded <- cryptoAdapter.pad(jsonBytes)
        byte[] padded = cryptoAdapter.pad(jsonBytes);
        
        // 3. counter <- counterStore.getAndIncrement(docUuid)
        long counter = counterStore.getAndIncrement(docUuid);
        
        // 4. nonce <- cryptoAdapter.buildNonce(teamKeyVersion, UUID.fromString(docUuid), counter)
        UUID uuid = UUID.fromString(docUuid);
        byte[] nonce = cryptoAdapter.buildNonce(teamKeyVersion, uuid, counter);
        
        // 5. aad <- UUID.fromString(docUuid) as 16 bytes + versionSeq as 4 bytes big-endian
        byte[] aad = ByteBuffer.allocate(20)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .putInt(versionSeq)
                .array();
        
        // 6. ciphertext <- cryptoAdapter.encryptDocument(padded, teamKey, nonce, aad)
        byte[] ciphertext = cryptoAdapter.encryptDocument(padded, teamKey, nonce, aad);
        
        // 7. return EncryptedDocumentPayload(base64(ciphertext), base64(nonce), base64(aad), counter, versionSeq)
        return new EncryptedDocumentPayload(
                Base64.getEncoder().encodeToString(ciphertext),
                Base64.getEncoder().encodeToString(nonce),
                Base64.getEncoder().encodeToString(aad),
                counter,
                versionSeq
        );
    }

    public Map<String, Object> decrypt(
        String docUuid,
        byte[] teamKey,
        EncryptedDocumentPayload payload
    ) throws AEADBadTagException, IOException {
        // 1. ciphertext <- Base64.decode(payload.ciphertextBase64())
        byte[] ciphertext = Base64.getDecoder().decode(payload.ciphertextBase64());
        
        // 2. nonce <- Base64.decode(payload.nonceBase64())
        byte[] nonce = Base64.getDecoder().decode(payload.nonceBase64());
        
        // 3. aad <- Base64.decode(payload.aadBase64())
        byte[] aad = Base64.getDecoder().decode(payload.aadBase64());
        
        // 4. padded <- cryptoAdapter.decryptDocument(ciphertext, teamKey, nonce, aad)
        // AEADBadTagException propagates - do NOT catch
        byte[] padded = cryptoAdapter.decryptDocument(ciphertext, teamKey, nonce, aad);
        
        // 5. jsonBytes <- cryptoAdapter.unpad(padded)
        byte[] jsonBytes = cryptoAdapter.unpad(padded);
        
        // 6. return ObjectMapper.readValue(jsonBytes, Map.class)
        return objectMapper.readValue(jsonBytes, Map.class);
    }
}
