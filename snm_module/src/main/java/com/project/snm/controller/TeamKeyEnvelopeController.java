package com.project.snm.controller;

import com.project.snm.model.mysql.TeamKeyEnvelopeRecord;
import com.project.snm.repository.TeamKeyEnvelopeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/teams/{teamId}/envelopes")
public class TeamKeyEnvelopeController {

    private final TeamKeyEnvelopeRepository repository;

    public TeamKeyEnvelopeController(TeamKeyEnvelopeRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> uploadEnvelope(@PathVariable String teamId, @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String envelopeBase64 = body.get("envelopeBase64");

        if (userId == null || envelopeBase64 == null) {
            return ResponseEntity.badRequest().build();
        }

        // Step 7: Upsert logic (find and update or create new)
        TeamKeyEnvelopeRecord record = repository.findByTeamIdAndUserId(teamId, userId)
                .orElse(new TeamKeyEnvelopeRecord());
        
        record.setTeamId(teamId);
        record.setUserId(userId);
        record.setEnvelopeBase64(envelopeBase64);

        repository.save(record);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyEnvelope(@PathVariable String teamId, Principal principal) {
        // Step 7: Resolve userId from JWT Principal (not from request body)
        String userId = principal.getName();

        return repository.findByTeamIdAndUserId(teamId, userId)
                .map(record -> ResponseEntity.ok(Map.of("envelopeBase64", record.getEnvelopeBase64())))
                .orElse(ResponseEntity.notFound().build());
    }
}
