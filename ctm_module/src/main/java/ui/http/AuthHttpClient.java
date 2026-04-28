package ui.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.List;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple HTTP client for Auth related calls.
 * Rebuilt for Phase 1 & 2.
 */
public class AuthHttpClient {

    private final HttpClient client;
    private final String baseUrl = "http://localhost:8080"; // Default dev URL
    private final ObjectMapper mapper = new ObjectMapper();

    public record WorkspaceSummary(String teamId, String name, String ownerUserId, String ownerUsername, String lastSyncedAt) {}
    public record CreateWorkspaceResponse(String teamId, String workspaceCode) {}
    public record JoinWorkspaceResponse(String teamId, String name, String status) {}

    public AuthHttpClient() {
        this.client = HttpClient.newHttpClient();
    }

    public void setJwt(String jwt) {
        ui.SessionState.setToken(jwt);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public HttpClient getClient() {
        return client;
    }

    /**
     * POST /auth/register
     */
    public CompletableFuture<String> register(String email, String username, String password, String salt, String vaultBlob) {
        try {
            Map<String, String> body = Map.of(
                "email", email,
                "username", username,
                "password", password,
                "salt", salt,
                "vault_blob", vaultBlob
            );

            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 201) {
                            return "SUCCESS";
                        } else if (response.statusCode() == 409) {
                            return "CONFLICT";
                        } else {
                            throw new RuntimeException("HTTP " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * POST /auth/login/challenge
     */
    @SuppressWarnings("unchecked")
    public CompletableFuture<Map<String, String>> challenge(String identifier) {
        try {
            Map<String, String> body = Map.of("identifier", identifier);
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/login/challenge"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                return mapper.readValue(response.body(), Map.class);
                            } catch (Exception e) {
                                throw new RuntimeException("Parse error", e);
                            }
                        } else {
                            throw new RuntimeException("User not found");
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * POST /auth/login/verify
     */
    @SuppressWarnings("unchecked")
    public CompletableFuture<String> verify(String identifier, String authProof) {
        try {
            Map<String, String> body = Map.of(
                "identifier", identifier,
                "auth_proof", authProof
            );
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/login/verify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                Map<String, String> resp = mapper.readValue(response.body(), Map.class);
                                return resp.get("token");
                            } catch (Exception e) {
                                throw new RuntimeException("Parse error", e);
                            }
                        } else {
                            throw new RuntimeException("Unauthorized");
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * GET /workspaces
     */
    public List<WorkspaceSummary> fetchWorkspaces() throws Exception {
        String token = ui.SessionState.getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/workspaces"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), mapper.getTypeFactory().constructCollectionType(List.class, WorkspaceSummary.class));
        } else {
            throw new RuntimeException("Failed to fetch workspaces: " + response.statusCode());
        }
    }

    /**
     * GET /workspaces/{id}/envelope
     */
    public String fetchTeamKeyEnvelope(String teamId) throws Exception {
        String token = ui.SessionState.getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/workspaces/" + teamId + "/envelope"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("Failed to fetch envelope: " + response.statusCode());
        }
    }

    /**
     * GET /workspaces/{id}/owner-public-key
     */
    public byte[] fetchOwnerPublicKey(String teamId) throws Exception {
        String token = ui.SessionState.getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/workspaces/" + teamId + "/owner-public-key"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return Base64.getDecoder().decode(response.body().trim().replace("\"", ""));
        } else {
            throw new RuntimeException("Failed to fetch owner public key: " + response.statusCode());
        }
    }

    /**
     * GET /api/teams/{id}/code
     */
    public String fetchWorkspaceCode(String teamId) throws Exception {
        String token = ui.SessionState.getToken();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/teams/" + teamId + "/code"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body().trim().replace("\"", "");
        } else {
            throw new RuntimeException("Failed to fetch code: " + response.statusCode());
        }
    }

    /**
     * POST /workspaces
     */
    public CompletableFuture<CreateWorkspaceResponse> createWorkspace(String name, String ownerPublicKeyBase64) {
        try {
            String token = ui.SessionState.getToken();
            Map<String, String> body = Map.of("name", name, "ownerPublicKeyBase64", ownerPublicKeyBase64);
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/workspaces"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(resp -> {
                        try {
                            return mapper.readValue(resp.body(), CreateWorkspaceResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * POST /workspaces/join
     */
    public CompletableFuture<JoinWorkspaceResponse> joinWorkspace(String workspaceCode) {
        try {
            String token = ui.SessionState.getToken();
            Map<String, String> body = Map.of("workspaceCode", workspaceCode);
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/workspaces/join"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(resp -> {
                        try {
                            return mapper.readValue(resp.body(), JoinWorkspaceResponse.class);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * POST /workspaces/{id}/envelopes
     */
    public CompletableFuture<Void> postKeyEnvelope(String teamId, String userId, String envelopeBase64) {
        try {
            String token = ui.SessionState.getToken();
            Map<String, String> body = Map.of("userId", userId, "envelopeBase64", envelopeBase64);
            String json = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/workspaces/" + teamId + "/envelopes"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(resp -> null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
