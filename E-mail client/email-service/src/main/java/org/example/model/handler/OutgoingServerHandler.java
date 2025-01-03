package org.example.model.handler;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.ServerConnection;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.ServerConnectionDto;

import java.util.Properties;
@Slf4j
public class OutgoingServerHandler extends EmailProtocolHandler {
    @Override
    protected Properties setupProperties(ServerConnectionDto serverConnection, EmailAccountDto emailAccount) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", serverConnection.getHost());
        properties.put("mail.smtp.port", serverConnection.getPort());
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        return properties;
    }

    @Override
    protected boolean testConnection(Session session, ServerConnectionDto serverConnection, EmailAccountDto emailAccount) {
        try {
            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();
            return true;
        } catch (MessagingException e) {
            log.warn("Outgoing server connection failed: " + e.getMessage());
            return false;
        }
    }
}
