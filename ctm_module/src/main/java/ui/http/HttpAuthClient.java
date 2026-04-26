package ui.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import client.crypto.EncryptedDocumentPayload;

public class HttpAuthClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String jwt;

    public record WorkspaceSummary(String teamId, String name, String ownerUserId, String lastSyncedAt) {}
    public record DocumentMeta(String documentUuid, int versionSeq) {}

    public HttpAuthClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public HttpClient getClient() {
        return httpClient;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public List<WorkspaceSummary> fetchWorkspaces() throws Exception {
        HttpRequest request = newRequestBuilder("/workspaces").GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new RuntimeException("Failed to fetch workspaces: " + response.body());
        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, WorkspaceSummary.class));
    }

    public String fetchTeamKeyEnvelope(String teamId) throws Exception {
        HttpRequest request = newRequestBuilder("/workspaces/" + teamId + "/envelope").GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new RuntimeException("Failed to fetch envelope: " + response.body());
        return response.body();
    }

    public byte[] fetchOwnerPublicKey(String teamId) throws Exception {
        HttpRequest request = newRequestBuilder("/workspaces/" + teamId + "/owner-public-key").GET().build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) throw new RuntimeException("Failed to fetch owner public key");
        return response.body();
    }

    public List<DocumentMeta> fetchWorkspaceMetadata(String teamId) throws Exception {
        HttpRequest request = newRequestBuilder("/workspaces/" + teamId + "/metadata").GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new RuntimeException("Failed to fetch metadata: " + response.body());
        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, DocumentMeta.class));
    }

    public EncryptedDocumentPayload fetchDocument(String documentUuid) throws Exception {
        HttpRequest request = newRequestBuilder("/documents/" + documentUuid).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) throw new RuntimeException("Failed to fetch document: " + response.body());
        return objectMapper.readValue(response.body(), EncryptedDocumentPayload.class);
    }

    public CompletableFuture<Map<String, String>> challenge(String email) {
        Map<String, String> body = Map.of("email", email);
        return post("/auth/login/challenge", body);
    }

    public CompletableFuture<Map<String, String>> verify(String email, String authProof) {
        Map<String, String> body = Map.of(
            "email", email,
            "authProof", authProof
        );
        return post("/auth/login/verify", body);
    }

    public CompletableFuture<Map<String, String>> register(String email, String authProof, 
                                                           String publicKeyBase64, String vaultBlobBase64, 
                                                           String saltBase64) {
        Map<String, String> body = Map.of(
            "email", email,
            "authProof", authProof,
            "publicKeyBase64", publicKeyBase64,
            "vaultBlobBase64", vaultBlobBase64,
            "saltBase64", saltBase64
        );
        return post("/auth/register", body);
    }

    private HttpRequest.Builder newRequestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json");
        if (jwt != null) {
            builder.header("Authorization", "Bearer " + jwt);
        }
        return builder;
    }

    private CompletableFuture<Map<String, String>> post(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = newRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                    try {
                        return objectMapper.readValue(response.body(), Map.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse response", e);
                    }
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
