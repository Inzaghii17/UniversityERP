package edu.univ.erp.service;

import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorService {

    public static class SectionGradeRow {
        public final int enrollmentId;
        public final int studentId;
        public final String rollNo;
        public final Integer quiz;
        public final Integer midterm;
        public final Integer endsem;
        public final Double finalScore;
        public final String finalGrade;

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
                    // default preview 20/30/50 (UI can override with its own weights)
                    if (quiz != null && mid != null && end != null) {
                        pct = quiz * 0.20 + mid * 0.30 + end * 0.50;
                    }

                    out.add(new SectionGradeRow(regId, userId, roll, quiz, mid, end, pct, fg));
                }
            }
        }

        return out;
    }


    public void saveComponentScores(int enrollmentId, Double quizD, Double midD, Double endD) throws SQLException {
        String lookup = "SELECT user_id, section_id FROM registrations WHERE reg_id = ?";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(lookup)) {

            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Enrollment not found: " + enrollmentId);
                int userId = rs.getInt("user_id");
                int sectionId = rs.getInt("section_id");

                Integer quiz = quizD == null ? null : (int) Math.round(quizD);
                Integer mid = midD == null ? null : (int) Math.round(midD);
                Integer end = endD == null ? null : (int) Math.round(endD);

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
                    String upd = "UPDATE grades SET quiz = ?, midsem = ?, endsem = ? WHERE grade_id = ?";
                    try (PreparedStatement psu = c.prepareStatement(upd)) {
                        if (quiz == null) psu.setNull(1, Types.INTEGER); else psu.setInt(1, quiz);
                        if (mid == null) psu.setNull(2, Types.INTEGER); else psu.setInt(2, mid);
                        if (end == null) psu.setNull(3, Types.INTEGER); else psu.setInt(3, end);
                        psu.setInt(4, existingGradeId);
                        psu.executeUpdate();
                    }
                } else {
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


    public void computeAndSaveFinalForSection(int sectionId, int wq, int wm, int we) throws SQLException {
        if (wq + wm + we != 100) throw new SQLException("Weights must sum to 100");

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
                        continue; // incomplete
                    }

                    double percent = quiz * (wq / 100.0) + mid * (wm / 100.0) + end * (we / 100.0);
                    String letter = percentToLetter(percent);

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
        double cg = Math.round(p) / 10.0; // same CGPA conversion as UI

        if (cg == 10) return "A+";
        if (cg >= 9) return "A";
        if (cg >= 8) return "B";
        if (cg >= 7) return "B-";
        if (cg >= 6) return "C";
        if (cg >= 5) return "C-";
        if (cg >= 4) return "D+";
        if (cg >= 3) return "D";
        return "F";
    }
}
