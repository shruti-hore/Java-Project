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
import java.io.IOException;

public class HttpAuthClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String jwt;

    // --- MOCK INBOX STATE ---
    public record InboxItem(String id, String type, String usernameOrEmail, String teamId) {}
    private final java.util.List<InboxItem> mockInbox = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    public CompletableFuture<List<InboxItem>> fetchInbox() {
        return CompletableFuture.supplyAsync(() -> new java.util.ArrayList<>(mockInbox));
    }

    public CompletableFuture<Void> respondToInbox(String id, boolean accept) {
        return CompletableFuture.supplyAsync(() -> {
            mockInbox.removeIf(i -> i.id().equals(id));
            return null;
        });
    }

    public CompletableFuture<Void> sendInvite(String teamId, String email) {
        return CompletableFuture.supplyAsync(() -> {
            mockInbox.add(new InboxItem(java.util.UUID.randomUUID().toString(), "INVITE", email, teamId));
            return null;
        });
    }

    public CompletableFuture<Void> removeMember(String teamId, String username) {
        return CompletableFuture.completedFuture(null);
    }

    public record WorkspaceSummary(String teamId, String name, String ownerUserId, String ownerUsername, String lastSyncedAt) {}
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

    public CompletableFuture<Map<String, String>> register(String email, String username, String authProof, 
                                                           String publicKeyBase64, String vaultBlobBase64, 
                                                           String saltBase64) {
        Map<String, String> body = Map.of(
            "email", email,
            "username", username,
            "authProof", authProof,
            "publicKeyBase64", publicKeyBase64,
            "vaultBlobBase64", vaultBlobBase64,
            "saltBase64", saltBase64
        );
        return post("/auth/register", body);
    }

    public record JoinWorkspaceResponse(String teamId, String name, String status) {}
    public record CreateWorkspaceResponse(String teamId, String workspaceCode) {}

    public CompletableFuture<JoinWorkspaceResponse> joinWorkspace(String workspaceCode) {
        return CompletableFuture.supplyAsync(() -> {
            mockInbox.add(new InboxItem(java.util.UUID.randomUUID().toString(), "JOIN REQUEST", "user", "team-" + workspaceCode));
            return new JoinWorkspaceResponse("mock-id", "mock-name", "PENDING");
        });
    }

    public CompletableFuture<CreateWorkspaceResponse> createWorkspace(String name, String ownerPublicKeyBase64) {
        Map<String, String> body = Map.of(
            "name", name,
            "ownerPublicKeyBase64", ownerPublicKeyBase64
        );
        return post("/workspaces", body).thenApply(map -> {
            return new CreateWorkspaceResponse(
                (String)map.get("teamId"), 
                (String)map.get("workspaceCode")
            );
        });
    }

    public CompletableFuture<Void> postKeyEnvelope(String teamId, String userId, String envelopeBase64) {
        Map<String, String> body = Map.of(
            "userId", userId,
            "envelopeBase64", envelopeBase64
        );
        return post("/workspaces/" + teamId + "/envelopes", body).thenApply(map -> null);
    }

    public String fetchWorkspaceCode(String teamId) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder("/api/teams/" + teamId + "/code")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Failed to fetch workspace code: " + response.body());
        }
        
        // Assume the response body is just the code string or a JSON object with a 'code' field.
        // Let's parse as JSON if it looks like it, otherwise return raw string.
        try {
            Map<String, String> map = objectMapper.readValue(response.body(), Map.class);
            return map.get("code") != null ? map.get("code") : response.body();
        } catch (Exception e) {
            return response.body();
        }
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
                        String responseBody = response.body();
                        if (responseBody == null || responseBody.isBlank()) {
                            return java.util.Map.of();
                        }
                        return objectMapper.readValue(responseBody, Map.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse response", e);
                    }
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
