package org.example.model.handler;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.ServerConnectionDto;

import java.util.Properties;
@Slf4j
public class IncomingServerHandler extends EmailProtocolHandler {
    @Override
    protected Properties setupProperties(ServerConnectionDto serverConnection, EmailAccountDto emailAccount) {
        Properties properties = new Properties();
        String protocol = serverConnection.getProtocol().toLowerCase();
        properties.put("mail." + protocol + ".host", serverConnection.getHost());
        properties.put("mail." + protocol + ".port", serverConnection.getPort());
        properties.put("mail." + protocol + ".ssl.enable", "true");
        return properties;
    }

    @Override
    protected boolean testConnection(Session session, ServerConnectionDto serverConnection, EmailAccountDto emailAccount) {
        try (Store store = session.getStore(serverConnection.getProtocol().toLowerCase())) {
            store.connect(serverConnection.getHost(), emailAccount.getEmailAddress(), emailAccount.getPassword());
            return true;
        } catch (MessagingException e) {
            log.warn("Incoming server connection failed: " + e.getMessage());
            return false;
        }
    }
}

