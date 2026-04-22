package service;
import java.util.List;
import model.Task;

public class TaskService {

    private MongoService mongo = new MongoService();

    public List<Task> getAllTasks() {
        return mongo.getTasks();
    }

    public void addTask(Task t) {
        mongo.addTask(t);
    }

    public void updateTask(Task t) {
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
        t.setStatus("DONE");
        t.setCompleted(true);
        mongo.updateStatus(t.getId(), "DONE");
        mongo.updateCompletion(t.getId(), true);
    }
}