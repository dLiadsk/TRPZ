package org.example.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.model.common.ProtocolType;

@Data
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "server_connection")
public class ServerConnectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "server_id_seq")
    @SequenceGenerator(name = "server_id_seq", sequenceName = "server_id_seq")
    private Long id;
    private String host;
    private int port;
    private String protocol;

    public ServerConnectionEntity(String host, int port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }
}
