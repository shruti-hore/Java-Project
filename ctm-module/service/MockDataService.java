import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MockDataService {

    public static List<Task> getSampleTasks() {

        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task("1", "Design UI", "Create dashboard layout", LocalDate.now().plusDays(2)));
        tasks.add(new Task("2", "Implement Backend", "Setup Spring Boot APIs", LocalDate.now().plusDays(5)));
        tasks.add(new Task("3", "Testing", "Perform integration testing", LocalDate.now().plusDays(7)));

        return tasks;
    }
}