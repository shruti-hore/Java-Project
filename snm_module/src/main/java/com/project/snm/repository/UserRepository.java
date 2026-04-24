package com.project.snm.repository;

import com.project.snm.model.mysql.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserRecord, String> {
    Optional<UserRecord> findByEmailHmac(String emailHmac);
}
