package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.domain.Grade;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class StatsDialog extends JDialog {

    public StatsDialog(Frame owner, int sectionId) {
        super(owner, "Class Stats", true);
        setSize(480, 320);
        setLayout(new BorderLayout());

        List<Grade> grades = GradesDAO.getGradesBySection(sectionId);

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0,1,6,6));
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        p.add(new JLabel("Students with grade rows: " + grades.size()));

        DoubleSummaryStatistics quizStats = grades.stream()
                .filter(g -> g.getQuiz() != null)
                .mapToDouble(g -> g.getQuiz())
                .summaryStatistics();
        p.add(new JLabel("Quiz - avg: " + safe(quizStats.getAverage()) + "  min: " + safe(quizStats.getMin()) + "  max: " + safe(quizStats.getMax())));

        DoubleSummaryStatistics midStats = grades.stream()
                .filter(g -> g.getMidsem() != null)
                .mapToDouble(g -> g.getMidsem())
                .summaryStatistics();
        p.add(new JLabel("Midsem - avg: " + safe(midStats.getAverage()) + "  min: " + safe(midStats.getMin()) + "  max: " + safe(midStats.getMax())));

        DoubleSummaryStatistics endStats = grades.stream()
                .filter(g -> g.getEndsem() != null)
                .mapToDouble(g -> g.getEndsem())
                .summaryStatistics();
        p.add(new JLabel("Endsem - avg: " + safe(endStats.getAverage()) + "  min: " + safe(endStats.getMin()) + "  max: " + safe(endStats.getMax())));

        DoubleSummaryStatistics finalStats = grades.stream()
                .map(g -> {
                    try { return Double.valueOf(g.getFinalGrade()); }
                    catch (Exception ex) { return Double.NaN; }
                })
                .filter(d -> !Double.isNaN(d))
                .mapToDouble(d -> d)
                .summaryStatistics();
        p.add(new JLabel("Final - avg: " + safe(finalStats.getAverage()) + "  min: " + safe(finalStats.getMin()) + "  max: " + safe(finalStats.getMax())));

        add(new JScrollPane(p), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Close");
        ok.addActionListener(e -> dispose());
        bottom.add(ok);
        add(bottom, BorderLayout.SOUTH);
    }

    private String safe(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return "N/A";
        return String.format("%.2f", v);
    }
}
