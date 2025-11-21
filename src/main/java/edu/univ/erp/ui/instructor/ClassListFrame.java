package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.SectionGradeRow;
import edu.univ.erp.data.SettingsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Frame that shows the roster for a specific section using InstructorService.sectionGrades.
 * Read-only view; shows banner when maintenance ON.
 */
public class ClassListFrame extends JFrame {

    private final int sectionId;
    private final DefaultTableModel model;
    private final JTable table;

    public ClassListFrame(int sectionId) {
        super("Class List - Section " + sectionId);
        this.sectionId = sectionId;
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        model = new DefaultTableModel(new Object[]{"Reg ID", "Student ID", "Roll No", "Quiz", "Midsem", "Endsem", "Final Grade"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        boolean maint = false;
        try { maint = SettingsDAO.isMaintenance(); } catch (Exception ignored) {}
        if (maint) {
            JLabel banner = new JLabel("MAINTENANCE MODE ACTIVE - READ ONLY", SwingConstants.CENTER);
            banner.setOpaque(true);
            banner.setBackground(Color.ORANGE);
            banner.setForeground(Color.BLACK);
            banner.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            top.add(banner, BorderLayout.NORTH);
        }
        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadRoster());

        loadRoster();
    }

    private void loadRoster() {
        model.setRowCount(0);
        InstructorService svc = new InstructorService();
        try {
            List<SectionGradeRow> rows = svc.sectionGrades(sectionId);
            for (SectionGradeRow r : rows) {
                model.addRow(new Object[]{
                        r.enrollmentId,
                        r.studentId,
                        r.rollNo != null ? r.rollNo : "",
                        r.quiz != null ? r.quiz : "",
                        r.midterm != null ? r.midterm : "",
                        r.endsem != null ? r.endsem : "",
                        r.finalGrade != null ? r.finalGrade : ""
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load roster: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
