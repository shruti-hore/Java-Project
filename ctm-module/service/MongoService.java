package service;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MongoService {

    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "task_manager";
    private static final String COLLECTION = "tasks";

    private static MongoCollection<Document> getCollection() {
        MongoClient client = MongoClients.create(URI);
        MongoDatabase db = client.getDatabase(DB_NAME);
        return db.getCollection(COLLECTION);
    }

    // ===== ADD TASK =====
    public static void addTask(Task task) {

        MongoCollection<Document> col = getCollection();

        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("deadline", task.getDeadline().toString())
                .append("completed", task.isCompleted());

        col.insertOne(doc);
    }

    // ===== GET TASKS =====
    public static List<Task> getTasks() {

        MongoCollection<Document> col = getCollection();
        List<Task> list = new ArrayList<>();

        for (Document d : col.find()) {

            Task t = new Task(
                    d.getString("title"),
                    d.getString("description"),
                    LocalDate.parse(d.getString("deadline")),
                    d.getBoolean("completed")
            );

            list.add(t);
        }

        return list;
    }

    // ===== DELETE TASK =====
    public static void deleteTask(Task task) {

        MongoCollection<Document> col = getCollection();

        col.deleteOne(Filters.and(
                Filters.eq("title", task.getTitle()),
                Filters.eq("deadline", task.getDeadline().toString())
        ));
    }

    // ===== UPDATE TASK =====
    public static void updateTask(Task oldTask, Task newTask) {

        MongoCollection<Document> col = getCollection();

        col.updateOne(
                Filters.and(
                        Filters.eq("title", oldTask.getTitle()),
                        Filters.eq("deadline", oldTask.getDeadline().toString())
                ),
                new Document("$set", new Document("title", newTask.getTitle())
                        .append("description", newTask.getDescription())
                        .append("deadline", newTask.getDeadline().toString())
                        .append("completed", newTask.isCompleted()))
        );
    }
}