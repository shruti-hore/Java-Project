package model;

public class TestModel {
    public static void main(String[] args) {

        Task t1 = new Task("Test Task", "20 Apr", false);

        System.out.println(t1.getDetails());

        t1.markComplete();

        System.out.println("After update: " + t1.getDetails());
    }
}