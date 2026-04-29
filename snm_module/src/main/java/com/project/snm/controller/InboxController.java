package com.project.snm.controller;

import com.project.snm.model.mysql.InboxItem;
import com.project.snm.service.InboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inbox")
@RequiredArgsConstructor
public class InboxController {
    private final InboxService inboxService;

    @GetMapping
    public List<InboxItem> getInbox(Principal principal) {
        return inboxService.getInboxForUser(principal.getName());
    }

    @PostMapping("/invite")
    public ResponseEntity<?> sendInvite(@RequestBody Map<String, String> body, Principal principal) {
        inboxService.sendInvite(body.get("teamId"), principal.getName(), body.get("email"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestToJoin(@RequestBody Map<String, String> body, Principal principal) {
        inboxService.requestToJoin(body.get("inviteCode"), principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        inboxService.respond(id, body.get("accept"));
        return ResponseEntity.ok().build();
    }
}
