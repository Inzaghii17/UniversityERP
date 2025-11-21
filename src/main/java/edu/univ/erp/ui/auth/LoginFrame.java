package edu.univ.erp.ui.auth;

import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ============================================================
        // LEFT PANEL (uses PNG design)
        // ============================================================
        ImageIcon leftImageIcon = new ImageIcon(getClass().getResource("/images/Loginframe_1.png"));
        Image leftImg = leftImageIcon.getImage();

        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(leftImg, 0, 0, getWidth(), getHeight(), null);
            }
        };

        // FIXED WIDTH PREVENTS STRETCHING IN FULLSCREEN
        leftPanel.setPreferredSize(new Dimension(450, 0));
        leftPanel.setMinimumSize(new Dimension(450, 0));
        leftPanel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        leftPanel.setLayout(null); // Your PNG contains all design elements

        // ============================================================
        // RIGHT SIDE CONTAINER
        // ============================================================
        JPanel rightContainer = new JPanel(new GridBagLayout());
        rightContainer.setBackground(Color.WHITE);

        // LOGIN CARD
        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);

        loginCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        // LOGIN PANEL CONTENT
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));

        loginPanel.setOpaque(false);


        JLabel signInLabel = new JLabel("Login to your Account");
        signInLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        signInLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // USERNAME FIELD
        usernameField = new JTextField();
        styleTextField(usernameField, "Username");

        // PASSWORD FIELD (WITH CORRECT HIDING LOGIC)
        passwordField = new JPasswordField();
        stylePasswordField(passwordField, "Password");

        JCheckBox remember = new JCheckBox("Remember me");
        remember.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remember.setBackground(Color.WHITE);
        remember.setAlignmentX(Component.RIGHT_ALIGNMENT);
        remember.setBorder(new EmptyBorder(0, 0, 0, 10));

        // LOGIN BUTTON
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBackground(new Color(108, 75, 255));
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(240, 48));
        loginBtn.setMaximumSize(new Dimension(240, 48));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setBorder(BorderFactory.createLineBorder(new Color(108, 75, 255), 1, true));

        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(90, 60, 230));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginBtn.setBackground(new Color(108, 75, 255));
            }
        });

        loginBtn.addActionListener(e -> doLogin());

        // ADD ITEMS TO LOGIN PANEL
        loginPanel.add(signInLabel);
        loginPanel.add(Box.createVerticalStrut(25));
        loginPanel.add(usernameField);
        loginPanel.add(Box.createVerticalStrut(15));
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(15));
        loginPanel.add(remember);
        loginPanel.add(Box.createVerticalStrut(25));
        loginPanel.add(loginBtn);

        // ADD TO CARD
        loginCard.add(loginPanel);

        // CENTER CARD
        rightContainer.add(loginCard, new GridBagConstraints());

        add(leftPanel, BorderLayout.WEST);
        add(rightContainer, BorderLayout.CENTER);

        setVisible(true);
    }

    // ============================================================
    // STYLE NORMAL TEXT FIELD
    // ============================================================
    private void styleTextField(JComponent field, String placeholder) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(300, 40));
        field.setMaximumSize(new Dimension(300, 40));

        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        if (field instanceof JTextField tf) {

            tf.setForeground(new Color(150, 150, 150));
            tf.setText(placeholder);

            tf.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (tf.getText().equals(placeholder)) {
                        tf.setText("");
                        tf.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (tf.getText().isEmpty()) {
                        tf.setForeground(new Color(150, 150, 150));
                        tf.setText(placeholder);
                    }
                }
            });
        }
    }

    // ============================================================
    // STYLE PASSWORD FIELD (FIXED)
    // ============================================================
    private void stylePasswordField(JPasswordField pf, String placeholder) {

        pf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pf.setPreferredSize(new Dimension(300, 40));
        pf.setMaximumSize(new Dimension(300, 40));

        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        pf.setForeground(new Color(150, 150, 150));
        pf.setEchoChar((char) 0);
        pf.setText(placeholder);

        pf.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                String text = new String(pf.getPassword());

                if (text.equals(placeholder)) {
                    pf.setText("");
                    pf.setForeground(Color.BLACK);
                    pf.setEchoChar('•'); // enable masking
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String text = new String(pf.getPassword());

                if (text.isEmpty()) {
                    pf.setForeground(new Color(150, 150, 150));
                    pf.setEchoChar((char) 0);
                    pf.setText(placeholder);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // ============================================================
    // LOGIN LOGIC
    // ============================================================
    private void doLogin() {
        try {
            if (SettingsDAO.isMaintenance()) {
                JOptionPane.showMessageDialog(this, "System is under maintenance. Try later.");
                return;
            }

            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());

            User loggedInUser = AuthService.login(user, pass);

            if (loggedInUser == null) {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            switch (loggedInUser.getRole()) {
                case "ADMIN" -> new AdminDashboard(loggedInUser).setVisible(true);


                case "INSTRUCTOR" ->   new InstructorDashboard(loggedInUser).setVisible(true);
                case "STUDENT" -> new StudentDashboard(loggedInUser).setVisible(true);
            }

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage());
        }
    }
}