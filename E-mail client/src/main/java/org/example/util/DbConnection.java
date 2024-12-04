package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static volatile DbConnection dbConnection;
    private final Connection connection;
    private static final String URL = "jdbc:postgresql://127.0.0.1:5432/E-mail client";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    private DbConnection(){
         this.connection = connect();
    }
    public static DbConnection getInstance(){
        if (dbConnection == null){
            synchronized (DbConnection.class){
                if (dbConnection == null){
                    dbConnection = new DbConnection();
                }
            }
        }
        return dbConnection;
    }

    private static Connection connect() {
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

    public Connection getConnection() {
        return connection;
    }
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                System.out.println("Failed to close the connection.");
                e.printStackTrace();
            }
        }
    }
}
