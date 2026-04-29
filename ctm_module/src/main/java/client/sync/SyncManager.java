package client.sync;

import auth.session.SessionState;
import client.crypto.EncryptedDocumentPayload;
import client.service.EncryptedTaskService;
import javafx.application.Platform;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * WIRE-FIX: Background synchronization manager.
 * Periodically pushes dirty local changes to the server and handles conflicts.
 */
public class SyncManager {
    private final LocalCache localCache;
    private final ConflictResolver conflictResolver;
    private final EncryptedTaskService encryptedTaskService;
    private final SessionState session;
    private final HttpClient httpClient;
    private final String serverBaseUrl;
    private final Consumer<ConflictPair> onConflict;
    private final Runnable onSyncStart;
    private final Runnable onSyncEnd;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private boolean running = false;

    public SyncManager(LocalCache localCache, ConflictResolver conflictResolver,
                       EncryptedTaskService encryptedTaskService, SessionState session,
                       HttpClient httpClient, String serverBaseUrl,
                       Consumer<ConflictPair> onConflict,
                       Runnable onSyncStart, Runnable onSyncEnd) {
        this.localCache = localCache;
        this.conflictResolver = conflictResolver;
        this.encryptedTaskService = encryptedTaskService;
        this.session = session;
        this.httpClient = httpClient;
        this.serverBaseUrl = serverBaseUrl;
        this.onConflict = onConflict;
        this.onSyncStart = onSyncStart;
        this.onSyncEnd = onSyncEnd;
    }

    public void start() {
        if (running) return;
        running = true;
        executor.scheduleWithFixedDelay(this::sync, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        executor.shutdownNow();
    }

    private void sync() {
        if (!running) return;
        try {
            onSyncStart.run();
            pushDirtyChanges();
        } catch (Exception e) {
            System.err.println("Sync cycle failed: " + e.getMessage());
        } finally {
            onSyncEnd.run();
        }
    }

    private void pushDirtyChanges() {
        List<String> dirtyUuids = localCache.getDirtyDocumentUuids();
        for (String uuid : dirtyUuids) {
            if (!running) break;
            try {
                pushDocument(uuid);
            } catch (Exception e) {
                if ("CONFLICT".equals(e.getMessage())) {
                    handleConflict(uuid);
                } else {
                    System.err.println("Failed to push " + uuid + ": " + e.getMessage());
                }
            }
        }
    }

    private void pushDocument(String uuid) throws IOException, InterruptedException {
        Optional<EncryptedDocumentPayload> payloadOpt = localCache.getDocument(uuid);
        if (payloadOpt.isEmpty()) return;

        EncryptedDocumentPayload localPayload = payloadOpt.get();
        String teamIdFromDb = localCache.getTeamId(uuid);

        if (teamIdFromDb != null) {
            encryptedTaskService.pushPayload(uuid, teamIdFromDb, localPayload);
            localCache.markClean(uuid, localPayload.versionSeq() + 1);
            System.out.println("Successfully pushed " + uuid + " to server.");
        }
    }

    private void handleConflict(String uuid) {
        // Fetch remote version, then notify UI
        try {
            Optional<EncryptedDocumentPayload> localOpt = localCache.getDocument(uuid);
            if (localOpt.isEmpty()) return;
            
            // fetch latest from server
            // EncryptedDocumentPayload remote = encryptedTaskService.fetchLatest(uuid); 
            // onConflict.accept(new ConflictPair(uuid, teamId, localOpt.get(), remote.blobId(), session.getTeamKey(teamId)));
            
            System.out.println("Conflict detected for " + uuid + " (SyncManager)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
