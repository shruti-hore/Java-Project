package com.project.snm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConflictResponse {
    private String currentBlobRef;
    private String incomingBlobRef;
}
