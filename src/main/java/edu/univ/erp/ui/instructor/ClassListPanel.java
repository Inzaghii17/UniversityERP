package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.Section;
import edu.univ.erp.data.SectionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class ClassListPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private int instructorId;
    private Consumer<Section> selectionListener;

    public ClassListPanel(int instructorId) {
        this.instructorId = instructorId;
        setLayout(new BorderLayout());

        String[] cols = {"section_id", "Course Code", "Course Title", "Semester", "Year", "Capacity", "Room"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4 || columnIndex == 5) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadSections());
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                int r = table.getSelectedRow();
                if (r >= 0 && selectionListener != null) {
                    Integer sectionId = (Integer) table.getModel().getValueAt(r, 0);
                    List<Section> secs = SectionDAO.getSectionsByInstructor(instructorId);
                    for (Section s : secs) {
                        if (s.getSectionId() == sectionId) {
                            selectionListener.accept(s);
                            break;
                        }
                    }
                }
            }
        });

        loadSections();
    }

    public void setSectionSelectionListener(Consumer<Section> listener) {
        this.selectionListener = listener;
    }

    public void loadSections() {
        model.setRowCount(0);
        List<Section> sections = SectionDAO.getSectionsByInstructor(instructorId);
        for (Section s : sections) {
            model.addRow(new Object[]{
                    s.getSectionId(),
                    s.getCourseCode(),
                    s.getCourseTitle(),
                    s.getSemester(),
                    s.getYear(),
                    s.getCapacity(),
                    s.getRoom()
            });
        }
        // hide id column visually
        if (table.getColumnModel().getColumnCount() > 0) {
            try { table.removeColumn(table.getColumnModel().getColumn(0)); } catch (Exception ignored) {}
        }
    }
}
