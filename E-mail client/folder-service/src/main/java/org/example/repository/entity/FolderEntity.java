package org.example.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Entity
@Table(name = "folders")
@Data
public class FolderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "folder_seq")
    @SequenceGenerator(name = "folder_seq", sequenceName = "folder_sequence")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderEmailAddressEntity> folderEmailAddresses = new ArrayList<>();
}

