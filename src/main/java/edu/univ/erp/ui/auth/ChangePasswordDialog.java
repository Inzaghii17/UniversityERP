package edu.univ.erp.ui.auth;

import edu.univ.erp.service.AuthService;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final User user;
    private final JPasswordField oldPass = new JPasswordField();
    private final JPasswordField newPass = new JPasswordField();
    private final JPasswordField confirmPass = new JPasswordField();

    public ChangePasswordDialog(Frame owner, User user) {
        super(owner, "Change Password", true);
        this.user = user;
        initUI();
    }

    private void initUI() {
        setSize(420, 300);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel main = new JPanel();
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        main.add(new JLabel("Current Password:"));
        oldPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        main.add(oldPass);
        main.add(Box.createVerticalStrut(10));

        main.add(new JLabel("New Password:"));
        newPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        main.add(newPass);
        main.add(Box.createVerticalStrut(10));

        main.add(new JLabel("Confirm New Password:"));
        confirmPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        main.add(confirmPass);
        main.add(Box.createVerticalStrut(15));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton changeBtn = new JButton("Change");
        JButton cancelBtn = new JButton("Cancel");

        changeBtn.addActionListener(e -> handleChange());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(cancelBtn);
        buttons.add(changeBtn);

        add(main, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void handleChange() {
        String oldP = new String(oldPass.getPassword());
        String newP = new String(newPass.getPassword());
        String conf = new String(confirmPass.getPassword());

        if (oldP.isEmpty() || newP.isEmpty() || conf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields required.");
            return;
        }

        if (!newP.equals(conf)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.");
            return;
        }

        if (newP.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.");
            return;
        }

        try {
            boolean ok = AuthService.changePassword(user.getUserId(), oldP, newP);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Old password incorrect.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Password changed successfully.");
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
