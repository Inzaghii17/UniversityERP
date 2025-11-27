import edu.univ.erp.ui.auth.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        LoginFrame.main(args);
    }
}
