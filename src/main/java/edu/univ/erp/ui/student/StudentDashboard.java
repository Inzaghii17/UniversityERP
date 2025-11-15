package edu.univ.erp.ui.student;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.GradesDAO;
import edu.univ.erp.data.RegistrationDAO;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.util.List;

public class StudentDashboard extends JFrame {

    private final User student;
    private final JPanel contentPanel = new JPanel(new BorderLayout());

    // tables and models
    private final DefaultTableModel catalogModel = new DefaultTableModel(new String[]{"SectionID","Course","Title","Semester","Year","DayTime","Room","Cap","Instructor"}, 0);
    private final DefaultTableModel registeredModel = new DefaultTableModel(new String[]{"SectionID","Course","Title","Semester","Year","DayTime","Room"}, 0);
    private final DefaultTableModel timetableModel = new DefaultTableModel(new String[]{"Day/Time","Course","Room","Instructor"}, 0);
    private final DefaultTableModel gradesModel = new DefaultTableModel(new String[]{"SectionID","Quiz","Midsem","Endsem","Final"}, 0);

    public StudentDashboard(User student) {
        this.student = student;
        setTitle("Student Dashboard - " + student.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));

        JButton btnCatalog = new JButton("Browse Courses");
        JButton btnRegistered = new JButton("My Registrations");
        JButton btnTimetable = new JButton("Timetable");
        JButton btnGrades = new JButton("Grades");
        JButton btnTranscript = new JButton("Transcript (CSV)");

        for (JButton b : new JButton[]{btnCatalog, btnRegistered, btnTimetable, btnGrades, btnTranscript}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setFocusPainted(false);
            sidebar.add(Box.createVerticalStrut(8));
            sidebar.add(b);
        }

        // Content panel initial
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        showCatalog(); // default

        // add listeners
        btnCatalog.addActionListener(e -> showCatalog());
        btnRegistered.addActionListener(e -> showRegistrations());
        btnTimetable.addActionListener(e -> showTimetable());
        btnGrades.addActionListener(e -> showGrades());
        btnTranscript.addActionListener(e -> exportTranscriptCSV());

        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // ---------- Catalog view ----------
    private void showCatalog() {
        contentPanel.removeAll();

        JTable table = new JTable(catalogModel);
        JScrollPane sp = new JScrollPane(table);

        JPanel btnBar = new JPanel();
        JButton btnRegister = new JButton("Register Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnBar.add(btnRegister);
        btnBar.add(btnRefresh);

        btnRegister.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a section first."); return; }
            int sectionId = (int) catalogModel.getValueAt(r, 0);
            attemptRegister(sectionId);
        });

        btnRefresh.addActionListener(e -> loadCatalog());

        contentPanel.add(new JLabel("Browse Catalog", SwingConstants.LEFT), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(btnBar, BorderLayout.SOUTH);

        loadCatalog();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadCatalog() {
        catalogModel.setRowCount(0);
        List<Section> list = CourseDAO.getCatalogSections();
        for (Section s : list) {
            catalogModel.addRow(new Object[]{
                    s.getSectionId(),
                    s.getCourseCode(),
                    s.getCourseTitle(),
                    s.getSemester(),
                    s.getYear(),
                    s.getDayTime(),
                    s.getRoom(),
                    s.getCapacity(),
                    s.getInstructorName()
            });
        }
    }

    // ---------- Registrations view ----------
    private void showRegistrations() {
        contentPanel.removeAll();

        JTable table = new JTable(registeredModel);
        JScrollPane sp = new JScrollPane(table);
        JPanel btnBar = new JPanel();
        JButton btnDrop = new JButton("Drop Selected");
        JButton btnRefresh = new JButton("Refresh");
        btnBar.add(btnDrop);
        btnBar.add(btnRefresh);

        btnDrop.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Select a registered section to drop."); return; }
            int sectionId = (int) registeredModel.getValueAt(r, 0);
            attemptDrop(sectionId);
        });

        btnRefresh.addActionListener(e -> loadRegistrations());

