package edu.univ.erp.ui.admin;

import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final User user;

    public AdminDashboard(User user) {
        this.user = user;
        setTitle("Admin Dashboard - " + user.getUsername());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addCourse = new JButton("Add Course");
        JButton addSection = new JButton("Add Section");
        JButton refresh = new JButton("Refresh Lists");
        JButton toggle = new JButton("Toggle Maintenance");

        top.add(addCourse);
        top.add(addSection);
        top.add(toggle);
        top.add(refresh);

        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        JTextArea coursesArea = new JTextArea(); coursesArea.setEditable(false);
        JTextArea secsArea = new JTextArea(); secsArea.setEditable(false);
        tabs.add("Courses", new JScrollPane(coursesArea));
        tabs.add("Sections", new JScrollPane(secsArea));
        add(tabs, BorderLayout.CENTER);

        addCourse.addActionListener(e -> {
            JTextField code = new JTextField();
            JTextField title = new JTextField();
            JTextField credits = new JTextField();
            Object[] msg = {"Code:", code, "Title:", title, "Credits:", credits};
            int r = JOptionPane.showConfirmDialog(this, msg, "Create Course", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                try {
                    Course c = new Course();
                    c.setCode(code.getText().trim());
                    c.setTitle(title.getText().trim());
                    c.setCredits(Integer.parseInt(credits.getText().trim()));
                    CourseDAO.create(c);
                    JOptionPane.showMessageDialog(this, "Course created");
                    refresh.doClick();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        addSection.addActionListener(e -> {
            try {
                // choose course
                java.util.List<Course> courses = CourseDAO.listAll();
                String[] courseCodes = courses.stream().map(c -> c.getCourseId() + " - " + c.getCode()).toArray(String[]::new);
                JTextField instructorId = new JTextField();
                JTextField semester = new JTextField();
                JTextField year = new JTextField();
                JTextField capacity = new JTextField("30");
                JTextField dayTime = new JTextField();
                JTextField room = new JTextField();
                JComboBox<String> courseBox = new JComboBox<>(courseCodes);
                Object[] msg = {"Course:", courseBox, "Instructor ID (optional):", instructorId,
                        "Semester:", semester, "Year:", year, "Capacity:", capacity, "Day/Time:", dayTime, "Room:", room};
                int r = JOptionPane.showConfirmDialog(this, msg, "Create Section", JOptionPane.OK_CANCEL_OPTION);
                if (r == JOptionPane.OK_OPTION) {
                    String sel = (String) courseBox.getSelectedItem();
                    int cid = Integer.parseInt(sel.split(" - ")[0].trim());
                    Section s = new Section();
                    s.setCourseId(cid);
                    String instr = instructorId.getText().trim();
                    s.setInstructorId(instr.isEmpty()? 0 : Integer.parseInt(instr));
                    s.setSemester(semester.getText().trim());
                    s.setYear(Integer.parseInt(year.getText().trim()));
                    s.setCapacity(Integer.parseInt(capacity.getText().trim()));
                    s.setDayTime(dayTime.getText().trim());
                    s.setRoom(room.getText().trim());
                    SectionDAO.create(s);
                    JOptionPane.showMessageDialog(this, "Section created");
                    refresh.doClick();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        });

        toggle.addActionListener(e -> {
            try {
                boolean on = SettingsDAO.isMaintenanceOn();
                SettingsDAO.setMaintenance(!on);
                JOptionPane.showMessageDialog(this, "Maintenance now: " + (!on));
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        refresh.addActionListener(e -> {
            try {
                StringBuilder sb = new StringBuilder();
                List<Course> courses = CourseDAO.listAll();
                for (Course c : courses) sb.append(String.format("%d: %s - %s [%d]\n", c.getCourseId(), c.getCode(), c.getTitle(), c.getCredits()));
                coursesArea.setText(sb.toString());

                StringBuilder sb2 = new StringBuilder();
                java.util.List<Section> secs = SectionDAO.listAll();
                for (Section s : secs) sb2.append(String.format("Sec %d: %s (%s) by %d - %s %d cap:%d\n",
                        s.getSectionId(), s.getCourseCode(), s.getCourseTitle(), s.getInstructorId(), s.getSemester(), s.getYear(), s.getCapacity()));
                secsArea.setText(sb2.toString());
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        refresh.doClick();
    }
}
