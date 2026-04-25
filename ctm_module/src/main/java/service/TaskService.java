package service;
import java.util.List;
import model.Task;

public class TaskService {

    private WorkflowService workflow = new WorkflowService();

    // The direct database path is removed per Part 3 Step 3.
    // The UI should directly use EncryptedTaskService instead of TaskService for CRUD operations,
    // as tasks are now encrypted blobs sent via HTTP.
    
    @Deprecated
    public List<Task> getAllTasks(String userId, String teamId) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use SyncManager to load tasks.");
    }

    @Deprecated
    public void addTask(Task t) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use EncryptedTaskService.");
    }

    @Deprecated
    public void updateTask(Task t) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use EncryptedTaskService.");
    }

    @Deprecated
    public void deleteTask(String id) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use EncryptedTaskService.");
    }

    @Deprecated
    public void markInProgress(Task t) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use EncryptedTaskService.");
    }

    @Deprecated
    public void markDone(Task t) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use EncryptedTaskService.");
    }

    @Deprecated
    public void updateStatus(Task t, String status) {
        throw new UnsupportedOperationException("Legacy database access is removed. Use EncryptedTaskService.");
    }
}