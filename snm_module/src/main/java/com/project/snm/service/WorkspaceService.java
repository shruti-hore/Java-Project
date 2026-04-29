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
    private final com.project.snm.repository.JoinRequestRepository joinRequestRepository;

    public WorkspaceService(TeamRepository teamRepository,
                            TeamMemberRepository teamMemberRepository,
                            UserRepository userRepository,
                            com.project.snm.repository.JoinRequestRepository joinRequestRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.joinRequestRepository = joinRequestRepository;
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

    /** Joins a workspace by invite code. Creates a PENDING join request. */
    public Team joinWorkspace(String workspaceCode, String userId) {
        Team team = teamRepository.findByWorkspaceCode(workspaceCode)
                .orElseThrow(() -> new NotFoundException("Workspace not found for code: " + workspaceCode));

        if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userId)) {
            log.info("User {} is already a member of team {}", userId, team.getId());
            return team;
        }

        if (joinRequestRepository.findByTeamIdAndUserIdAndStatus(team.getId(), userId, "PENDING").isPresent()) {
            log.info("Join request already pending for user {} to team {}", userId, team.getId());
            return team;
        }

        com.project.snm.model.mysql.JoinRequest request = new com.project.snm.model.mysql.JoinRequest();
        request.setTeamId(team.getId());
        request.setUserId(userId);
        request.setUsername(getUsernameById(userId));
        request.setStatus("PENDING");
        joinRequestRepository.save(request);

        log.info("User {} requested to join workspace {}", userId, team.getId());
        return team;
    }

    /** Finalizes membership after owner approval. */
    public void acceptJoinRequest(Long requestId) {
        com.project.snm.model.mysql.JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Join request not found: " + requestId));
        
        request.setStatus("ACCEPTED");
        joinRequestRepository.save(request);

        TeamMember member = new TeamMember();
        member.setTeamId(request.getTeamId());
        member.setUserId(request.getUserId());
        member.setRole("MEMBER");
        member.setJoinedAt(Instant.now());
        teamMemberRepository.save(member);
        
        log.info("Join request accepted: {} for team {}", request.getUserId(), request.getTeamId());
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

    /** Gets the username for a given userId UUID. */
    public String getUsernameById(String userId) {
        return userRepository.findById(userId)
                .map(UserRecord::getUsername)
                .orElse("User");
    }

    private String generateWorkspaceCode() {
        byte[] bytes = new byte[6];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Returns all members of a team. */
    public List<TeamMember> getTeamMembers(String teamId) {
        return teamMemberRepository.findByTeamId(teamId);
    }

    /** Returns the public key of any user by ID. */
    public String getUserPublicKeyBase64(String userId) {
        return userRepository.findById(userId)
                .map(UserRecord::getPublicKeyBase64)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
