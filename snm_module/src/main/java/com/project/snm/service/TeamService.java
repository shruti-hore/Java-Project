package com.project.snm.service;

import com.project.snm.model.mysql.Team;
import com.project.snm.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public java.util.Optional<Team> getTeamById(String teamId) {
        return teamRepository.findById(teamId);
    }
}