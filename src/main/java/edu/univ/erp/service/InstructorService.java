package edu.univ.erp.service;

import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InstructorService - lightweight service used by instructor UI.
 *
 * Exposes:
 *  - sectionGrades(sectionId) -> list of SectionGradeRow
 *  - saveComponentScores(enrollmentId, q, mid, end)
 *  - computeAndSaveFinalForSection(sectionId, wq, wm, we)
 *
 * Note: We purposely do NOT change DB schema. To avoid duplicate grade rows we
 * check existence (SELECT) and then either INSERT or UPDATE.
 */
public class InstructorService {

    public static class SectionGradeRow {
        public final int enrollmentId; // registrations.reg_id
        public final int studentId;    // users.user_id
        public final String rollNo;    // students.roll_no (may be null)
        public final Integer quiz;
        public final Integer midterm;
        public final Integer endsem;
        public final Double finalScore; // percent computed (may be null)
        public final String finalGrade; // letter (may be null)

        public SectionGradeRow(int enrollmentId, int studentId, String rollNo,
                               Integer quiz, Integer midterm, Integer endsem,
                               Double finalScore, String finalGrade) {
            this.enrollmentId = enrollmentId;
            this.studentId = studentId;
            this.rollNo = rollNo;
            this.quiz = quiz;
            this.midterm = midterm;
            this.endsem = endsem;
            this.finalScore = finalScore;
            this.finalGrade = finalGrade;
        }
    }

