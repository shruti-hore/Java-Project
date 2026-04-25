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

    private HttpRequest.Builder authorizedRequest(String path) {
        if (jwt == null || jwt.trim().isEmpty()) {
            throw new SessionExpiredException("Missing JWT. Cannot perform authorized request.");
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
