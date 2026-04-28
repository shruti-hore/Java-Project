package com.project.snm.service;

import com.project.snm.model.User;
import com.project.snm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Service handling user business logic: registration and login checks.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Registers a new user.
     * 1. Checks for duplicates.
     * 2. BCrypt-hashes the auth proof.
     * 3. Saves to DB.
     */
    public void registerUser(String email, String username, String authProof, String salt, String vaultBlob) throws Exception {
        // Step 1: Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("CONFLICT_EMAIL");
        }

        // Step 2: Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new Exception("CONFLICT_USERNAME");
        }

        // Step 3: BCrypt-hash the incoming auth proof (the password)
        // Note: The 'authProof' sent from client is already an Argon2id hash.
        String hashedPassword = BCrypt.hashpw(authProof, BCrypt.gensalt());

        // Step 4: Create and save the new user record
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setBcryptHash(hashedPassword);
        user.setSalt(salt);
        user.setVaultBlob(vaultBlob);

        userRepository.save(user);
    }

    /**
     * Phase 1 Login: Fetch salt and vault blob by email or username.
     */
    public Optional<User> fetchChallenge(String identifier) {
        // Look up by email first, then username
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier));
    }

    /**
     * Phase 2 Login: Verify auth proof and return JWT.
     */
    public String verifyLogin(String identifier, String authProof) throws Exception {
        User user = fetchChallenge(identifier)
                .orElseThrow(() -> new Exception("USER_NOT_FOUND"));

        // BCrypt-check the incoming authProof against the stored hash
        if (!BCrypt.checkpw(authProof, user.getBcryptHash())) {
            throw new Exception("INVALID_CREDENTIALS");
        }

        // Generate a simple JWT (simulated for now, or use existing logic)
        // For this rebuild, we'll return a dummy token or use a real JWT library if configured.
        return "JWT_TOKEN_FOR_" + user.getEmail(); 
    }
}
