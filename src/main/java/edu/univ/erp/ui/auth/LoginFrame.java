package edu.univ.erp.ui.auth;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private static final java.util.concurrent.ConcurrentHashMap<String, Attempt> attempts =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static class Attempt {
        int count = 0;
        long lockedUntilMs = 0L;
    }

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

        leftPanel.setPreferredSize(new Dimension(450, 0));
        leftPanel.setMinimumSize(new Dimension(450, 0));
        leftPanel.setMaximumSize(new Dimension(450, Integer.MAX_VALUE));
        leftPanel.setLayout(null);

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

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setOpaque(false);

        JLabel signInLabel = new JLabel("Login to your Account");
        signInLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        signInLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField();
        styleTextField(usernameField, "Username");

        passwordField = new JPasswordField();
        stylePasswordField(passwordField, "Password");

        JCheckBox remember = new JCheckBox("Remember me");
        remember.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remember.setBackground(Color.WHITE);
        remember.setAlignmentX(Component.RIGHT_ALIGNMENT);
        remember.setBorder(new EmptyBorder(0, 0, 0, 10));

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

        loginPanel.add(signInLabel);
        loginPanel.add(Box.createVerticalStrut(25));
        loginPanel.add(usernameField);
        loginPanel.add(Box.createVerticalStrut(15));
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(15));
        loginPanel.add(remember);
        loginPanel.add(Box.createVerticalStrut(25));
        loginPanel.add(loginBtn);

        loginCard.add(loginPanel);

        rightContainer.add(loginCard, new GridBagConstraints());

        add(leftPanel, BorderLayout.WEST);
        add(rightContainer, BorderLayout.CENTER);

        setVisible(true);
    }

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
                    pf.setEchoChar('•');
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

    // ============================================================
    // FIXED LOGIN LOGIC (Maintenance DOES NOT block login)
    // ============================================================
    private void doLogin() {
        try {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password.");
                return;
            }

            Attempt att = attempts.computeIfAbsent(user, k -> new Attempt());
            long now = System.currentTimeMillis();

            // Check lockout
            if (att.lockedUntilMs > now) {
                long remain = att.lockedUntilMs - now;
                long mins = (remain / 1000) / 60;
                long secs = (remain / 1000) % 60;
                JOptionPane.showMessageDialog(this,
                        "Account locked. Try again in " + mins + "m " + secs + "s.");
                return;
            }

            User loggedInUser = AuthService.login(user, pass);

            if (loggedInUser == null) {
                att.count++;

                if (att.count >= 5) {
                    att.lockedUntilMs = now + (5 * 60 * 1000);
                    JOptionPane.showMessageDialog(this,
                            "Too many failed attempts. Account locked for 5 minutes.");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid. Attempts left: " + (5 - att.count));
                }

                return;
            }

            // SUCCESS → reset
            attempts.remove(user);

            switch (loggedInUser.getRole().toUpperCase()) {
                case "ADMIN" -> new edu.univ.erp.ui.admin.AdminDashboard(loggedInUser).setVisible(true);
                case "INSTRUCTOR" -> new edu.univ.erp.ui.instructor.InstructorDashboard(loggedInUser).setVisible(true);
                case "STUDENT" -> new edu.univ.erp.ui.student.StudentDashboard(loggedInUser).setVisible(true);
            }

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
