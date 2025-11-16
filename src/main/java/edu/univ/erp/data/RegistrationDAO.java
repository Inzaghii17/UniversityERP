package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    // Check if user already registered
    public static boolean isRegistered(int userId, int sectionId) {
        String sql = "SELECT 1 FROM registrations WHERE user_id=? AND section_id=?";
        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Seats left = capacity - current count
    public static int seatsLeft(int sectionId) {
        String sql = """
            SELECT capacity - COUNT(r.user_id) AS free_seats
            FROM sections s
            LEFT JOIN registrations r ON s.section_id = r.section_id
            WHERE s.section_id = ?
            GROUP BY s.capacity
        """;
        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("free_seats");
            }

        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Perform a registration
    public static boolean register(int userId, int sectionId) {
        String sql = "INSERT INTO registrations (user_id, section_id) VALUES (?, ?)";
        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, sectionId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Drop registration
    public static boolean drop(int userId, int sectionId) {
        String sql = "DELETE FROM registrations WHERE user_id=? AND section_id=?";
        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, sectionId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Get all registered sections for a student
    public static List<Section> getRegisteredSections(int userId) {
        List<Section> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, s.course_id, c.code AS course_code, c.title AS course_title,
                   s.semester, s.year, s.day_time, s.room, s.capacity,
                   (SELECT username FROM auth_db.users u WHERE u.user_id = s.instructor_id) AS instructor_name
            FROM registrations r
            JOIN sections s ON r.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            WHERE r.user_id = ?
            ORDER BY s.section_id
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Section sec = new Section();
                    sec.setSectionId(rs.getInt("section_id"));
                    sec.setCourseId(rs.getInt("course_id"));
                    sec.setCourseCode(rs.getString("course_code"));
                    sec.setCourseTitle(rs.getString("course_title"));
                    sec.setSemester(rs.getString("semester"));
                    sec.setYear(rs.getInt("year"));
                    sec.setDayTime(rs.getString("day_time"));
                    sec.setRoom(rs.getString("room"));
                    sec.setCapacity(rs.getInt("capacity"));
                    sec.setInstructorName(rs.getString("instructor_name"));

                    list.add(sec);
                }
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }
}
