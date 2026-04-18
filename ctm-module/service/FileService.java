package service;

import model.Task;

import java.io.*;
import java.util.*;

public class FileService {

    private static final String FILE_NAME = "tasks.txt";

    // Save tasks to file
    public static void saveTasks(List<Task> tasks) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (Task t : tasks) {
                // Format: title|deadline|status
                writer.write(t.getTitle() + "|" + t.getDeadline() + "|" + t.isCompleted());
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load tasks from file
    public static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();

        File file = new File(FILE_NAME);

        // If file doesn't exist, return empty list
        if (!file.exists()) return tasks;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {

            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");

                String title = parts[0];
                String deadline = parts[1];
                boolean status = Boolean.parseBoolean(parts[2]);

                tasks.add(new Task(title, deadline, status));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return tasks;
    }
}