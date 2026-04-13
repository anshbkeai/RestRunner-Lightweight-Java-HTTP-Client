package com.restrunner.web.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebDB {
    private static class Holder {
        private static final WebDB INSTANCE = new WebDB();
    }

    public static WebDB getInstance() {
        return Holder.INSTANCE;
    }

    private final Connection conn;

    private WebDB() {
        String userHome = System.getProperty("user.home");
        String url = "jdbc:h2:file:" + userHome + "/.myapp/db;AUTO_SERVER=TRUE";
        try {
            conn = DriverManager.getConnection(url, "sa", "");
            initWebTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to H2", e);
        }
    }

    public Connection getConn() {
        return conn;
    }

    private void initWebTables() {
        try (Statement stmt = conn.createStatement()) {
            // Table for web authentication
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS web_users (
                    username VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    token VARCHAR(255),
                    last_login TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Re-ensure main tables exist just in case (though they should)
            // But we don't want to change existing code, just use the DB.
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize web tables", e);
        }
    }

    public boolean signup(String username, String password) {
        String sql = "INSERT INTO web_users (username, password) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public String login(String username, String password) {
        String sql = "SELECT password FROM web_users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedPass = rs.getString("password");
                if (storedPass.equals(password)) {
                    String token = java.util.UUID.randomUUID().toString();
                    updateLoginInfo(username, token);
                    return token;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateLoginInfo(String username, String token) {
        String sql = "UPDATE web_users SET last_login = ?, token = ? WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, token);
            ps.setString(3, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isValidToken(String token) {
        String sql = "SELECT 1 FROM web_users WHERE token = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public LocalDateTime getLastLogin(String username) {
        String sql = "SELECT last_login FROM web_users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("last_login");
                return ts != null ? ts.toLocalDateTime() : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Map<String, Object>> getHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM api_history ORDER BY created_at DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("uri", rs.getString("uri"));
                row.put("method", rs.getString("method"));
                row.put("request_json", rs.getString("request_json"));
                row.put("response_json", rs.getString("response_json"));
                row.put("created_at", rs.getTimestamp("created_at"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
