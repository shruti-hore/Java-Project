package service;

import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.combine;
import com.mongodb.client.*;
import client.model.Task;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.io.*;

import static com.mongodb.client.model.Filters.eq;

public class MongoService {

    private final MongoCollection<Document> collection;
    private final MongoDatabase db;

    public MongoService() {
        String uri = "mongodb://localhost:27017";
        String dbName = "taskdb";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                uri = prop.getProperty("mongo.uri", uri);
                dbName = prop.getProperty("mongo.db", dbName);
            }
        } catch (IOException ex) {
            // Fallback to defaults
        }

        MongoClient client = MongoClients.create(uri);
        db = client.getDatabase(dbName);
        collection = db.getCollection("tasks");
    }

    /**
     * @deprecated Use EncryptedTaskService
     */
    @Deprecated
    public String addTask(Task task) {
        Document doc = new Document("title", task.getTitle())
                .append("description", task.getDescription())
                .append("deadline", task.getDeadline())
                .append("completed", task.isCompleted())
                .append("status", task.getStatus())
                .append("priority", task.getPriority())
                .append("userId", task.getUserId())
                .append("teamId", task.getTeamId());

        collection.insertOne(doc);
        return doc.getObjectId("_id").toString();
    }

    public List<Task> getTasks(String userId, String teamId) {
        List<Task> list = new ArrayList<>();
        List<Document> filters = new ArrayList<>();
        if (userId != null)
            filters.add(new Document("userId", userId));
        if (teamId != null)
            filters.add(new Document("teamId", teamId));

        Document query = filters.isEmpty() ? new Document() : new Document("$or", filters);

        for (Document doc : collection.find(query)) {
            ObjectId id = doc.getObjectId("_id");
            String title = doc.getString("title");
            String desc = doc.getString("description");
            String deadline = doc.getString("deadline");
            Boolean completed = doc.getBoolean("completed");
            String status = doc.getString("status");
            String priority = doc.getString("priority");
            String uId = doc.getString("userId");
            String tId = doc.getString("teamId");

            if (status == null)
                status = "DEADLINE";
            if (priority == null)
                priority = "Low";
            if (completed == null)
                completed = false;

            list.add(new Task(id.toString(), title, desc, deadline, completed, status, priority, uId, tId));
        }

        return list;
    }

    public void deleteTask(String id) {
        if (id == null)
            return;
        collection.deleteOne(eq("_id", new ObjectId(id)));
    }

    public void updateStatus(String id, String status) {
        if (id == null)
            return;
        collection.updateOne(
                eq("_id", new ObjectId(id)),
                set("status", status));
    }

    public void updateCompletion(String id, boolean completed) {
        if (id == null)
            return;
        collection.updateOne(
                eq("_id", new ObjectId(id)),
                set("completed", completed));
    }

    /**
     * @deprecated Use EncryptedTaskService
     */
    @Deprecated
    public void updateTask(Task t) {
        if (t.getId() == null)
            return;
        collection.updateOne(
                eq("_id", new ObjectId(t.getId())),
                combine(
                        set("title", t.getTitle()),
                        set("description", t.getDescription()),
                        set("deadline", t.getDeadline()),
                        set("completed", t.isCompleted()),
                        set("status", t.getStatus()),
                        set("priority", t.getPriority())));
    }

    public void createTeam(model.Team team) {
        Document doc = new Document("name", team.getName())
                .append("ownerId", team.getOwnerId())
                .append("members", team.getMembers());

        MongoCollection<Document> teams = db.getCollection("teams");
        teams.insertOne(doc);
        team.setId(doc.getObjectId("_id").toString());
    }

    @SuppressWarnings("unchecked")
    public List<Team> getTeamsForUser(String email) {
        List<Team> list = new ArrayList<>();
        for (Document doc : db.getCollection("teams").find(com.mongodb.client.model.Filters.in("members", email))) {
            String ownerUsername = doc.getString("ownerUsername");
            if (ownerUsername == null) ownerUsername = doc.getString("ownerId");
            Team t = new Team(doc.getObjectId("_id").toString(), doc.getString("name"), doc.getString("ownerId"), ownerUsername);
            List<String> members = (List<String>) doc.get("members");
            if (members != null) for (String m : members) t.addMember(m);
            list.add(t);
        }
        return list;
    }
}
