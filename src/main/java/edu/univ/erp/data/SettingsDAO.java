package edu.univ.erp.data;

import edu.univ.erp.util.DBUtil;

import java.sql.*;

public class SettingsDAO {
    public static boolean isMaintenanceOn() throws SQLException {
        String sql = "SELECT value FROM settings WHERE `key` = 'maintenance'";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return "true".equalsIgnoreCase(rs.getString("value"));
            return false;
        }
    }

    public static void setMaintenance(boolean on) throws SQLException {
        String v = on ? "true" : "false";
        String sql = "INSERT INTO settings (`key`,`value`) VALUES ('maintenance',?) ON DUPLICATE KEY UPDATE `value` = ?";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v);
            ps.setString(2, v);
            ps.executeUpdate();
        }
    }
}
