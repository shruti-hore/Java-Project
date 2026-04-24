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
        if (userRepository.findByEmailHmac(request.getEmailHmac()).isPresent()) {
            throw new ConflictException("User already exists with given email HMAC");
        }

        UserRecord user = new UserRecord();
        user.setEmailHmac(request.getEmailHmac());
        user.setBcryptHash(request.getBcryptHash());
        user.setPublicKeyBase64(request.getPublicKeyBase64());
        user.setVaultBlobBase64(request.getVaultBlobBase64());
        user.setSaltBase64(request.getSaltBase64());

        return userRepository.save(user);
    }

    public UserRecord findByEmailHmac(String emailHmac) {
        return userRepository.findByEmailHmac(emailHmac)
                .orElseThrow(() -> new NotFoundException("User not found with email HMAC"));
    }
}
