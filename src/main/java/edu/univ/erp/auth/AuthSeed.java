package edu.univ.erp.auth;

import edu.univ.erp.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AuthSeed {
    public static void main(String[] args) {
        String username = "admin1";
        String role = "ADMIN";
        String plain = "admin123";

        try (Connection c = DBUtil.getAuthConnection()) {
            String hash = PasswordUtils.hashPassword(plain);
            System.out.println("Generated hash: " + hash);

            try (PreparedStatement del = c.prepareStatement("DELETE FROM users WHERE username = ?")) {
                del.setString(1, username);
                del.executeUpdate();
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO users (username, role, password_hash) VALUES (?, ?, ?)")) {
                ins.setString(1, username);
                ins.setString(2, role);
                ins.setString(3, hash);
                ins.executeUpdate();
            }
            System.out.println("Seeded user: " + username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
