package client.service;

import auth.session.SessionState;
import client.crypto.DocumentCryptoService;
import client.crypto.EncryptedDocumentPayload;
import client.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.crypto.AEADBadTagException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WIRE-FIX: Secure task service that handles client-side encryption.
 * Now includes methods for bulk fetching and deletion via the secure pipeline.
 */
public class EncryptedTaskService {

    private final DocumentCryptoService cryptoService;
    private final SessionState session;
    private final HttpClient httpClient;
    private final String serverBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EncryptedTaskService(DocumentCryptoService cryptoService, SessionState session, 
                                HttpClient httpClient, String serverBaseUrl) {
        this.cryptoService = cryptoService;
        this.session = session;
        this.httpClient = httpClient;
        this.serverBaseUrl = serverBaseUrl;
    }

    public void saveTask(Task task, String teamId, short teamKeyVersion, int currentVersionSeq, SessionState session) throws IOException, InterruptedException {
        // Matches TaskService expectations
        saveTask(task, teamId, teamKeyVersion, currentVersionSeq);
    }

    public void saveTask(Task task, String teamId, short teamKeyVersion, int currentVersionSeq) throws IOException, InterruptedException {
        Map<String, Object> taskFields = new HashMap<>();
        taskFields.put("title", task.getTitle());
        taskFields.put("description", task.getDescription());
        taskFields.put("deadline", task.getDeadline());
        taskFields.put("priority", task.getPriority());
        taskFields.put("assigneeUserId", task.getUserId());
        taskFields.put("status", task.getStatus());
        taskFields.put("completed", task.isCompleted());

        byte[] teamKey = session.getTeamKey(teamId);
        EncryptedDocumentPayload payload = cryptoService.encrypt(task.getId(), teamKeyVersion, currentVersionSeq, teamKey, taskFields);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("teamId", teamId);
        requestBody.put("ciphertextBase64", payload.ciphertextBase64());
        requestBody.put("nonceBase64", payload.nonceBase64());
        requestBody.put("aadBase64", payload.aadBase64());
        requestBody.put("expectedVersionSeq", currentVersionSeq);

        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/documents/" + task.getId() + "/versions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + session.getJwt())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    @SuppressWarnings("unchecked")
    public List<Task> getAllTasksForTeam(String teamId, SessionState session) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverBaseUrl + "/workspaces/" + teamId + "/documents"))
                    .header("Authorization", "Bearer " + session.getJwt())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<Map<String, Object>> docs = objectMapper.readValue(response.body(), new TypeReference<>() {});
            List<Task> tasks = new ArrayList<>();

            for (Map<String, Object> doc : docs) {
                String docUuid = (String) doc.get("id");
                Map<String, Object> payloadMap = (Map<String, Object>) doc.get("latestPayload");
                if (payloadMap == null) continue;

                EncryptedDocumentPayload payload = new EncryptedDocumentPayload(
                        (String) payloadMap.get("ciphertextBase64"),
                        (String) payloadMap.get("nonceBase64"),
                        (String) payloadMap.get("aadBase64"),
                        0, // counter not needed for decryption
                        (Integer) payloadMap.get("versionSeq")
                );

                try {
                    Task t = loadTask(docUuid, teamId, payload);
                    t.setCurrentVersionSeq(payload.versionSeq());
                    tasks.add(t);
                } catch (AEADBadTagException e) {
                    // Skip documents we can't decrypt
                }
            }
            return tasks;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Task loadTask(String docUuid, String teamId, EncryptedDocumentPayload payload) throws AEADBadTagException, IOException {
        byte[] teamKey = session.getTeamKey(teamId);
        Map<String, Object> fields = cryptoService.decrypt(docUuid, teamKey, payload);

        return new Task(
                docUuid,
                (String) fields.get("title"),
                (String) fields.get("description"),
                (String) fields.get("deadline"),
                (Boolean) fields.get("completed"),
                (String) fields.get("status"),
                (String) fields.get("priority"),
                (String) fields.get("assigneeUserId"),
                teamId
        );
    }

    public void deleteTask(String docUuid, SessionState session) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/documents/" + docUuid))
                .header("Authorization", "Bearer " + session.getJwt())
                .DELETE()
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }
}
