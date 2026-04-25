package ui.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpAuthClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpAuthClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
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

    private CompletableFuture<Map<String, String>> post(String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
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
