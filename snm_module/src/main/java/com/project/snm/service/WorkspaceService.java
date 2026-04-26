package com.project.snm.service;

import com.project.snm.model.mysql.Team;
import com.project.snm.model.mysql.TeamMember;
import com.project.snm.model.mysql.UserRecord;
import com.project.snm.repository.TeamMemberRepository;
import com.project.snm.repository.TeamRepository;
import com.project.snm.repository.UserRepository;
import com.project.snm.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkspaceService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public WorkspaceService(TeamRepository teamRepository,
                            TeamMemberRepository teamMemberRepository,
                            UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    /** Returns all teams the given user (by userId UUID) is a member of. */
    public List<Team> getWorkspacesForUser(String userId) {
        List<TeamMember> memberships = teamMemberRepository.findByUserId(userId);
        List<String> teamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .collect(Collectors.toList());
        return teamRepository.findAllById(teamIds);
    }

    /** Creates a new team, adds creator as OWNER member, returns the team. */
    public Team createWorkspace(String name, String ownerUserId) {
        String code = generateWorkspaceCode();

        Team team = new Team();
        team.setTeamName(name);
        team.setOwnerUserId(ownerUserId);
        team.setWorkspaceCode(code);
        team.setCreatedAt(Instant.now());
        team = teamRepository.save(team);

        // Auto-add creator as OWNER member
        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(ownerUserId);
        member.setRole("OWNER");
        member.setJoinedAt(Instant.now());
        teamMemberRepository.save(member);

        log.info("Workspace created: {} by user {}", team.getId(), ownerUserId);
        return team;
    }

    /** Joins a workspace by invite code. Adds the user as a MEMBER. */
    public Team joinWorkspace(String workspaceCode, String userId) {
        Team team = teamRepository.findByWorkspaceCode(workspaceCode)
                .orElseThrow(() -> new NotFoundException("Workspace not found for code: " + workspaceCode));

        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userId)) {
            log.info("User {} is already a member of team {}", userId, team.getId());
            return team; // idempotent
        }

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinedAt(Instant.now());
        teamMemberRepository.save(member);

        log.info("User {} joined workspace {}", userId, team.getId());
        return team;
    }

    /** Fetches a team by ID, throws 404 if not found. */
    public Team getTeamById(String teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Workspace not found: " + teamId));
    }

    /** Gets the owner's public key bytes for a given team. */
    public String getOwnerPublicKeyBase64(String teamId) {
        Team team = getTeamById(teamId);
        UserRecord owner = userRepository.findById(team.getOwnerUserId())
                .orElseThrow(() -> new NotFoundException("Owner not found for team: " + teamId));
        return owner.getPublicKeyBase64();
    }

    private String generateWorkspaceCode() {
        byte[] bytes = new byte[6];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
