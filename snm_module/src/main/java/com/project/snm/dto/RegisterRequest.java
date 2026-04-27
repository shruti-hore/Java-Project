package com.project.snm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String username;
    private String authProof;
    private String publicKeyBase64;
    private String vaultBlobBase64;
    private String saltBase64;
}
