package com.project.snm.repository;

import com.project.snm.model.mysql.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {
    Optional<Team> findByWorkspaceCode(String workspaceCode);
}