package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DBUtil;

import java.sql.*;

public class AuthDAO {

    public static User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, role, password_hash FROM users WHERE username = ?";
        try (Connection c = DBUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setRole(rs.getString("role"));
                    return u;
                } else {
                    return null;
                }
            }
        }
    }

    public static String getPasswordHash(String username) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = DBUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("password_hash") : null;
            }
        }
    }

    public static int createUser(String username, String role, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, role, password_hash) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            try (ResultSet g = ps.getGeneratedKeys()) {
                if (g.next()) return g.getInt(1);
                else throw new SQLException("No ID returned");
            }
        }
    }
}
