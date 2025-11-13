package edu.univ.erp.data;

import edu.univ.erp.util.DBUtil;

import java.sql.*;

public class EnrollmentDAO {
    public static boolean enroll(int studentId, int sectionId) throws SQLException {
        // check duplicate
        String dup = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(dup)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        }

        // check capacity
        String capSql = "SELECT capacity, (SELECT COUNT(*) FROM enrollments e WHERE e.section_id = ? AND e.status='enrolled') as taken FROM sections WHERE section_id = ?";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(capSql)) {
            ps.setInt(1, sectionId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int capacity = rs.getInt("capacity");
                    int taken = rs.getInt("taken");
                    if (taken >= capacity) return false; // full
                } else return false; // no such section
            }
        }

        String ins = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, 'enrolled')";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(ins)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static boolean drop(int studentId, int sectionId) throws SQLException {
        String del = "DELETE FROM enrollments WHERE student_id = ? AND section_id = ?";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(del)) {
            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            return ps.executeUpdate() > 0;
        }
    }
}
