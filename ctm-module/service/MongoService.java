package service;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.combine;
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

            list.add(new Task(id.toString(), title, desc, deadline, completed, status));
        }

        return list;
    }

    public void deleteTask(String id) {
        collection.deleteOne(eq("_id", new ObjectId(id)));
    }

    public void updateStatus(String id, String status) {
        collection.updateOne(
            eq("_id", new ObjectId(id)),
            set("status", status)
        );
    }

    public void updateCompletion(String id, boolean completed) {
        collection.updateOne(
            eq("_id", new ObjectId(id)),
            set("completed", completed)
        );
    }

    public void updateTask(Task t) {
        collection.updateOne(
            eq("_id", new ObjectId(t.getId())),
            combine(
                set("title", t.getTitle()),
                set("description", t.getDescription()),
                set("deadline", t.getDeadline()),
                set("status", t.getStatus()),
                set("completed", t.isCompleted())
            )
        );
    }
}