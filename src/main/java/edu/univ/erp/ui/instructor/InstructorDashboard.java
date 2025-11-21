package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.User;
import edu.univ.erp.data.SettingsDAO;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Instructor Dashboard with two buttons:
 * - My Sections
 * - Grades
 *
 * Opens new frames (Option A).
 */
public class InstructorDashboard extends JFrame {

    private final User currentUser;

    public InstructorDashboard(User currentUser) {
        super("Instructor Dashboard - " + (currentUser != null ? currentUser.getUsername() : ""));
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 420);
        setLocationRelativeTo(null);
        setLayout(null);

        // Banner if maintenance
        boolean maint = false;
        try { maint = SettingsDAO.isMaintenance(); } catch (Exception ignored) {}
        if (maint) {
            JLabel b = new JLabel("MAINTENANCE MODE ACTIVE - READ ONLY", SwingConstants.CENTER);
            b.setOpaque(true);
            b.setBackground(java.awt.Color.ORANGE);
            b.setForeground(java.awt.Color.BLACK);
            b.setBounds(0, 0, 700, 40);
            add(b);
        }

        JLabel welcome = new JLabel("Welcome, " + (currentUser != null ? currentUser.getUsername() : "Instructor"), SwingConstants.CENTER);
        welcome.setFont(welcome.getFont().deriveFont(20f));
        welcome.setBounds(0, 50, 700, 40);
        add(welcome);

        JButton btnSections = new JButton("My Sections");
        btnSections.setBounds(200, 140, 280, 60);
        add(btnSections);

        JButton btnGrades = new JButton("Grades");
        btnGrades.setBounds(200, 220, 280, 60);
        add(btnGrades);

        btnSections.addActionListener(e -> {
            try {
                SectionsFrame sf = new SectionsFrame(currentUser.getUserId());
                sf.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to open Sections: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnGrades.addActionListener(e -> {
            try {
                GradesFrame gf = new GradesFrame(currentUser.getUserId());
                gf.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to open Grades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
