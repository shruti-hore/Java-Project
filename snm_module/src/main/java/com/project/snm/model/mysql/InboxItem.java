package com.project.snm.model.mysql;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "inbox_items")
public class InboxItem {
    @Id
    private String id;
    
    private String type; // "INVITE" | "REQUEST"
    private String senderId;
    private String senderName;
    private String receiverId;
    private String teamId;
    private String teamName;
    private String status; // "PENDING", "ACCEPTED", "REJECTED"
    private Instant timestamp;
}
