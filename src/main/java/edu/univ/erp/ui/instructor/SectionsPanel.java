package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SectionsPanel extends JPanel {

    private final User instructor;
    private final DefaultTableModel model;

    public SectionsPanel(User instructor) {
        this.instructor = instructor;

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"SectionID", "Course", "Title", "Day/Time", "Room", "Semester", "Year"}, 0
        );

        JTable table = new JTable(model);
        table.setRowHeight(26);

        add(new JLabel("My Sections", SwingConstants.LEFT), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadSections();
    }

    private void loadSections() {
        model.setRowCount(0);
        List<Section> list = InstructorDAO.getSectionsForInstructor(instructor.getUserId());

        for (Section s : list) {
            model.addRow(new Object[]{
                    s.getSectionId(),
                    s.getCourseCode(),
                    s.getCourseTitle(),
                    s.getDayTime(),
                    s.getRoom(),
                    s.getSemester(),
                    s.getYear()
            });
        }
    }
}
