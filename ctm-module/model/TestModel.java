import java.time.LocalDate;

public class TestModel {
    public static void main(String[] args) {

        Task t1 = new Task("1", "Test Task", "Check model", LocalDate.now());

        System.out.println(t1.getDetails());

        t1.markComplete();

        System.out.println("After update: " + t1.getDetails());
    }
}