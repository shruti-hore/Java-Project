import java.time.LocalDate;

import ctm-module.model.ProjectItem;

public class Task extends ProjectItem {

    private String description;
    private boolean isCompleted;
    private LocalDate deadline;

    public Task(String id, String title, String description, LocalDate deadline) {
        super(id, title);
        this.description = description;
        this.deadline = deadline;
        this.isCompleted = false;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void markComplete() {
        this.isCompleted = true;
    }

    @Override
    public String getDetails() {
        return title + " | Due: " + deadline + " | Done: " + isCompleted;
    }

    public void updateStatus(boolean status) {
        this.isCompleted = status;
    }
}