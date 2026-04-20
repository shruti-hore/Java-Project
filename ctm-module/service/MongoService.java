package service;

import com.mongodb.client.*;
import model.Task;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

public class MongoService {

    private final MongoCollection<Document> collection;

    public MongoService() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("taskdb");
        collection = db.getCollection("tasks");
    }

    public void addTask(Task task) {
        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("deadline", task.getDeadline())
                .append("completed", task.isCompleted())
                .append("status", task.getStatus());

        collection.insertOne(doc);
    }

    public List<Task> getTasks() {
        List<Task> list = new ArrayList<>();

        for (Document doc : collection.find()) {
            ObjectId id = doc.getObjectId("_id");
            String title = doc.getString("title");
            String desc = doc.getString("description");
            String deadline = doc.getString("deadline");
            Boolean completed = doc.getBoolean("completed");

            String status = doc.getString("status");

            // 🔥 CRITICAL FIX
            if (status == null) status = "DEADLINE";

            list.add(new Task(id, title, desc, deadline, completed, status));
        }

        return list;
    }

    public void deleteTask(ObjectId id) {
        collection.deleteOne(eq("_id", id));
    }

    public void updateStatus(ObjectId id, String status) {
        if (status == null) status = "DEADLINE";

        collection.updateOne(eq("_id", id),
                new Document("$set", new Document("status", status)));
    }

    public void updateCompletion(ObjectId id, boolean completed) {
        collection.updateOne(eq("_id", id),
                new Document("$set", new Document("completed", completed)));
    }
}