package edu.univ.erp.ui.student;

import edu.univ.erp.data.EnrollmentDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final User user;
    private final DefaultTableModel model;

    public StudentDashboard(User user) {
        this.user = user;
        setTitle("Student Dashboard - " + user.getUsername());
        setSize(900, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"SectionID","Course","Title","Semester","Year","DayTime","Room","Cap"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton enroll = new JButton("Enroll Selected");
        JButton refresh = new JButton("Refresh");
        south.add(enroll); south.add(refresh);
        add(south, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadSections());
        enroll.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a section"); return; }
            int secId = (int) model.getValueAt(r, 0);
            try {
                boolean ok = EnrollmentDAO.enroll(user.getUserId(), secId);
                JOptionPane.showMessageDialog(this, ok? "Enrolled!" : "Unable to enroll (duplicate or full).");
                refresh.doClick();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        loadSections();
    }

    private void loadSections() {
        try {
            model.setRowCount(0);
            List<Section> secs = SectionDAO.listAll();
            for (Section s : secs) {
                model.addRow(new Object[]{s.getSectionId(), s.getCourseCode(), s.getCourseTitle(), s.getSemester(), s.getYear(), s.getDayTime(), s.getRoom(), s.getCapacity()});
            }
        } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }
}
