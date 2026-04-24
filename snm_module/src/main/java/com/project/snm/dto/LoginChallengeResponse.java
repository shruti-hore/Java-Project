package com.project.snm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginChallengeResponse {
    private String saltBase64;
    private String vaultBlobBase64;

    public LoginChallengeResponse(String saltBase64, String vaultBlobBase64) {
        this.saltBase64 = saltBase64;
        this.vaultBlobBase64 = vaultBlobBase64;
    }
}
