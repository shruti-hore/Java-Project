import java.util.*;
import model.Task;

public class Project {

    private String projectId;
    private String projectName;
    private List<Task> tasks;

    public Project(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public String getProjectName() {
        return projectName;
    }
}