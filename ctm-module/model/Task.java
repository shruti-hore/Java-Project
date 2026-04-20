package model;

import org.bson.types.ObjectId;

public class Task {
    private ObjectId id;
    private String title;
    private String description;
    private String deadline;
    private boolean completed;
    private String status;

    public Task(ObjectId id, String title, String description, String deadline, boolean completed, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
        this.status = (status == null) ? "DEADLINE" : status;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDeadline() { return deadline; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getStatus() {
        return (status == null) ? "DEADLINE" : status;
    }

    public void setStatus(String status) {
        this.status = (status == null) ? "DEADLINE" : status;
    }
}