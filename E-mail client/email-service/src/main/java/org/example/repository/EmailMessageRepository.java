package org.example.repository;

import org.example.model.EmailMessage;
import org.example.repository.entity.EmailMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailMessageRepository extends JpaRepository<EmailMessageEntity, String> {

    @Query("SELECT e.messageId FROM EmailMessageEntity e")
    List<String> findAllMessageIds();
}
