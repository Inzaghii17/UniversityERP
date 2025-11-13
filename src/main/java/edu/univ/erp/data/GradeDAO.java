package edu.univ.erp.data;

import edu.univ.erp.util.DBUtil;

import java.sql.*;

public class GradeDAO {
    public static void insertGrade(int enrollmentId, String component, double score) throws SQLException {
        String sql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setDouble(3, score);
            ps.executeUpdate();
        }
    }

    // get grades for an enrollment
    public static ResultSet getGradesForEnrollment(Connection c, int enrollmentId) throws SQLException {
        String sql = "SELECT * FROM grades WHERE enrollment_id = ?";
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, enrollmentId);
        return ps.executeQuery();
    }
}
