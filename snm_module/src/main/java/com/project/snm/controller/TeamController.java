package com.project.snm.controller;

import com.project.snm.model.mysql.Team;
import com.project.snm.service.TeamService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }

    /** Returns the workspace invite code for a given teamId. */
    @GetMapping("/{teamId}/code")
    public ResponseEntity<java.util.Map<String, String>> getWorkspaceCode(@PathVariable String teamId) {
        return teamService.getTeamById(teamId)
                .map(t -> ResponseEntity.ok(java.util.Map.of("code", t.getWorkspaceCode() != null ? t.getWorkspaceCode() : "")))
                .orElse(ResponseEntity.notFound().build());
    }
}