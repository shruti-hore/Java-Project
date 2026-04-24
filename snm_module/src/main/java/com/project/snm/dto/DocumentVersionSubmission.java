package com.project.snm.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DocumentVersionSubmission {
    private Long teamId;
    private String ciphertextBase64;
    private String nonceBase64;
    private String aadBase64;
    private Map<String, Integer> vectorClock;
    private Long expectedVersionSeq;
}
