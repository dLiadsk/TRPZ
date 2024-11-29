package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailAccount {
    private Long id;
    private String emailAddress;
    private String password;
    private ServerConnection incomingServer;  // IMAP/POP3 сервер
    private ServerConnection outgoingServer;  // SMTP сервер
    // Підключення до сервера
    public boolean connectToServer() {
        // Підключення до вхідного і вихідного сервера
        boolean incomingConnected = incomingServer.connect();
        boolean outgoingConnected = outgoingServer.connect();
        return incomingConnected && outgoingConnected;
    }

    // Метод для автентифікації
    public boolean authenticate() {
        // Логіка для автентифікації на сервері
        // Підключається до серверів, перевіряє логін та пароль
        return connectToServer();  // Перевірка підключення
    }
}