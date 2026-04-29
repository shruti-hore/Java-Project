package com.project.snm.controller;

import com.project.snm.model.mysql.DocumentVersion;
import com.project.snm.model.mysql.Team;
import com.project.snm.model.mysql.TeamKeyEnvelopeRecord;
import com.project.snm.repository.DocumentVersionRepository;
import com.project.snm.repository.TeamKeyEnvelopeRepository;
import com.project.snm.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Workspace Controller — handles /workspaces/** endpoints.
 *
 * The JWT principal name is the UserRecord.userId (UUID string).
 * All authenticated endpoints extract userId from Principal.getName().
 */
@Slf4j
@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final TeamKeyEnvelopeRepository envelopeRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public WorkspaceController(WorkspaceService workspaceService,
                               TeamKeyEnvelopeRepository envelopeRepository,
                               DocumentVersionRepository documentVersionRepository) {
        this.workspaceService = workspaceService;
        this.envelopeRepository = envelopeRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    // ─── GET /workspaces ─────────────────────────────────────────────────────
    /** Returns all workspaces the authenticated user is a member of. */
    @GetMapping
    public ResponseEntity<?> listWorkspaces(Principal principal) {
        String userId = principal.getName();
        log.info("Listing workspaces for userId={}", userId);

        List<Team> teams = workspaceService.getWorkspacesForUser(userId);
        List<Map<String, String>> result = teams.stream().map(t -> {
            String ownerName = workspaceService.getUsernameById(t.getOwnerUserId());
            return Map.of(
                "teamId",      t.getId(),
                "name",        t.getTeamName() != null ? t.getTeamName() : "",
                "ownerUserId", t.getOwnerUserId() != null ? t.getOwnerUserId() : "",
                "ownerUsername", ownerName != null ? ownerName : "User",
                "lastSyncedAt", ""
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ─── POST /workspaces ────────────────────────────────────────────────────
    /** Creates a new workspace. Expects {name, ownerPublicKeyBase64} in body. */
    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody Map<String, String> body,
                                             Principal principal) {
        String userId = principal.getName();
        String name   = body.get("name");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("name is required");
        }

        Team team = workspaceService.createWorkspace(name, userId);
        log.info("Created workspace {} (code={}) for userId={}", team.getId(), team.getWorkspaceCode(), userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "teamId",        team.getId(),
                "workspaceCode", team.getWorkspaceCode()
        ));
    }

    // ─── POST /workspaces/join ───────────────────────────────────────────────
    /** Joins a workspace by invite code. Expects {workspaceCode} in body. */
    @PostMapping("/join")
    public ResponseEntity<?> joinWorkspace(@RequestBody Map<String, String> body,
                                           Principal principal) {
        String userId = principal.getName();
        String code   = body.get("workspaceCode");

        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body("workspaceCode is required");
        }

        Team team = workspaceService.joinWorkspace(code, userId);
        log.info("User {} joined workspace {}", userId, team.getId());

        return ResponseEntity.ok(Map.of(
                "teamId", team.getId(),
                "name",   team.getTeamName() != null ? team.getTeamName() : "",
                "status", "JOINED"
        ));
    }

    // ─── GET /workspaces/{teamId}/envelope ───────────────────────────────────
    /** Returns the team-key envelope for the calling user in this workspace. */
    @GetMapping("/{teamId}/envelope")
    public ResponseEntity<String> getEnvelope(@PathVariable String teamId,
                                              Principal principal) {
        String userId = principal.getName();
        return envelopeRepository.findByTeamIdAndUserId(teamId, userId)
                .map(env -> ResponseEntity.ok(env.getEnvelopeBase64()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── POST /workspaces/{teamId}/envelopes ─────────────────────────────────
    /** Uploads (or overwrites) the team-key envelope for a specific user. */
    @PostMapping("/{teamId}/envelopes")
    public ResponseEntity<?> uploadEnvelope(@PathVariable String teamId,
                                            @RequestBody Map<String, String> body) {
        String userId       = body.get("userId");
        String envelopeB64  = body.get("envelopeBase64");

        if (userId == null || envelopeB64 == null) {
            return ResponseEntity.badRequest().body("userId and envelopeBase64 are required");
        }

        TeamKeyEnvelopeRecord record = envelopeRepository
                .findByTeamIdAndUserId(teamId, userId)
                .orElse(new TeamKeyEnvelopeRecord());

        record.setTeamId(teamId);
        record.setUserId(userId);
        record.setEnvelopeBase64(envelopeB64);
        envelopeRepository.save(record);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ─── GET /workspaces/{teamId}/owner-public-key ───────────────────────────
    /** Returns the workspace owner's raw X25519 public key bytes. */
    @GetMapping("/{teamId}/owner-public-key")
    public ResponseEntity<byte[]> getOwnerPublicKey(@PathVariable String teamId) {
        String publicKeyBase64 = workspaceService.getOwnerPublicKeyBase64(teamId);
        byte[] raw = Base64.getDecoder().decode(publicKeyBase64);
        return ResponseEntity.ok(raw);
    }

    // ─── GET /workspaces/{teamId}/metadata ───────────────────────────────────
    /** Returns the latest document version metadata for all docs in this workspace. */
    @GetMapping("/{teamId}/metadata")
    public ResponseEntity<?> getMetadata(@PathVariable String teamId) {
        List<DocumentVersion> latest = documentVersionRepository.findLatestVersionsByTeamId(teamId);
        List<Map<String, Object>> result = latest.stream().map(v -> Map.<String, Object>of(
                "documentUuid", v.getDocumentUuid(),
                "versionSeq",   v.getVersionSeq()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ─── GET /workspaces/{teamId}/members ───────────────────────────────────
    /** Returns a list of all members in the workspace. */
    @GetMapping("/{teamId}/members")
    public ResponseEntity<?> listMembers(@PathVariable String teamId) {
        List<com.project.snm.model.mysql.TeamMember> members = workspaceService.getTeamMembers(teamId);
        List<Map<String, Object>> result = members.stream().map(m -> {
            boolean hasEnv = envelopeRepository.findByTeamIdAndUserId(teamId, m.getUserId()).isPresent();
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("userId", m.getUserId());
            map.put("username", workspaceService.getUsernameById(m.getUserId()));
            map.put("role", m.getRole());
            map.put("hasEnvelope", hasEnv);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ─── GET /workspaces/users/{userId}/public-key ──────────────────────────
    /** Returns the raw X25519 public key bytes for any user. */
    @GetMapping("/users/{userId}/public-key")
    public ResponseEntity<byte[]> getUserPublicKey(@PathVariable String userId) {
        String publicKeyBase64 = workspaceService.getUserPublicKeyBase64(userId);
        byte[] raw = Base64.getDecoder().decode(publicKeyBase64);
        return ResponseEntity.ok(raw);
    }
}
