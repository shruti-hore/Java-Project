package com.project.snm.service;

import com.project.snm.dto.CreateTeamRequest;
import com.project.snm.model.mysql.Team;
import com.project.snm.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team createTeam(CreateTeamRequest request) {
        Team team = new Team();
        team.setTeamName(request.getTeamName());
        team.setCreatedBy(request.getCreatedBy());
        team.setCreatedAt(Instant.now());

        return teamRepository.save(team);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
}