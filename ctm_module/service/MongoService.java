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
    private final MongoDatabase db;

    public MongoService() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        db = client.getDatabase("taskdb");
        collection = db.getCollection("tasks");
    }

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
        if (userId != null) filters.add(new Document("userId", userId));
        if (teamId != null) filters.add(new Document("teamId", teamId));
        
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

            if (status == null) status = "DEADLINE";
            if (priority == null) priority = "Low";
            if (completed == null) completed = false;

            list.add(new Task(id.toString(), title, desc, deadline, completed, status, priority, uId, tId));
        }

        return list;
    }

    public void deleteTask(String id) {
        if (id == null) return;
        collection.deleteOne(eq("_id", new ObjectId(id)));
    }

    public void updateStatus(String id, String status) {
        if (id == null) return;
        collection.updateOne(
            eq("_id", new ObjectId(id)),
            set("status", status)
        );
    }

    public void updateCompletion(String id, boolean completed) {
        if (id == null) return;
        collection.updateOne(
            eq("_id", new ObjectId(id)),
            set("completed", completed)
        );
    }

    public void updateTask(Task t) {
        if (t.getId() == null) return;
        collection.updateOne(
            eq("_id", new ObjectId(t.getId())),
            combine(
                set("title", t.getTitle()),
                set("description", t.getDescription()),
                set("deadline", t.getDeadline()),
                set("status", t.getStatus()),
                set("completed", t.isCompleted()),
                set("priority", t.getPriority()),
                set("userId", t.getUserId()),
                set("teamId", t.getTeamId())
            )
        );
    }

    public void createTeam(model.Team team) {
        Document doc = new Document("name", team.getName())
                .append("ownerId", team.getOwnerId())
                .append("members", team.getMembers());
        
        MongoCollection<Document> teams = db.getCollection("teams");
        teams.insertOne(doc);
        team.setId(doc.getObjectId("_id").toString());
    }

    public void inviteToTeam(String teamId, String email) {
        MongoCollection<Document> teams = db.getCollection("teams");
        teams.updateOne(
            eq("_id", new ObjectId(teamId)),
            com.mongodb.client.model.Updates.addToSet("members", email)
        );
    }

    public List<model.Team> getTeamsForUser(String email) {
        List<model.Team> list = new ArrayList<>();
        MongoCollection<Document> teams = db.getCollection("teams");
        
        for (Document doc : teams.find(com.mongodb.client.model.Filters.in("members", email))) {
            model.Team t = new model.Team(
                doc.getObjectId("_id").toString(),
                doc.getString("name"),
                doc.getString("ownerId")
            );
            List<String> m = (List<String>) doc.get("members");
            if (m != null) {
                for (String member : m) t.addMember(member);
            }
            list.add(t);
        }
        return list;
    }
}