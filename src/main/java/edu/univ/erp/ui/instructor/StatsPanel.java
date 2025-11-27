package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatsPanel extends JPanel {

    private final JComboBox<Section> sectionDropdown = new JComboBox<>();
    private final User instructor;

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);
    private final Color CONTENT_BG = new Color(245, 245, 245);

    public StatsPanel(User instructor) {
        this.instructor = instructor;

        setLayout(new BorderLayout(12, 12));
        setBackground(CONTENT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        /* ------------------------------------------------------------------
         * TOP CONTROL BAR
         * ------------------------------------------------------------------ */
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        top.setOpaque(false);

        JLabel lbl = new JLabel("Select Section:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        top.add(lbl);

        // Populate dropdown
        for (Section s : InstructorDAO.getSectionsForInstructor(instructor.getUserId())) {
            sectionDropdown.addItem(s);
        }
        sectionDropdown.setPreferredSize(new Dimension(320, 32));
        sectionDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        top.add(sectionDropdown);

        JButton show = purpleButton("Show Stats");
        show.setPreferredSize(new Dimension(130, 36));
        top.add(show);

        add(top, BorderLayout.NORTH);

        /* ------------------------------------------------------------------
         * CENTER DISPLAY AREA
         * ------------------------------------------------------------------ */
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(15, 15, 15, 15));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        add(sp, BorderLayout.CENTER);

        /* ------------------------------------------------------------------
         * ACTION HANDLING
         * ------------------------------------------------------------------ */
        show.addActionListener(e -> {
            Section sec = (Section) sectionDropdown.getSelectedItem();
            if (sec == null) return;

            double[] averages = GradesDAO.getAverages(sec.getSectionId());

            area.setText(
                    "Class Averages\n" +
                            "-------------------------------\n" +
                            "Quiz Average : " + format(averages[0]) + "\n" +
                            "Mid Average  : " + format(averages[1]) + "\n" +
                            "End Average  : " + format(averages[2]) + "\n"
            );
        });
    }

    /* ------------------------------------------------------------------
     * FORMAT HELPER
     * ------------------------------------------------------------------ */
    private String format(double v) {
        return String.format("%.2f", v);
    }

    /* ------------------------------------------------------------------
     * PURPLE BUTTON STYLE (matches entire theme)
     * ------------------------------------------------------------------ */
    private JButton purpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PURPLE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(PURPLE); }
        });

        return btn;
    }
}
