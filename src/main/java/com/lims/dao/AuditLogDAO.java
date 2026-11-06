package com.lims.dao;

import com.lims.model.AuditLog;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for AUDIT_LOGS table.
 */
public class AuditLogDAO {

    private static final Logger LOGGER = Logger.getLogger(AuditLogDAO.class.getName());

    public List<AuditLog> findRecent(String actionFilter, String usernameFilter) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT TOP 200 LogID, UserID, Username, Action, TableName, RecordID, OldValues, NewValues, IPAddress, UserAgent, Timestamp " +
                "FROM AUDIT_LOGS WHERE (? = '' OR Action = ?) AND (? = '' OR Username LIKE ?) ORDER BY Timestamp DESC, LogID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String action = actionFilter == null ? "" : actionFilter.trim();
            String username = usernameFilter == null ? "" : usernameFilter.trim();
            ps.setString(1, action); ps.setString(2, action);
            ps.setString(3, username); ps.setString(4, "%" + username + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog(rs.getObject("UserID") == null ? null : rs.getInt("UserID"),
                            rs.getString("Username"), rs.getString("Action"), rs.getString("TableName"),
                            rs.getString("RecordID"), rs.getString("OldValues"), rs.getString("NewValues"),
                            rs.getString("IPAddress"), rs.getString("UserAgent"));
                    log.setLogId(rs.getInt("LogID"));
                    log.setTimestamp(rs.getTimestamp("Timestamp"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing audit logs", e);
        }
        return logs;
    }

    /**
     * Inserts an audit trail record into AUDIT_LOGS table.
     */
    public boolean insert(AuditLog log) {
        String sql = "INSERT INTO AUDIT_LOGS (UserID, Username, Action, TableName, RecordID, OldValues, NewValues, IPAddress, UserAgent, Timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (log.getUserId() != null && log.getUserId() > 0) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }

            ps.setString(2, log.getUsername());
            ps.setString(3, log.getAction());
            ps.setString(4, log.getTableName());
            ps.setString(5, log.getRecordId());
            ps.setString(6, log.getOldValues());
            ps.setString(7, log.getNewValues());
            ps.setString(8, log.getIpAddress());
            ps.setString(9, log.getUserAgent());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting audit log: " + log.getAction() + " on " + log.getTableName(), e);
            return false;
        }
    }
}
