package edu.univ.erp.access;

import edu.univ.erp.data.SettingsDAO;
import edu.univ.erp.domain.User;

/**
 * Centralized helper for checking maintenance mode & edit permissions.
 */
public class AccessControl {

    /**
     * Returns TRUE if maintenance mode is ON.
     * Uses your SettingsDAO.isMaintenance() directly.
     */
    
    public static boolean isReadOnlyNow() {
        try {
            return SettingsDAO.isMaintenance();
        } catch (Exception ex) {
            System.err.println("Error checking maintenance flag: " + ex.getMessage());
            // Fail-safe: assume NOT readonly (safer during dev/debug)
            return false;
        }
    }

    /**
     * Returns TRUE if the user is allowed to modify data.
     * Admins can modify even when maintenance is ON.
     * Students & instructors cannot modify during maintenance.
     */
    public static boolean canModify(User user) {
        if (user == null) return false;

        String role = user.getRole();
        if (role == null) return false;

        // Admin has full access even during maintenance
        if ("admin".equalsIgnoreCase(role)) return true;

        // All others cannot modify when maintenance is active
        return !isReadOnlyNow();
    }
}
