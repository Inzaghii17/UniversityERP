package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DBUtil;
import java.sql.*;
import java.util.*;

public class SectionDAO {

    public static List<Section> getAllSections() throws SQLException {
        List<Section> list = new ArrayList<>();
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, capacity, day_time, room FROM sections";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setCapacity(rs.getInt("capacity"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));
                list.add(s);
            }
        }
        return list;
    }

    public static void addSection(Section s) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, semester, year, capacity, day_time, room) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getERPConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getCourseId());
            ps.setInt(2, s.getInstructorId());
            ps.setString(3, s.getSemester());
            ps.setInt(4, s.getYear());
            ps.setInt(5, s.getCapacity());
            ps.setString(6, s.getDayTime());
            ps.setString(7, s.getRoom());
            ps.executeUpdate();
        }
    }

    public static List<Section> listAll() {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT section_id, course_id, instructor_id, semester, year, capacity, day_time, room FROM sections";

        try (Connection conn = DBUtil.getERPConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Section s = new Section();
                s.setSectionId(rs.getInt("section_id"));
                s.setCourseId(rs.getInt("course_id"));
                s.setInstructorId(rs.getInt("instructor_id"));
                s.setSemester(rs.getString("semester"));
                s.setYear(rs.getInt("year"));
                s.setCapacity(rs.getInt("capacity"));
                s.setDayTime(rs.getString("day_time"));
                s.setRoom(rs.getString("room"));

                sections.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error fetching sections: " + e.getMessage());
        }

        return sections;
    }

}
