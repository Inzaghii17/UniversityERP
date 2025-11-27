package edu.univ.erp.ui.student;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.data.RegistrationDAO;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.FileWriter;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final User student;
    private final JPanel contentPanel = new JPanel(new BorderLayout());

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);
    private final Color CONTENT_BG = new Color(245, 245, 245);

    private final DefaultTableModel catalogModel = new DefaultTableModel(
            new String[]{"SectionID", "Course", "Title", "Semester", "Year", "DayTime", "Room", "Cap", "Instructor"}, 0);

    private final DefaultTableModel registeredModel = new DefaultTableModel(
            new String[]{"SectionID", "Course", "Title", "Semester", "Year", "DayTime", "Room"}, 0);

    private final DefaultTableModel timetableModel = new DefaultTableModel(
            new String[]{"Day/Time", "Course", "Room", "Instructor"}, 0);

    private final DefaultTableModel gradesModel = new DefaultTableModel(
            new String[]{"SectionID", "Quiz", "Midsem", "Endsem", "Final"}, 0);

    // === Maintenance Mode Flag ===
    private final boolean maintenanceOn = AccessControl.isMaintenance();

    // === Maintenance Banner ===
    private JPanel maintenanceBanner;
    private JLabel maintenanceLabel;

    public StudentDashboard(User student) {
        this.student = student;

        setTitle("Student Dashboard - " + student.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        /* ----------------------------------------------------------------
         * SIDEBAR
         * ---------------------------------------------------------------- */
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 16, 20, 16));

        JButton btnCatalog = createSidebarButton("Browse Courses");
        JButton btnRegistered = createSidebarButton("My Registrations");
        JButton btnTimetable = createSidebarButton("Timetable");
        JButton btnGrades = createSidebarButton("Grades");
        JButton btnTranscript = createSidebarButton("Transcript (CSV)");

        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnCatalog);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(btnRegistered);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(btnTimetable);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(btnGrades);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(btnTranscript);
        sidebar.add(Box.createVerticalStrut(14));

// NEW: Change Password
        // --- Change Password ---
        JButton btnChangePass = createSidebarButton("Change Password");
        btnChangePass.addActionListener(e ->
                new edu.univ.erp.ui.auth.ChangePasswordDialog(this, student).setVisible(true)
        );
        sidebar.add(btnChangePass);
        sidebar.add(Box.createVerticalStrut(14));

