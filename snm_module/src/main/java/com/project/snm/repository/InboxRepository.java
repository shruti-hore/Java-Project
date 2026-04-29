package com.project.snm.repository;

import com.project.snm.model.mysql.InboxItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InboxRepository extends JpaRepository<InboxItem, String> {
    List<InboxItem> findByReceiverIdOrderByTimestampDesc(String receiverId);
}
