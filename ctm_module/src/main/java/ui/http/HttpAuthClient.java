package ui.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import client.crypto.EncryptedDocumentPayload;
import exceptions.SessionExpiredException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpAuthClient {

    private final HttpClient client;
    private final String baseUrl;
    private String jwt;
    private final ObjectMapper objectMapper;

    public record WorkspaceSummary(String teamId, String name, String ownerUserId, String lastSyncedAt) {}
    public record DocumentMeta(String documentUuid, int versionSeq, String lastModifiedAt) {}

    public HttpAuthClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public HttpClient getClient() {
        return client;
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
        Map<String, String> body = Map.of("workspaceCode", workspaceCode);
        return post("/workspaces/join", body).thenApply(map -> {
            return new JoinWorkspaceResponse(
                (String)map.get("teamId"), 
                (String)map.get("name"), 
                (String)map.get("status")
            );
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

    @SuppressWarnings("unchecked")
    public String fetchWorkspaceCode(String teamId) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder("/api/teams/" + teamId + "/code")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Failed to fetch workspace code: " + response.body());
        }
        return response.body();
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

    @SuppressWarnings("unchecked")
    private CompletableFuture<Map<String, String>> post(String path, Object body) {
        try {
            HttpRequest request = newRequestBuilder(path).POST(objectMapper.writeValueAsBytes(body)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401) {
                throw new SessionExpiredException("Unauthorized (401)");
            }
            if (response.statusCode() != 200) {
                throw new IOException("HTTP call failed with code: " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), new TypeReference<>() {});
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        } catch (IOException e) {
            throw new IOException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    public List<WorkspaceSummary> fetchWorkspaces() throws IOException {
        HttpRequest request = authorizedRequest("/teams/mine").GET().build();
        String json = sendAndGetString(request);
        return objectMapper.readValue(json, new TypeReference<List<WorkspaceSummary>>() {});
    }

    public List<DocumentMeta> fetchWorkspaceMetadata(String teamId) throws IOException {
        HttpRequest request = authorizedRequest("/teams/" + teamId + "/documents/metadata").GET().build();
        String json = sendAndGetString(request);
        return objectMapper.readValue(json, new TypeReference<List<DocumentMeta>>() {});
    }

    public EncryptedDocumentPayload fetchDocument(String documentUuid) throws IOException {
        HttpRequest request = authorizedRequest("/documents/" + documentUuid + "/versions/latest").GET().build();
        String json = sendAndGetString(request);
        return objectMapper.readValue(json, EncryptedDocumentPayload.class);
    }

    public String fetchTeamKeyEnvelope(String teamId) throws IOException {
        HttpRequest request = authorizedRequest("/teams/" + teamId + "/envelopes/me").GET().build();
        return sendAndGetString(request);
    }

    public byte[] fetchOwnerPublicKey(String teamId) throws IOException {
        HttpRequest request = authorizedRequest("/teams/" + teamId + "/owner-public-key").GET().build();
        return sendAndGetBytes(request);
    }

    private String sendAndGetString(HttpRequest request) throws IOException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401) {
                throw new SessionExpiredException("Unauthorized (401)");
            }
            if (response.statusCode() != 200) {
                throw new IOException("HTTP call failed with code: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        }
    }

    private byte[] sendAndGetBytes(HttpRequest request) throws IOException {
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 401) {
                throw new SessionExpiredException("Unauthorized (401)");
            }
            if (response.statusCode() != 200) {
                throw new IOException("HTTP call failed with code: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        }
    }

    private HttpRequest.Builder authorizedRequest(String path) {
        if (jwt == null || jwt.trim().isEmpty()) {
            throw new SessionExpiredException("Missing JWT. Cannot perform authorized request.");
        }
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + jwt);
    }
}

    public HttpClient getClient() {
        return client;
    }

<<<<<<< HEAD
    private HttpRequest.Builder authorizedRequest(String path) {
        if (jwt == null || jwt.trim().isEmpty()) {
            throw new SessionExpiredException("Missing JWT. Cannot perform authorized request.");
=======
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
        Map<String, String> body = Map.of("workspaceCode", workspaceCode);
        return post("/workspaces/join", body).thenApply(map -> {
            return new JoinWorkspaceResponse(
                (String)map.get("teamId"), 
                (String)map.get("name"), 
                (String)map.get("status")
            );
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

    @SuppressWarnings("unchecked")
    public String fetchWorkspaceCode(String teamId) throws IOException, InterruptedException {
        HttpRequest request = newRequestBuilder("/api/teams/" + teamId + "/code")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Failed to fetch workspace code: " + response.body());
>>>>>>> d0a1891 (Fix: General dependencies fix. Removes useless calls.)
        }
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + jwt);
    }

    private String sendAndGetString(HttpRequest request) throws IOException {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 401) {
                throw new SessionExpiredException("Unauthorized (401)");
            }
            if (response.statusCode() != 200) {
                throw new IOException("HTTP call failed with code: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        }
    }

<<<<<<< HEAD
    private byte[] sendAndGetBytes(HttpRequest request) throws IOException {
=======
    private HttpRequest.Builder newRequestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json");
        if (jwt != null) {
            builder.header("Authorization", "Bearer " + jwt);
        }
        return builder;
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Map<String, String>> post(String path, Object body) {
>>>>>>> d0a1891 (Fix: General dependencies fix. Removes useless calls.)
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 401) {
                throw new SessionExpiredException("Unauthorized (401)");
            }
            if (response.statusCode() != 200) {
                throw new IOException("HTTP call failed with code: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HTTP request interrupted", e);
        }
    }

    public List<WorkspaceSummary> fetchWorkspaces() throws IOException {
        HttpRequest request = authorizedRequest("/teams/mine").GET().build();
        String json = sendAndGetString(request);
        return objectMapper.readValue(json, new TypeReference<List<WorkspaceSummary>>() {});
    }

    public List<DocumentMeta> fetchWorkspaceMetadata(String teamId) throws IOException {
        HttpRequest request = authorizedRequest("/teams/" + teamId + "/documents/metadata").GET().build();
        String json = sendAndGetString(request);
        return objectMapper.readValue(json, new TypeReference<List<DocumentMeta>>() {});
    }

    public EncryptedDocumentPayload fetchDocument(String documentUuid) throws IOException {
        HttpRequest request = authorizedRequest("/documents/" + documentUuid + "/versions/latest").GET().build();
        String json = sendAndGetString(request);
        return objectMapper.readValue(json, EncryptedDocumentPayload.class);
    }

    public String fetchTeamKeyEnvelope(String teamId) throws IOException {
        HttpRequest request = authorizedRequest("/teams/" + teamId + "/envelopes/me").GET().build();
        return sendAndGetString(request);
    }

    public byte[] fetchOwnerPublicKey(String teamId) throws IOException {
        HttpRequest request = authorizedRequest("/teams/" + teamId + "/owner-public-key").GET().build();
        return sendAndGetBytes(request);
    }
}
