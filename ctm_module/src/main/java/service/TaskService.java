package service;

import model.Task;
import java.util.List;
import java.util.ArrayList;

public class TaskService {

    private final EncryptedTaskService encryptedTaskService;

    public TaskService(EncryptedTaskService encryptedTaskService) {
        this.encryptedTaskService = encryptedTaskService;
    }

    public List<Task> getAllTasks(String userId, String teamId) {
        // In the new architecture, tasks are fetched via EncryptedTaskService from the SNM backend.
        // For now, returning an empty list until the fetch logic is fully integrated.
        // SyncManager will populate the ObservableList in DashboardUI directly.
        return new ArrayList<>();
    }

    public void addTask(Task t) {
        try {
            // Note: teamKeyVersion and currentVersionSeq should be tracked properly.
            // Using 1 and 0 as placeholders for now.
            encryptedTaskService.saveTask(t, t.getTeamId(), (short) 1, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTask(Task t) {
        try {
            // Logic for version sequencing should be handled by SyncManager/LocalCache
            encryptedTaskService.saveTask(t, t.getTeamId(), (short) 1, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTask(String id) {
        // Delete logic needs to be implemented in SNM backend and EncryptedTaskService
    }

    public void updateStatus(Task t, String status) {
        t.setStatus(status);
        if ("DONE".equals(status)) {
            t.setCompleted(true);
        } else {
            t.setCompleted(false);
        }
        updateTask(t);
    }
}