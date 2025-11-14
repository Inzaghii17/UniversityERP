package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DBUtil;
import java.sql.*;
import java.util.*;

public class UserDAO {

    public static List<User> listAll() throws SQLException {
        String sql = "SELECT * FROM users";
        try (Connection c = DBUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setRole(rs.getString("role"));
                list.add(u);
            }
            return list;
        }
    }

    public static void addUser(String username, String role, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, role, password_hash) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, role);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
        }
    }

    public static void deleteUser(int userId) throws SQLException {
        try (Connection c = DBUtil.getAuthConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
