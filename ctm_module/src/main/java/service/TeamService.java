package service;

import model.Team;
import java.util.List;

public class TeamService {
    private final MongoService mongo = new MongoService();

    public List<Team> getTeamsForUser(String email) {
        return mongo.getTeamsForUser(email);
    }

    public void createTeam(String name, String ownerEmail) {
        Team team = new Team(null, name, ownerEmail);
        team.addMember(ownerEmail);
        mongo.createTeam(team);
    }

    public void joinTeam(String teamId, String email) {
        mongo.inviteToTeam(teamId, email);
    }
}
