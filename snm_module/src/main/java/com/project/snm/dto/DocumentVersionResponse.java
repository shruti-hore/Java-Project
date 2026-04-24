package com.project.snm.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DocumentVersionResponse {
    private Long versionSeq;
    private String ciphertextBase64;
    private String nonceBase64;
    private String aadBase64;
    private Map<String, Integer> vectorClock;
}
