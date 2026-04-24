package com.project.snm.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginVerifyRequest {
    private String emailHmac;
    private String bcryptHash;
}
