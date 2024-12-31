package org.example.repository.entity;

import jakarta.mail.Session;
import jakarta.persistence.*;
import lombok.*;
import org.example.model.ServerConnection;
import org.example.model.common.ProtocolType;

@Data
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "emailAccount")
public class EmailAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_id_seq")
    @SequenceGenerator(name = "email_id_seq", sequenceName = "email_id_seq")
    private Long id;
    private String emailAddress;
    private String password;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "incoming_server_id")
    private ServerConnectionEntity incomingServer;

    @ManyToOne
    @JoinColumn(name = "outgoing_server_id")
    private ServerConnectionEntity outgoingServer;

    private Boolean autoconfig;
}