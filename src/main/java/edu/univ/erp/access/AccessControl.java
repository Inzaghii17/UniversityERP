package edu.univ.erp.access;

import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.User;


public class AccessControl {


    public static boolean isMaintenance() {
        try {
            return SettingsDAO.isMaintenance();
        } catch (Exception ex) {
            System.err.println("Error checking maintenance flag: " + ex.getMessage());
            return false;
        }
    }

    public static boolean canModify(User user) {
        if (user == null) return false;

        String role = user.getRole();
        if (role == null) return false;

        // Admin ALWAYS allowed
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }


        return !isMaintenance();
    }

    public static boolean isReadOnly(User user) {
        return !canModify(user);
    }
}
