package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.model.User;
import org.example.model.handler.EmailProtocolHandler;
import org.example.model.handler.IncomingServerHandler;
import org.example.model.handler.OutgoingServerHandler;
import org.example.repository.EmailAccountRepository;

import jakarta.mail.*;
import org.example.repository.UserRepository;

import java.util.Objects;
import java.util.Properties;

import java.sql.SQLException;
@Slf4j
public class EmailAccountService {
    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;

    public EmailAccountService(EmailAccountRepository emailAccountRepository, UserRepository userRepository) {
        this.emailAccountRepository = emailAccountRepository;
        this.userRepository = userRepository;
    }


    public EmailAccount addEmailAccount(User user, String email, String password) throws SQLException {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

        EmailAccount emailAccount = new EmailAccount.EmailAccountBuilder(email, password)
                .setAutoconfig(true)
                .setId(generateEmailAccountId())
                .build();
        //authentication
        EmailAccount authorizedEmailAccount = authorizeEmail(emailAccount);
        if (authorizedEmailAccount != null) {
            user.getEmailAccounts().add(emailAccount);
            log.info("Email account added to user: " + user.getUsername() + " (" + emailAccount.getEmailAddress() + ")");
            userRepository.save(user);
            emailAccountRepository.save(emailAccount);
            return authorizedEmailAccount;
        }
        throw new IllegalArgumentException("some problems");
    }

    public void deleteEmailAccount(User user, EmailAccount emailAccount) throws SQLException {
        EmailAccount emailAccountInUser = user.getEmailAccounts().stream()
                .filter(emailAccount1 -> emailAccount1.getId().equals(emailAccount.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "User doesn't contain account with email: " + emailAccount.getEmailAddress()
                ));

        user.getEmailAccounts().remove(emailAccountInUser);
        userRepository.save(user);
        emailAccountRepository.delete(emailAccount);
    }


    private Long generateEmailAccountId() throws SQLException {
        return (long) (emailAccountRepository.findAll().size() + 1);
    }

    public EmailAccount authorizeEmail(EmailAccount emailAccount) {
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
                .setId(emailAccount.getId())
                .setAutoconfig(true)
                .setIncomingServerSession(incomingSession)
                .setOutgoingServerSession(outgoingSession)
                .build();

        log.info("Authorization successful for: " + emailAccount.getEmailAddress());
        return authorizedEmailAccount;
    }


}
