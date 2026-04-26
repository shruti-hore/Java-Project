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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        UserRecord user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", user.getUserId()));
    }

    @PostMapping("/login/challenge")
    public ResponseEntity<LoginChallengeResponse> challenge(@RequestBody LoginChallengeRequest request) {
        log.info("Login challenge requested for email: {}", request.getEmail());
        // Return salt and vault blob for the given email
        UserRecord user = userService.findByEmail(request.getEmail());
        return ResponseEntity.ok(new LoginChallengeResponse(
                user.getSaltBase64(), 
                user.getVaultBlobBase64(),
                user.getPublicKeyBase64()
        ));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verify(@RequestBody LoginVerifyRequest request) {
        log.info("Verifying login for email: {}", request.getEmail());
        try {
            UserRecord user = userService.findByEmail(request.getEmail());
            log.info("User found. Checking password...");
            if (BCrypt.checkpw(request.getAuthProof(), user.getBcryptHash())) {
                log.info("Password verified successfully.");
                String token = jwtService.issueToken(user.getUserId());
                return ResponseEntity.ok(new LoginVerifyResponse(token, user.getUserId(), user.getPublicKeyBase64()));
            } else {
                log.warn("Password verification failed for email: {}", request.getEmail());
            }
        } catch (Exception e) {
            log.error("Error during verification: ", e);
            // fall through — do not distinguish "no account" from "wrong password"
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestBody Map<String, String> body) {
        // Public key lookup (requires JWT)
        String email = body.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            UserRecord user = userService.findByEmail(email);
            return ResponseEntity.ok(Map.of("publicKeyBase64", user.getPublicKeyBase64()));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
