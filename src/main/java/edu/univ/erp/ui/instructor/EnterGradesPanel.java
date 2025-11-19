package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.SectionGradeRow;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.GradesDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class EnterGradesPanel extends JPanel {

    private Section section;
    private JTable table;
    private DefaultTableModel model;
    private final InstructorService svc = new InstructorService();

    public EnterGradesPanel(Section section) {
        this.section = section;
        setLayout(new BorderLayout());

        // Columns: enrollmentId (hidden), student_id (visible), quiz, midsem, endsem, final (computed)
        String[] cols = {"enrollment_id", "Student ID", "Quiz", "Midsem", "Endsem", "Final"};
        model = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 1) return Integer.class;
                if (col >= 2 && col <= 4) return Integer.class;
                return String.class;
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 3 || col == 4; // edit components only
            }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Section: "));
        top.add(new JLabel(section.getCourseCode() + " - " + section.getCourseTitle() + " (" + section.getSemester() + " " + section.getYear() + ")"));
        top.add(Box.createHorizontalStrut(12));
        top.add(new JLabel("Weights Q/M/E: "));
        JTextField wq = new JTextField(String.valueOf(section.getQuizWeight()), 3);
        JTextField wm = new JTextField(String.valueOf(section.getMidsemWeight()), 3);
        JTextField we = new JTextField(String.valueOf(section.getEndsemWeight()), 3);
        top.add(wq); top.add(wm); top.add(we);

        JButton persistWeights = new JButton("Set Weights");
        persistWeights.addActionListener(e -> {
            try {
                int q = Integer.parseInt(wq.getText().trim());
                int m = Integer.parseInt(wm.getText().trim());
                int en = Integer.parseInt(we.getText().trim());
                if (q + m + en != 100) {
                    JOptionPane.showMessageDialog(this, "Weights must sum to 100");
                    return;
                }
                boolean ok = SectionDAO.updateWeights(section.getSectionId(), q, m, en);
                if (ok) {
                    section.setQuizWeight(q);
                    section.setMidsemWeight(m);
                    section.setEndsemWeight(en);
                    recomputeFinalsInTable();
                    JOptionPane.showMessageDialog(this, "Weights saved.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to persist weights.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter integer weights.");
            }
        });
        top.add(persistWeights);

        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Component Scores");
        saveBtn.addActionListener(e -> saveComponentScores());
        bottom.add(saveBtn);

        JButton computeBtn = new JButton("Compute & Persist Final Grades");
        computeBtn.addActionListener(e -> computeAndPersistFinals(wq, wm, we));
        bottom.add(computeBtn);

        JButton statsBtn = new JButton("Show Class Stats");
        statsBtn.addActionListener(e -> showStats());
        bottom.add(statsBtn);

        add(bottom, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            List<SectionGradeRow> rows = svc.sectionGrades(section.getSectionId());
            for (SectionGradeRow r : rows) {
                Object finalVal = r.finalGrade != null ? r.finalGrade : (r.finalScore == null ? "" : String.valueOf(Math.round(r.finalScore)));
                model.addRow(new Object[]{ r.enrollmentId, r.studentId, r.quiz, r.midterm, r.endsem, finalVal });
            }
            // hide enrollment_id column visually but keep in model
            if (table.getColumnModel().getColumnCount() > 0) {
                try { table.removeColumn(table.getColumnModel().getColumn(0)); } catch (Exception ignored) {}
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load grades: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Integer parseInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        try {
            String s = o.toString().trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) { return null; }
    }

    private void saveComponentScores() {
        try {
            for (int i = 0; i < model.getRowCount(); i++) {
                Integer enrollmentId = (Integer) model.getValueAt(i, 0);
                Integer q = parseInteger(model.getValueAt(i, 2));
                Integer m = parseInteger(model.getValueAt(i, 3));
                Integer e = parseInteger(model.getValueAt(i, 4));
                // InstructorService.saveComponentScores expects Double or null
                svc.saveComponentScores(enrollmentId,
                        q == null ? null : (double) q,
                        m == null ? null : (double) m,
                        e == null ? null : (double) e);
            }
            JOptionPane.showMessageDialog(this, "Component scores saved.");
            // reload final column preview
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving scores: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void recomputeFinalsInTable() {
        for (int i = 0; i < model.getRowCount(); i++) {
            Integer q = parseInteger(model.getValueAt(i, 2));
            Integer m = parseInteger(model.getValueAt(i, 3));
            Integer en = parseInteger(model.getValueAt(i, 4));
            double finalNum = (q == null ? 0 : q) * (section.getQuizWeight()/100.0)
                    + (m == null ? 0 : m) * (section.getMidsemWeight()/100.0)
                    + (en == null ? 0 : en) * (section.getEndsemWeight()/100.0);
            model.setValueAt(String.valueOf((int)Math.round(finalNum)), i, 5);
        }
    }

    private void computeAndPersistFinals(JTextField wq, JTextField wm, JTextField we) {
        // overloaded helper that extracts weights from fields
        try {
            int q = Integer.parseInt(wq.getText().trim());
            int m = Integer.parseInt(wm.getText().trim());
            int e = Integer.parseInt(we.getText().trim());
            if (q + m + e != 100) {
                JOptionPane.showMessageDialog(this, "Weights must sum to 100");
                return;
            }
            // persist weights first
            boolean ok = SectionDAO.updateWeights(section.getSectionId(), q, m, e);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to persist weights.");
                return;
            }
            section.setQuizWeight(q);
            section.setMidsemWeight(m);
            section.setEndsemWeight(e);

            // compute and save finals via service
            svc.computeAndSaveFinalForSection(section.getSectionId(), q, m, e);
            JOptionPane.showMessageDialog(this, "Final grades computed and saved.");
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter integer weights.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error computing finals: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void computeAndPersistFinals(javax.swing.text.JTextComponent wq, javax.swing.text.JTextComponent wm, javax.swing.text.JTextComponent we) {
        // compatibility overload for method reference (not used externally)
        computeAndPersistFinals((JTextField) wq, (JTextField) wm, (JTextField) we);
    }

    private void showStats() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        StatsDialog dlg = new StatsDialog(owner, section.getSectionId());
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
