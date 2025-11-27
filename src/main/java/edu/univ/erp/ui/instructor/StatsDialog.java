package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.domain.Grade;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class StatsDialog extends JDialog {

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);
    private final Color CONTENT_BG = new Color(245, 245, 245);

    public StatsDialog(Frame owner, int sectionId) {
        super(owner, "Class Statistics", true);

        setSize(900, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);

        /* ------------------------------------------------------------------
         * LEFT IMAGE SIDEBAR (SAME AS ALL OTHER SCREENS)
         * ------------------------------------------------------------------ */
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel title = new JLabel("Statistics", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(title);
        sidebar.add(Box.createVerticalGlue());

        add(sidebar, BorderLayout.WEST);

        /* ------------------------------------------------------------------
         * MAIN CONTENT AREA (MATCH STUDENT & INSTRUCTOR STYLE)
         * ------------------------------------------------------------------ */
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(CONTENT_BG);
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        add(content, BorderLayout.CENTER);

        /* ------------------------------------------------------------------
         * STATS TEXT AREA (INSIDE A LIGHT CARD)
         * ------------------------------------------------------------------ */
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        statsArea.setBackground(Color.WHITE);
        statsArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        statsArea.setLineWrap(true);
        statsArea.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(statsArea);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        content.add(sp, BorderLayout.CENTER);

        /* ------------------------------------------------------------------
         * BOTTOM BUTTON BAR (MATCH THEME)
         * ------------------------------------------------------------------ */
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);

        JButton btnClose = purpleButton("Close");
        btnClose.setPreferredSize(new Dimension(110, 40));
        bottom.add(btnClose);

        content.add(bottom, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());

        /* ------------------------------------------------------------------
         * LOAD STATISTICS
         * ------------------------------------------------------------------ */
        try {
            List<Grade> grades = GradesDAO.getGradesBySection(sectionId);

            DoubleSummaryStatistics quizStats = grades.stream()
                    .filter(g -> g.getQuiz() != null)
                    .mapToDouble(Grade::getQuiz)
                    .summaryStatistics();

            DoubleSummaryStatistics midStats = grades.stream()
                    .filter(g -> g.getMidsem() != null)
                    .mapToDouble(Grade::getMidsem)
                    .summaryStatistics();

            DoubleSummaryStatistics endStats = grades.stream()
                    .filter(g -> g.getEndsem() != null)
                    .mapToDouble(Grade::getEndsem)
                    .summaryStatistics();

            StringBuilder sb = new StringBuilder();
            sb.append("Total Students: ").append(grades.size()).append("\n\n");
            sb.append("Quiz Avg: ").append(formatStat(quizStats)).append("\n");
            sb.append("Mid Avg:  ").append(formatStat(midStats)).append("\n");
            sb.append("End Avg:  ").append(formatStat(endStats)).append("\n");

            statsArea.setText(sb.toString());

        } catch (Exception ex) {
            statsArea.setText("Failed to load stats: " + ex.getMessage());
        }
    }

    /* ------------------------------------------------------------------
     * FORMAT UTIL
     * ------------------------------------------------------------------ */
    private String formatStat(DoubleSummaryStatistics s) {
        if (s == null || s.getCount() == 0) return "N/A";
        return String.format("%.2f  (min: %.2f   max: %.2f)",
                s.getAverage(), s.getMin(), s.getMax());
    }

    /* ------------------------------------------------------------------
     * PURPLE BUTTON
     * ------------------------------------------------------------------ */
    private JButton purpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(PURPLE); }
        });

        return btn;
    }

    /* ------------------------------------------------------------------
     * IMAGE PANEL (same everywhere)
     * ------------------------------------------------------------------ */
    class ImagePanel extends JPanel {
        private final Image img;

        public ImagePanel(String resourcePath) {
            ImageIcon icon = null;
            try { icon = new ImageIcon(getClass().getResource(resourcePath)); }
            catch (Exception ignored) {}
            img = icon != null ? icon.getImage() : null;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null)
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
