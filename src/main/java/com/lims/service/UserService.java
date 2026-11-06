package com.lims.service;

import com.lims.dao.UserDAO;
import com.lims.model.AuditLog;
import com.lims.model.User;
import com.lims.util.PasswordUtils;

/**
 * Service managing User Authentication, RBAC, and Account business rules.
 */
public class UserService {

    private final UserDAO userDAO;
    private final AuditLogService auditLogService;

    public UserService() {
        this.userDAO = new UserDAO();
        this.auditLogService = new AuditLogService();
    }

    public UserService(UserDAO userDAO, AuditLogService auditLogService) {
        this.userDAO = userDAO;
        this.auditLogService = auditLogService;
    }

    /**
     * Authenticates user against username, password and active status.
     * Automatically logs LOGIN event to AUDIT_LOGS table.
     */
    public User authenticate(String username, String rawPassword, String ipAddress, String userAgent) {
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

        // Audit Trail: Record Login
        auditLogService.log(new AuditLog(
                user.getUserId(),
                user.getUsername(),
                "LOGIN",
                "USERS",
                String.valueOf(user.getUserId()),
                null,
                "Đăng nhập thành công",
                ipAddress,
                userAgent
        ));

        return user;
    }

    /**
     * Overloaded authenticate for backwards compatibility or test calls.
     */
    public User authenticate(String username, String rawPassword) {
        return authenticate(username, rawPassword, null, null);
    }

    /**
     * Records LOGOUT event to AUDIT_LOGS table.
     */
    public void recordLogout(User user, String ipAddress, String userAgent) {
        if (user != null) {
            auditLogService.log(new AuditLog(
                    user.getUserId(),
                    user.getUsername(),
                    "LOGOUT",
                    "USERS",
                    String.valueOf(user.getUserId()),
                    null,
                    "Đăng xuất khỏi hệ thống",
                    ipAddress,
                    userAgent
            ));
        }
    }
}
