package edu.univ.erp.data;

import edu.univ.erp.util.DBUtil;
import java.sql.*;


public class SettingsDAO {


    public static boolean isMaintenance() throws SQLException {
        String sql = "SELECT value FROM settings WHERE `key`='maintenance'";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("value"));
            }
            return false;
        }
    }


    public static void toggleMaintenance() throws SQLException {
        boolean now = !isMaintenance();
        String sql = "UPDATE settings SET value=? WHERE `key`='maintenance'";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, now ? "true" : "false");
            ps.executeUpdate();
        }
    }
}
