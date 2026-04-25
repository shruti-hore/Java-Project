package ui.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpAuthClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private String jwt; // In-memory only

    public HttpAuthClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public HttpRequest.Builder authorizedRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path));
        if (jwt != null && !jwt.isEmpty()) {
            builder.header("Authorization", "Bearer " + jwt);
        }
        return builder;
    }

    public ChallengeResponse challenge(String emailHmac) throws IOException {
        try {
            String jsonBody = "{\"emailHmac\":\"" + escapeJson(emailHmac) + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/login/challenge"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.getLogger(HttpAuthClient.class.getName()).log(System.Logger.Level.INFO, "POST /auth/login/challenge - Status: " + response.statusCode());

            if (response.statusCode() != 200) {
                throw new IOException("Challenge failed with status: " + response.statusCode());
            }

            String body = response.body();
            String salt = extractJsonString(body, "saltBase64");
            String vault = extractJsonString(body, "vaultBlobBase64");
            return new ChallengeResponse(salt, vault);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public String verify(String emailHmac, String bcryptHash) throws AuthException, IOException {
        try {
            String jsonBody = "{\"emailHmac\":\"" + escapeJson(emailHmac) + "\",\"bcryptHash\":\"" + escapeJson(bcryptHash) + "\"}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/login/verify"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.getLogger(HttpAuthClient.class.getName()).log(System.Logger.Level.INFO, "POST /auth/login/verify - Status: " + response.statusCode());

            if (response.statusCode() == 401) {
                throw new AuthException("Invalid credentials");
            } else if (response.statusCode() != 200) {
                throw new IOException("Verify failed with status: " + response.statusCode());
            }

            String token = extractJsonString(response.body(), "token");
            return token != null ? token : response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    public String register(RegisterPayload payload) throws IOException, ConflictException {
        try {
            String jsonBody = "{" +
                    "\"emailHmac\":\"" + escapeJson(payload.emailHmac()) + "\"," +
                    "\"bcryptHash\":\"" + escapeJson(payload.bcryptHash()) + "\"," +
                    "\"publicKeyBase64\":\"" + escapeJson(payload.publicKeyBase64()) + "\"," +
                    "\"vaultBlobBase64\":\"" + escapeJson(payload.vaultBlobBase64()) + "\"," +
                    "\"saltBase64\":\"" + escapeJson(payload.saltBase64()) + "\"" +
                    "}";
                    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.getLogger(HttpAuthClient.class.getName()).log(System.Logger.Level.INFO, "POST /auth/register - Status: " + response.statusCode());

            if (response.statusCode() == 409) {
                throw new ConflictException("User already exists");
            } else if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new IOException("Register failed with status: " + response.statusCode());
            }

            String userId = extractJsonString(response.body(), "userId");
            return userId != null ? userId : response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
    
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "\\n");
    }
    
    private String extractJsonString(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\": \"";
            start = json.indexOf(search);
        }
        if (start == -1) return null;
        
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        
        return json.substring(start, end);
    }

    public record ChallengeResponse(String saltBase64, String vaultBlobBase64) {}

    public record RegisterPayload(
            String emailHmac,
            String bcryptHash,
            String publicKeyBase64,
            String vaultBlobBase64,
            String saltBase64
    ) {}

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }

    public static class ConflictException extends Exception {
        public ConflictException(String message) { super(message); }
    }
}
