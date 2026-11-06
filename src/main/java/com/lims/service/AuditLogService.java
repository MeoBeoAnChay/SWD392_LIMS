package com.lims.service;

import com.google.gson.Gson;
import com.lims.dao.AuditLogDAO;
import com.lims.model.AuditLog;

/**
 * Service managing audit logging and JSON serialization of state changes.
 */
public class AuditLogService {

    private final AuditLogDAO auditLogDAO;
    private final Gson gson;

    public AuditLogService() {
        this.auditLogDAO = new AuditLogDAO();
        this.gson = new Gson();
    }

    public AuditLogService(AuditLogDAO auditLogDAO) {
        this.auditLogDAO = auditLogDAO;
        this.gson = new Gson();
    }

    /**
     * Direct logging of an AuditLog instance.
     */
    public boolean log(AuditLog log) {
        if (log == null) return false;
        return auditLogDAO.insert(log);
    }

    /**
     * Logs an action with old and new entity objects automatically serialized to JSON.
     */
    public boolean logAction(Integer userId, String username, String action, String tableName,
                             String recordId, Object oldEntity, Object newEntity,
                             String ipAddress, String userAgent) {
        String oldValues = (oldEntity != null) ? gson.toJson(oldEntity) : null;
        String newValues = (newEntity != null) ? gson.toJson(newEntity) : null;

        AuditLog log = new AuditLog(userId, username, action, tableName, recordId,
                oldValues, newValues, ipAddress, userAgent);
        return auditLogDAO.insert(log);
    }
}
