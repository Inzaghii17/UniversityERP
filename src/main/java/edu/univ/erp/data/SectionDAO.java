package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {
    public static List<Section> listAll() throws SQLException {
        String sql = "SELECT s.section_id,s.course_id,c.code,c.title,s.instructor_id,s.semester,s.year,s.capacity,s.day_time,s.room " +
                "FROM sections s JOIN courses c ON s.course_id=c.course_id";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Section> out = new ArrayList<>();
            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setCourseCode(rs.getString("code"));
                s.setCourseTitle(rs.getString("title"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setCapacity(rs.getInt("capacity"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));
                out.add(s);
            }
            return out;
        }
    }

    public static int create(Section s) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, semester, year, capacity, day_time, room) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getCourseId());
            if (s.getInstructorId() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, s.getInstructorId());
            ps.setString(3, s.getSemester());
            ps.setInt(4, s.getYear());
            ps.setInt(5, s.getCapacity());
            ps.setString(6, s.getDayTime());
            ps.setString(7, s.getRoom());
            ps.executeUpdate();
            try (ResultSet g = ps.getGeneratedKeys()) {
                if (g.next()) return g.getInt(1);
                else throw new SQLException("No section id");
            }
        }
    }
}
