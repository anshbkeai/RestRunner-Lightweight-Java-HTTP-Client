package com.restrunner.core.user.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private final static class Holder {
        private static final ConnectDB INSTANCE = new ConnectDB();
    }

    public static ConnectDB getInstance() {
        return Holder.INSTANCE;
    }

    private final Connection conn;

    public ConnectDB() {

        String userHome = System.getProperty("user.home");
        String url = "jdbc:h2:file:" + userHome + "/.myapp/db";

        try {
             conn = DriverManager.getConnection(url, "sa", "");
             initDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Connection getConn(){
        return conn;
    }

    private void initDatabase() {
        try (var stmt = conn.createStatement()) {

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                userID VARCHAR(255) PRIMARY KEY,
                email  VARCHAR(255),
                token VARCHAR(255) 
            )
        """);

            stmt.execute("""
                       CREATE TABLE IF NOT EXISTS api_history  (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           uri VARCHAR(1000),
                           method VARCHAR(10),
                           request_json CLOB,
                           response_json CLOB,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           synced Boolean
                       );
                    """);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DB", e);
        }
    }
}
