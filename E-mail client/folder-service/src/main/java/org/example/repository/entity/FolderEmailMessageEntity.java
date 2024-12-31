package org.example.repository.entity;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Entity
@Table(name = "folder_email_message")
@Data
public class FolderEmailMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "folder_email_seq")
    @SequenceGenerator(name = "folder_email_seq", sequenceName = "folder_email_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "folder_email_id", nullable = false, referencedColumnName = "id")
    private FolderEmailAddressEntity folderEmailAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name = "email_message_id", nullable = false, referencedColumnName = "message_id")
    private EmailMessageEntity message;
}