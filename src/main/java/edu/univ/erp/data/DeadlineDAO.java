package edu.univ.erp.data;

import edu.univ.erp.util.DBUtil;

import java.sql.*;

public class DeadlineDAO {

    public static Date getDropDeadline(int sectionId) {
        String sql = "SELECT drop_deadline FROM deadlines WHERE section_id=?";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDate("drop_deadline");

        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    public static boolean setDropDeadline(int sectionId, Date deadline) throws SQLException {
        String sql = """
            INSERT INTO deadlines (section_id, drop_deadline)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE drop_deadline=VALUES(drop_deadline)
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setDate(2, deadline);

            return ps.executeUpdate() > 0;
        }
    }
}
