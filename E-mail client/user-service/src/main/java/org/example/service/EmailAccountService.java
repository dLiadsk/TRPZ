package org.example.service;

import jakarta.mail.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.User;
import org.example.model.handler.EmailProtocolHandler;
import org.example.model.handler.IncomingServerHandler;
import org.example.model.handler.OutgoingServerHandler;
import org.example.repository.EmailAccountRepository;
import org.example.repository.ServerConnectionRepository;
import org.example.repository.UserRepository;
import org.example.repository.entity.EmailAccountEntity;
import org.example.repository.entity.ServerConnectionEntity;
import org.example.repository.entity.UserEntity;
import org.example.service.dto.EmailAccountDto;
import org.example.service.mapper.EmailAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAccountService {
    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;
    private final EmailAccountMapper emailAccountMapper;
    private final ServerConnectionRepository serverConnectionRepository;


    @Transactional
    public void addEmailAccount(String username, EmailAccountDto emailAccountDto) {
        Objects.requireNonNull(username, "username cannot be null");
        Objects.requireNonNull(emailAccountDto.getEmailAddress(), "Email cannot be null");
        Objects.requireNonNull(emailAccountDto.getPassword(), "Password cannot be null");
        EmailAccount emailAccount;
        if (emailAccountDto.getAutoconfig()) {
            emailAccount = new EmailAccount.EmailAccountBuilder(emailAccountDto.getEmailAddress(), emailAccountDto.getPassword())
                    .setAutoconfig(emailAccountDto.getAutoconfig())
                    .build();
        } else {
            emailAccount = emailAccountMapper.mapToModel(emailAccountDto);
        }
        //authentication
        EmailAccount authorizedEmailAccount = authorizeEmail(emailAccount);
        if (authorizedEmailAccount != null) {

            UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
            ServerConnectionEntity in = emailAccountMapper.mapToServerConnectionEntity(authorizedEmailAccount.getIncomingServer());
            ServerConnectionEntity out = emailAccountMapper.mapToServerConnectionEntity(authorizedEmailAccount.getOutgoingServer());
            ServerConnectionEntity incomingServer;
            ServerConnectionEntity outgoingServer;
            if (!serverConnectionRepository.existsByHostAndPortAndProtocol(in.getHost(), in.getPort(), in.getProtocol())) {
                incomingServer = serverConnectionRepository.save(in);
            } else {
                incomingServer = serverConnectionRepository.findByHostAndPortAndProtocol(in.getHost(), in.getPort(), in.getProtocol());
            }
            if (!serverConnectionRepository.existsByHostAndPortAndProtocol(out.getHost(), out.getPort(), out.getProtocol())) {
                outgoingServer = serverConnectionRepository.save(out);
            } else {
                outgoingServer = serverConnectionRepository.findByHostAndPortAndProtocol(out.getHost(), out.getPort(), out.getProtocol());
            }
            EmailAccountEntity emailAccountEntity = emailAccountMapper.mapToEntity(authorizedEmailAccount, userEntity);


            emailAccountEntity.setIncomingServer(incomingServer);
            emailAccountEntity.setOutgoingServer(outgoingServer);

            log.info("Email account added to user: " + username + " (" + emailAccount.getEmailAddress() + ")");

            emailAccountMapper.mapToEmailAccountDto(emailAccountRepository.save(emailAccountEntity));
            return;
        }
        throw new IllegalArgumentException("Authorization failed for email: " + emailAccountDto.getEmailAddress());
    }


    private EmailAccount authorizeEmail(EmailAccount emailAccount) {
        EmailProtocolHandler incomingHandler = new IncomingServerHandler();
        Session incomingSession = incomingHandler.authorize(emailAccount, emailAccount.getIncomingServer());

        if (incomingSession == null) {
            log.warn("Authorization failed on incoming server for: " + emailAccount.getEmailAddress());
            return null;
        }

        EmailProtocolHandler outgoingHandler = new OutgoingServerHandler();
        Session outgoingSession = outgoingHandler.authorize(emailAccount, emailAccount.getOutgoingServer());


        if (outgoingSession == null) {
            log.warn("Authorization failed on outgoing server for: " + emailAccount.getEmailAddress());
            return null;
        }

        EmailAccount authorizedEmailAccount = new EmailAccount.EmailAccountBuilder(emailAccount.getEmailAddress(), emailAccount.getPassword())
                .setAutoconfig(true)
                .build();

        log.info("Authorization successful for: " + emailAccount.getEmailAddress());
        return authorizedEmailAccount;
    }

    @Transactional
    public void deleteEmailAccount(User user, EmailAccount emailAccount) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(emailAccount, "EmailAccount cannot be null");

        UserEntity userEntity = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EmailAccountEntity emailAccountEntity = emailAccountRepository.findByEmailAddress(emailAccount.getEmailAddress())
                .orElseThrow(() -> new IllegalArgumentException("Email account not found"));

        if (!emailAccountEntity.getUser().getId().equals(userEntity.getId())) {
            throw new IllegalArgumentException("Email account does not belong to the user");
        }

        emailAccountRepository.delete(emailAccountEntity);
        log.info("Email account deleted: " + emailAccountEntity.getEmailAddress());
    }


}
