package com.project.snm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginVerifyResponse {
    private String jwt;
    private String userId;
    private String username;
    private String publicKeyBase64;

    public LoginVerifyResponse(String jwt, String userId, String username, String publicKeyBase64) {
        this.jwt = jwt;
        this.userId = userId;
        this.username = username;
        this.publicKeyBase64 = publicKeyBase64;
    }
}
