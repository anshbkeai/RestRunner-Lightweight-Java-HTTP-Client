package com.restrunner.core.user.db;



import com.restrunner.core.user.pojo.User;

import java.sql.*;
import java.util.Optional;

public class UserDB {

    private static class Holder {
        private static final UserDB INSTANCE = new UserDB();
    }

    public static UserDB getInstance() {
        return Holder.INSTANCE;
    }

    private final Connection conn;

    private UserDB() {
        this.conn = ConnectDB.getInstance().getConn();
    }

    // ✅ CREATE / SAVE USER
    public void save(User user) {
        String sql = "MERGE INTO users (userID, email, token) KEY(userID) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUserID());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getToken());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    // ✅ GET USER BY ID
    public Optional<User> getUser() {
        String sql = "SELECT * FROM users ";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {


            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = map(rs);
                return Optional.of(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user", e);
        }

        return Optional.empty();
    }

    // ✅ GET USER BY TOKEN
    public Optional<User> getByToken(String token) {
        String sql = "SELECT * FROM users WHERE token = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user by token", e);
        }

        return Optional.empty();
    }

    // ✅ UPDATE TOKEN
    public void updateToken(String userId, String token) {
        String sql = "UPDATE users SET token = ? WHERE userID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, userId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update token", e);
        }
    }

    // ✅ DELETE USER
    public void delete(String userId) {
        String sql = "DELETE FROM users WHERE userID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    // ✅ CHECK EXISTS
    public boolean exists(String userId) {
        String sql = "SELECT 1 FROM users WHERE userID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user existence", e);
        }
    }

    // 🔁 Mapper
    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("userID"),
                rs.getString("email"),
                rs.getString("token")
        );
    }
}
