package org.example.repository.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
@Table(name = "folder_email_addresses")
@Data
public class FolderEmailAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "folder_email_addresses_id_seq")
    @SequenceGenerator(name = "folder_email_addresses_id_seq", sequenceName = "folder_email_addresses_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private FolderEntity folder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "email_address_id", nullable = false)
    private EmailAddressEntity emailAddress;

    @OneToMany(mappedBy = "folderEmailAddress", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderEmailMessageEntity> folderEmailMessages = new ArrayList<>();
}