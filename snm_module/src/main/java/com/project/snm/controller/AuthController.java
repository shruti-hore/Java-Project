package com.project.snm.controller;

import com.project.snm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controller for Auth related endpoints.
 * Simplified for Phase 1 rebuild.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * POST /auth/register
     * Receives user registration data and saves to DB.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String username = request.get("username");
            String authProof = request.get("password"); // raw auth proof from frontend
            String salt = request.get("salt");
            String vaultBlob = request.get("vault_blob");

            userService.registerUser(email, username, authProof, salt, vaultBlob);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully"));

        } catch (Exception e) {
            if (e.getMessage().startsWith("CONFLICT")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * POST /auth/login/challenge
     * Phase 1: Fetch user salt and vault blob.
     */
    @PostMapping("/login/challenge")
    public ResponseEntity<?> challenge(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        return userService.fetchChallenge(identifier)
                .map(user -> ResponseEntity.ok(Map.of(
                        "salt", user.getSalt(),
                        "vault_blob", user.getVaultBlob()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * POST /auth/login/verify
     * Phase 2: Verify auth proof and return JWT.
     */
    @PostMapping("/login/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        try {
            String identifier = request.get("identifier");
            String authProof = request.get("auth_proof");

            String jwt = userService.verifyLogin(identifier, authProof);
            return ResponseEntity.ok(Map.of("token", jwt));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
