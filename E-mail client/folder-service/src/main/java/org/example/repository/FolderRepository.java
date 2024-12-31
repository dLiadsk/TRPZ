package org.example.repository;

import org.example.dto.EmailMessageContextDto;
import org.example.repository.entity.EmailMessageEntity;
import org.example.repository.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Long> {

    @Query("SELECT f FROM FolderEntity f " +
            "JOIN FolderEmailAddressEntity fea ON fea.folder.id = f.id " +
            "JOIN EmailAddressEntity ea ON ea.id = fea.emailAddress.id " +
            "WHERE ea.address = :emailAddress")
    List<FolderEntity> findFoldersByEmailAddress(@Param("emailAddress") String emailAddress);
    Optional<FolderEntity> findByName(String name);

}
