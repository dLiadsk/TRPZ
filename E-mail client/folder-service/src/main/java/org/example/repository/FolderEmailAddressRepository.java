package org.example.repository;

import org.example.repository.entity.EmailAddressEntity;
import org.example.repository.entity.FolderEmailAddressEntity;
import org.example.repository.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface FolderEmailAddressRepository extends JpaRepository<FolderEmailAddressEntity, Long> {
    Optional<FolderEmailAddressEntity> findByFolderAndEmailAddress(FolderEntity folder, EmailAddressEntity emailAddress);
}
