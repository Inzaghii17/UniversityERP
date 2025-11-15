package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    // Student already enrolled?
    public static boolean isRegistered(int studentId, int sectionId) {
        String sql = "SELECT 1 FROM registrations WHERE student_id=? AND section_id=?";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);

            return ps.executeQuery().next();

        } catch (Exception e) { e.printStackTrace(); }

        return false;
    }

    // Available seats?
    public static int seatsLeft(int sectionId) {
        String sql = """
            SELECT capacity - (
                SELECT COUNT(*) FROM registrations WHERE section_id=?
            ) AS seats_left
            FROM sections
            WHERE section_id=?
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, sectionId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("seats_left");

        } catch (Exception e) { e.printStackTrace(); }

        return 0;
    }

    // Register student
    public static boolean register(int studentId, int sectionId) throws SQLException {
        String sql = "INSERT INTO registrations (student_id, section_id, registration_date) VALUES (?, ?, CURDATE())";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);
            return ps.executeUpdate() > 0;
        }
    }

    // Drop section
    public static boolean drop(int studentId, int sectionId) throws SQLException {
        String sql = "DELETE FROM registrations WHERE student_id=? AND section_id=?";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);

            return ps.executeUpdate() > 0;
        }
    }

    // Student's registered sections
    public static List<Section> getRegisteredSections(int studentId) {
        List<Section> list = new ArrayList<>();

        String sql = """
            SELECT s.*, c.code AS course_code, c.title AS course_title,
                   (SELECT username FROM auth_db.users u WHERE u.user_id = s.instructor_id) AS instructor_name
            FROM registrations r
            JOIN sections s ON r.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
            WHERE r.student_id=?
            ORDER BY s.day_time
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setCourseCode(rs.getString("course_code"));
                s.setCourseTitle(rs.getString("course_title"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setInstructorName(rs.getString("instructor_name"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setCapacity(rs.getInt("capacity"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));

                list.add(s);
            }
        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // ADMIN: Get all registrations
    public static List<String> getAllRegistrations() {
        List<String> list = new ArrayList<>();

        String sql = """
            SELECT r.reg_id,
                   a.username AS student,
                   c.code AS course_code,
                   s.section_id
            FROM registrations r
            JOIN auth_db.users a ON r.student_id = a.user_id
            JOIN sections s ON r.section_id = s.section_id
            JOIN courses c ON s.course_id = c.course_id
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String row = "RegID: " + rs.getInt("reg_id") +
                        " | Student: " + rs.getString("student") +
                        " | Course: " + rs.getString("course_code") +
                        " | Section: " + rs.getInt("section_id");

                list.add(row);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }
}
