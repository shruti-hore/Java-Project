public abstract class ProjectItem {

    protected String id;
    protected String title;

    public ProjectItem(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    // Abstract method (Polymorphism entry point)
    public abstract String getDetails();
}