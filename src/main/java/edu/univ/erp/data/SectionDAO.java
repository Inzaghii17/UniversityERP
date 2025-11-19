package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {

    // -------------------------------
    // ADMIN: Add new section
    // -------------------------------
    public static void addSection(Section s) throws SQLException {
        String sql = """
            INSERT INTO sections (course_id, instructor_id, semester, year, capacity, day_time, room,
                                  quiz_weight, midsem_weight, endsem_weight)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getCourseId());
            ps.setInt(2, s.getInstructorId());
            ps.setString(3, s.getSemester());
            ps.setInt(4, s.getYear());
            ps.setInt(5, s.getCapacity());
            ps.setString(6, s.getDayTime());
            ps.setString(7, s.getRoom());

            // NEW weight fields
            ps.setInt(8, s.getQuizWeight());
            ps.setInt(9, s.getMidsemWeight());
            ps.setInt(10, s.getEndsemWeight());

            ps.executeUpdate();
        }
    }

    // -------------------------------
    // ADMIN: Update section
    // -------------------------------
    public static void updateSection(Section s) throws SQLException {
        String sql = """
            UPDATE sections
            SET course_id=?, instructor_id=?, semester=?, year=?, capacity=?, day_time=?, room=?,
                quiz_weight=?, midsem_weight=?, endsem_weight=?
            WHERE section_id=?
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getCourseId());
            ps.setInt(2, s.getInstructorId());
            ps.setString(3, s.getSemester());
            ps.setInt(4, s.getYear());
            ps.setInt(5, s.getCapacity());
            ps.setString(6, s.getDayTime());
            ps.setString(7, s.getRoom());

            ps.setInt(8, s.getQuizWeight());
            ps.setInt(9, s.getMidsemWeight());
            ps.setInt(10, s.getEndsemWeight());

            ps.setInt(11, s.getSectionId());
            ps.executeUpdate();
        }
    }

    // -------------------------------
    // ADMIN: Delete section
    // -------------------------------
    public static void deleteSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id=?";
        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }

    // -------------------------------
    // ADMIN: List all sections
    // -------------------------------
    public static List<Section> getAllSections() {
        List<Section> list = new ArrayList<>();

        String sql = """
            SELECT s.*, c.code AS course_code, c.title AS course_title,
                   (SELECT username FROM auth_db.users u WHERE u.user_id = s.instructor_id) AS instructor_name
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            ORDER BY s.section_id
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

                // NEW weights
                s.setQuizWeight(rs.getInt("quiz_weight"));
                s.setMidsemWeight(rs.getInt("midsem_weight"));
                s.setEndsemWeight(rs.getInt("endsem_weight"));

                list.add(s);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // -------------------------------
    // INSTRUCTOR: Get their sections
    // -------------------------------
    public static List<Section> getSectionsByInstructor(int instructorId) {
        List<Section> list = new ArrayList<>();

        String sql = """
            SELECT s.*, c.code AS course_code, c.title AS course_title
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            WHERE instructor_id=?
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setCourseCode(rs.getString("course_code"));
                s.setCourseTitle(rs.getString("course_title"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setCapacity(rs.getInt("capacity"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));

                // NEW weights
                s.setQuizWeight(rs.getInt("quiz_weight"));
                s.setMidsemWeight(rs.getInt("midsem_weight"));
                s.setEndsemWeight(rs.getInt("endsem_weight"));

                list.add(s);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // -------------------------------
    // INSTRUCTOR: Update only weights
    // -------------------------------
    public static boolean updateWeights(int sectionId, int q, int m, int e) {
        String sql = """
            UPDATE sections 
            SET quiz_weight=?, midsem_weight=?, endsem_weight=?
            WHERE section_id=?
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, q);
            ps.setInt(2, m);
            ps.setInt(3, e);
            ps.setInt(4, sectionId);

            return ps.executeUpdate() > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
