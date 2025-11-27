package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.SectionGradeRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class GradesFrame extends JFrame {

    private final int instructorId;
    private Integer selectedSectionId = null;
    private final InstructorService service = new InstructorService();

    private boolean maintenanceOn = false;

    private JTable table;
    private JComboBox<Section> sectionDropdown;
    private JSpinner spQuiz, spMid, spEnd;

    private JButton btnSave, btnCompute;

    private final Color PURPLE = new Color(102, 53, 139);
    private final Color PURPLE_DARK = new Color(85, 44, 115);
    private final Color CONTENT_BG = new Color(245, 245, 245);

    private JPanel maintenanceBanner;
    private JLabel maintenanceLabel;

    public GradesFrame(int instructorId) {
        this.instructorId = instructorId;

        maintenanceOn = AccessControl.isMaintenance();

        setTitle("Enter Grades");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        /* ===============================================================
         * TOP HEADER
         * =============================================================== */
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setPreferredSize(new Dimension(1000, 60));
        topHeader.setBackground(PURPLE);

        JLabel headerTitle = new JLabel("Enter Grades", SwingConstants.LEFT);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setBorder(new EmptyBorder(0, 18, 0, 0));

        JButton exitBtn = new JButton("Exit");
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(PURPLE_DARK);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> dispose());

        topHeader.add(headerTitle, BorderLayout.WEST);
        topHeader.add(exitBtn, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        /* ===============================================================
         * MAINTENANCE MODE BANNER
         * =============================================================== */
        maintenanceBanner = new JPanel();
        maintenanceBanner.setBackground(new Color(255, 200, 50));
        maintenanceBanner.setPreferredSize(new Dimension(1000, 32));

        maintenanceLabel = new JLabel("⚠ Maintenance Mode is ON — READ-ONLY MODE");
        maintenanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceLabel.setForeground(Color.DARK_GRAY);
        maintenanceLabel.setBorder(new EmptyBorder(5, 15, 5, 0));

        maintenanceBanner.add(maintenanceLabel);
        maintenanceBanner.setVisible(maintenanceOn);
        add(maintenanceBanner, BorderLayout.SOUTH);

        /* ===============================================================
         * LEFT SIDEBAR
         * =============================================================== */
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(220, 0));
        add(sidebar, BorderLayout.WEST);

        /* ===============================================================
         * MAIN CONTENT AREA
         * =============================================================== */
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.setBackground(CONTENT_BG);
        add(content, BorderLayout.CENTER);

        /* ===============================================================
         * TOP CONTROL ROW
         * =============================================================== */
        JPanel controlRow = new JPanel(new BorderLayout());
        controlRow.setOpaque(false);

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftControls.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Section:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));

        sectionDropdown = new JComboBox<>();
        sectionDropdown.setPreferredSize(new Dimension(250, 28));
        sectionDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        leftControls.add(lblSelect);
        leftControls.add(sectionDropdown);
        controlRow.add(leftControls, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        rightControls.setOpaque(false);

        JLabel lq = new JLabel("Quiz %:");
        JLabel lm = new JLabel("Mid %:");
        JLabel le = new JLabel("End %:");

        Font f = new Font("Segoe UI", Font.BOLD, 13);
        lq.setFont(f);
        lm.setFont(f);
        le.setFont(f);

        spQuiz = new JSpinner(new SpinnerNumberModel(20, 0, 100, 1));
        spMid = new JSpinner(new SpinnerNumberModel(30, 0, 100, 1));
        spEnd = new JSpinner(new SpinnerNumberModel(50, 0, 100, 1));

        Dimension spDim = new Dimension(55, 24);
        spQuiz.setPreferredSize(spDim);
        spMid.setPreferredSize(spDim);
        spEnd.setPreferredSize(spDim);

        rightControls.add(lq); rightControls.add(spQuiz);
        rightControls.add(lm); rightControls.add(spMid);
        rightControls.add(le); rightControls.add(spEnd);

        controlRow.add(rightControls, BorderLayout.EAST);
        content.add(controlRow, BorderLayout.NORTH);

        /* ===============================================================
         * TABLE
         * =============================================================== */
        table = new JTable(new DefaultTableModel(
                new Object[]{"EnrollmentID", "StudentID", "Quiz", "MidSem", "EndSem", "Final CGPA", "Final Grade"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return !maintenanceOn && (c == 2 || c == 3 || c == 4);
            }
        }) {
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 2 || column == 3 || column == 4) {
                    try {
                        double d = Double.parseDouble(aValue.toString());
                        if (d < 0 || d > 100) {
                            JOptionPane.showMessageDialog(null, "Marks must be between 0 and 100.");
                            return;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Invalid number.");
                        return;
                    }
                }
                super.setValueAt(aValue, row, column);
            }
        };

        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        content.add(sp, BorderLayout.CENTER);

        /* ===============================================================
         * BOTTOM BUTTON BAR
         * =============================================================== */
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setOpaque(false);

        JButton btnLoad = purpleButton("Load");
        btnSave = purpleButton("Save Components");
        btnCompute = purpleButton("Compute Finals");
        JButton btnStats = purpleButton("Stats");

        // NEW IMPORT BUTTON (no functionality)
        JButton btnImport = purpleButton("Import CSV");

        JButton btnExport = purpleButton("Export CSV");

        bottom.add(btnLoad);
        bottom.add(btnSave);
        bottom.add(btnCompute);
        bottom.add(btnStats);

        // add import + export together
        bottom.add(btnImport);
        bottom.add(btnExport);

        content.add(bottom, BorderLayout.SOUTH);

        if (maintenanceOn) {
            btnSave.setEnabled(false);
            btnCompute.setEnabled(false);
            spQuiz.setEnabled(false);
            spMid.setEnabled(false);
            spEnd.setEnabled(false);
        }

        /* ACTIONS */
        btnLoad.addActionListener(e -> loadSelectedSectionData());

        btnSave.addActionListener(e -> {
            if (maintenanceOn) {
                JOptionPane.showMessageDialog(this, "Maintenance Mode — Editing is disabled.");
                return;
            }
            saveComponentScores();
        });

        btnCompute.addActionListener(e -> {
            if (maintenanceOn) {
                JOptionPane.showMessageDialog(this, "Maintenance Mode — Editing is disabled.");
                return;
            }
            computeFinals();
        });

        btnStats.addActionListener(e -> new StatsDialog(this, selectedSectionId).setVisible(true));
        btnExport.addActionListener(this::exportCsvFromTable);

        loadSections();
    }

    /* ========================================================== */
    /* VALIDATION HELPER */
    /* ========================================================== */
    private void validateScores() {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        for (int i = 0; i < m.getRowCount(); i++) {
            Double q = parse(m.getValueAt(i, 2));
            Double md = parse(m.getValueAt(i, 3));
            Double e = parse(m.getValueAt(i, 4));

            if ((q != null && q > 100) || (md != null && md > 100) || (e != null && e > 100))
                throw new IllegalArgumentException("Marks cannot exceed 100 (Row " + (i+1) + ")");

            if ((q != null && q < 0) || (md != null && md < 0) || (e != null && e < 0))
                throw new IllegalArgumentException("Marks cannot be negative (Row " + (i+1) + ")");
        }
    }

    /* ========================================================== */
    /* SAVE COMPONENT SCORES */
    /* ========================================================== */
    private void saveComponentScores() {
        try {
            validateScores();

            DefaultTableModel m = (DefaultTableModel) table.getModel();

            for (int i = 0; i < m.getRowCount(); i++) {
                int id = Integer.parseInt(m.getValueAt(i, 0).toString());
                Double q = parse(m.getValueAt(i, 2));
                Double md = parse(m.getValueAt(i, 3));
                Double e = parse(m.getValueAt(i, 4));

                service.saveComponentScores(id, q, md, e);
            }

            JOptionPane.showMessageDialog(this, "Saved!");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    /* ========================================================== */
    /* COMPUTE FINALS */
    /* ========================================================== */
    private void computeFinals() {
        try {
            validateScores();

            int wq = (int) spQuiz.getValue();
            int wm = (int) spMid.getValue();
            int we = (int) spEnd.getValue();

            if (wq + wm + we != 100) {
                JOptionPane.showMessageDialog(this, "Weights must sum to 100.");
                return;
            }

            service.computeAndSaveFinalForSection(selectedSectionId, wq, wm, we);
            JOptionPane.showMessageDialog(this, "Finals computed.");

            loadTable();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Compute failed: " + ex.getMessage());
        }
    }

    /* ========================================================== */
    /* LOAD SECTIONS */
    /* ========================================================== */
    private void loadSections() {
        try {
            sectionDropdown.removeAllItems();
            for (Section s : SectionDAO.getSectionsByInstructor(instructorId))
                sectionDropdown.addItem(s);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load sections: " + ex.getMessage());
        }
    }

    private void loadSelectedSectionData() {
        Section sec = (Section) sectionDropdown.getSelectedItem();
        if (sec == null) return;
        selectedSectionId = sec.getSectionId();
        loadTable();
    }

    private void loadTable() {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);

        try {
            List<SectionGradeRow> rows = service.sectionGrades(selectedSectionId);

            int wq = (int) spQuiz.getValue();
            int wm = (int) spMid.getValue();
            int we = (int) spEnd.getValue();

            for (SectionGradeRow r : rows) {
                Double q = r.quiz != null ? r.quiz.doubleValue() : null;
                Double md = r.midterm != null ? r.midterm.doubleValue() : null;
                Double e = r.endsem != null ? r.endsem.doubleValue() : null;

                Double cgpa = null;
                String letter = "";

                if (q != null && md != null && e != null) {
                    double percent = q * wq / 100.0 + md * wm / 100.0 + e * we / 100.0;
                    double cg = Math.round(percent) / 10.0;
                    cg = Math.round(cg * 100.0) / 100.0;
                    letter = computeLetter(cg);
                    cgpa = cg;
                }

                m.addRow(new Object[]{
                        r.enrollmentId,
                        r.studentId,
                        r.quiz,
                        r.midterm,
                        r.endsem,
                        cgpa == null ? "" : cgpa,
                        cgpa == null ? "" : letter
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load: " + ex.getMessage());
        }
    }

    /* ========================================================== */
    /* UTILITY METHODS */
    /* ========================================================== */
    private JButton purpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setFocusPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(PURPLE_DARK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(PURPLE);
            }
        });

        return btn;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(26);
        t.setSelectionBackground(PURPLE);
        t.setSelectionForeground(Color.WHITE);

        JTableHeader header = t.getTableHeader();
        header.setBackground(PURPLE);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    private Double parse(Object v) {
        if (v == null) return null;
        try {
            return Double.parseDouble(v.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String computeLetter(double cg) {
        if (cg == 10) return "A+";
        if (cg >= 9) return "A";
        if (cg >= 8) return "B";
        if (cg >= 7) return "B-";
        if (cg >= 6) return "C";
        if (cg >= 5) return "C-";
        if (cg >= 4) return "D+";
        if (cg >= 3) return "D";
        return "F";
    }

    private void exportCsvFromTable(ActionEvent e) {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("section_" + selectedSectionId + "_grades.csv"));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File f = fc.getSelectedFile();
            TableModel m = table.getModel();

            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {

                for (int c = 0; c < m.getColumnCount(); c++)
                    pw.print((c > 0 ? "," : "") + m.getColumnName(c));
                pw.println();

                for (int r = 0; r < m.getRowCount(); r++) {
                    for (int c = 0; c < m.getColumnCount(); c++)
                        pw.print((c > 0 ? "," : "") + m.getValueAt(r, c));
                    pw.println();
                }
            }

            JOptionPane.showMessageDialog(this, "Exported!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }

    /* ========================================================== */
    /* IMAGE PANEL */
    /* ========================================================== */
    class ImagePanel extends JPanel {
        private final Image img;
        public ImagePanel(String path) {
            ImageIcon icon = null;
            try { icon = new ImageIcon(getClass().getResource(path)); }
            catch (Exception ignored) {}
            img = (icon != null ? icon.getImage() : null);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null)
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
