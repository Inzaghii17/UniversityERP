package edu.univ.erp.ui.admin;

import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageSectionsFrame extends JFrame {

    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Course ID", "Instructor ID", "Semester", "Year", "Capacity", "Time", "Room"}, 0);

    public ManageSectionsFrame() {
        setTitle("Manage Sections");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add Section");

        refreshBtn.addActionListener(e -> loadSections());
        addBtn.addActionListener(e -> addSection());

        panel.add(refreshBtn);
        panel.add(addBtn);

        add(panel, BorderLayout.SOUTH);
        loadSections();
    }

    private void loadSections() {
        try {
            model.setRowCount(0);
            List<Section> list = SectionDAO.getAllSections();
            for (Section s : list) {
                model.addRow(new Object[]{
                        s.getSectionId(), s.getCourseId(), s.getInstructorId(),
                        s.getSemester(), s.getYear(), s.getCapacity(), s.getDayTime(), s.getRoom()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + e.getMessage());
        }
    }

    private void addSection() {
        JTextField courseId = new JTextField();
        JTextField instructorId = new JTextField();
        JTextField semester = new JTextField();
        JTextField year = new JTextField();
        JTextField capacity = new JTextField();
        JTextField dayTime = new JTextField();
        JTextField room = new JTextField();

        Object[] msg = {"Course ID:", courseId, "Instructor ID:", instructorId, "Semester:", semester, "Year:", year, "Capacity:", capacity, "Day/Time:", dayTime, "Room:", room};

        if (JOptionPane.showConfirmDialog(this, msg, "Add Section", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Section s = new Section();
                s.setCourseId(Integer.parseInt(courseId.getText()));
                s.setInstructorId(Integer.parseInt(instructorId.getText()));
                s.setSemester(semester.getText());
                s.setYear(Integer.parseInt(year.getText()));
                s.setCapacity(Integer.parseInt(capacity.getText()));
                s.setDayTime(dayTime.getText());
                s.setRoom(room.getText());
                SectionDAO.addSection(s);
                loadSections();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
