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
        try (Connection c = DBUtil.getAuthConnection()) {
            String hash = PasswordUtils.hashPassword(password);

            // Delete existing user if any
            try (PreparedStatement del = c.prepareStatement("DELETE FROM users WHERE username=?")) {
                del.setString(1, username);
                del.executeUpdate();
            }

            // Insert new one
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO users (username, role, password_hash) VALUES (?, ?, ?)")) {
                ps.setString(1, username);
                ps.setString(2, role);
                ps.setString(3, hash);
                ps.executeUpdate();
            }

            System.out.println("✅ Created user: " + username + " (" + role + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
