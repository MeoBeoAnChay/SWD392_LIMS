package com.lims.service;

import com.lims.dao.UserDAO;
import com.lims.model.User;
import com.lims.util.PasswordUtils;

/**
 * Service managing User Authentication and Account business rules.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates user against username, password and active status.
     *
     * @param username     username input
     * @param rawPassword  plain text password
     * @return User object with loaded roles if valid, null otherwise
     */
    public User authenticate(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null;
        }

        // Check account active status
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return null;
        }

        // Verify password using SHA-256 / BCrypt
        if (!PasswordUtils.checkPassword(rawPassword.trim(), user.getPasswordHash())) {
            return null;
        }

        // Attach roles to user for authorization
        user.setRoles(userDAO.getUserRoles(user.getUserId()));
        return user;
    }
}
