package org.example;

import org.example.util.DbConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        Connection dbConnection = DbConnection.connect();
    }
}