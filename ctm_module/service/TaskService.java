package service;
import java.util.List;
import model.Task;

public class TaskService {

    private MongoService mongo = new MongoService();
    private WorkflowService workflow = new WorkflowService();

    public List<Task> getAllTasks(String userId, String teamId) {
        return mongo.getTasks(userId, teamId);
    }

    public void addTask(Task t) {
        workflow.applyRules(t);
        String id = mongo.addTask(t);
        t.setId(id);
    }

    public void updateTask(Task t) {
        workflow.applyRules(t);
        mongo.updateTask(t);
    }

    public void deleteTask(String id) {
        mongo.deleteTask(id);
    }

    public void markInProgress(Task t) {
        t.setStatus("IN_PROGRESS");
        mongo.updateStatus(t.getId(), "IN_PROGRESS");
    }

    public void markDone(Task t) {
        updateStatus(t, "DONE");
    }

    public void updateStatus(Task t, String status) {
        t.setStatus(status);
        if (status.equals("DONE")) {
            t.setCompleted(true);
            mongo.updateCompletion(t.getId(), true);
        } else {
            t.setCompleted(false);
            mongo.updateCompletion(t.getId(), false);
        }
        mongo.updateStatus(t.getId(), status);
    }
}