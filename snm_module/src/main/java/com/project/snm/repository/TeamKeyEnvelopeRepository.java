package com.project.snm.repository;

import com.project.snm.model.mysql.TeamKeyEnvelopeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeamKeyEnvelopeRepository extends JpaRepository<TeamKeyEnvelopeRecord, String> {
    Optional<TeamKeyEnvelopeRecord> findByTeamIdAndUserId(String teamId, String userId);
}
