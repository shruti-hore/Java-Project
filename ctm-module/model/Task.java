package model;

import java.time.LocalDate;

public class Task {

    private String title;
    private String description; // updated
    private LocalDate deadline;
    private boolean completed;

    public Task(String title, String description, LocalDate deadline, boolean completed) {
        this.title = title;
        this.description = description; // updated
        this.deadline = deadline;
        this.completed = completed;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() { // updated
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return completed;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) { // updated
        this.description = description;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}