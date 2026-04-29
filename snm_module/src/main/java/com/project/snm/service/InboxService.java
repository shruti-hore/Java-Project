package com.project.snm.service;

import com.project.snm.model.mysql.InboxItem;
import com.project.snm.model.mysql.Team;
import com.project.snm.repository.InboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxRepository inboxRepository;
    private final WorkspaceService workspaceService;
    private final ServerEncryptionService encryptionService;

    public List<InboxItem> getInboxForUser(String userId) {
        List<InboxItem> items = inboxRepository.findByReceiverIdOrderByTimestampDesc(userId);
        // Temporarily disabled decryption
        // for (InboxItem item : items) {
        //     item.setSenderName(encryptionService.decrypt(item.getSenderName()));
        //     item.setTeamName(encryptionService.decrypt(item.getTeamName()));
        // }
        return items;
    }

    public void sendInvite(String teamId, String ownerId, String receiverEmail) {
        Team team = workspaceService.getTeamById(teamId);
        InboxItem item = new InboxItem();
        item.setId(UUID.randomUUID().toString());
        item.setType("INVITE");
        item.setSenderId(ownerId);
        item.setSenderName(workspaceService.getUsernameById(ownerId));
        item.setReceiverId(receiverEmail);
        item.setTeamId(teamId);
        item.setTeamName(team.getTeamName());
        item.setStatus("PENDING");
        item.setTimestamp(Instant.now());
        inboxRepository.save(item);
    }

    public void requestToJoin(String workspaceCode, String userId) {
        Team team = workspaceService.joinWorkspaceByCodeOnly(workspaceCode);
        InboxItem item = new InboxItem();
        item.setId(UUID.randomUUID().toString());
        item.setType("REQUEST");
        item.setSenderId(userId);
        item.setSenderName(workspaceService.getUsernameById(userId));
        item.setReceiverId(team.getOwnerUserId());
        item.setTeamId(team.getId());
        item.setTeamName(team.getTeamName());
        item.setStatus("PENDING");
        item.setTimestamp(Instant.now());
        inboxRepository.save(item);
    }

    public void respond(String itemId, boolean accept) {
        InboxItem item = inboxRepository.findById(itemId).orElseThrow();
        if (accept) {
            item.setStatus("ACCEPTED");
            workspaceService.joinWorkspace(item.getTeamId(), item.getReceiverId());
        } else {
            item.setStatus("REJECTED");
        }
        inboxRepository.delete(item); // User said remove from inbox UI
    }
}
