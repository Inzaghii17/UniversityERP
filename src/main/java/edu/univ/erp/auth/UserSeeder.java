package edu.univ.erp.auth;

import edu.univ.erp.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserSeeder {
    public static void main(String[] args) {
        addUser("stu1", "STUDENT", "stu123");
        addUser("inst1", "INSTRUCTOR", "inst123");
    }

    private static void addUser(String username, String role, String password) {
        try (Connection conn = DBUtil.getAuthConnection()) {
            String hash = PasswordUtils.hashPassword(password);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, role, password_hash) VALUES (?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash), role=VALUES(role)"
            )) {
                ps.setString(1, username);
                ps.setString(2, role);
                ps.setString(3, hash);
                ps.executeUpdate();
                System.out.println("✅ User added/updated: " + username + " (" + role + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
