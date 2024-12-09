package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.model.common.ProtocolType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ServerConnection {
    private String host;
    private int port;
    private ProtocolType protocol;
    // Підключення до сервера
    public boolean connect() {
        // Логіка підключення до сервера з урахуванням протоколу та безпеки
        System.out.println("Connecting to server: " + host + " on port " + port + " using " + protocol );
        // Псевдопідключення для прикладу
        return true;  // Потрібно додати реальну логіку для підключення
    }

    // Відключення від сервера
    public void disconnect() {
        System.out.println("Disconnecting from server: " + host);
        // Логіка для відключення
    }



}