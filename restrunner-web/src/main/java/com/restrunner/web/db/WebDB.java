package com.restrunner.web.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restrunner.web.pojo.History;

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


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc","root","root");
            initWebTables();
        } catch (SQLException e ) {
            throw new RuntimeException("Failed to connect to H2", e);
        }
        catch (ClassNotFoundException e) {
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


            stmt.execute("""
                       CREATE TABLE IF NOT EXISTS api_history_web (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           uri VARCHAR(1000),
                           method VARCHAR(10),
                           request_json LONGTEXT,
                           response_json LONGTEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           synced BOOLEAN DEFAULT TRUE
                       );
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
        String sql = "SELECT id, uri, method, request_json, response_json, created_at FROM api_history_web ORDER BY created_at DESC";

        ObjectMapper objectMapper = new ObjectMapper();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("id", rs.getInt("id"));
                row.put("uri", rs.getString("uri"));
                row.put("method", rs.getString("method"));
                row.put("created_at", rs.getTimestamp("created_at"));

                // 🔥 Parse response_json → extract useful fields
                String responseJson = rs.getString("response_json");
                if (responseJson != null) {
                    try {
                        Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);

                        row.put("status", responseMap.get("statusCode"));
                        row.put("error", responseMap.get("error"));

                        // optional: trim body preview
                        String body = (String) responseMap.get("body");
                        if (body != null && body.length() > 100) {
                            body = body.substring(0, 100) + "...";
                        }
                        row.put("response_preview", body);

                    } catch (Exception e) {
                        row.put("status", "parse_error");
                    }
                }

                // 🔥 Optional: request preview
                String requestJson = rs.getString("request_json");
                if (requestJson != null) {
                    try {
                        Map<String, Object> requestMap = objectMapper.readValue(requestJson, Map.class);

                        String body = (String) requestMap.get("body");
                        if (body != null && body.length() > 100) {
                            body = body.substring(0, 100) + "...";
                        }
                        row.put("request_preview", body);

                    } catch (Exception ignored) {}
                }

                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void saveHistory(History history, ObjectMapper objectMapper) {
        String sql = """
        INSERT INTO api_history_web (uri, method, request_json, response_json, synced)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, history.getUri());
            ps.setString(2, history.getRequest().getRequestMethod().toString());

            // 🔥 Convert nested objects → JSON
            String requestJson = objectMapper.writeValueAsString(history.getRequest());
            String responseJson = objectMapper.writeValueAsString(history.getResponse());

            ps.setString(3, requestJson);
            ps.setString(4, responseJson);

            ps.setBoolean(5, true); // already synced on server

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
