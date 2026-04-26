package service;

import client.service.EncryptedTaskService;
import auth.session.SessionState;
import client.model.Task;
import java.util.List;

public class TaskService {
    private final EncryptedTaskService encryptedTaskService;
    private final SessionState session;
    private final String teamId;
    private final WorkflowService workflow = new WorkflowService();

    public TaskService(EncryptedTaskService encryptedTaskService,
                       SessionState session, String teamId) {
        this.encryptedTaskService = encryptedTaskService;
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
            encryptedTaskService.saveTask(t, teamId, (short) 1, 0, session);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add task: " + e.getMessage(), e);
        }
    }

    public void updateTask(Task t) {
        try {
            workflow.applyRules(t);
            encryptedTaskService.saveTask(t, teamId, (short) 1, t.getCurrentVersionSeq(), session);
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