        contentPanel.add(new JLabel("My Registrations", SwingConstants.LEFT), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(btnBar, BorderLayout.SOUTH);

        loadRegistrations();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadRegistrations() {
        registeredModel.setRowCount(0);
        List<Section> list = RegistrationDAO.getRegisteredSections(student.getUserId());
        for (Section s : list) {
            registeredModel.addRow(new Object[]{
                    s.getSectionId(),
                    s.getCourseCode(),
                    s.getCourseTitle(),
                    s.getSemester(),
                    s.getYear(),
                    s.getDayTime(),
                    s.getRoom()
            });
        }
    }

    // ---------- Timetable view ----------
    // Simple timetable: show registrations grouped by day/time
    private void showTimetable() {
        contentPanel.removeAll();

        JTable table = new JTable(timetableModel);
        JScrollPane sp = new JScrollPane(table);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadTimetable());

        contentPanel.add(new JLabel("Timetable", SwingConstants.LEFT), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(btnRefresh, BorderLayout.SOUTH);

        loadTimetable();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadTimetable() {
        timetableModel.setRowCount(0);
        List<Section> regs = RegistrationDAO.getRegisteredSections(student.getUserId());
        for (Section s: regs) {
            String instructor = s.getInstructorName() == null ? "" : s.getInstructorName();
            timetableModel.addRow(new Object[]{ s.getDayTime(), s.getCourseTitle(), s.getRoom(), instructor });
        }
    }

    // ---------- Grades view ----------
    private void showGrades() {
        contentPanel.removeAll();
        JTable table = new JTable(gradesModel);
        JScrollPane sp = new JScrollPane(table);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadGrades());

        contentPanel.add(new JLabel("Grades", SwingConstants.LEFT), BorderLayout.NORTH);
        contentPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(refresh, BorderLayout.SOUTH);

        loadGrades();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadGrades() {
        gradesModel.setRowCount(0);
        List<Grade> list = GradesDAO.getGradesForStudent(student.getUserId());
        for (Grade g : list) {
            gradesModel.addRow(new Object[]{
                    g.getSectionId(),
                    g.getQuiz(),
                    g.getMidsem(),
                    g.getEndsem(),
                    g.getFinalGrade()
            });
        }
    }

    // ---------- Actions ----------
    private void attemptRegister(int sectionId) {
        try {
            if (RegistrationDAO.isRegistered(student.getUserId(), sectionId)) {
                JOptionPane.showMessageDialog(this, "You are already registered in this section.");
                return;
            }
            int seats = RegistrationDAO.seatsLeft(sectionId);
            if (seats <= 0) {
                JOptionPane.showMessageDialog(this, "Section full.");
                return;
            }
            boolean ok = RegistrationDAO.register(student.getUserId(), sectionId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Registered successfully.");
                loadCatalog(); loadRegistrations(); loadTimetable();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed (maybe concurrent change).");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error registering: " + e.getMessage());
        }
    }

    private void attemptDrop(int sectionId) {
        try {
            // Check deadline if deadlines table exists
            String checkSql = "SELECT drop_deadline FROM deadlines WHERE section_id=?";
            try (var con = edu.univ.erp.util.DBUtil.getERPConnection();
                 var ps = con.prepareStatement(checkSql)) {
                ps.setInt(1, sectionId);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        java.sql.Date dl = rs.getDate("drop_deadline");
                        if (dl != null && dl.toLocalDate().isBefore(java.time.LocalDate.now())) {
                            JOptionPane.showMessageDialog(this, "Drop deadline has passed. Cannot drop.");
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                // no deadlines table or error -> allow drop by default
            }

            boolean ok = RegistrationDAO.drop(student.getUserId(), sectionId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Dropped successfully.");
                loadRegistrations(); loadCatalog(); loadTimetable();
            } else {
                JOptionPane.showMessageDialog(this, "Drop failed.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error dropping: " + e.getMessage());
        }
    }

    private void exportTranscriptCSV() {
        // generate CSV from grades table
        try {
            List<Grade> list = GradesDAO.getGradesForStudent(student.getUserId());
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No grades to export.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("transcript_" + student.getUsername() + ".csv"));
            int opt = chooser.showSaveDialog(this);
            if (opt != JFileChooser.APPROVE_OPTION) return;
            java.io.File f = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(f)) {
                fw.write("SectionID,Quiz,Midsem,Endsem,FinalGrade\n");
                for (Grade g : list) {
                    fw.write(String.format("%d,%s,%s,%s,%s\n",
                            g.getSectionId(),
                            g.getQuiz()==null? "": g.getQuiz(),
                            g.getMidsem()==null? "": g.getMidsem(),
                            g.getEndsem()==null? "": g.getEndsem(),
                            g.getFinalGrade()==null? "": g.getFinalGrade()
                    ));
                }
            }
            JOptionPane.showMessageDialog(this, "Transcript saved to " + f.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }
}
