package sync;

import auth.session.SessionState;
import crypto.EncryptedDocumentPayload;
import service.EncryptedTaskService;
import javafx.application.Platform;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * PIPE-08 Fix [failure mode]: SyncManager runs on a background daemon thread 
 * to prevent UI freezing. It processes the offline queue in FIFO order, 
 * handles conflicts via Platform.runLater(), and manages retries for transient errors.
 */
public class SyncManager {

    private final LocalCache localCache;
    private final ConflictResolver conflictResolver;
    private final EncryptedTaskService taskService;
    private final Consumer<ConflictPair> conflictCallback;
    private final HttpClient httpClient;
    private final String serverBaseUrl;
    private final SessionState session;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private ScheduledExecutorService executor;

    public SyncManager(LocalCache localCache, ConflictResolver conflictResolver, 
                       EncryptedTaskService taskService, SessionState session,
                       HttpClient httpClient, String serverBaseUrl,
                       Consumer<ConflictPair> conflictCallback) {
        this.localCache = localCache;
        this.conflictResolver = conflictResolver;
        this.taskService = taskService;
        this.session = session;
        this.httpClient = httpClient;
        this.serverBaseUrl = serverBaseUrl;
        this.conflictCallback = conflictCallback;
    }

    public void start() {
        // Requirement 631: Run on background daemon thread
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SyncManager-Thread");
            t.setDaemon(true);
            return t;
        });

        // Requirement 659: Poll connectivity every 5 seconds
        executor.scheduleWithFixedDelay(this::drainAndSync, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void drainAndSync() {
        try {
            // 1. localCache.drainQueue() -> list of PendingOperation (FIFO)
            List<PendingOperation> queue = localCache.drainQueue();
            
            for (PendingOperation op : queue) {
                // Requirement 675: Skip operations with retryCount >= 5
                if (op.retryCount() >= 5) {
                    continue;
                }

                if (!processOperation(op)) {
                    // Stop processing the queue if we have a connection error 
                    // (but continue if it was a 409 or other logic error)
                    break;
                }
            }
        } catch (Exception e) {
            // Log or handle error
        }
    }

    private boolean processOperation(PendingOperation op) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("teamId", Long.parseLong(op.teamId()));
            requestBody.put("ciphertextBase64", op.ciphertextBase64());
            requestBody.put("nonceBase64", op.nonceBase64());
            requestBody.put("aadBase64", op.aadBase64());
            requestBody.put("expectedVersionSeq", (long) op.versionSeq());
            requestBody.put("vectorClock", objectMapper.readValue(op.vectorClockJson(), Map.class));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverBaseUrl + "/documents/" + op.docUuid() + "/versions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                // b. On 201: localCache.deleteOperation(op.id())
                localCache.deleteOperation(op.id());
                return true;
            } else if (response.statusCode() == 409) {
                // c. On 409: invoke conflictResolver.resolve() -> ConflictPair
                // Parse blobRef from 409 body
                Map<String, String> body = objectMapper.readValue(response.body(), Map.class);
                String serverBlobRef = body.get("currentBlobRef");
                
                byte[] teamKey = session.getTeamKey(op.teamId());
                EncryptedDocumentPayload localPayload = new EncryptedDocumentPayload(
                        op.ciphertextBase64(), op.nonceBase64(), op.aadBase64(), 0, op.versionSeq()
                );
                
                ConflictPair pair = conflictResolver.resolve(op.docUuid(), op.teamId(), localPayload, serverBlobRef, teamKey);
                
                // Platform.runLater(() -> conflictCallback.accept(conflictPair))
                Platform.runLater(() -> conflictCallback.accept(pair));
                
                localCache.incrementRetryCount(op.id());
                return true; // Conflict is not a connection error, continue queue
            } else {
                // d. On other error: localCache.incrementRetryCount(op.id()), continue
                localCache.incrementRetryCount(op.id());
                return true;
            }
        } catch (IOException | InterruptedException e) {
            // Connection error? Stop the queue processing for this cycle
            return false;
        } catch (Exception e) {
            try {
                localCache.incrementRetryCount(op.id());
            } catch (Exception ignored) {}
            return true;
        }
    }
}
