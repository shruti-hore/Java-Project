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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

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
        UserRecord user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", user.getUserId()));
    }

    @PostMapping("/login/challenge")
    public ResponseEntity<LoginChallengeResponse> challenge(@RequestBody LoginChallengeRequest request) {
        // Return salt and vault blob for the given email
        UserRecord user = userService.findByEmail(request.getEmail());
        return ResponseEntity.ok(new LoginChallengeResponse(user.getSaltBase64(), user.getVaultBlobBase64()));
    }

    @PostMapping("/login/verify")
    public ResponseEntity<?> verify(@RequestBody LoginVerifyRequest request) {
        try {
            UserRecord user = userService.findByEmail(request.getEmail());
            if (BCrypt.checkpw(request.getAuthProof(), user.getBcryptHash())) {
                String token = jwtService.issueToken(user.getUserId());
                return ResponseEntity.ok(new LoginVerifyResponse(token, user.getUserId(), user.getPublicKeyBase64()));
            }
        } catch (Exception e) {
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
