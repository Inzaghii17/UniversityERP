package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class SectionsFrame extends JFrame {

    private final int instructorId;
    private JTable table;
    private DefaultTableModel model;

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);
    private final Color CONTENT_BG = new Color(245, 245, 245);

    // === Maintenance Banner ===
    private JPanel maintenanceBanner;
    private JLabel maintenanceLabel;

    public SectionsFrame(int instructorId) {
        this.instructorId = instructorId;

        setTitle("My Sections");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        /* =============================================================
         * TOP PURPLE HEADER
         * ============================================================= */
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setPreferredSize(new Dimension(1000, 60));
        topHeader.setBackground(PURPLE);

        JLabel headerTitle = new JLabel("My Sections", SwingConstants.LEFT);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setBorder(new EmptyBorder(0, 18, 0, 0));

        JButton exitBtn = new JButton("Close");
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setBackground(PURPLE_DARK);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> dispose());

        topHeader.add(headerTitle, BorderLayout.WEST);
        topHeader.add(exitBtn, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        /* =============================================================
         * MAINTENANCE BANNER (READ-ONLY INFO)
         * ============================================================= */
        maintenanceBanner = new JPanel();
        maintenanceBanner.setBackground(new Color(255, 200, 50));
        maintenanceBanner.setPreferredSize(new Dimension(1000, 32));

        maintenanceLabel = new JLabel("⚠ Maintenance Mode is ON — READ-ONLY MODE");
        maintenanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceLabel.setForeground(Color.DARK_GRAY);
        maintenanceLabel.setBorder(new EmptyBorder(5, 15, 5, 0));
        maintenanceBanner.add(maintenanceLabel);

        maintenanceBanner.setVisible(AccessControl.isMaintenance());
        add(maintenanceBanner, BorderLayout.SOUTH);

        /* =============================================================
         * LEFT SIDEBAR IMAGE
         * ============================================================= */
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);

        /* =============================================================
         * MAIN CONTENT
         * ============================================================= */
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(CONTENT_BG);
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(content, BorderLayout.CENTER);

        /* =============================================================
         * TABLE
         * ============================================================= */
        model = new DefaultTableModel(
                new Object[]{"Section ID", "Course Code", "Title", "Semester", "Year", "Capacity", "Day/Time", "Room"},
                0
        ) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader head = table.getTableHeader();
        head.setBackground(PURPLE);
        head.setForeground(Color.WHITE);
        head.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        content.add(sp, BorderLayout.CENTER);

        /* =============================================================
         * BOTTOM BUTTON BAR
         * ============================================================= */
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setOpaque(false);

        JButton btnView = purpleButton("View Class List");
        JButton btnRefresh = purpleButton("Refresh");

        bottom.add(btnView);
        bottom.add(btnRefresh);

        content.add(bottom, BorderLayout.SOUTH);

        /* =============================================================
         * ACTIONS (Viewing only — allowed even in maintenance)
         * ============================================================= */
        btnRefresh.addActionListener(e -> loadSections());

        btnView.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a section first.");
                return;
            }

            int secId = Integer.parseInt(table.getValueAt(row, 0).toString());
            new ClassListFrame(secId).setVisible(true);
        });

        loadSections();
    }

    /* =============================================================
     * PURPLE BUTTON STYLE
     * ============================================================= */
    private JButton purpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(PURPLE); }
        });

        return btn;
    }

    /* =============================================================
     * LOAD SECTIONS
     * ============================================================= */
    private void loadSections() {
        model.setRowCount(0);

        try {
            List<Section> list = SectionDAO.getSectionsByInstructor(instructorId);
            for (Section s : list) {
                model.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseCode(),
                        s.getCourseTitle(),
                        s.getSemester(),
                        s.getYear(),
                        s.getCapacity(),
                        s.getDayTime(),
                        s.getRoom()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load sections: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =============================================================
     * SIDEBAR IMAGE PANEL
     * ============================================================= */
    class ImagePanel extends JPanel {
        private final Image img;
        ImagePanel(String path) {
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
