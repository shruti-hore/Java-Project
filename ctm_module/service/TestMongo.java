package service;

import com.mongodb.client.*;
import org.bson.Document;

public class TestMongo {

    public static void main(String[] args) {

        // Connect to MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

        // Access database
        MongoDatabase database = mongoClient.getDatabase("task_manager");

        // Access collection
        MongoCollection<Document> collection = database.getCollection("tasks");

        System.out.println("Connected to MongoDB!");

        // ===== INSERT TEST =====
        Document task = new Document("title", "Test Task")
                .append("description", "MongoDB connection test")
                .append("deadline", "2026-04-25")
                .append("completed", false);

        collection.insertOne(task);
        System.out.println("Inserted test task!");

        // ===== FETCH TEST =====
        FindIterable<Document> tasks = collection.find();

        System.out.println("\nTasks in DB:");
        for (Document doc : tasks) {
            System.out.println(doc.toJson());
        }

        // Close connection
        mongoClient.close();
    }
}