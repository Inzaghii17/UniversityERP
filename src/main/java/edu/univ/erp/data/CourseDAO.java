package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    public static List<Course> listAll() throws SQLException {
        String sql = "SELECT course_id, code, title, credits FROM courses";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Course> out = new ArrayList<>();
            while (rs.next()) {
                Course co = new Course();
                co.setCourseId(rs.getInt("course_id"));
                co.setCode(rs.getString("code"));
                co.setTitle(rs.getString("title"));
                co.setCredits(rs.getInt("credits"));
                out.add(co);
            }
            return out;
        }
    }

    public static int create(Course course) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getTitle());
            ps.setInt(3, course.getCredits());
            ps.executeUpdate();
            try (ResultSet g = ps.getGeneratedKeys()) {
                if (g.next()) return g.getInt(1);
                else throw new SQLException("No course id");
            }
        }
    }
}

