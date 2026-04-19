package model;

public class Task {

    private String title;
    private String description; // updated
    private String deadline;
    private boolean completed; // FIXED (this was missing!)

    // NEW constructor (used in UI)
    public Task(String title, String description, String deadline, boolean completed) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
    }

    // OLD constructor (for compatibility) // updated
    public Task(String title, String deadline, boolean completed) {
        this.title = title;
        this.description = ""; // default empty description // updated
        this.deadline = deadline;
        this.completed = completed;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; } // updated
    public String getDeadline() { return deadline; }
    public boolean isCompleted() { return completed; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; } // updated
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public void markComplete() {
        this.completed = true;
    }

    public String getDetails() { // updated
        return "Title: " + title +
            " | Description: " + description +
            " | Deadline: " + deadline +
            " | Completed: " + completed;
    }
}