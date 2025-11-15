package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

/**
 * AdminDashboard.java
 *
 * This is the main Admin control panel UI.
 * It provides buttons to:
 *  - Add new courses
 *  - Manage users (students, instructors, admins)
 *  - Toggle maintenance mode
 *  - Exit the system
 */
public class AdminDashboard extends JFrame {

    private final User adminUser;

    public AdminDashboard(User user) {
        this.adminUser = user;
        setTitle("Admin Dashboard - " + user.getUsername());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // === Header ===
        JLabel title = new JLabel("Welcome, " + user.getUsername() + " (Admin)", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // === Buttons Panel ===
        JPanel buttons = new JPanel(new GridLayout(4, 1, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(30, 200, 30, 200));

        // Button 1: Add Course
        JButton manageCoursesBtn = new JButton("Manage Courses");
        manageCoursesBtn.addActionListener(e -> new ManageCoursesFrame().setVisible(true));
        buttons.add(manageCoursesBtn);


        // Button 2: Manage Users
        JButton manageUsersBtn = new JButton("Manage Users");
        manageUsersBtn.addActionListener(e -> new ManageUsersFrame().setVisible(true));
        buttons.add(manageUsersBtn);

        // Button 3: Toggle Maintenance Mode
        JButton toggleMaintenanceBtn = new JButton("Toggle Maintenance Mode");
        toggleMaintenanceBtn.addActionListener(e -> toggleMaintenance());
        buttons.add(toggleMaintenanceBtn);

        // Button 4: Exit
        JButton exitBtn = new JButton("Logout / Exit");
        exitBtn.addActionListener(e -> System.exit(0));
        buttons.add(exitBtn);

        add(buttons, BorderLayout.CENTER);
    }

    // ========== Add Course Feature ==========
    private void openAddCourseDialog() {
        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField creditsField = new JTextField();

        Object[] msg = {
                "Course Code:", codeField,
                "Course Title:", titleField,
                "Credits:", creditsField
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "Add New Course", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                if (codeField.getText().isBlank() || titleField.getText().isBlank() || creditsField.getText().isBlank()) {
                    JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int credits = Integer.parseInt(creditsField.getText());

                Course c = new Course();
                c.setCode(codeField.getText());
                c.setTitle(titleField.getText());
                c.setCredits(credits);

                CourseDAO.addCourse(c);
                JOptionPane.showMessageDialog(this, "✅ Course added successfully!");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Credits must be a number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ========== Toggle Maintenance Mode ==========
    private void toggleMaintenance() {
        try {
            SettingsDAO.toggleMaintenance();
            boolean now = SettingsDAO.isMaintenance();
            JOptionPane.showMessageDialog(this,
                    now ? "⚠️ Maintenance mode ENABLED.\nStudents and Instructors cannot log in."
                            : "✅ Maintenance mode DISABLED.\nSystem is open to users.",
                    "Maintenance Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Maintenance Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========== Main Launcher ==========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User dummy = new User();
            dummy.setUsername("admin1");
            dummy.setRole("ADMIN");
            new AdminDashboard(dummy).setVisible(true);
        });
    }
}
