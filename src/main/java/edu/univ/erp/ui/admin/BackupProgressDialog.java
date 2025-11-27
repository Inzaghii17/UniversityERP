package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class BackupProgressDialog extends JDialog {

    private final JLabel statusLabel = new JLabel("Working…");
    private final JProgressBar bar = new JProgressBar();

    public BackupProgressDialog(JFrame parent, String title) {
        super(parent, title, true);

        setLayout(new BorderLayout(10, 10));
        setSize(350, 140);
        setLocationRelativeTo(parent);

        bar.setIndeterminate(true);

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(statusLabel, BorderLayout.NORTH);
        add(bar, BorderLayout.CENTER);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}
