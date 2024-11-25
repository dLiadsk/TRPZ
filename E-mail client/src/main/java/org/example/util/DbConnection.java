package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    static final String URL = "jdbc:postgresql://127.0.0.1:5432/E-mail client";
    static final String USER = "postgres";
    static final String PASSWORD = "admin";
    public static Connection connect() {
        Connection connection = null;
        try {
            // Встановлення з'єднання з базою даних
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to PostgreSQL server.");
            e.printStackTrace();
        }
        return connection;
    }
}
