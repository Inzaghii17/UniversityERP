package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ManageCoursesFrame extends JFrame {

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);

    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Code", "Title", "Credits"}, 0
    );
    private JTable table = new JTable(model);

    public ManageCoursesFrame() {
        setTitle("Manage Courses");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        styleTable();
        loadCourses();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton addBtn = styledButton("Add");
        JButton editBtn = styledButton("Edit");
        JButton deleteBtn = styledButton("Delete");
        JButton refreshBtn = styledButton("Refresh");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> openAddCourseDialog());
        editBtn.addActionListener(e -> editCourse());
        deleteBtn.addActionListener(e -> deleteCourse());
        refreshBtn.addActionListener(e -> loadCourses());
    }

    // ---------------- Styling ----------------

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PURPLE_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(PURPLE); }
        });

        return btn;
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
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

    // ---------------- Load Courses ----------------

    private void loadCourses() {
        try {
            model.setRowCount(0);
            List<Course> list = CourseDAO.getAllCourses();

            for (Course c : list) {
                model.addRow(new Object[]{
                        c.getCourseId(),
                        c.getCode(),
                        c.getTitle(),
                        c.getCredits()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    // ---------------- Add Course Dialog ----------------

    private void openAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Add Course", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 5, 8));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl1 = new JLabel("Code:");
        JLabel lbl2 = new JLabel("Title:");
        JLabel lbl3 = new JLabel("Credits:");

        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl3.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextField code = new JTextField();
        JTextField title = new JTextField();
        JTextField credits = new JTextField();

        code.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        title.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        credits.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(lbl1); panel.add(code);
        panel.add(lbl2); panel.add(title);
        panel.add(lbl3); panel.add(credits);

        // Buttons
        JButton save = styledButton("Save");
        JButton cancel = styledButton("Cancel");

        cancel.addActionListener(e -> dialog.dispose());

        save.addActionListener(e -> {
            try {
                Course c = new Course();
                c.setCode(code.getText());
                c.setTitle(title.getText());
                c.setCredits(Integer.parseInt(credits.getText()));

                CourseDAO.addCourse(c);
                loadCourses();
                JOptionPane.showMessageDialog(this, "Course added successfully!");
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        JPanel bottom = new JPanel();
        bottom.add(save);
        bottom.add(cancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---------------- Edit Course ----------------

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course first");
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        String codeVal = (String) table.getValueAt(row, 1);
        String titleVal = (String) table.getValueAt(row, 2);
        int creditsVal = (int) table.getValueAt(row, 3);

        openEditCourseDialog(id, codeVal, titleVal, creditsVal);
    }

    private void openEditCourseDialog(int id, String codeVal, String titleVal, int creditsVal) {
        JDialog dialog = new JDialog(this, "Edit Course", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 5, 8));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lbl1 = new JLabel("Code:");
        JLabel lbl2 = new JLabel("Title:");
        JLabel lbl3 = new JLabel("Credits:");

        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl3.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JTextField code = new JTextField(codeVal);
        JTextField title = new JTextField(titleVal);
        JTextField credits = new JTextField(String.valueOf(creditsVal));

        code.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        title.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        credits.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(lbl1); panel.add(code);
        panel.add(lbl2); panel.add(title);
        panel.add(lbl3); panel.add(credits);

        // Buttons
        JButton save = styledButton("Save");
        JButton cancel = styledButton("Cancel");

        cancel.addActionListener(e -> dialog.dispose());

        save.addActionListener(e -> {
            try {
                Course c = new Course();
                c.setCourseId(id);
                c.setCode(code.getText());
                c.setTitle(title.getText());
                c.setCredits(Integer.parseInt(credits.getText()));

                CourseDAO.updateCourse(c);
                loadCourses();
                JOptionPane.showMessageDialog(this, "Course updated successfully!");
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        JPanel bottom = new JPanel();
        bottom.add(save);
        bottom.add(cancel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ---------------- Delete ----------------

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a course first");
            return;
        }

        int id = (int) table.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this,
                "Delete this course?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try {
                CourseDAO.deleteCourse(id);
                loadCourses();
                JOptionPane.showMessageDialog(this, "Course deleted!");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
}
