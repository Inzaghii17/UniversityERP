package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Frame: shows sections for instructorId. Select section and click "View Class List".
 * Uses SectionDAO.getSectionsByInstructor(int).
 */
public class SectionsFrame extends JFrame {

    private final int instructorId;
    private final DefaultTableModel model;
    private final JTable table;

    public SectionsFrame(int instructorId) {
        super("My Sections");
        this.instructorId = instructorId;
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        model = new DefaultTableModel(new Object[]{"Section ID", "Course Code", "Title", "Semester", "Year", "Capacity", "Day/Time", "Room"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnViewClass = new JButton("View Class List");
        JButton btnRefresh = new JButton("Refresh");
        bottom.add(btnViewClass);
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadSections());

        btnViewClass.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a section first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Object idObj = table.getValueAt(r, 0);
            if (idObj == null) {
                JOptionPane.showMessageDialog(this, "Invalid section selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int sectionId;
            try {
                sectionId = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid section id.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ClassListFrame clf = new ClassListFrame(sectionId);
            clf.setVisible(true);
        });

        loadSections();
    }

    private void loadSections() {
        model.setRowCount(0);
        try {
            List<Section> list = SectionDAO.getSectionsByInstructor(instructorId);
            if (list == null || list.isEmpty()) {
                // nothing
            } else {
                for (Section s : list) {
                    model.addRow(new Object[]{
                            s.getSectionId(),
                            s.getCourseCode(),
                            s.getCourseTitle(),
                            s.getSemester(),
                            s.getYear(),
                            s.getCapacity(),
                            s.getDayTime(),
                            s.getRoom()
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load sections: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
