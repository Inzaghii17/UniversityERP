package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    private static final int WORKLOAD = 12;

    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(WORKLOAD));
    }

    public static boolean checkPassword(String plain, String hashed) {
        if (hashed == null || hashed.isEmpty()) return false;
        return BCrypt.checkpw(plain, hashed);
    }
}
