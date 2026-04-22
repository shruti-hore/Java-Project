package model;

import org.bson.types.ObjectId;

public class Task extends ProjectItem {

    private String description;
    private String deadline;
    private boolean completed;
    private String priority;

    public Task(String id, String title, String description,
                String deadline, boolean completed, String status, String priority) {

        super(id, title, status);
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
        this.priority = priority;
    }

    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }
    public boolean isCompleted() { return completed; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    @Override
    public String getDetails() {
        return title + " - " + description + " (Due: " + deadline + ")";
    }
}