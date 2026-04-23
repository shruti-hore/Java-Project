package com.project.snm.dto;

import lombok.Data;

@Data
public class CreateTeamRequest {
    private String teamName;
    private Long createdBy;
}