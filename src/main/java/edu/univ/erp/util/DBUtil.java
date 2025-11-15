package edu.univ.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {

    private static final String USER = "root";          // your MySQL username
    private static final String PASS = "Ishank@17";       // your MySQL password
    private static final String AUTH_DB_URL = "jdbc:mysql://localhost:3306/auth_db?serverTimezone=UTC";
    private static final String ERP_DB_URL  = "jdbc:mysql://localhost:3306/erp_db?serverTimezone=UTC";

    public static Connection getAuthConnection() {
        try {
            return DriverManager.getConnection(AUTH_DB_URL, USER, PASS);
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to auth_db: " + e.getMessage(), e);
        }
    }

    public static Connection getERPConnection() {
        try {
            return DriverManager.getConnection(ERP_DB_URL, USER, PASS);
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to erp_db: " + e.getMessage(), e);
        }
    }
}
