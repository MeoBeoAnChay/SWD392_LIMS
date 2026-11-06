package com.lims.dao;

import com.lims.model.Handover;
import com.lims.model.ImportReceipt;
import com.lims.model.ImportReceiptItem;
import com.lims.model.ImportRequestItem;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarehouseDAO {
    private static final Logger LOGGER = Logger.getLogger(WarehouseDAO.class.getName());

    public boolean isActiveWarehouseStaff(int userId) {
        String sql = "SELECT COUNT(*) FROM USERS u JOIN USER_ROLES ur ON ur.UserID=u.UserID JOIN ROLES r ON r.RoleID=ur.RoleID " +
                "WHERE u.UserID=? AND u.Status='ACTIVE' AND LOWER(r.RoleName)=LOWER('Warehouse Staff')";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to verify Warehouse Staff access", e); return false; }
    }

    public List<Handover> findAssignedHandovers(int userId) {
        List<Handover> handovers = new ArrayList<>();
        String sql = "SELECT h.HandoverID, h.ImportRequestID, h.HandoverBy, h.HandoverDate, h.HandoverQuantity, h.HandoverLocation, " +
                "s.SupplierName, creator.FullName AS PurchasingStaffName, " +
                "CASE WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='COMPLETED') THEN 'COMPLETED' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='PENDING_MANAGER') THEN 'PENDING_MANAGER' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='REJECTED') THEN 'REJECTED' ELSE 'WAITING' END AS ReceiptStatus, " +
                "(SELECT TOP 1 r.CancelReason FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='REJECTED' ORDER BY r.ImportReceiptID DESC) AS RejectReason " +
                "FROM HANDOVERS h JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID " +
                "JOIN USERS creator ON creator.UserID=ir.CreatedBy WHERE h.ReceivedBy=? AND ir.Status IN ('APPROVED','COMPLETED') " +
                "ORDER BY h.HandoverDate DESC, h.HandoverID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) {
                Handover handover = new Handover(); handover.setHandoverId(rs.getInt("HandoverID"));
                handover.setRequestId(rs.getInt("ImportRequestID")); handover.setHandoverBy(rs.getString("HandoverBy"));
                handover.setHandoverDate(rs.getTimestamp("HandoverDate")); handover.setHandoverQuantity(rs.getInt("HandoverQuantity"));
                handover.setLocation(rs.getString("HandoverLocation")); handover.setSupplierName(rs.getString("SupplierName"));
                handover.setPurchasingStaffName(rs.getString("PurchasingStaffName")); handover.setReceiptStatus(rs.getString("ReceiptStatus"));
                handover.setRejectReason(rs.getString("RejectReason")); handovers.add(handover);
            } }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list assigned handovers", e); }
        return handovers;
    }

    public Handover findAvailableHandover(int handoverId, int userId) {
        String sql = "SELECT h.HandoverID, h.ImportRequestID, h.HandoverBy, h.HandoverDate, h.HandoverQuantity, h.HandoverLocation, " +
                "s.SupplierName, creator.FullName AS PurchasingStaffName, ir.Status AS RequestStatus, " +
                "CASE WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='COMPLETED') THEN 1 ELSE 0 END AS IsCompleted, " +
                "CASE WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='PENDING_MANAGER') THEN 1 ELSE 0 END AS IsPending " +
                "FROM HANDOVERS h JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID " +
                "JOIN USERS creator ON creator.UserID=ir.CreatedBy " +
                "WHERE h.HandoverID=? AND h.ReceivedBy=?";
        Handover handover;
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, handoverId); ps.setInt(2, userId); try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !("APPROVED".equalsIgnoreCase(rs.getString("RequestStatus")) || "COMPLETED".equalsIgnoreCase(rs.getString("RequestStatus")))
                        || rs.getBoolean("IsCompleted") || rs.getBoolean("IsPending")) return null;
                handover = new Handover(); handover.setHandoverId(rs.getInt("HandoverID"));
                handover.setRequestId(rs.getInt("ImportRequestID")); handover.setHandoverBy(rs.getString("HandoverBy"));
                handover.setHandoverDate(rs.getTimestamp("HandoverDate")); handover.setHandoverQuantity(rs.getInt("HandoverQuantity"));
                handover.setLocation(rs.getString("HandoverLocation")); handover.setSupplierName(rs.getString("SupplierName"));
                handover.setPurchasingStaffName(rs.getString("PurchasingStaffName"));
            }
            handover.setItems(findHandoverItems(conn, handoverId)); return handover;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to open handover " + handoverId, e); return null; }
    }

    public int createImportReceipt(int handoverId, int warehouseUserId, String note, List<ImportReceiptItem> items) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            int requestId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT h.ImportRequestID, ir.Status FROM HANDOVERS h WITH (UPDLOCK, HOLDLOCK) JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID WHERE h.HandoverID=? AND h.ReceivedBy=?")) {
                ps.setInt(1, handoverId); ps.setInt(2, warehouseUserId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !("APPROVED".equalsIgnoreCase(rs.getString("Status")) || "COMPLETED".equalsIgnoreCase(rs.getString("Status"))))
                        throw new SQLException("Handover is unavailable to this warehouse user");
                    requestId = rs.getInt("ImportRequestID");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM IMPORT_RECEIPTS WITH (UPDLOCK, HOLDLOCK) WHERE HandoverID=? AND Status IN ('PENDING_MANAGER','COMPLETED')")) {
                ps.setInt(1, handoverId); try (ResultSet rs = ps.executeQuery()) { if (!rs.next() || rs.getInt(1) > 0) throw new SQLException("Handover already has a pending or approved receipt"); }
            }
            Map<Integer, Integer> expected = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT ProductID, Quantity FROM HANDOVER_DETAILS WHERE HandoverID=?")) {
                ps.setInt(1, handoverId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) expected.put(rs.getInt("ProductID"), rs.getInt("Quantity")); }
            }
            Map<Integer, Integer> actual = new LinkedHashMap<>();
            for (ImportReceiptItem item : items) actual.merge(item.getProductId(), 1, Integer::sum);
            if (expected.isEmpty() || !expected.equals(actual)) throw new SQLException("Serial entries must match the handover quantities by product");

            int receiptId;
            String insertReceipt = "INSERT INTO IMPORT_RECEIPTS (ImportRequestID, HandoverID, UserID, Status, Note, PaperSigned, PaperSignedAt) VALUES (?, ?, ?, 'PENDING_MANAGER', ?, 1, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(insertReceipt, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, requestId); ps.setInt(2, handoverId); ps.setInt(3, warehouseUserId); ps.setString(4, note); ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Import receipt ID was not generated"); receiptId = keys.getInt(1); }
            }
            String duplicateSql = "SELECT CASE WHEN EXISTS (SELECT 1 FROM INVENTORY WHERE SerialNumber=?) OR EXISTS (SELECT 1 FROM IMPORT_RECEIPT_DETAILS d JOIN IMPORT_RECEIPTS r ON r.ImportReceiptID=d.ImportReceiptID WHERE d.SerialNumber=? AND r.Status<>'REJECTED') THEN 1 ELSE 0 END";
            try (PreparedStatement duplicate = conn.prepareStatement(duplicateSql);
                 PreparedStatement detail = conn.prepareStatement("INSERT INTO IMPORT_RECEIPT_DETAILS (ImportReceiptID, ProductID, SerialNumber, Location, Condition) VALUES (?, ?, ?, ?, ?)")) {
                for (ImportReceiptItem item : items) {
                    duplicate.setString(1, item.getSerialNumber()); duplicate.setString(2, item.getSerialNumber());
                    try (ResultSet rs = duplicate.executeQuery()) { if (rs.next() && rs.getInt(1) != 0) throw new SQLException("Duplicate serial number " + item.getSerialNumber()); }
                    detail.setInt(1, receiptId); detail.setInt(2, item.getProductId()); detail.setString(3, item.getSerialNumber());
                    detail.setString(4, item.getLocation()); detail.setString(5, item.getCondition()); detail.addBatch();
                }
                detail.executeBatch();
            }
            conn.commit(); return receiptId;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Unable to create import receipt for handover " + handoverId, e); return -1; }
    }

    private List<ImportRequestItem> findHandoverItems(Connection conn, int handoverId) throws SQLException {
        List<ImportRequestItem> items = new ArrayList<>();
        String sql = "SELECT hd.ProductID, p.ProductCode, p.ProductName, hd.Quantity FROM HANDOVER_DETAILS hd JOIN PRODUCTS p ON p.ProductID=hd.ProductID WHERE hd.HandoverID=? ORDER BY hd.HandoverDetailID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.setInt(1, handoverId); try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { ImportRequestItem item = new ImportRequestItem(); item.setProductId(rs.getInt("ProductID")); item.setProductCode(rs.getString("ProductCode")); item.setProductName(rs.getString("ProductName")); item.setQuantity(rs.getInt("Quantity")); items.add(item); }
        } }
        return items;
    }
}
