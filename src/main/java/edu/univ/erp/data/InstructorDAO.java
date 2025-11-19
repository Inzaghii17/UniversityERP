package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDAO {

    public static List<Section> getSectionsForInstructor(int instructorId) {
        List<Section> list = new ArrayList<>();

        String sql = """
            SELECT s.section_id, s.course_id,
                   c.code AS course_code, c.title AS course_title,
                   s.semester, s.year, s.day_time, s.room
            FROM sections s
            JOIN courses c ON c.course_id = s.course_id
            WHERE s.instructor_id = ?
            ORDER BY c.code
            """;

        try (Connection con = DBUtil.getERPConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Section sec = new Section();
                sec.setSectionId(rs.getInt("section_id"));
                sec.setCourseId(rs.getInt("course_id"));
                sec.setCourseCode(rs.getString("course_code"));
                sec.setCourseTitle(rs.getString("course_title"));
                sec.setSemester(rs.getString("semester"));
                sec.setYear(rs.getInt("year"));
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
