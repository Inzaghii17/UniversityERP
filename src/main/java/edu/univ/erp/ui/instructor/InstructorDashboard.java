package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.domain.User;
import edu.univ.erp.ui.auth.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class InstructorDashboard extends JFrame {

    private final User currentUser;

    private final Color PURPLE = new Color(100, 52, 136);
    private final Color PURPLE_DARK = new Color(85, 44, 115);

    private JPanel maintenanceBanner;

    public InstructorDashboard(User currentUser) {
        super("Instructor Dashboard — " + currentUser.getUsername());
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        /* -----------------------------------------------------------
         * TOP BAR
         * ----------------------------------------------------------- */
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(1200, 70));
        topBar.setBackground(PURPLE);

        JLabel title = new JLabel("Instructor Dashboard — " + currentUser.getUsername());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBorder(new EmptyBorder(0, 22, 0, 0));
        topBar.add(title, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        styleHeaderBtn(btnLogout);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JButton btnChangePassword = new JButton("Change Password");
        styleHeaderBtn(btnChangePassword);
        btnChangePassword.addActionListener(e ->
                new edu.univ.erp.ui.auth.ChangePasswordDialog(this, currentUser).setVisible(true)
        );

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 15));
        rightButtons.setOpaque(false);
        rightButtons.add(btnChangePassword);
        rightButtons.add(btnLogout);

        topBar.add(rightButtons, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        /* -----------------------------------------------------------
         * MAINTENANCE BANNER
         * ----------------------------------------------------------- */
        maintenanceBanner = new JPanel();
        maintenanceBanner.setBackground(new Color(255, 200, 50));
        maintenanceBanner.setPreferredSize(new Dimension(1200, 32));

        JLabel mLabel = new JLabel("⚠ Maintenance Mode is ON — READ-ONLY");
        mLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mLabel.setForeground(Color.DARK_GRAY);

        maintenanceBanner.add(mLabel);
        maintenanceBanner.setVisible(AccessControl.isMaintenance());
        add(maintenanceBanner, BorderLayout.SOUTH);

        /* -----------------------------------------------------------
         * LEFT SIDEBAR IMAGE
         * ----------------------------------------------------------- */
        JPanel sidebar = new ImagePanel("/images/side_column.png");
        sidebar.setPreferredSize(new Dimension(280, 0));
        add(sidebar, BorderLayout.WEST);

        /* -----------------------------------------------------------
         * MAIN CENTER CONTENT
         * ----------------------------------------------------------- */
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(40, 40, 40, 40));
        add(content, BorderLayout.CENTER);

        /* -----------------------------------------------------------
         * LEFT TEXT AREA
         * ----------------------------------------------------------- */
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel dashTitle = new JLabel("Instructor Dashboard");
        dashTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        dashTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeMsg = new JLabel(
                "<html>Welcome Instructor!<br><br>" +
                        "Use this dashboard to manage:<br>" +
                        "• Student section lists<br>" +
                        "• Enter & compute grades<br>" +
                        "• Export gradebook CSV<br>" +
                        "• View class statistics<br><br>" +
                        "</html>"
        );
        welcomeMsg.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeMsg.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(dashTitle);
        textPanel.add(Box.createVerticalStrut(20));
        textPanel.add(welcomeMsg);

        content.add(textPanel, BorderLayout.WEST);

        /* -----------------------------------------------------------
         * RIGHT — QUICK ACTIONS GRADIENT CARD
         * ----------------------------------------------------------- */
        JPanel quickCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(245, 235, 255),
                        getWidth(), getHeight(), new Color(230, 210, 255)
                );

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
            }
        };

        quickCard.setOpaque(false);
        quickCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        quickCard.setPreferredSize(new Dimension(350, 0));   // width fixed, height flexible
        quickCard.setLayout(new BoxLayout(quickCard, BoxLayout.Y_AXIS));

        JLabel qaTitle = new JLabel("Quick Actions");
        qaTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        qaTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        qaTitle.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton btnSections = createTileButton("My Sections");
        JButton btnGrades = createTileButton("Grades");

        // Center buttons and adjust spacing
        btnSections.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGrades.setAlignmentX(Component.CENTER_ALIGNMENT);

        quickCard.add(qaTitle);
        quickCard.add(Box.createVerticalStrut(45));
        quickCard.add(btnSections);
        quickCard.add(Box.createVerticalStrut(35));
        quickCard.add(btnGrades);
        quickCard.add(Box.createVerticalGlue());

        JPanel rightWrap = new JPanel();
        rightWrap.setOpaque(false);
        rightWrap.setLayout(new BorderLayout());
        rightWrap.add(quickCard, BorderLayout.CENTER);

        content.add(rightWrap, BorderLayout.EAST);

        /* -----------------------------------------------------------
         * ACTIONS
         * ----------------------------------------------------------- */
        btnSections.addActionListener(e ->
                new SectionsFrame(currentUser.getUserId()).setVisible(true));

        btnGrades.addActionListener(e ->
                new GradesFrame(currentUser.getUserId()).setVisible(true));
    }

    /* -----------------------------------------------------------
     * STYLE — HEADER BUTTON
     * ----------------------------------------------------------- */
    private void styleHeaderBtn(JButton b) {
        b.setBackground(PURPLE_DARK);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setFocusPainted(false);
    }

    /* -----------------------------------------------------------
     * TILE BUTTONS (bigger & more filled)
     * ----------------------------------------------------------- */
    private JButton createTileButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = getModel().isRollover() ? PURPLE_DARK : PURPLE;

                // Soft shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 12, 20, 20);

                // Main filled button
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
            }
        };

        b.setFont(new Font("Segoe UI", Font.BOLD, 24));
        b.setForeground(Color.WHITE);

        // Wider, more rectangular, fits nicely inside 350px card
        b.setPreferredSize(new Dimension(300, 110));
        b.setMaximumSize(new Dimension(300, 110));

        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return b;
    }

    /* -----------------------------------------------------------
     * SIDEBAR IMAGE PANEL
     * ----------------------------------------------------------- */
    class ImagePanel extends JPanel {
        private final Image img;

        public ImagePanel(String path) {
            ImageIcon icon = null;
            try {
                icon = new ImageIcon(getClass().getResource(path));
            } catch (Exception ignored) {}
            img = icon != null ? icon.getImage() : null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null)
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
