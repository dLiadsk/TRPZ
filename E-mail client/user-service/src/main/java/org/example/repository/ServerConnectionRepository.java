package org.example.repository;
import org.example.repository.entity.ServerConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerConnectionRepository extends JpaRepository<ServerConnectionEntity, Long> {
    boolean existsByHostAndPortAndProtocol(String host, int port, String protocol);
    ServerConnectionEntity findByHostAndPortAndProtocol(String host, int port, String protocol);
}
