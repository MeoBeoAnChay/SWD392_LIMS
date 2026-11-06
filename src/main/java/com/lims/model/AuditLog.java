package com.lims.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Entity representing AUDIT_LOGS table.
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private int logId;
    private Integer userId;
    private String username;
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, APPROVE, REJECT
    private String tableName;
    private String recordId;
    private String oldValues; // JSON formatted string
    private String newValues; // JSON formatted string
    private String ipAddress;
    private String userAgent;
    private Timestamp timestamp;

    public AuditLog() {
    }

    public AuditLog(Integer userId, String username, String action, String tableName,
                    String recordId, String oldValues, String newValues,
                    String ipAddress, String userAgent) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.tableName = tableName;
        this.recordId = recordId;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
