import java.time.LocalDate;

public class TestModel {
    public static void main(String[] args) {

        Task t1 = new Task("1", "Complete Report", "Finish DAA report", LocalDate.now().plusDays(2));

        System.out.println(t1.getDetails());

        t1.markComplete();

        System.out.println("After completion: " + t1.getDetails());
    }
}