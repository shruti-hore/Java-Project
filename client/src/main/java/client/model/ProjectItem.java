package client.model;

public abstract class ProjectItem {

    protected String id;
    protected String title;
    protected String status; // DEADLINE, IN_PROGRESS, DONE

    public ProjectItem(String id, String title, String status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Polymorphism entry point
    public abstract String getDetails();
}
