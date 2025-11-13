package edu.univ.erp.ui.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        setTitle("University ERP - Login");
        setSize(420, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(3,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.add(new JLabel("Username:"));
        usernameField = new JTextField();
        p.add(usernameField);
        p.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        p.add(passwordField);
        JButton login = new JButton("Login");
        p.add(new JLabel());
        p.add(login);
        add(p);

        login.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Provide credentials"); return; }
        try {
            User user = AuthService.login(u, p);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
                return;
            }
            JOptionPane.showMessageDialog(this, "Welcome " + user.getUsername() + " (" + user.getRole() + ")");
            SwingUtilities.invokeLater(() -> {
                dispose();
                switch (user.getRole().toUpperCase()) {
                    case "ADMIN": new AdminDashboard(user).setVisible(true); break;
                    case "INSTRUCTOR": new InstructorDashboard(user).setVisible(true); break;
                    case "STUDENT": new StudentDashboard(user).setVisible(true); break;
                    default: JOptionPane.showMessageDialog(null,"Unknown role");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
