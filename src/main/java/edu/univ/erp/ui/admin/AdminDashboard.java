package edu.univ.erp.ui.admin;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.util.BackupRestoreUtil;   // <-- NEW IMPORT

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class AdminDashboard extends JFrame {

    private final User adminUser;

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);

    private JPanel maintenanceBanner;
    private JLabel maintenanceLabel;

    public AdminDashboard(User user) {
        this.adminUser = user;

        setTitle("Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getRootPane().setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // === LEFT SIDEBAR ===
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setLayout(null);
        add(sidebar, BorderLayout.WEST);

        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PURPLE);
        header.setPreferredSize(new Dimension(700, 70));

        JLabel title = new JLabel("Admin Dashboard — " + user.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        header.add(title, BorderLayout.WEST);

        JButton exitBtn = styledButtonSmall("Logout");
        exitBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JButton changePassBtn = styledButtonSmall("Change Password");
        changePassBtn.addActionListener(e ->
                new edu.univ.erp.ui.auth.ChangePasswordDialog(this, adminUser).setVisible(true)
        );


        JButton btnBackup = styledButtonSmall("Backup DB");
        btnBackup.addActionListener(e -> doBackup());

        JButton btnRestore = styledButtonSmall("Restore DB");
        btnRestore.addActionListener(e -> doRestore());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightButtons.setOpaque(false);

        rightButtons.add(btnBackup);
        rightButtons.add(btnRestore);
        rightButtons.add(changePassBtn);
        rightButtons.add(exitBtn);

        header.add(rightButtons, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // === MAINTENANCE BANNER ===
        maintenanceBanner = new JPanel();
        maintenanceBanner.setBackground(new Color(255, 200, 50));
        maintenanceBanner.setPreferredSize(new Dimension(700, 35));

        maintenanceLabel = new JLabel("⚠ Maintenance Mode is ON — Students & Instructors are READ-ONLY");
        maintenanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        maintenanceLabel.setForeground(Color.DARK_GRAY);
        maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));
        maintenanceBanner.add(maintenanceLabel);

        maintenanceBanner.setVisible(AccessControl.isMaintenance());
        add(maintenanceBanner, BorderLayout.SOUTH);

        // === CONTENT GRID ===
        JPanel content = new JPanel(new GridLayout(2, 2, 20, 20));
        content.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        content.setBackground(new Color(245, 245, 245));

        JButton manageCoursesBtn = styledButton("Manage Courses");
        manageCoursesBtn.addActionListener(e -> new ManageCoursesFrame().setVisible(true));

        JButton manageSectionsBtn = styledButton("Manage Sections");
        manageSectionsBtn.addActionListener(e -> new ManageSectionsFrame().setVisible(true));

        JButton manageUsersBtn = styledButton("Manage Users");
        manageUsersBtn.addActionListener(e -> new ManageUsersFrame().setVisible(true));

        JButton toggleMaintenanceBtn = styledButton("Toggle Maintenance Mode");
        toggleMaintenanceBtn.addActionListener(e -> {
            try {
                SettingsDAO.toggleMaintenance();
                boolean now = SettingsDAO.isMaintenance();
                maintenanceBanner.setVisible(now);

                JOptionPane.showMessageDialog(this,
                        now ?
                                "⚠ Maintenance mode ENABLED.\nStudents & Instructors CAN log in but CANNOT make changes." :
                                "✅ Maintenance mode DISABLED.\nFull access restored.",
                        "Maintenance Updated",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error toggling maintenance: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        content.add(manageCoursesBtn);
        content.add(manageSectionsBtn);
        content.add(manageUsersBtn);
        content.add(toggleMaintenanceBtn);

        add(content, BorderLayout.CENTER);
    }

    // =============== BACKUP METHOD ===============
    private void doBackup() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save ERP Backup");
        fc.setSelectedFile(new File("erp_backup.sql"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File out = fc.getSelectedFile();

        JDialog dlg = new JDialog(this, "Backing Up", true);
        dlg.setSize(300, 100);
        dlg.setLocationRelativeTo(this);
        dlg.add(new JLabel("Running mysqldump…", SwingConstants.CENTER));
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        new Thread(() -> {
            try {
                BackupRestoreUtil.backupDatabase(out);
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Backup saved:\n" + out.getAbsolutePath());
            } catch (Exception ex) {
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Backup failed:\n" + ex.getMessage());
            }
        }).start();

        dlg.setVisible(true);
    }


    private void doRestore() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select SQL File");

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File in = fc.getSelectedFile();

        if (JOptionPane.showConfirmDialog(this,
                "Restoring will overwrite ERP DB.\nContinue?",
                "Confirm Restore",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        JDialog dlg = new JDialog(this, "Restoring DB", true);
        dlg.setSize(300, 100);
        dlg.setLocationRelativeTo(this);
        dlg.add(new JLabel("Importing SQL…", SwingConstants.CENTER));
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        new Thread(() -> {
            try {
                BackupRestoreUtil.restoreDatabase(in);
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Restore complete.");
            } catch (Exception ex) {
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Restore failed:\n" + ex.getMessage());
            }
        }).start();

        dlg.setVisible(true);
    }

    // ==== Styled Buttons ====
    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(PURPLE); }
        });

        return btn;
    }

    private JButton styledButtonSmall(String text) {
        JButton btn = styledButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return btn;
    }

    // === SIDEBAR IMAGE PANEL ===
    class ImagePanel extends JPanel {
        private final Image img;
        public ImagePanel(String resourcePath) {
            img = new ImageIcon(getClass().getResource(resourcePath)).getImage();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
