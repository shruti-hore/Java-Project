package sync;

import crypto.DocumentCryptoService;
import crypto.EncryptedDocumentPayload;
import dto.DocumentVersionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.AEADBadTagException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * PIPE-06 Fix [failure mode]: ConflictResolver fetches both versions, decrypts them,
 * and presents them for resolution. It ensures AEADBadTagException propagates 
 * if any version (local or server) has been tampered with.
 */
public class ConflictResolver {

    private final DocumentCryptoService cryptoService;
    private final HttpClient httpClient;
    private final String serverBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConflictResolver(DocumentCryptoService cryptoService, HttpClient httpClient, String serverBaseUrl) {
        this.cryptoService = cryptoService;
        this.httpClient = httpClient;
        this.serverBaseUrl = serverBaseUrl;
    }

    public ConflictPair resolve(
        String documentUuid,
        String teamId,
        EncryptedDocumentPayload localPayload,  // the payload the client just tried to post
        String serverBlobRef,                   // from the 409 response body
        byte[] teamKey
    ) throws AEADBadTagException, IOException, InterruptedException {
        // 1. Fetch server blob by serverBlobRef -> EncryptedDocumentPayload (nonce + AAD + ciphertext)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/documents/versions/by-blob/" + serverBlobRef))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch server version for conflict resolution: " + response.statusCode());
        }

        DocumentVersionResponse serverResp = objectMapper.readValue(response.body(), DocumentVersionResponse.class);
        EncryptedDocumentPayload serverPayload = new EncryptedDocumentPayload(
                serverResp.getCiphertextBase64(),
                serverResp.getNonceBase64(),
                serverResp.getAadBase64(),
                0, // counterValue not needed for decryption
                serverResp.getVersionSeq().intValue()
        );

        // 2. Decrypt localPayload -> localFields
        Map<String, Object> localFields = cryptoService.decrypt(documentUuid, teamKey, localPayload);

        // 3. Decrypt server blob -> serverFields
        Map<String, Object> serverFields = cryptoService.decrypt(documentUuid, teamKey, serverPayload);

        // 4. Return ConflictPair(localFields, serverFields, serverVersionSeq)
        return new ConflictPair(localFields, serverFields, serverResp.getVersionSeq().intValue());
    }

    public void postResolution(
        String documentUuid,
        String teamId,
        short teamKeyVersion,
        byte[] teamKey,
        Map<String, Object> resolvedFields,
        int expectedVersionSeq    // serverVersionSeq from ConflictPair
    ) throws IOException, InterruptedException {
        // 1. Encrypt resolvedFields
        EncryptedDocumentPayload payload = cryptoService.encrypt(documentUuid, teamKeyVersion, expectedVersionSeq, teamKey, resolvedFields);

        // 2. Post to /documents/{uuid}/versions
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("teamId", teamId);
        requestBody.put("ciphertextBase64", payload.ciphertextBase64());
        requestBody.put("nonceBase64", payload.nonceBase64());
        requestBody.put("aadBase64", payload.aadBase64());
        requestBody.put("expectedVersionSeq", (long) expectedVersionSeq);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/documents/" + documentUuid + "/versions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 409) {
            // Requirement 519: throw ConflictException on repeated 409
            throw new RuntimeException("Conflict resolution failed: concurrent update detected");
        } else if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new IOException("Failed to post resolution: " + response.statusCode() + " " + response.body());
        }
    }
}
