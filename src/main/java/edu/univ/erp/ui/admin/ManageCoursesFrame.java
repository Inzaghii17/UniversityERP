package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.domain.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageCoursesFrame extends JFrame {

    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Code", "Title", "Credits"}, 0);
    private JTable table = new JTable(model);

    public ManageCoursesFrame() {
        setTitle("Manage Courses");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        loadCourses();

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addCourse());
        editBtn.addActionListener(e -> editCourse());
        deleteBtn.addActionListener(e -> deleteCourse());
        refreshBtn.addActionListener(e -> loadCourses());
    }

    private void loadCourses() {
        try {
            model.setRowCount(0);
            List<Course> list = CourseDAO.getAllCourses();
            for (Course c : list) {
                model.addRow(new Object[]{c.getCourseId(), c.getCode(), c.getTitle(), c.getCredits()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + e.getMessage());
        }
    }

    private void addCourse() {
        JTextField code = new JTextField();
        JTextField title = new JTextField();
        JTextField credits = new JTextField();
        Object[] msg = {"Code:", code, "Title:", title, "Credits:", credits};

        if (JOptionPane.showConfirmDialog(this, msg, "Add Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Course c = new Course();
                c.setCode(code.getText());
                c.setTitle(title.getText());
                c.setCredits(Integer.parseInt(credits.getText()));
                CourseDAO.addCourse(c);
                loadCourses();
                JOptionPane.showMessageDialog(this, "Course added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage());
            }
        }
    }

    private void editCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.");
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        String code = (String) table.getValueAt(row, 1);
        String title = (String) table.getValueAt(row, 2);
        int credits = (int) table.getValueAt(row, 3);

        JTextField codeField = new JTextField(code);
        JTextField titleField = new JTextField(title);
        JTextField creditsField = new JTextField(String.valueOf(credits));
        Object[] msg = {"Code:", codeField, "Title:", titleField, "Credits:", creditsField};

        if (JOptionPane.showConfirmDialog(this, msg, "Edit Course", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Course c = new Course();
                c.setCourseId(id);
                c.setCode(codeField.getText());
                c.setTitle(titleField.getText());
                c.setCredits(Integer.parseInt(creditsField.getText()));
                CourseDAO.updateCourse(c);
                loadCourses();
                JOptionPane.showMessageDialog(this, "Course updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating course: " + ex.getMessage());
            }
        }
    }

    private void deleteCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.");
            return;
        }

        int id = (int) table.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this course?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                CourseDAO.deleteCourse(id);
                loadCourses();
                JOptionPane.showMessageDialog(this, "Course deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage());
            }
        }
    }
}
