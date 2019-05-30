package com.mrb.fixme.core.db;

import de.vandermeer.asciitable.AsciiTable;

import java.sql.*;

public class Database {
    private static final String DATABASE_URL = "jdbc:sqlite::resource:transactions.db";
    private static final String INSERT_QUERY = "INSERT INTO transactions(market_name, broker_name, op_type, instrument, " +
            "price, quantity, result, comment) VALUES(?,?,?,?,?,?,?,?)";
    private static final String SELECT_QUERY = "SELECT * FROM transactions";
    private static Connection connection;

    public static void connect() {
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

    private static Connection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    public static void insert(String marketName, String brokerName, String type, String instrument,
                              String price, String quantity, String result, String comment) {
        final Connection localConnection = getConnection();
        if (localConnection != null) {
            try (final PreparedStatement pstmt = localConnection.prepareStatement(INSERT_QUERY)) {
                pstmt.setString(1, marketName);
                pstmt.setString(2, brokerName);
                pstmt.setString(3, type);
                pstmt.setString(4, instrument);
                pstmt.setString(5, price);
                pstmt.setString(6, quantity);
                pstmt.setString(7, result);
                pstmt.setString(8, comment);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error on insert");
            } finally {
                close();
            }
        }
    }

    public static void selectAll() {
        final Connection localConnection = getConnection();
        if (localConnection != null) {
            try (final Statement stmt = localConnection.createStatement();
                final ResultSet rs = stmt.executeQuery(SELECT_QUERY)) {
                final AsciiTable at = new AsciiTable();
                at.addRule();
                at.addRow("id", "market_name", "broker_name", "op_type", "result");
                while (rs.next()) {
                    at.addRule();
                    at.addRow(rs.getInt("id"),
                            rs.getString("market_name"),
                            rs.getString("broker_name"),
                            rs.getString("op_type"),
                            rs.getString("result"));
                }
                at.addRule();
                System.out.println(at.render());
            } catch (SQLException e) {
                System.out.println("Error on insert");
            } finally {
                close();
            }
        }
    }
}
