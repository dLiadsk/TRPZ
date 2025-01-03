package org.example.repository;

import org.example.dto.EmailMessageContextDto;
import org.example.repository.entity.EmailAddressEntity;
import org.example.repository.entity.EmailMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailAddressRepository extends JpaRepository<EmailAddressEntity, Long> {
    Optional<EmailAddressEntity> findByAddress(String address);

}
