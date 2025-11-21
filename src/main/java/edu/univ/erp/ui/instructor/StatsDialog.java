package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.domain.Grade;

import javax.swing.*;
import java.awt.*;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Simple modal to show class stats using GradesDAO.getGradesBySection.
 */
public class StatsDialog extends JDialog {

    public StatsDialog(Frame owner, int sectionId) {
        super(owner, "Class Statistics", true);
        setSize(520, 360);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        try {
            List<Grade> grades = GradesDAO.getGradesBySection(sectionId);
            DoubleSummaryStatistics q = grades.stream().filter(g -> g.getQuiz() != null).mapToDouble(g -> g.getQuiz()).summaryStatistics();
            DoubleSummaryStatistics m = grades.stream().filter(g -> g.getMidsem() != null).mapToDouble(g -> g.getMidsem()).summaryStatistics();
            DoubleSummaryStatistics e = grades.stream().filter(g -> g.getEndsem() != null).mapToDouble(g -> g.getEndsem()).summaryStatistics();

            StringBuilder sb = new StringBuilder();
            sb.append("Rows: ").append(grades.size()).append("\n\n");
            sb.append("Quiz  - avg: ").append(formatStat(q)).append("\n");
            sb.append("Mid   - avg: ").append(formatStat(m)).append("\n");
            sb.append("End   - avg: ").append(formatStat(e)).append("\n");

            area.setText(sb.toString());
        } catch (Exception ex) {
            area.setText("Failed to load stats: " + ex.getMessage());
            ex.printStackTrace();
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);
    }

    private String formatStat(DoubleSummaryStatistics s) {
        if (s == null || s.getCount() == 0) return "N/A";
        return String.format("%.2f (min: %.2f max: %.2f)", s.getAverage(), s.getMin(), s.getMax());
    }
}
