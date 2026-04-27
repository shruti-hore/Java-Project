package model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String id;
    private String name;
    private String ownerId;
    private String ownerUsername;
    private List<String> members;

    public Team(String id, String name, String ownerId, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.members = new ArrayList<>();
        this.members.add(ownerId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public List<String> getMembers() { return members; }
    public void addMember(String email) { if (!members.contains(email)) members.add(email); }
}
