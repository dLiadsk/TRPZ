package org.example.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.model.common.EmailStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "email_message")
public class EmailMessageEntity {
    @Id
    @Column(name = "message_id")
    private String messageId;

    private String subject;

    @Column(name = "sender")
    private String from;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "email_recipients", joinColumns = @JoinColumn(name = "email_id"))
    @Column(name = "recipient")
    private List<String> to;

    private LocalDateTime sentDate;

    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "email_attachments_names", joinColumns = @JoinColumn(name = "email_id"))
    @Column(name = "attachment_name")
    private List<String> attachmentPaths; // Список назв вкладень

    @Enumerated(EnumType.STRING)
    private EmailStatus emailStatus; // Статус листа

}