package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // -------------------------------------
    // ADMIN FEATURES (add / update / delete)
    // -------------------------------------

    // Add new course
    public static void addCourse(Course c) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.executeUpdate();
        }
    }

    // Update existing course
    public static void updateCourse(Course c) throws SQLException {
        String sql = "UPDATE courses SET code=?, title=?, credits=? WHERE course_id=?";
        try (Connection conn = DBUtil.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.setInt(4, c.getCourseId());
            ps.executeUpdate();
        }
    }

    // Delete a course
    public static void deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id=?";
        try (Connection conn = DBUtil.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }

    // Get all courses (for admin Manage Courses page)
    public static List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try (Connection conn = DBUtil.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCode(rs.getString("code"));
                c.setTitle(rs.getString("title"));
                c.setCredits(rs.getInt("credits"));
                list.add(c);
            }
        }
        return list;
    }

    // -------------------------------------
    // STUDENT FEATURES (course catalog)
    // -------------------------------------

    // Returns all sections with course info + instructor info
    public static List<Section> getCatalogSections() {
        List<Section> list = new ArrayList<>();
        String sql = """
            SELECT s.section_id, s.course_id, c.code AS course_code, c.title AS course_title,
                   s.instructor_id, s.semester, s.year, s.capacity, s.day_time, s.room,
                   (SELECT username FROM auth_db.users u WHERE u.user_id = s.instructor_id) AS instructor_name
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            ORDER BY c.code, s.section_id
            """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Section sec = new Section();
                sec.setSectionId(rs.getInt("section_id"));
                sec.setCourseId(rs.getInt("course_id"));
                sec.setCourseCode(rs.getString("course_code"));
                sec.setCourseTitle(rs.getString("course_title"));
                sec.setInstructorId(rs.getInt("instructor_id"));
                sec.setInstructorName(rs.getString("instructor_name"));
                sec.setSemester(rs.getString("semester"));
                sec.setYear(rs.getInt("year"));
                sec.setCapacity(rs.getInt("capacity"));
                sec.setDayTime(rs.getString("day_time"));
                sec.setRoom(rs.getString("room"));

                list.add(sec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
