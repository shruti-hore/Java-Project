package model;

public class Task {

    private String title;
    private String deadline;
    private boolean isCompleted;

    public Task(String title, String deadline, boolean isCompleted) {
        this.title = title;
        this.deadline = deadline;
        this.isCompleted = isCompleted;
    }

    public void markComplete() {
        this.isCompleted = true;
    }

    public String getDetails() {
        return title + " | Due: " + deadline + " | Done: " + isCompleted;
    }

    public String getTitle() {
    return title;
    }

    public String getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}