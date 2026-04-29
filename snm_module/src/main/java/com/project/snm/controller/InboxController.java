package com.project.snm.controller;

import com.project.snm.model.mysql.JoinRequest;
import com.project.snm.model.mysql.Team;
import com.project.snm.repository.JoinRequestRepository;
import com.project.snm.repository.TeamRepository;
import com.project.snm.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/inbox")
public class InboxController {

    private final JoinRequestRepository joinRequestRepository;
    private final TeamRepository teamRepository;
    private final WorkspaceService workspaceService;

    public InboxController(JoinRequestRepository joinRequestRepository,
                           TeamRepository teamRepository,
                           WorkspaceService workspaceService) {
        this.joinRequestRepository = joinRequestRepository;
        this.teamRepository = teamRepository;
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public ResponseEntity<?> getInbox(Principal principal) {
        String userId = principal.getName();
        // Find all teams where this user is owner
        List<Team> ownedTeams = teamRepository.findByOwnerUserId(userId);
        List<String> teamIds = ownedTeams.stream().map(Team::getId).collect(Collectors.toList());

        if (teamIds.isEmpty()) return ResponseEntity.ok(List.of());

        // Find all PENDING requests for these teams
        List<JoinRequest> requests = joinRequestRepository.findByTeamIdInAndStatus(teamIds, "PENDING");
        
        List<Map<String, Object>> result = requests.stream().map(r -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", r.getId().toString());
            map.put("type", "INVITE");
            map.put("usernameOrEmail", r.getUsername());
            map.put("teamId", r.getTeamId());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean accept = body.getOrDefault("accept", false);
        if (accept) {
            workspaceService.acceptJoinRequest(id);
        } else {
            JoinRequest request = joinRequestRepository.findById(id).orElse(null);
            if (request != null) {
                request.setStatus("REJECTED");
                joinRequestRepository.save(request);
            }
        }
        return ResponseEntity.ok().build();
    }
}
