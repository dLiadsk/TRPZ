package org.example.service.mapper;

import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.model.common.ProtocolType;
import org.example.repository.entity.EmailAccountEntity;
import org.example.repository.entity.ServerConnectionEntity;
import org.example.repository.entity.UserEntity;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.ServerConnectionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class EmailAccountMapper {
    public EmailAccountEntity mapToEntity(EmailAccount emailAccount, UserEntity userEntity) {
        return EmailAccountEntity.builder()
                .emailAddress(emailAccount.getEmailAddress())
                .password(emailAccount.getPassword())
                .autoconfig(emailAccount.getAutoconfig())
                .user(userEntity)
                .incomingServer(mapToEntity(emailAccount.getIncomingServer()))
                .outgoingServer(mapToEntity(emailAccount.getOutgoingServer()))
                .build();
    }

    public EmailAccount mapToModel(EmailAccountDto dto) {
        return new EmailAccount.EmailAccountBuilder(dto.getEmailAddress(), dto.getPassword())
                .setAutoconfig(dto.getAutoconfig())
                .setIncomingServer(mapToServerConnection(dto.getIncomingServer()))
                .setOutgoingServer(mapToServerConnection(dto.getOutgoingServer()))
                .build();
    }
    private ServerConnection mapToServerConnection(ServerConnectionDto serverConnection){
        if (serverConnection == null) {
            return null;
        }
        return new ServerConnection(serverConnection.getHost(), serverConnection.getPort(), ProtocolType.valueOf(serverConnection.getProtocol()));
    }
    private ServerConnectionDto mapToServerConnectionDto(ServerConnectionEntity serverConnection){
        if (serverConnection == null) {
            return null;
        }
        return ServerConnectionDto.builder()
                .protocol(serverConnection.getProtocol())
                .port(serverConnection.getPort())
                .host(serverConnection.getHost()).build();
    }
    public ServerConnectionEntity mapToEntity(ServerConnection serverConnection) {
        return new ServerConnectionEntity(
                null,
                serverConnection.getHost(),
                serverConnection.getPort(),
                serverConnection.getProtocol().toString()
        );
    }
    public EmailAccountDto mapToEmailAccountDto(EmailAccountEntity entity){
        if (entity == null) {
            return null;
        }
        return EmailAccountDto.builder()
                .password(entity.getPassword())
                .emailAddress(entity.getEmailAddress())
                .outgoingServer(mapToServerConnectionDto(entity.getOutgoingServer()))
                .incomingServer(mapToServerConnectionDto(entity.getIncomingServer()))
                .autoconfig(entity.getAutoconfig()).build();
    }
    public ServerConnectionEntity mapToServerConnectionEntity(ServerConnection serverConnection){
        if (serverConnection == null) {
            return null;
        }
        return new ServerConnectionEntity(serverConnection.getHost(), serverConnection.getPort(), serverConnection.getProtocol().name());
    }
}