// --- Logout (NEW) ---
        JButton btnLogout = createSidebarButton("Logout");
        btnLogout.addActionListener(e -> {
            dispose();
            new edu.univ.erp.ui.auth.LoginFrame().setVisible(true);
        });
        sidebar.add(btnLogout);

        sidebar.add(Box.createVerticalGlue());


        add(sidebar, BorderLayout.WEST);

        /* ----------------------------------------------------------------
         * MAINTENANCE BANNER
         * ---------------------------------------------------------------- */
        maintenanceBanner = new JPanel();
        maintenanceBanner.setBackground(new Color(255, 200, 50));
        maintenanceBanner.setPreferredSize(new Dimension(1000, 32));

        maintenanceLabel = new JLabel("⚠ Maintenance Mode is ON — You cannot Register/Drop Courses.");
        maintenanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceLabel.setForeground(Color.DARK_GRAY);
        maintenanceLabel.setBorder(new EmptyBorder(5, 15, 5, 0));
        maintenanceBanner.add(maintenanceLabel);

        maintenanceBanner.setVisible(maintenanceOn);
        add(maintenanceBanner, BorderLayout.SOUTH);

        /* ----------------------------------------------------------------
         * MAIN CONTENT PANEL
         * ---------------------------------------------------------------- */
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPanel.setBackground(CONTENT_BG);
        showCatalog();

        btnCatalog.addActionListener(e -> showCatalog());
        btnRegistered.addActionListener(e -> showRegistrations());
        btnTimetable.addActionListener(e -> showTimetable());
        btnGrades.addActionListener(e -> showGrades());
        btnTranscript.addActionListener(e -> exportTranscriptCSV());

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // ------------ Sidebar Button Style ------------
    private JButton createSidebarButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        b.setPreferredSize(new Dimension(180, 56));
        b.setBackground(PURPLE);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(PURPLE); }
        });

        return b;
    }

    // ============================================================
    // CATALOG SCREEN (REGISTER HERE — must be disabled in maintenance)
    // ============================================================
    private void showCatalog() {
        contentPanel.removeAll();

        JTable table = new JTable(catalogModel);
        styleDataTable(table);
        JScrollPane sp = new JScrollPane(table);

        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(CONTENT_BG);
        bottomBar.setBorder(new EmptyBorder(16, 12, 16, 12));

        JButton btnRegister = purpleButton("Register Selected");
        JButton btnRefresh = purpleButton("Refresh");

        // Disable if maintenance mode is ON
        if (maintenanceOn) btnRegister.setEnabled(false);

        bottomBar.add(btnRegister);
        bottomBar.add(Box.createHorizontalStrut(10));
        bottomBar.add(btnRefresh);

        btnRegister.addActionListener(e -> {
            if (maintenanceOn) {
                JOptionPane.showMessageDialog(this, "Maintenance Mode: Registration disabled.");
                return;
            }

            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a section first."); return; }
            int sectionId = (int) catalogModel.getValueAt(r, 0);
            attemptRegister(sectionId);
        });

        btnRefresh.addActionListener(e -> loadCatalog());

        contentPanel.add(new JLabel("Browse Courses"), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(bottomBar, BorderLayout.SOUTH);

        loadCatalog();
        refreshContent();
    }

    private void loadCatalog() {
        catalogModel.setRowCount(0);
        try {
            List<Section> list = CourseDAO.getCatalogSections();
            for (Section s : list) {
                catalogModel.addRow(new Object[]{
                        s.getSectionId(), s.getCourseCode(), s.getCourseTitle(),
                        s.getSemester(), s.getYear(), s.getDayTime(),
                        s.getRoom(), s.getCapacity(), s.getInstructorName()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading catalog: " + e.getMessage());
        }
    }

    // ============================================================
    // REGISTRATION SCREEN
    // ============================================================
    private void showRegistrations() {
        contentPanel.removeAll();

        JTable table = new JTable(registeredModel);
        styleDataTable(table);
        JScrollPane sp = new JScrollPane(table);

        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(CONTENT_BG);
        bottomBar.setBorder(new EmptyBorder(16, 12, 16, 12));

        JButton btnDrop = purpleButton("Drop Selected");
        JButton btnRefresh = purpleButton("Refresh");

        // Maintenance mode → Drop disabled
        if (maintenanceOn) btnDrop.setEnabled(false);

        bottomBar.add(btnDrop);
        bottomBar.add(Box.createHorizontalStrut(10));
        bottomBar.add(btnRefresh);

        btnDrop.addActionListener(e -> {
            if (maintenanceOn) {
                JOptionPane.showMessageDialog(this, "Maintenance Mode: Dropping disabled.");
                return;
            }

            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a registered section."); return; }
            int sectionId = (int) registeredModel.getValueAt(r, 0);
            attemptDrop(sectionId);
        });

        btnRefresh.addActionListener(e -> loadRegistrations());

        contentPanel.add(new JLabel("My Registrations"), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(bottomBar, BorderLayout.SOUTH);

        loadRegistrations();
        refreshContent();
    }

    private void loadRegistrations() {
        registeredModel.setRowCount(0);
        try {
            List<Section> list = RegistrationDAO.getRegisteredSections(student.getUserId());
            for (Section s : list) {
                registeredModel.addRow(new Object[]{
                        s.getSectionId(), s.getCourseCode(), s.getCourseTitle(),
                        s.getSemester(), s.getYear(), s.getDayTime(), s.getRoom()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading registrations: " + e.getMessage());
        }
    }

    // ============================================================
    // TIMETABLE (read-only)
    // ============================================================
    private void showTimetable() {
        contentPanel.removeAll();

        JTable table = new JTable(timetableModel);
        styleDataTable(table);
        JScrollPane sp = new JScrollPane(table);

        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(CONTENT_BG);
        bottomBar.setBorder(new EmptyBorder(16, 12, 16, 12));

        JButton btnRefresh = purpleButton("Refresh");
        bottomBar.add(btnRefresh);
        btnRefresh.addActionListener(e -> loadTimetable());

        contentPanel.add(new JLabel("Timetable"), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(bottomBar, BorderLayout.SOUTH);

        loadTimetable();
        refreshContent();
    }

    private void loadTimetable() {
        timetableModel.setRowCount(0);
        try {
            List<Section> regs = RegistrationDAO.getRegisteredSections(student.getUserId());
            for (Section s : regs) {
                timetableModel.addRow(new Object[]{
                        s.getDayTime(), s.getCourseTitle(),
                        s.getRoom(), s.getInstructorName()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + e.getMessage());
        }
    }

    // ============================================================
    // GRADES (read-only)
    // ============================================================
    private void showGrades() {
        contentPanel.removeAll();

        JTable table = new JTable(gradesModel);
        styleDataTable(table);
        JScrollPane sp = new JScrollPane(table);

        JPanel bottomBar = new JPanel();
        bottomBar.setBackground(CONTENT_BG);
        bottomBar.setBorder(new EmptyBorder(16, 12, 16, 12));

        JButton btnRefresh = purpleButton("Refresh");
        bottomBar.add(btnRefresh);
        btnRefresh.addActionListener(e -> loadGrades());

        contentPanel.add(new JLabel("Grades"), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(bottomBar, BorderLayout.SOUTH);

        loadGrades();
        refreshContent();
    }

    private void loadGrades() {
        gradesModel.setRowCount(0);
        try {
            List<Grade> list = GradesDAO.getGradesForStudent(student.getUserId());
            for (Grade g : list) {
                gradesModel.addRow(new Object[]{
                        g.getSectionId(), g.getQuiz(),
                        g.getMidsem(), g.getEndsem(), g.getFinalGrade()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + e.getMessage());
        }
    }

    // ============================================================
    // BACKEND WRITES → DISABLED IN MAINTENANCE
    // ============================================================
    private void attemptRegister(int sectionId) {
        try {
            if (maintenanceOn) {
                JOptionPane.showMessageDialog(this, "Maintenance Mode: Registration disabled.");
                return;
            }

            if (RegistrationDAO.isRegistered(student.getUserId(), sectionId)) {
                JOptionPane.showMessageDialog(this, "Already registered.");
                return;
            }

            int seats = RegistrationDAO.seatsLeft(sectionId);
            if (seats <= 0) {
                JOptionPane.showMessageDialog(this, "Section full.");
                return;
            }

            if (RegistrationDAO.register(student.getUserId(), sectionId)) {
                JOptionPane.showMessageDialog(this, "Registered.");
                loadCatalog();
                loadRegistrations();
                loadTimetable();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void attemptDrop(int sectionId) {
        try {
            if (maintenanceOn) {
                JOptionPane.showMessageDialog(this, "Maintenance Mode: Dropping disabled.");
                return;
            }

            boolean ok = RegistrationDAO.drop(student.getUserId(), sectionId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Dropped.");
                loadRegistrations();
                loadCatalog();
                loadTimetable();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error dropping: " + e.getMessage());
        }
    }

    // ============================================================
    // EXPORT TRANSCRIPT (allowed)
    // ============================================================
    private void exportTranscriptCSV() {
        try {
            List<Grade> list = GradesDAO.getGradesForStudent(student.getUserId());
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No grades.");
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("transcript_" + student.getUsername() + ".csv"));

            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write("SectionID,Quiz,Midsem,Endsem,Final\n");
                for (Grade g : list) {
                    fw.write(g.getSectionId() + "," +
                            g.getQuiz() + "," +
                            g.getMidsem() + "," +
                            g.getEndsem() + "," +
                            g.getFinalGrade() + "\n");
                }
            }

            JOptionPane.showMessageDialog(this, "Transcript saved.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================
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

    private JButton purpleButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { if (btn.isEnabled()) btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { if (btn.isEnabled()) btn.setBackground(PURPLE); }
        });

        return btn;
    }

    private void refreshContent() {
        contentPanel.revalidate();
        contentPanel.repaint();
    }

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
