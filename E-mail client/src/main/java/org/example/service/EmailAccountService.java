package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.model.User;
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
        // Перевірка авторизації на вхідному сервері (IMAP)
        Session incomingServerSession = connectIncomingServer(emailAccount.getIncomingServer(), emailAccount);
        if (incomingServerSession == null) {
            log.warn("Authorization failed on incoming server for: " + emailAccount.getEmailAddress());
            return null;
        }
        // Перевірка авторизації на вихідному сервері (SMTP)
        Session outgoingServerSession = connectOutgoingServer(emailAccount.getOutgoingServer(), emailAccount);
        if (outgoingServerSession == null) {
            log.warn("Authorization failed on outgoing server for: " + emailAccount.getEmailAddress());
            return null;
        }
        EmailAccount authorizedEmailAccount = new EmailAccount.EmailAccountBuilder(emailAccount.getEmailAddress(), emailAccount.getPassword())
                .setId(emailAccount.getId())
                .setAutoconfig(true)
                .setIncomingServerSession(incomingServerSession)
                .setOutgoingServerSession(outgoingServerSession)
                .build();

        log.info("Authorization successful for: " + emailAccount.getEmailAddress());
        return authorizedEmailAccount;
    }

    private Session connectIncomingServer(ServerConnection serverConnection, EmailAccount emailAccount) {
        Session session = createSession(serverConnection, emailAccount, false);
        try (Store store = session.getStore(serverConnection.getProtocol().name().toLowerCase())) {
            store.connect(serverConnection.getHost(), emailAccount.getEmailAddress(), emailAccount.getPassword());
            log.info("Incoming server authorization successful: " + serverConnection.getHost());
            return session;
        } catch (MessagingException e) {
            log.warn("Incoming server authorization failed: " + e.getMessage());
            return null;
        }
    }

    private Session connectOutgoingServer(ServerConnection serverConnection, EmailAccount emailAccount) {

        Session session = createSession(serverConnection, emailAccount, true);

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            log.info("Outgoing server authorization successful: " + serverConnection.getHost());
            return session;
        } catch (MessagingException e) {
            log.warn("Outgoing server authorization failed: " + e.getMessage());
            return null;
        }
    }
    private Session createSession(ServerConnection serverConnection, EmailAccount emailAccount, boolean isSmtp) {
        Properties properties = new Properties();
        properties.put("mail." + serverConnection.getProtocol().name().toLowerCase() + ".host", serverConnection.getHost());
        properties.put("mail." + serverConnection.getProtocol().name().toLowerCase() + ".port", serverConnection.getPort());
        properties.put("mail." + serverConnection.getProtocol().name().toLowerCase() + ".ssl.enable", "true");
        if (isSmtp){
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
        }
        System.out.println(properties);
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAccount.getEmailAddress(), emailAccount.getPassword());
            }
        });
    }

}
