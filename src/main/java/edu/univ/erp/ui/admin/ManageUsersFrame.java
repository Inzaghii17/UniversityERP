package edu.univ.erp.ui.admin;

import edu.univ.erp.data.UserDAO;
import edu.univ.erp.domain.User;
import edu.univ.erp.auth.PasswordUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageUsersFrame extends JFrame {
    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Username","Role"},0);

    public ManageUsersFrame() {
        setTitle("Manage Users");
        setSize(600,400);
        setLocationRelativeTo(null);

        JTable table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);

        JButton add = new JButton("Add User");
        JButton del = new JButton("Delete User");

        add.addActionListener(e -> addUser());
        del.addActionListener(e -> deleteUser(table));

        JPanel buttons = new JPanel();
        buttons.add(add);
        buttons.add(del);

        add(pane, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        loadUsers();
    }

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

    private void addUser() {
        JTextField uname = new JTextField();
        JTextField pass = new JTextField();
        String[] roles = {"STUDENT","INSTRUCTOR","ADMIN"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        Object[] msg = {"Username:", uname, "Password:", pass, "Role:", roleBox};
        if (JOptionPane.showConfirmDialog(this, msg, "Add User", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String hash = PasswordUtils.hashPassword(pass.getText());
                UserDAO.addUser(uname.getText(), (String) roleBox.getSelectedItem(), hash);
                JOptionPane.showMessageDialog(this, "User added!");
                loadUsers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void deleteUser(JTable t) {
        int row = t.getSelectedRow();
        if (row < 0) return;
        int id = (int) t.getValueAt(row, 0);
        try {
            UserDAO.deleteUser(id);
            JOptionPane.showMessageDialog(this, "User deleted!");
            loadUsers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
