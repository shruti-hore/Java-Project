package service;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import model.Task;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MongoService {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "taskmanager";
    private static final String COLLECTION_NAME = "tasks";

    private static MongoCollection<Document> collection;

    static {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        collection = database.getCollection(COLLECTION_NAME);
    }

    // ===================== ADD TASK =====================
    public static void addTask(Task task) {
        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("deadline", task.getDeadline().toString())
                .append("completed", task.isCompleted());

        collection.insertOne(doc);

        // IMPORTANT: set generated _id back to task
        task.setId(doc.getObjectId("_id"));
    }

    // ===================== GET TASKS =====================
    public static List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        FindIterable<Document> docs = collection.find();

        for (Document doc : docs) {
            ObjectId id = doc.getObjectId("_id");
            String title = doc.getString("title");
            String desc = doc.getString("description");
            LocalDate deadline = LocalDate.parse(doc.getString("deadline"));
            boolean completed = doc.getBoolean("completed", false);

            Task t = new Task(id, title, desc, deadline, completed);
            tasks.add(t);
        }

        return tasks;
    }

    // ===================== DELETE TASK =====================
    public static void deleteTask(Task task) {
        collection.deleteOne(Filters.eq("_id", task.getId()));
    }

    // ===================== UPDATE TASK =====================
    public static void updateTask(Task oldTask, Task newTask) {

        collection.updateOne(
                Filters.eq("_id", oldTask.getId()),

                Updates.combine(
                        Updates.set("title", newTask.getTitle()),
                        Updates.set("description", newTask.getDescription()),
                        Updates.set("deadline", newTask.getDeadline().toString()),
                        Updates.set("completed", newTask.isCompleted())
                )
        );
    }
}