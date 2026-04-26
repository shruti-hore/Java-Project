package service;

import model.Team;
import java.util.List;
import java.io.IOException;

public class TeamService {
    private final ui.http.HttpAuthClient httpClient;

    public TeamService(ui.http.HttpAuthClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<Team> getTeamsForUser(String email) throws IOException {
        try {
            List<ui.http.HttpAuthClient.WorkspaceSummary> summaries = httpClient.fetchWorkspaces();
            return summaries.stream()
                .map(ws -> new Team(ws.teamId(), ws.name(), ws.ownerUserId()))
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            throw new IOException("Failed to fetch workspaces", e);
        }
    }

    public void createTeam(String name, String ownerEmail) {
        throw new UnsupportedOperationException("Use DashboardUI.showCreateTeamDialog()");
    }

    public void joinTeam(String teamId, String email) {
        throw new UnsupportedOperationException("Use DashboardUI.showJoinTeamDialog()");
    }
}
