package org.example.service;

import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.model.User;
import org.example.repository.EmailAccountRepository;

import jakarta.mail.*;
import org.example.repository.UserRepository;

import java.util.Optional;
import java.util.Properties;

import java.sql.SQLException;

public class EmailAccountService {
    private final EmailAccountRepository emailAccountRepository;
    private final UserRepository userRepository;

    public EmailAccountService(EmailAccountRepository emailAccountRepository, UserRepository userRepository) {
        this.emailAccountRepository = emailAccountRepository;
        this.userRepository = userRepository;
    }


    public EmailAccount addEmailAccount(User user, EmailAccount emailAccount) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }
        emailAccount.setId(generateEmailAccountId());
        emailAccount.setId(1L);
        emailAccount.autoConfigure();
        //authentication
        if (authorizeEmail(emailAccount)) {
            user.getEmailAccounts().add(emailAccount);
            System.out.println("Email account added to user: " + user.getUsername() + " (" + emailAccount.getEmailAddress() + ")");
            userRepository.save(user);
            return emailAccountRepository.save(emailAccount);
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

    public boolean authorizeEmail(EmailAccount emailAccount) {
        // Перевірка авторизації на вхідному сервері (IMAP)
        if (!testIncomingServer(emailAccount.getIncomingServer(), emailAccount)) {
            System.out.println("Authorization failed on incoming server for: " + emailAccount.getEmailAddress());
            return false;
        }
        // Перевірка авторизації на вихідному сервері (SMTP)
        if (!testOutgoingServer(emailAccount.getOutgoingServer(), emailAccount)) {
            System.out.println("Authorization failed on outgoing server for: " + emailAccount.getEmailAddress());
            return false;
        }
        System.out.println("Authorization successful for: " + emailAccount.getEmailAddress());
        return true;
    }

    // Тестування підключення до вхідного сервера
    private boolean testIncomingServer(ServerConnection serverConnection, EmailAccount emailAccount) {
        Properties properties = new Properties();
        properties.put("mail.imap.host", serverConnection.getHost());
        properties.put("mail.imap.port", serverConnection.getPort());
        properties.put("mail.imap.ssl.enable", "true");
//        properties.put("mail.debug", "true");


        Session session = Session.getInstance(properties);
        try (Store store = session.getStore(serverConnection.getProtocol().name().toLowerCase())) {
            store.connect(serverConnection.getHost(), emailAccount.getEmailAddress(), emailAccount.getPassword());
            System.out.println("Incoming server authorization successful: " + serverConnection.getHost());
            return true;
        } catch (MessagingException e) {
            System.out.println("Incoming server authorization failed: " + e.getMessage());
            return false;
        }
    }

    // Тестування підключення до вихідного сервера
    private boolean testOutgoingServer(ServerConnection serverConnection, EmailAccount emailAccount) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", serverConnection.getHost());
        properties.put("mail.smtp.port", serverConnection.getPort());
        properties.put("mail.smtp.ssl.enable", "true");
//        properties.put("mail.debug", "true");


        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAccount.getEmailAddress(), emailAccount.getPassword());
            }
        });

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            System.out.println("Outgoing server authorization successful: " + serverConnection.getHost());
            return true;
        } catch (MessagingException e) {
            System.out.println("Outgoing server authorization failed: " + e.getMessage());
            return false;
        }
    }
}
