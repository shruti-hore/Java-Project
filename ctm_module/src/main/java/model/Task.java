package model;

import org.bson.types.ObjectId;

public class Task extends ProjectItem {

    private String description;
    private String deadline;
    private boolean completed;
    private String priority;

    private String userId;
    private String teamId;

    public Task(String id, String title, String description,
                String deadline, boolean completed, String status, String priority, String userId, String teamId) {

        super(id, title, status);
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
        this.priority = priority;
        this.userId = userId;
        this.teamId = teamId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

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