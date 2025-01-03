package org.example.repository;

import org.example.repository.entity.EmailMessageEntity;
import org.example.repository.entity.FolderEmailAddressEntity;
import org.example.repository.entity.FolderEmailMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderEmailMessageRepository extends JpaRepository<FolderEmailMessageEntity, Long> {
    boolean existsByFolderEmailAddressAndMessage(FolderEmailAddressEntity folderEmailAddress, EmailMessageEntity message);
    List<FolderEmailMessageEntity> findFolderEmailMessageEntitiesByFolderEmailAddress(FolderEmailAddressEntity folderEmailAddress);
    void deleteByFolderEmailAddressAndMessage(FolderEmailAddressEntity folderEmailAddress, EmailMessageEntity message);
}