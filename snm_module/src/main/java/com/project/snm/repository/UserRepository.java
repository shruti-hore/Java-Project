package com.project.snm.repository;

import com.project.snm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Simplified repository for User operations.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Find a user by email for login challenge or registration check
    Optional<User> findByEmail(String email);
    
    // Find a user by username for registration check
    Optional<User> findByUsername(String username);
}
