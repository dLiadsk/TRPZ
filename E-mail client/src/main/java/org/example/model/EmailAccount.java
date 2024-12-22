package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.model.common.ProtocolType;

@Data
@RequiredArgsConstructor
public class EmailAccount {
    private Long id;
    private String emailAddress;
    private String password;
    private ServerConnection incomingServer;  // IMAP/POP3 сервер
    private ServerConnection outgoingServer;  // SMTP сервер
    // Підключення до сервера

    public EmailAccount(String emailAddress, String password) {
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public void autoConfigure() {
        String domain = emailAddress.split("@")[1];
        switch (domain) {
            case "gmail.com" -> {
                incomingServer = new ServerConnection("imap.gmail.com", 993, ProtocolType.IMAP);
                outgoingServer = new ServerConnection("smtp.gmail.com", 465, ProtocolType.SMTP);
            }
            case "ukr.net" -> {
                incomingServer = new ServerConnection("imap.ukr.net", 993, ProtocolType.IMAP);
                outgoingServer = new ServerConnection("smtp.ukr.net", 465, ProtocolType.SMTP);
            }
            case "i.ua" -> {
                incomingServer = new ServerConnection("imap.i.ua", 993, ProtocolType.IMAP);
                outgoingServer = new ServerConnection("smtp.i.ua", 465, ProtocolType.SMTP);
            }
            default -> System.out.println("Автонастройка недоступна для домену: " + domain);
        }
    }

}