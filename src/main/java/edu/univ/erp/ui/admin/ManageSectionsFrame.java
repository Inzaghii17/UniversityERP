package edu.univ.erp.ui.admin;

import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ManageSectionsFrame extends JFrame {

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);

    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Course ID", "Instructor ID", "Semester", "Year", "Capacity", "Time", "Room"}, 0);

    private JTable table = new JTable(model);

    public ManageSectionsFrame() {
        setTitle("Manage Sections");
        setSize(1000, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        styleTable();
        loadSections();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setBackground(new Color(245, 245, 245));

        JButton refreshBtn = styledButton("Refresh");
        JButton addBtn = styledButton("Add Section");
        JButton deleteBtn = styledButton("Delete Section");

        refreshBtn.addActionListener(e -> loadSections());
        addBtn.addActionListener(e -> openAddSectionDialog());
        deleteBtn.addActionListener(e -> deleteSelectedSection());

        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    // ============================
    // TABLE STYLE
    // ============================
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

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

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

    // ============================
    // LOAD SECTIONS
    // ============================
    private void loadSections() {
        try {
            model.setRowCount(0);
            List<Section> list = SectionDAO.getAllSections();
            for (Section s : list) {
                model.addRow(new Object[]{
                        s.getSectionId(),
                        s.getCourseId(),
                        s.getInstructorId(),
                        s.getSemester(),
                        s.getYear(),
                        s.getCapacity(),
                        s.getDayTime(),
                        s.getRoom()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sections: " + e.getMessage());
        }
    }

    // ============================
    // DELETE SECTION (SAFE DELETE)
    // ============================
    private void deleteSelectedSection() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a section to delete.");
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        try {
            int count = SectionDAO.countRegistrations(sectionId);

            if (count > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "❌ Cannot delete this section.\n" +
                                count + " student(s) are registered.",
                        "Delete Blocked",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete Section ID: " + sectionId + " ?\n(No students are registered.)",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            SectionDAO.deleteSection(sectionId);
            loadSections();

            JOptionPane.showMessageDialog(this, "Section deleted successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting section: " + e.getMessage());
        }
    }

    // ============================
    // ADD SECTION (STYLED DIALOG)
    // ============================
    private void openAddSectionDialog() {

        JDialog dialog = new JDialog(this, "Add Section", true);
        dialog.setSize(400, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel();
        header.setBackground(PURPLE);
        header.setPreferredSize(new Dimension(400, 40));
        header.add(new JLabel("<html><font color='white' size='+1'>Add Section</font></html>"));

        dialog.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(14, 1, 5, 5));

        JTextField courseId = new JTextField();
        JTextField instructorId = new JTextField();
        JTextField semester = new JTextField();
        JTextField year = new JTextField();
        JTextField capacity = new JTextField();
        JTextField dayTime = new JTextField();
        JTextField room = new JTextField();

        form.add(new JLabel("Course ID:")); form.add(courseId);
        form.add(new JLabel("Instructor ID:")); form.add(instructorId);
        form.add(new JLabel("Semester:")); form.add(semester);
        form.add(new JLabel("Year:")); form.add(year);
        form.add(new JLabel("Capacity:")); form.add(capacity);
        form.add(new JLabel("Day/Time:")); form.add(dayTime);
        form.add(new JLabel("Room:")); form.add(room);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveBtn = styledButton("Save");
        JButton cancelBtn = styledButton("Cancel");

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                Section s = new Section();
                s.setCourseId(Integer.parseInt(courseId.getText()));
                s.setInstructorId(Integer.parseInt(instructorId.getText()));
                s.setSemester(semester.getText());
                s.setYear(Integer.parseInt(year.getText()));
                s.setCapacity(Integer.parseInt(capacity.getText()));
                s.setDayTime(dayTime.getText());
                s.setRoom(room.getText());

                SectionDAO.addSection(s);
                loadSections();
                dialog.dispose();

                JOptionPane.showMessageDialog(this, "Section added successfully!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
