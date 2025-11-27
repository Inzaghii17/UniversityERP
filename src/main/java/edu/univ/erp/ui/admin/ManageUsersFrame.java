package edu.univ.erp.ui.admin;

import edu.univ.erp.data.UserDAO;
import edu.univ.erp.domain.User;
import edu.univ.erp.auth.PasswordUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ManageUsersFrame extends JFrame {

    private final Color PURPLE = new Color(100, 52, 136);      // main deep violet
    private final Color PURPLE_DARK = new Color(85, 44, 115);  // hover shade

    private DefaultTableModel model =
            new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0);

    private JTable table = new JTable(model);

    public ManageUsersFrame() {

        setTitle("Manage Users");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        styleTable();

        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(pane, BorderLayout.CENTER);

        JButton add = styledButton("Add User");
        JButton del = styledButton("Delete User");
        JButton refresh = styledButton("Refresh");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttons.setBackground(new Color(245, 245, 245));
        buttons.add(add);
        buttons.add(del);
        buttons.add(refresh);

        add(buttons, BorderLayout.SOUTH);

        add.addActionListener(e -> showAddUserPopup());
        del.addActionListener(e -> deleteUser());
        refresh.addActionListener(e -> loadUsers());

        loadUsers();
    }

    // ========= PURPLE BUTTON =========
    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(PURPLE); }
        });

        return btn;
    }

    // ========= STYLE TABLE =========
    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setRowHeight(30);
        table.setSelectionBackground(PURPLE);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));

        JTableHeader header = table.getTableHeader();
        header.setBackground(PURPLE);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    // ========= LOAD USERS =========
    private void loadUsers() {
        try {
            model.setRowCount(0);
            List<User> list = UserDAO.listAll();
            for (User u : list)
                model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole()});
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ========= ADD-USER POPUP =========
    private void showAddUserPopup() {

        JDialog popup = new JDialog(this, "Add User", true);
        popup.setSize(420, 380);
        popup.setLocationRelativeTo(this);
        popup.setLayout(new BorderLayout());
        popup.getRootPane().setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel form = new JPanel();
        form.setLayout(new GridLayout(6, 1, 10, 8));

        JLabel lblU = new JLabel("Username:");
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JTextField uname = new JTextField();
        uname.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel lblP = new JLabel("Password:");
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JTextField pass = new JTextField();
        pass.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel lblR = new JLabel("Role:");
        lblR.setFont(new Font("Segoe UI", Font.BOLD, 15));

        String[] roles = {"STUDENT", "INSTRUCTOR", "ADMIN"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        form.add(lblU);
        form.add(uname);
        form.add(lblP);
        form.add(pass);
        form.add(lblR);
        form.add(roleBox);

        // ---- Buttons ----
        JButton save = styledButton("Save");
        JButton cancel = styledButton("Cancel");
        cancel.setBackground(new Color(180, 180, 180));
        cancel.setForeground(Color.BLACK);
        cancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { cancel.setBackground(new Color(150, 150, 150)); }
            public void mouseExited(java.awt.event.MouseEvent e) { cancel.setBackground(new Color(180, 180, 180)); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.add(save);
        btnPanel.add(cancel);

        // ---- SAVE ACTION ----
        save.addActionListener(e -> {
            try {
                String username = uname.getText();
                String password = pass.getText();
                String role = (String) roleBox.getSelectedItem();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Fields cannot be empty");
                    return;
                }

                String hash = PasswordUtils.hashPassword(password);
                UserDAO.addUser(username, role, hash);

                JOptionPane.showMessageDialog(this, "User added!");
                loadUsers();
                popup.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        cancel.addActionListener(e -> popup.dispose());

        popup.add(form, BorderLayout.CENTER);
        popup.add(btnPanel, BorderLayout.SOUTH);
        popup.setVisible(true);
    }

    // ========= DELETE USER =========
    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to delete");
            return;
        }

        int id = (int) table.getValueAt(row, 0);

        try {
            UserDAO.deleteUser(id);
            JOptionPane.showMessageDialog(this, "User deleted!");
            loadUsers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
