package com.lims.dao;

import com.lims.model.Role;
import com.lims.model.User;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for USERS and ROLES.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, Username, FullName, Email, Phone, Status, CreatedAt FROM USERS ORDER BY UserID DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User(rs.getInt("UserID"), rs.getString("Username"), null,
                        rs.getString("FullName"), rs.getString("Email"), rs.getString("Phone"),
                        rs.getString("Status"), rs.getTimestamp("CreatedAt"));
                user.setRoles(getUserRoles(user.getUserId()));
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing users", e);
        }
        return users;
    }

    public List<Role> findAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT RoleID, RoleName, Description FROM ROLES ORDER BY RoleID";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) roles.add(new Role(rs.getInt("RoleID"), rs.getString("RoleName"), rs.getString("Description")));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing roles", e);
        }
        return roles;
    }

    public boolean isActiveAdmin(int userId) {
        String sql = "SELECT COUNT(*) FROM USERS u JOIN USER_ROLES ur ON ur.UserID = u.UserID JOIN ROLES r ON r.RoleID = ur.RoleID " +
                "WHERE u.UserID = ? AND u.Status = 'ACTIVE' AND LOWER(r.RoleName) = LOWER(?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, "Admin");
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking admin access for user " + userId, e);
            return false;
        }
    }

    public boolean changePassword(int userId, String currentHash, String newHash) {
        String sql = "UPDATE USERS SET PasswordHash=? WHERE UserID=? AND PasswordHash=? AND Status='ACTIVE'";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash); ps.setInt(2, userId); ps.setString(3, currentHash);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to change password for user " + userId, e);
            return false;
        }
    }

    public int countActiveAdmins() {
        String sql = "SELECT COUNT(DISTINCT u.UserID) FROM USERS u JOIN USER_ROLES ur ON ur.UserID = u.UserID JOIN ROLES r ON r.RoleID = ur.RoleID " +
                "WHERE u.Status = 'ACTIVE' AND LOWER(r.RoleName) = LOWER(?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Admin");
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting active admins", e);
            return 0;
        }
    }

    public boolean create(User user, String rawPassword, List<Integer> roleIds) {
        String insertUser = "INSERT INTO USERS (Username, PasswordHash, FullName, Email, Phone, Status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, org.mindrot.jbcrypt.BCrypt.hashpw(rawPassword, org.mindrot.jbcrypt.BCrypt.gensalt(10)));
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getPhone());
                ps.setString(6, user.getStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No generated user ID");
                    user.setUserId(keys.getInt(1));
                }
            }
            replaceRoles(conn, user.getUserId(), roleIds);
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user", e);
            return false;
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE USERS SET FullName = ?, Email = ?, Phone = ?, Status = ? WHERE UserID = ?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user " + user.getUserId(), e);
            return false;
        }
    }

    public boolean updateRoles(int userId, List<Integer> roleIds) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            replaceRoles(conn, userId, roleIds);
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating roles for user " + userId, e);
            return false;
        }
    }

    private void replaceRoles(Connection conn, int userId, List<Integer> roleIds) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM USER_ROLES WHERE UserID = ?")) {
            delete.setInt(1, userId);
            delete.executeUpdate();
        }
        if (roleIds == null) return;
        try (PreparedStatement insert = conn.prepareStatement("INSERT INTO USER_ROLES (UserID, RoleID) SELECT ?, RoleID FROM ROLES WHERE RoleID = ?")) {
            for (Integer roleId : roleIds) {
                if (roleId == null) continue;
                insert.setInt(1, userId);
                insert.setInt(2, roleId);
                if (insert.executeUpdate() != 1) throw new SQLException("Unknown role ID: " + roleId);
            }
        }
    }

    /**
     * Finds active or existing user by username.
     */
    public User findByUsername(String username) {
        String sql = "SELECT UserID, Username, PasswordHash, FullName, Email, Phone, Status, CreatedAt " +
                     "FROM USERS WHERE Username = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            rs.getString("PasswordHash"),
                            rs.getString("FullName"),
                            rs.getString("Email"),
                            rs.getString("Phone"),
                            rs.getString("Status"),
                            rs.getTimestamp("CreatedAt")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
        }
        return null;
    }

    /**
     * Gets all assigned roles for a given user.
     */
    public List<Role> getUserRoles(int userId) {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.RoleID, r.RoleName, r.Description " +
                     "FROM ROLES r " +
                     "INNER JOIN USER_ROLES ur ON r.RoleID = ur.RoleID " +
                     "WHERE ur.UserID = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(new Role(
                            rs.getInt("RoleID"),
                            rs.getString("RoleName"),
                            rs.getString("Description")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching roles for userId: " + userId, e);
        }
        return roles;
    }
}
