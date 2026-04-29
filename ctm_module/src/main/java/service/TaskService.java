package service;

import client.service.EncryptedTaskService;
import auth.session.SessionState;
import client.model.Task;
import java.util.List;

public class TaskService {
    private final EncryptedTaskService encryptedTaskService;
    private final client.sync.LocalCache localCache;
    private final auth.session.SessionState session;
    private final String teamId;
    private final WorkflowService workflow = new WorkflowService();

    public TaskService(EncryptedTaskService encryptedTaskService,
                       client.sync.LocalCache localCache,
                       auth.session.SessionState session, String teamId) {
        this.encryptedTaskService = encryptedTaskService;
        this.localCache = localCache;
        this.session = session;
        this.teamId = teamId;
    }

    public List<Task> getAllTasks(String userId, String teamId) {
        // Fetch metadata and decrypt each document.
        // Call synchronously as TaskService is called from background tasks in UI.
        return encryptedTaskService.getAllTasksForTeam(teamId, session);
    }

    public void addTask(Task t) {
        try {
            workflow.applyRules(t);
            // Phase 1: Save to local cache first (dirty)
            java.util.Map<String, Object> fields = new java.util.HashMap<>();
            fields.put("title", t.getTitle());
            fields.put("description", t.getDescription());
            fields.put("deadline", t.getDeadline());
            fields.put("status", t.getStatus());
            fields.put("priority", t.getPriority());
            fields.put("completed", t.isCompleted());
            fields.put("assigneeUserId", t.getUserId());

            // Use EncryptedTaskService to encrypt
            client.crypto.DocumentCryptoService crypto = new client.crypto.DocumentCryptoService(
                new auth.service.CryptoAdapter(new crypto.internal.CryptoServiceImpl()),
                new client.crypto.NonceCounterStore(java.nio.file.Paths.get("nonce_counters.txt"))
            );
            client.crypto.EncryptedDocumentPayload payload = crypto.encrypt(t.getId(), (short) 1, 0, session.getTeamKey(teamId), fields);
            
            localCache.cacheDocument(t.getId(), teamId, 0, payload, true);
            
            // Phase 2: Attempt immediate sync (optional, SyncManager will handle it otherwise)
            // encryptedTaskService.saveTask(t, teamId, (short) 1, 0, session);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add task: " + e.getMessage(), e);
        }
    }

    public void updateTask(Task t) {
        try {
            workflow.applyRules(t);
            // Phase 1: Save to local cache (dirty)
            java.util.Map<String, Object> fields = new java.util.HashMap<>();
            fields.put("title", t.getTitle());
            fields.put("description", t.getDescription());
            fields.put("deadline", t.getDeadline());
            fields.put("status", t.getStatus());
            fields.put("priority", t.getPriority());
            fields.put("completed", t.isCompleted());
            fields.put("assigneeUserId", t.getUserId());

            client.crypto.DocumentCryptoService crypto = new client.crypto.DocumentCryptoService(
                new auth.service.CryptoAdapter(new crypto.internal.CryptoServiceImpl()),
                new client.crypto.NonceCounterStore(java.nio.file.Paths.get("nonce_counters.txt"))
            );
            client.crypto.EncryptedDocumentPayload payload = crypto.encrypt(t.getId(), (short) 1, t.getCurrentVersionSeq(), session.getTeamKey(teamId), fields);

            localCache.cacheDocument(t.getId(), teamId, t.getCurrentVersionSeq(), payload, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task: " + e.getMessage(), e);
        }
    }

    public void deleteTask(String id) {
        try {
            encryptedTaskService.deleteTask(id, session);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task: " + e.getMessage(), e);
        }
    }

    public void markInProgress(Task t) {
        t.setStatus("IN_PROGRESS");
        updateTask(t);
    }

    public void markDone(Task t) {
        t.setStatus("DONE");
        t.setCompleted(true);
        updateTask(t);
    }

    public void updateStatus(Task t, String status) {
        t.setStatus(status);
        if (status.equals("DONE")) {
            t.setCompleted(true);
        } else {
            t.setCompleted(false);
        }
        updateTask(t);
    }
}
