package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.data.SectionDAO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InstructorDashboard extends JPanel {

    private JTabbedPane tabs;
    private int instructorId;

    public InstructorDashboard(int instructorId) {
        this.instructorId = instructorId;
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();

        ClassListPanel classList = new ClassListPanel(instructorId);
        tabs.addTab("My Sections", classList);

        JPanel statsPlaceholder = new JPanel(new BorderLayout());
        statsPlaceholder.add(new JLabel("Open a section and use 'Enter Grades' to access stats."), BorderLayout.CENTER);
        tabs.addTab("Class Stats", statsPlaceholder);

        add(tabs, BorderLayout.CENTER);

        classList.setSectionSelectionListener(section -> {
            EnterGradesPanel enterPanel = new EnterGradesPanel(section);
            String tabTitle = "Grades: " + section.getCourseCode() + " [" + section.getSectionId() + "]";
            int idx = tabs.indexOfTab(tabTitle);
            if (idx >= 0) {
                tabs.setComponentAt(idx, enterPanel);
                tabs.setSelectedIndex(idx);
            } else {
                tabs.addTab(tabTitle, enterPanel);
                tabs.setSelectedIndex(tabs.indexOfTab(tabTitle));
            }
        });
    }

    public void refreshSections() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component c = tabs.getComponentAt(i);
            if (c instanceof ClassListPanel) {
                ((ClassListPanel) c).loadSections();
                break;
            }
        }
    }

    // quick test main (optional)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Instructor Dashboard - Test");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            InstructorDashboard d = new InstructorDashboard(1);
            f.setContentPane(d);
            f.setSize(900, 600);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
