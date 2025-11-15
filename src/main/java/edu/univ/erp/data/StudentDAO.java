package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import edu.univ.erp.util.DBUtil;

import java.sql.*;

public class StudentDAO {

    // FIND student profile by userId
    public static Student findByUserId(int userId) throws SQLException {

        String sql = "SELECT user_id, roll_no, program, year FROM students WHERE user_id = ?";

        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();
                    s.setUserId(rs.getInt("user_id"));   // inherited from User
                    s.setRollNo(rs.getString("roll_no"));
                    s.setProgram(rs.getString("program"));
                    s.setYear(rs.getInt("year"));
                    return s;
                }
            }
        }

        return null;
    }

    // CREATE profile
    public static void createProfile(int userId, String rollNo, String program, int year) throws SQLException {

        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";

        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, rollNo);
            ps.setString(3, program);
            ps.setInt(4, year);

            ps.executeUpdate();
        }
    }
}
