package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.data.SectionDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InstructorDashboard extends JFrame {
    private final User user;

    public InstructorDashboard(User user) {
        this.user = user;
        setTitle("Instructor Dashboard - " + user.getUsername());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh My Sections");
        top.add(refresh);
        add(top, BorderLayout.NORTH);

        JTextArea area = new JTextArea(); area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        refresh.addActionListener(e -> {
            try {
                area.setText("");
                List<Section> all = SectionDAO.getAllSections();
                for (Section s : all) {
                    if (s.getInstructorId() == user.getUserId()) {
                        area.append(String.format("Sec %d - %s (%s) %s %d cap:%d\n", s.getSectionId(), s.getCourseCode(), s.getCourseTitle(), s.getSemester(), s.getYear(), s.getCapacity()));
                        // TODO: add grade entry UI per section later.
                    }
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        refresh.doClick();
    }
}
