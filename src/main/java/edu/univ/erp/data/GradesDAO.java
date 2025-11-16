package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradesDAO {

    // Student view
    public static List<Grade> getGradesForStudent(int studentId) {
        List<Grade> list = new ArrayList<>();

        String sql = """
            SELECT * FROM grades WHERE user_id=?
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Grade g = new Grade();
                g.setGradeId(rs.getInt("grade_id"));
                g.setStudentId(rs.getInt("student_id"));
                g.setSectionId(rs.getInt("section_id"));
                g.setQuiz((Integer) rs.getObject("quiz"));
                g.setMidsem((Integer) rs.getObject("midsem"));
                g.setEndsem((Integer) rs.getObject("endsem"));
                g.setFinalGrade(rs.getString("final_grade"));

                list.add(g);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // Instructor enters / updates grade
    public static boolean upsertGrade(Grade g) throws SQLException {
        String sql = """
            INSERT INTO grades (student_id, section_id, quiz, midsem, endsem, final_grade)
            VALUES (?,?,?,?,?,?)
            ON DUPLICATE KEY UPDATE
                quiz=VALUES(quiz),
                midsem=VALUES(midsem),
                endsem=VALUES(endsem),
                final_grade=VALUES(final_grade)
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getSectionId());
            ps.setObject(3, g.getQuiz());
            ps.setObject(4, g.getMidsem());
            ps.setObject(5, g.getEndsem());
            ps.setString(6, g.getFinalGrade());

            return ps.executeUpdate() > 0;
        }
    }

    // Admin report: all grades
    public static List<Grade> getAllGrades() {
        List<Grade> list = new ArrayList<>();

        String sql = "SELECT * FROM grades";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Grade g = new Grade();
                g.setGradeId(rs.getInt("grade_id"));
                g.setStudentId(rs.getInt("student_id"));
                g.setSectionId(rs.getInt("section_id"));
                g.setQuiz((Integer) rs.getObject("quiz"));
                g.setMidsem((Integer) rs.getObject("midsem"));
                g.setEndsem((Integer) rs.getObject("endsem"));
                g.setFinalGrade(rs.getString("final_grade"));

                list.add(g);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }
}
