package edu.univ.erp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBUtil {
    // EDIT these to match your local MySQL credentials
    private static final String USER = "root";
    private static final String PASS = "Ishank@17";
    private static final String AUTH_DB_URL = "jdbc:mysql://localhost:3306/auth_db?serverTimezone=UTC";
    private static final String ERP_DB_URL = "jdbc:mysql://localhost:3306/erp_db?serverTimezone=UTC";

    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(AUTH_DB_URL, USER, PASS);
    }

    public static Connection getERPConnection() throws SQLException {
        return DriverManager.getConnection(ERP_DB_URL, USER, PASS);
    }
}