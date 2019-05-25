package com.mrb.fixme.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String DATABASE_URL = "jdbc:sqlite::resource:transactions.db";
    private static Connection connection;

    public static synchronized void connect() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            System.out.println("Database connection failed");
        }
    }

    public static void close() {
        try {
            if (connection != null) {
                connection.close();
            }
            connection = null;
        } catch (SQLException e) {
            System.out.println("Database closing connection failed");
        }
    }

    private static synchronized Connection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    public static synchronized void insert() {
        final Connection localConnection = getConnection();
        if (localConnection != null) {
            System.out.println("Got connection, TODO insert data, instance: " + localConnection);
        }
    }
}
