package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorService.SectionGradeRow;
import edu.univ.erp.data.SettingsDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ClassListFrame extends JFrame {

    private final int sectionId;
    private final DefaultTableModel model;
    private final JTable table;

    private final Color PURPLE = new Color(100, 52, 136);      // #643488
    private final Color PURPLE_DARK = new Color(85, 44, 115);  // hover
    private final Color CONTENT_BG = new Color(245, 245, 245); // #F5F5F5

    public ClassListFrame(int sectionId) {
        super("Class List – Section " + sectionId);
        this.sectionId = sectionId;

        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        /* ----------------------------------------------------------------
         * LEFT SIDEBAR (MATCH STUDENT DASHBOARD)
         * ---------------------------------------------------------------- */
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 18, 20, 18));

        JLabel title = new JLabel("Class List");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(title);
        sidebar.add(Box.createVerticalGlue());

        add(sidebar, BorderLayout.WEST);

        /* ----------------------------------------------------------------
         * MAIN CONTENT PANEL — SAME STRUCTURE AS STUDENT DASHBOARD
         * ---------------------------------------------------------------- */
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(CONTENT_BG);
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(content, BorderLayout.CENTER);

        /* ----------------------------------------------------------------
         * TABLE + MODEL
         * ---------------------------------------------------------------- */
        model = new DefaultTableModel(
                new Object[]{"Reg ID", "Student ID", "Roll No",
                        "Quiz", "Midsem", "Endsem", "Final"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleDataTable(table);

        JScrollPane sp = new JScrollPane(table);
        content.add(sp, BorderLayout.CENTER);

        /* ---------------------------------------------------
         * MAINTENANCE BANNER
         * --------------------------------------------------- */
        try {
            if (SettingsDAO.isMaintenance()) {
                JLabel banner = new JLabel("⚠ Maintenance Mode Active — Read Only", SwingConstants.CENTER);
                banner.setOpaque(true);
                banner.setBackground(Color.ORANGE);
                banner.setForeground(Color.BLACK);
                banner.setFont(new Font("Segoe UI", Font.BOLD, 14));
                banner.setBorder(new EmptyBorder(10, 10, 10, 10));
                content.add(banner, BorderLayout.NORTH);
            }
        } catch (Exception ignored) {}

        /* ---------------------------------------------------
         * BOTTOM BUTTON BAR (MATCH STUDENT DASHBOARD)
         * --------------------------------------------------- */
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setBackground(CONTENT_BG);

        JButton btnRefresh = purpleButton("Refresh");
        bottomBar.add(btnRefresh);

        content.add(bottomBar, BorderLayout.SOUTH);

        /* ACTION */
        btnRefresh.addActionListener(e -> loadRoster());

        loadRoster();
    }

    /* ---------------------------------------------------
     * PURPLE BUTTON STYLE (SMALLER)
     * --------------------------------------------------- */
    private JButton purpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(PURPLE); }
        });

        return btn;
    }

    /* ---------------------------------------------------
     * TABLE STYLE (MATCH STUDENT)
     * --------------------------------------------------- */
    private void styleDataTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setSelectionBackground(PURPLE);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PURPLE);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    /* ---------------------------------------------------
     * LOAD DATA
     * --------------------------------------------------- */
    private void loadRoster() {
        model.setRowCount(0);

        InstructorService svc = new InstructorService();
        try {
            List<SectionGradeRow> list = svc.sectionGrades(sectionId);

            for (SectionGradeRow r : list) {
                model.addRow(new Object[]{
                        r.enrollmentId,
                        r.studentId,
                        r.rollNo == null ? "" : r.rollNo,
                        r.quiz == null ? "" : r.quiz,
                        r.midterm == null ? "" : r.midterm,
                        r.endsem == null ? "" : r.endsem,
                        r.finalGrade == null ? "" : r.finalGrade
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load class list: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ---------------------------------------------------
     * IMAGE PANEL SAME AS STUDENT
     * --------------------------------------------------- */
    class ImagePanel extends JPanel {
        private final Image img;

        public ImagePanel(String path) {
            ImageIcon icon = null;
            try { icon = new ImageIcon(getClass().getResource(path)); }
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
