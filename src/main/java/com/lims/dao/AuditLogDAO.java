package com.lims.dao;

import com.lims.model.AuditLog;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for AUDIT_LOGS table.
 */
public class AuditLogDAO {

    private static final Logger LOGGER = Logger.getLogger(AuditLogDAO.class.getName());

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
