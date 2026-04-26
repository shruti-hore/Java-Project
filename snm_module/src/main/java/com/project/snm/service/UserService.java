package com.project.snm.service;

import com.project.snm.dto.RegisterRequest;
import com.project.snm.exception.ConflictException;
import com.project.snm.exception.NotFoundException;
import com.project.snm.model.mysql.UserRecord;
import com.project.snm.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserRecord registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists.");
        }

        UserRecord user = new UserRecord();
        user.setEmail(request.getEmail());

        // Server BCrypts the authProof before storing — double layer of protection
        String storedHash = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(request.getAuthProof(), org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12));
        user.setBcryptHash(storedHash);

        user.setPublicKeyBase64(request.getPublicKeyBase64());
        user.setVaultBlobBase64(request.getVaultBlobBase64());
        user.setSaltBase64(request.getSaltBase64());

        return userRepository.save(user);
    }

    public UserRecord findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No account found for this email."));
    }
}