    /**
     * Return all enrolled students for section with their current component scores
     * and persisted final grade (letter). finalScore is computed on the fly only
     * if quiz/mid/end are present and weights are default 20/30/50 (not saved).
     */
    public List<SectionGradeRow> sectionGrades(int sectionId) throws SQLException {
        List<SectionGradeRow> out = new ArrayList<>();

        String sql = """
            SELECT r.reg_id,
                   r.user_id,
                   st.roll_no,
                   g.quiz, g.midsem, g.endsem, g.final_grade
            FROM registrations r
            LEFT JOIN grades g ON g.user_id = r.user_id AND g.section_id = r.section_id
            LEFT JOIN students st ON st.user_id = r.user_id
            WHERE r.section_id = ?
            ORDER BY r.reg_id
            """;
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int regId = rs.getInt("reg_id");
                    int userId = rs.getInt("user_id");
                    String roll = rs.getString("roll_no");
                    Integer quiz = (Integer) rs.getObject("quiz");
                    Integer mid = (Integer) rs.getObject("midsem");
                    Integer end = (Integer) rs.getObject("endsem");
                    String fg = rs.getString("final_grade");

                    Double pct = null;
                    // If components available, compute percent using default 20/30/50 (for preview)
                    if (quiz != null && mid != null && end != null) {
                        pct = quiz * 0.20 + mid * 0.30 + end * 0.50;
                    }

                    out.add(new SectionGradeRow(regId, userId, roll, quiz, mid, end, pct, fg));
                }
            }
        }

        return out;
    }

    /**
     * Save component scores for a given enrollment (registration/reg_id).
     * This will INSERT or UPDATE grades row for (user_id, section_id).
     *
     * We keep final_grade untouched here (so instructor can compute final later).
     */
    public void saveComponentScores(int enrollmentId, Double quizD, Double midD, Double endD) throws SQLException {
        // find user_id and section_id for this enrollment
        String lookup = "SELECT user_id, section_id FROM registrations WHERE reg_id = ?";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(lookup)) {

            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Enrollment not found: " + enrollmentId);
                int userId = rs.getInt("user_id");
                int sectionId = rs.getInt("section_id");

                // convert Double -> Integer (store as whole number). Null stays null.
                Integer quiz = quizD == null ? null : (int) Math.round(quizD);
                Integer mid = midD == null ? null : (int) Math.round(midD);
                Integer end = endD == null ? null : (int) Math.round(endD);

                // check existing grade row
                String sel = "SELECT grade_id, final_grade FROM grades WHERE user_id=? AND section_id=?";
                Integer existingGradeId = null;
                String existingFinal = null;
                try (PreparedStatement ps2 = c.prepareStatement(sel)) {
                    ps2.setInt(1, userId);
                    ps2.setInt(2, sectionId);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) {
                            existingGradeId = rs2.getInt("grade_id");
                            existingFinal = rs2.getString("final_grade");
                        }
                    }
                }

                if (existingGradeId != null) {
                    // UPDATE existing row
                    String upd = "UPDATE grades SET quiz = ?, midsem = ?, endsem = ? WHERE grade_id = ?";
                    try (PreparedStatement psu = c.prepareStatement(upd)) {
                        if (quiz == null) psu.setNull(1, Types.INTEGER); else psu.setInt(1, quiz);
                        if (mid == null) psu.setNull(2, Types.INTEGER); else psu.setInt(2, mid);
                        if (end == null) psu.setNull(3, Types.INTEGER); else psu.setInt(3, end);
                        psu.setInt(4, existingGradeId);
                        psu.executeUpdate();
                    }
                } else {
                    // INSERT new row (final_grade left NULL)
                    String ins = "INSERT INTO grades (user_id, section_id, quiz, midsem, endsem, final_grade) VALUES (?, ?, ?, ?, ?, NULL)";
                    try (PreparedStatement psi = c.prepareStatement(ins)) {
                        psi.setInt(1, userId);
                        psi.setInt(2, sectionId);
                        if (quiz == null) psi.setNull(3, Types.INTEGER); else psi.setInt(3, quiz);
                        if (mid == null) psi.setNull(4, Types.INTEGER); else psi.setInt(4, mid);
                        if (end == null) psi.setNull(5, Types.INTEGER); else psi.setInt(5, end);
                        psi.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Compute final percent and letter for every student in a section using given weights,
     * then persist the letter into grades.final_grade for each (insert row if missing).
     *
     * Note: Database does not have a dedicated column for percent; we persist the letter only.
     * If you want the percent saved as well, we would need to add a column (or store letter+percent
     * in final_grade string).
     */
    public void computeAndSaveFinalForSection(int sectionId, int wq, int wm, int we) throws SQLException {
        if (wq + wm + we != 100) throw new SQLException("Weights must sum to 100");

        // Get all registrations for section
        String sql = """
            SELECT r.reg_id, r.user_id
            FROM registrations r
            WHERE r.section_id = ?
            """;

        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");

                    // fetch current components if any
                    String sel = "SELECT quiz, midsem, endsem FROM grades WHERE user_id=? AND section_id=?";
                    Integer quiz = null, mid = null, end = null;
                    try (PreparedStatement ps2 = c.prepareStatement(sel)) {
                        ps2.setInt(1, userId);
                        ps2.setInt(2, sectionId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) {
                                quiz = (Integer) rs2.getObject("quiz");
                                mid = (Integer) rs2.getObject("midsem");
                                end = (Integer) rs2.getObject("endsem");
                            }
                        }
                    }

                    if (quiz == null || mid == null || end == null) {
                        // skip if incomplete components
                        continue;
                    }

                    double percent = quiz * (wq / 100.0) + mid * (wm / 100.0) + end * (we / 100.0);
                    String letter = percentToLetter(percent);

                    // Upsert final_grade for this user+section
                    // Check existing row
                    String sel2 = "SELECT grade_id FROM grades WHERE user_id=? AND section_id=?";
                    Integer gid = null;
                    try (PreparedStatement ps3 = c.prepareStatement(sel2)) {
                        ps3.setInt(1, userId);
                        ps3.setInt(2, sectionId);
                        try (ResultSet rs3 = ps3.executeQuery()) {
                            if (rs3.next()) gid = rs3.getInt("grade_id");
                        }
                    }

                    if (gid != null) {
                        String upd = "UPDATE grades SET final_grade = ? WHERE grade_id = ?";
                        try (PreparedStatement psu = c.prepareStatement(upd)) {
                            psu.setString(1, letter);
                            psu.setInt(2, gid);
                            psu.executeUpdate();
                        }
                    } else {
                        String ins = "INSERT INTO grades (user_id, section_id, quiz, midsem, endsem, final_grade) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement psi = c.prepareStatement(ins)) {
                            psi.setInt(1, userId);
                            psi.setInt(2, sectionId);
                            psi.setInt(3, quiz);
                            psi.setInt(4, mid);
                            psi.setInt(5, end);
                            psi.setString(6, letter);
                            psi.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private String percentToLetter(double p) {
        if (p >= 90) return "A";
        if (p >= 80) return "B";
        if (p >= 70) return "C";
        if (p >= 60) return "D";
        return "F";
    }
}
