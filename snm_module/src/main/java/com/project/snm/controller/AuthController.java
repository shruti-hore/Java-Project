package com.project.snm.controller;

import com.project.snm.dto.*;
import com.project.snm.exception.NotFoundException;
import com.project.snm.model.mysql.UserRecord;
import com.project.snm.security.JwtService;
import com.project.snm.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${server.pepper}")
    private String pepper;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Step 4: Register user using provided emailHmac
        UserRecord user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", user.getUserId()));
    }

    @PostMapping("/login/challenge")
    public ResponseEntity<LoginChallengeResponse> challenge(@RequestBody LoginChallengeRequest request) {
        // Step 4: Return salt and vault blob for the given emailHmac
        UserRecord user = userService.findByEmailHmac(request.getEmailHmac());
        return ResponseEntity.ok(new LoginChallengeResponse(user.getSaltBase64(), user.getVaultBlobBase64()));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verify(@RequestBody LoginVerifyRequest request) {
        try {
            // Step 4: Look up user by emailHmac
            UserRecord user = userService.findByEmailHmac(request.getEmailHmac());
            
            // Step 4: Verify BCrypt hash
            if (BCrypt.checkpw(request.getBcryptHash(), user.getBcryptHash())) {
                String token = jwtService.issueToken(user.getUserId());
                return ResponseEntity.ok(new LoginVerifyResponse(token, user.getUserId(), user.getPublicKeyBase64()));
            }
        } catch (Exception e) {
            // Fall through to 401
        }
        
        // Step 4: Do not distinguish "user not found" from "wrong password"
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestBody Map<String, String> body) {
        // Step 6: Public key lookup (requires JWT)
        String emailHmac = body.get("emailHmac");
        if (emailHmac == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            UserRecord user = userService.findByEmailHmac(emailHmac);
            return ResponseEntity.ok(Map.of("publicKeyBase64", user.getPublicKeyBase64()));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
