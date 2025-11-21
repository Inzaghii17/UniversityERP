package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

/**
 * A quick panel for viewing averages per selected section.
 * Uses GradesDAO.getAverages(sectionId) which you already have.
 */
public class StatsPanel extends JPanel {

    private final JComboBox<Section> sectionDropdown = new JComboBox<>();
    private final User instructor;

    public StatsPanel(User instructor) {
        this.instructor = instructor;

        setLayout(new BorderLayout(10, 10));

        for (Section s : InstructorDAO.getSectionsForInstructor(instructor.getUserId()))
            sectionDropdown.addItem(s);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Section: "));
        top.add(sectionDropdown);

        JButton show = new JButton("Show Stats");
        top.add(show);

        add(top, BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(area), BorderLayout.CENTER);

        show.addActionListener(e -> {
            Section sec = (Section) sectionDropdown.getSelectedItem();
            if (sec == null) return;

            double[] a = GradesDAO.getAverages(sec.getSectionId());
            area.setText(
                    "Class Averages\n" +
                            "-----------------\n" +
                            "Quiz: " + a[0] + "\n" +
                            "Mid : " + a[1] + "\n" +
                            "End : " + a[2] + "\n"
            );
        });
    }
}
