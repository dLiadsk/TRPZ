package org.example.service.mapper;

import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.model.User;
import org.example.repository.entity.EmailAccountEntity;
import org.example.repository.entity.ServerConnectionEntity;
import org.example.repository.entity.UserEntity;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.ServerConnectionDto;
import org.example.service.dto.UserDto;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
@Mapper(componentModel = "spring")
public class UserMapper {
    public UserDto mapToUserDto(UserEntity user){
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .emailAccounts(mapToListDto(user.getEmailAccounts()))
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getUsername())
                .build();
    }
    public List<EmailAccountDto> mapToListDto(List<EmailAccountEntity> accounts){
        if (accounts == null) {
            return null;
        }
        return accounts.stream().map(this::mapToAccountDto).toList();
    }
    public EmailAccountDto mapToAccountDto(EmailAccountEntity account){
        if (account == null) {
            return null;
        }
        return EmailAccountDto.builder()
                .autoconfig(account.getAutoconfig())
                .emailAddress(account.getEmailAddress())
                .incomingServer(mapToServerConnectionDto(account.getIncomingServer()))
                .outgoingServer(mapToServerConnectionDto(account.getOutgoingServer()))
                .password(account.getPassword())
                .build();
    }
    public ServerConnectionDto mapToServerConnectionDto(ServerConnectionEntity serverConnection){
        if (serverConnection == null) {
            return null;
        }
        return ServerConnectionDto.builder()
                .host(serverConnection.getHost())
                .port(serverConnection.getPort())
                .protocol(serverConnection.getProtocol()).build();
    }

}
