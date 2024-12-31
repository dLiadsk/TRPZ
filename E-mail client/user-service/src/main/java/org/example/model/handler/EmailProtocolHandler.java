package org.example.model.handler;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.ServerConnection;

import java.util.Properties;
@Slf4j
public abstract class EmailProtocolHandler {
    public final Session authorize(EmailAccount emailAccount, ServerConnection serverConnection) {
        Properties properties = setupProperties(serverConnection, emailAccount);
        Session session = createSession(properties, emailAccount);

        if (testConnection(session, serverConnection, emailAccount)) {
            log.info("Authorization successful for server: " + serverConnection.getHost());
            return session;
        } else {
            log.warn("Authorization failed for server: " + serverConnection.getHost());
            return null;
        }
    }

    protected abstract Properties setupProperties(ServerConnection serverConnection, EmailAccount emailAccount);

    protected abstract boolean testConnection(Session session, ServerConnection serverConnection, EmailAccount emailAccount);

    private Session createSession(Properties properties, EmailAccount emailAccount) {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailAccount.getEmailAddress(), emailAccount.getPassword());
            }
        });
    }
}
