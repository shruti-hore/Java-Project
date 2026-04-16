import java.util.List;

public class TestModel {
    public static void main(String[] args) {

        List<Task> tasks = MockDataService.getSampleTasks();

        for (Task t : tasks) {
            System.out.println(t.getDetails());
        }
    }
}