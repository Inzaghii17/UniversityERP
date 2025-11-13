package edu.univ.erp.service;

import edu.univ.erp.auth.AuthDAO;
import edu.univ.erp.auth.PasswordUtils;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;

import java.sql.SQLException;

public class AuthService {
    public static User login(String username, String password) throws SQLException {
        String hash = AuthDAO.getPasswordHash(username);
        if (hash == null) return null;
        if (!PasswordUtils.checkPassword(password, hash)) return null;
        User u = AuthDAO.findByUsername(username);
        if (u == null) return null;
        // load profile if student
        if ("STUDENT".equalsIgnoreCase(u.getRole())) {
            Student s = StudentDAO.findByUserId(u.getUserId());
            if (s != null) {
                s.setUsername(u.getUsername());
                s.setRole(u.getRole());
                return s;
            }
        }
        return u;
    }
}
