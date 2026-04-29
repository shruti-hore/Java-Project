package com.project.snm.repository;

import com.project.snm.model.mysql.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByTeamIdInAndStatus(List<String> teamIds, String status);
    Optional<JoinRequest> findByTeamIdAndUserIdAndStatus(String teamId, String userId, String status);
}
