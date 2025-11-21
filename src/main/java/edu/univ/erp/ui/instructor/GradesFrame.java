// CLEAN VERSION WITH FIXES — READY TO USE

package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.SectionGradeRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class GradesFrame extends JFrame {

    private final int instructorId;
    private final JComboBox<Section> cbSections;
    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField tfQ, tfM, tfE;
    private final JButton btnCompute, btnSaveComponents, btnComputePersist;

    private boolean regColumnHidden = false;

    public GradesFrame(int instructorId) {
        super("Enter Grades");
        this.instructorId = instructorId;
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        // TOP PANEL
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        top.add(new JLabel("Select Section:"));
        cbSections = new JComboBox<>();
        cbSections.setPreferredSize(new Dimension(420, 28));
        top.add(cbSections);

        top.add(new JLabel("Quiz %"));
        tfQ = new JTextField(3);
        top.add(tfQ);

        top.add(new JLabel("Mid %"));
        tfM = new JTextField(3);
        top.add(tfM);

        top.add(new JLabel("End %"));
        tfE = new JTextField(3);
        top.add(tfE);

        btnCompute = new JButton("Compute Final (Preview)");
        btnSaveComponents = new JButton("Save Component Scores");
        btnComputePersist = new JButton("Compute & Persist Final Grades");

        top.add(btnCompute);
        top.add(btnSaveComponents);
        top.add(btnComputePersist);

        add(top, BorderLayout.NORTH);

        // TABLE MODEL
        model = new DefaultTableModel(
                new Object[]{"Reg ID", "Student ID", "Roll No",
                        "Quiz", "Midsem", "Endsem", "Preview/Stored Final"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return !isReadOnly() && (column == 3 || column == 4 || column == 5);
            }
        };

        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // BOTTOM PANEL
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh Sections");
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);

        // Load sections
        loadSections();

        // -------------------- EVENT HANDLERS --------------------

        cbSections.addActionListener(ev -> {
            Section s = (Section) cbSections.getSelectedItem();
            if (s != null) {
                tfQ.setText(String.valueOf(s.getQuizWeight()));
                tfM.setText(String.valueOf(s.getMidsemWeight()));
                tfE.setText(String.valueOf(s.getEndsemWeight()));
                loadRosterForSection(s.getSectionId());
            } else {
                model.setRowCount(0);
            }
        });

        btnCompute.addActionListener(ev -> {
            try {
                computePreview();
                JOptionPane.showMessageDialog(this, "Preview computed.", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSaveComponents.addActionListener(ev -> {
            if (isReadOnly()) {
                JOptionPane.showMessageDialog(this, "Maintenance mode ON — cannot save.", "Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                saveComponentScores();
                JOptionPane.showMessageDialog(this, "Saved.", "OK", JOptionPane.INFORMATION_MESSAGE);

                Section s = (Section) cbSections.getSelectedItem();
                if (s != null) loadRosterForSection(s.getSectionId());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnComputePersist.addActionListener(ev -> {
            if (isReadOnly()) {
                JOptionPane.showMessageDialog(this, "Maintenance mode ON — cannot persist.", "Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Section s = (Section) cbSections.getSelectedItem();
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Select section first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                int q = parseInt(tfQ.getText());
                int m = parseInt(tfM.getText());
                int e = parseInt(tfE.getText());

                if (q + m + e != 100) {
                    JOptionPane.showMessageDialog(this, "Weights must sum to 100.", "Invalid", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                SectionDAO.updateWeights(s.getSectionId(), q, m, e);

                InstructorService svc = new InstructorService();
                svc.computeAndSaveFinalForSection(s.getSectionId(), q, m, e);

                JOptionPane.showMessageDialog(this, "Final saved.", "OK", JOptionPane.INFORMATION_MESSAGE);

                loadRosterForSection(s.getSectionId());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefresh.addActionListener(ev -> loadSections());

        // MAINTENANCE MODE BANNER
        if (isReadOnly()) {
            JLabel banner = new JLabel("MAINTENANCE MODE — READ ONLY", SwingConstants.CENTER);
            banner.setOpaque(true);
            banner.setBackground(Color.RED);
            banner.setForeground(Color.WHITE);
            banner.setFont(new Font("Segoe UI", Font.BOLD, 13));
            add(banner, BorderLayout.NORTH);

            tfQ.setEnabled(false);
            tfM.setEnabled(false);
            tfE.setEnabled(false);
            btnCompute.setEnabled(false);
            btnSaveComponents.setEnabled(false);
            btnComputePersist.setEnabled(false);
        }
    }

    private boolean isReadOnly() {
        try {
            return SettingsDAO.isMaintenance();
        } catch (Exception ex) {
            return false;
        }
    }

    private void loadSections() {
        cbSections.removeAllItems();
        try {
            List<Section> sec = SectionDAO.getSectionsByInstructor(instructorId);
            if (sec != null) {
                for (Section s : sec) cbSections.addItem(s);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRosterForSection(int sectionId) {
        model.setRowCount(0);

        try {
            InstructorService svc = new InstructorService();
            List<SectionGradeRow> rows = svc.sectionGrades(sectionId);

            for (SectionGradeRow r : rows) {
                model.addRow(new Object[]{
                        r.enrollmentId,
                        r.studentId,
                        r.rollNo != null ? r.rollNo : "",
                        r.quiz != null ? r.quiz : "",
                        r.midterm != null ? r.midterm : "",
                        r.endsem != null ? r.endsem : "",
                        r.finalGrade != null ? r.finalGrade : (r.finalScore != null ? String.format("%.2f", r.finalScore) : "")
                });
            }

            // Hide Reg ID column only once
            if (!regColumnHidden) {
                table.removeColumn(table.getColumnModel().getColumn(0));
                regColumnHidden = true;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void computePreview() {
        int q = parseInt(tfQ.getText());
        int m = parseInt(tfM.getText());
        int e = parseInt(tfE.getText());

        double nq = q / 100.0, nm = m / 100.0, ne = e / 100.0;

        for (int r = 0; r < model.getRowCount(); r++) {
            double qv = parseDouble(model.getValueAt(r, 3));
            double mv = parseDouble(model.getValueAt(r, 4));
            double ev = parseDouble(model.getValueAt(r, 5));

            double p = qv * nq + mv * nm + ev * ne;
            p = Math.round(p * 100.0) / 100.0;

            model.setValueAt(String.format("%.2f", p), r, 6);
        }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception ex) { return 0; }
    }

    private double parseDouble(Object o) {
        if (o == null) return 0.0;
        try { return Double.parseDouble(o.toString()); }
        catch (Exception ex) { return 0.0; }
    }

    private void saveComponentScores() throws SQLException {
        InstructorService svc = new InstructorService();

        for (int r = 0; r < model.getRowCount(); r++) {

            int enrollmentId = Integer.parseInt(model.getValueAt(r, 0).toString());

            Double q = safeDouble(model.getValueAt(r, 3));
            Double m = safeDouble(model.getValueAt(r, 4));
            Double en = safeDouble(model.getValueAt(r, 5));

            svc.saveComponentScores(enrollmentId, q, m, en);
        }
    }

    private Double safeDouble(Object val) {
        if (val == null) return null;
        String s = val.toString().trim();
        if (s.isEmpty()) return null;
        try { return Double.valueOf(s); } catch (Exception ex) { return null; }
    }
}
