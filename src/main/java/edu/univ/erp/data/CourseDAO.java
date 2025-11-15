package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.util.DBUtil;
import java.sql.*;
import java.util.*;

public class CourseDAO {

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

    // Get all courses
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
}
