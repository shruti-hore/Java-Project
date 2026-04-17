package service;

import java.util.*;
import model.Task;

public class MockDataService {

    public static List<Task> getSampleTasks() {

        List<Task> list = new ArrayList<>();

        list.add(new Task("Complete JavaFX UI", "20 Apr", false));
        list.add(new Task("Study DSA", "22 Apr", false));
        list.add(new Task("Submit Assignment", "25 Apr", true));

        return list;
    }
}