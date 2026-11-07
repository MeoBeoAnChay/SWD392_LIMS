package com.lims.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing USERS table.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private String status; // ACTIVE, INACTIVE, LOCKED
    private Timestamp createdAt;

    // Attached roles for RBAC authorization checks
    private List<Role> roles = new ArrayList<>();

    public User() {
    }

    public User(int userId, String username, String passwordHash, String fullName, 
                String email, String phone, String status, Timestamp createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = (roles != null) ? roles : new ArrayList<>();
    }

    /**
     * Checks if user has a specific role name (case-insensitive).
     */
    public boolean hasRole(String roleName) {
        if (roles == null || roleName == null) return false;
        for (Role role : roles) {
            if (role != null && roleName.equalsIgnoreCase(role.getRoleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the primary landing URL for the user based on their role.
     */
    public String getDefaultRedirectUrl() {
        if (hasRole("Admin")) {
            return "/app/admin/users";
        }
        if (hasRole("Manager")) {
            return "/app/manager/dashboard";
        }
        if (hasRole("Warehouse Staff")) {
            return "/app/warehouse/dashboard";
        }
        if (hasRole("Purchasing Staff")) {
            return "/app/purchasing/dashboard";
        }
        if (hasRole("Sales Staff")) {
            return "/app/sales/export-requests";
        }
        return "/login";
    }
}
