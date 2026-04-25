package client.service;

import auth.session.SessionState;
import client.crypto.DocumentCryptoService;
import client.crypto.EncryptedDocumentPayload;
import client.model.Task;
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
 * PIPE-03 Fix [failure mode]: Replaced plaintext write path with EncryptedTaskService.
 * Content fields (title, description, deadline, priority, assignee) are encrypted before 
 * being sent to the server. Metadata (UUID, teamId) remains plaintext for routing.
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

    public void saveTask(Task task, String teamId, short teamKeyVersion, int currentVersionSeq) throws IOException, InterruptedException {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task UUID must not be null for encryption");
        }

        // 1. Build taskFields map from task object
        Map<String, Object> taskFields = new HashMap<>();
        taskFields.put("title", task.getTitle());
        taskFields.put("description", task.getDescription());
        taskFields.put("deadline", task.getDeadline());
        taskFields.put("priority", task.getPriority());
        taskFields.put("assigneeUserId", task.getUserId());
        taskFields.put("status", task.getStatus());
        taskFields.put("completed", task.isCompleted());

        // 2. teamKey <- session.getTeamKey(teamId)
        byte[] teamKey = session.getTeamKey(teamId);

        // 3. payload <- cryptoService.encrypt(task.getId(), teamKeyVersion, currentVersionSeq, teamKey, taskFields)
        EncryptedDocumentPayload payload = cryptoService.encrypt(task.getId(), teamKeyVersion, currentVersionSeq, teamKey, taskFields);

        // 4. POST payload to /documents/{task.getId()}/versions
        // Note: For now, we wrap the payload in a request body suitable for the server Step 4
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("teamId", teamId);
        requestBody.put("ciphertextBase64", payload.ciphertextBase64());
        requestBody.put("nonceBase64", payload.nonceBase64());
        requestBody.put("aadBase64", payload.aadBase64());
        requestBody.put("expectedVersionSeq", currentVersionSeq);
        // vectorClock is omitted for now or handled separately

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverBaseUrl + "/documents/" + task.getId() + "/versions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // The actual implementation will be expanded in Step 8 (SyncManager)
        // For Step 3, we just perform the write.
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    public Task loadTask(String docUuid, String teamId, EncryptedDocumentPayload payload) throws AEADBadTagException, IOException {
        // 1. teamKey <- session.getTeamKey(teamId)
        byte[] teamKey = session.getTeamKey(teamId);

        // 2. fields <- cryptoService.decrypt(docUuid, teamKey, payload)
        Map<String, Object> fields = cryptoService.decrypt(docUuid, teamKey, payload);

        // 3. Reconstruct Task from fields map and return
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
}
