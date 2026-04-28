package service;

import com.mongodb.client.*;
import client.model.Task;
import model.Team;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

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
        Document query = new Document();
        if (userId != null) query.append("userId", userId);
        if (teamId != null) query.append("teamId", teamId);

        for (Document doc : collection.find(query)) {
            list.add(new Task(
                doc.getObjectId("_id").toString(),
                doc.getString("title"),
                doc.getString("description"),
                doc.getString("deadline"),
                doc.getBoolean("completed", false),
                doc.getString("status"),
                doc.getString("priority"),
                doc.getString("userId"),
                doc.getString("teamId")
            ));
        }
        return list;
    }

    public void updateTask(Task t) {
        collection.updateOne(eq("_id", new ObjectId(t.getId())),
            combine(
                set("title", t.getTitle()),
                set("description", t.getDescription()),
                set("deadline", t.getDeadline()),
                set("completed", t.isCompleted()),
                set("status", t.getStatus()),
                set("priority", t.getPriority())
            )
        );
    }

    public void deleteTask(String id) {
        collection.deleteOne(eq("_id", new ObjectId(id)));
    }

    public void updateStatus(String id, String status) {
        collection.updateOne(eq("_id", new ObjectId(id)), set("status", status));
    }

    public void updateCompletion(String id, boolean completed) {
        collection.updateOne(eq("_id", new ObjectId(id)), set("completed", completed));
    }

    public void createUser(String username, String password) {
        db.getCollection("users").insertOne(new Document("username", username).append("password", password));
    }

    public Document getUserByUsername(String username) {
        return db.getCollection("users").find(eq("username", username)).first();
    }

    public void createTeam(Team team) {
        Document doc = new Document("name", team.getName())
                .append("ownerId", team.getOwnerId())
                .append("members", team.getMembers());
        db.getCollection("teams").insertOne(doc);
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

    public void inviteToTeam(String teamId, String email) {
        db.getCollection("teams").updateOne(eq("_id", new ObjectId(teamId)), com.mongodb.client.model.Updates.addToSet("members", email));
    }
}
