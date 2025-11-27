package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradesDAO {


    public static List<Grade> getGradesForStudent(int userId) {
        List<Grade> list = new ArrayList<>();

        String sql = "SELECT * FROM grades WHERE user_id = ?";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Grade g = new Grade();
                g.setGradeId(rs.getInt("grade_id"));
                g.setStudentId(rs.getInt("user_id"));
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

    public static List<Grade> getStudentsInSection(int sectionId) {
        List<Grade> list = new ArrayList<>();

        String sql = """
            SELECT r.user_id AS user_id,
                   g.quiz, g.midsem, g.endsem, g.final_grade
            FROM registrations r
            LEFT JOIN grades g 
                   ON g.user_id = r.user_id 
                  AND g.section_id = r.section_id
            WHERE r.section_id = ?
            ORDER BY r.user_id
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Grade g = new Grade();
                g.setStudentId(rs.getInt("user_id"));
                g.setSectionId(sectionId);
                g.setQuiz((Integer) rs.getObject("quiz"));
                g.setMidsem((Integer) rs.getObject("midsem"));
                g.setEndsem((Integer) rs.getObject("endsem"));
                g.setFinalGrade(rs.getString("final_grade"));
                list.add(g);
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // --------------------------
    // INSTRUCTOR: Save → UPSERT
    // --------------------------
    public static void saveGrade(Grade g) {

        String sql = """
            INSERT INTO grades (user_id, section_id, quiz, midsem, endsem, final_grade)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                quiz = VALUES(quiz),
                midsem = VALUES(midsem),
                endsem = VALUES(endsem),
                final_grade = VALUES(final_grade)
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getSectionId());
            ps.setObject(3, g.getQuiz());
            ps.setObject(4, g.getMidsem());
            ps.setObject(5, g.getEndsem());
            ps.setString(6, g.getFinalGrade());

            ps.executeUpdate();

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --------------------------
    // INSTRUCTOR: Get all grades in section
    // (needed for Stats + recompute + UI refresh)
    // --------------------------
    public static List<Grade> getGradesBySection(int sectionId) {
        List<Grade> list = new ArrayList<>();

        String sql = "SELECT * FROM grades WHERE section_id = ?";

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Grade g = new Grade();
                g.setGradeId(rs.getInt("grade_id"));
                g.setStudentId(rs.getInt("user_id"));
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

    public static double[] getAverages(int sectionId) {
        String sql = """
            SELECT AVG(quiz) AS aq,
                   AVG(midsem) AS am,
                   AVG(endsem) AS ae
            FROM grades
            WHERE section_id = ?
        """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new double[]{
                        rs.getDouble("aq"),
                        rs.getDouble("am"),
                        rs.getDouble("ae")
                };
            }

        } catch (Exception e) { e.printStackTrace(); }

        return new double[]{0, 0, 0};
    }
}
