package org.example.repository;

import org.example.model.EmailAccount;
import org.example.repository.entity.EmailAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface EmailAccountRepository extends JpaRepository<EmailAccountEntity, Long> {
    Optional<EmailAccountEntity> findByEmailAddress(String emailAddress);
}
