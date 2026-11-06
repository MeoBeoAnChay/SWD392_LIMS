package com.lims.dao;

import com.lims.model.ImportRequest;
import com.lims.model.ImportRequestItem;
import com.lims.model.ImportReceipt;
import com.lims.model.ImportReceiptItem;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagerDAO {
    private static final Logger LOGGER = Logger.getLogger(ManagerDAO.class.getName());

    public boolean isActiveManager(int userId) {
        String sql = "SELECT COUNT(*) FROM USERS u JOIN USER_ROLES ur ON ur.UserID=u.UserID JOIN ROLES r ON r.RoleID=ur.RoleID " +
                "WHERE u.UserID=? AND u.Status='ACTIVE' AND LOWER(r.RoleName)=LOWER('Manager')";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to verify Manager access", e); return false; }
    }

    public List<ImportRequest> findImportRequests(String status) {
        List<ImportRequest> requests = new ArrayList<>();
        String sql = "SELECT ir.ImportRequestID, ir.SupplierID, s.SupplierName, ir.RequestDate, ir.Status, ir.CancelReason, " +
                "creator.FullName AS CreatedByName, approver.FullName AS ApprovedByName, ir.ApprovedDate " +
                "FROM IMPORT_REQUESTS ir JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID " +
                "JOIN USERS creator ON creator.UserID=ir.CreatedBy LEFT JOIN USERS approver ON approver.UserID=ir.ApprovedBy " +
                "WHERE (?='ALL' OR UPPER(ir.Status)=?) " +
                "ORDER BY CASE WHEN ir.Status='PENDING' THEN 0 ELSE 1 END, ir.RequestDate DESC, ir.ImportRequestID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportRequest request = new ImportRequest(); request.setRequestId(rs.getInt("ImportRequestID"));
                    request.setSupplierId(rs.getInt("SupplierID")); request.setSupplierName(rs.getString("SupplierName"));
                    request.setRequestDate(rs.getTimestamp("RequestDate")); request.setStatus(rs.getString("Status"));
                    request.setCancelReason(rs.getString("CancelReason")); request.setCreatedByName(rs.getString("CreatedByName"));
                    request.setApprovedByName(rs.getString("ApprovedByName")); request.setApprovedDate(rs.getTimestamp("ApprovedDate"));
                    requests.add(request);
                }
            }
            for (ImportRequest request : requests) {
                request.setItems(findRequestItems(conn, request.getRequestId()));
                request.setOrderedQuantity(request.getItems().stream().mapToInt(ImportRequestItem::getQuantity).sum());
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list import requests for manager", e); }
        return requests;
    }

    public ImportRequest findImportRequest(int requestId) {
        return findImportRequests("ALL").stream().filter(request -> request.getRequestId() == requestId).findFirst().orElse(null);
    }

    public boolean decidePendingRequest(int requestId, int managerId, String nextStatus, String reason) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("SELECT Status FROM IMPORT_REQUESTS WITH (UPDLOCK, HOLDLOCK) WHERE ImportRequestID=?")) {
                ps.setInt(1, requestId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"PENDING".equalsIgnoreCase(rs.getString("Status"))) throw new SQLException("Request is no longer pending");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM IMPORT_REQUEST_DETAILS WHERE ImportRequestID=?")) {
                ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) throw new SQLException("Request has no order items");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPORT_REQUESTS SET Status=?, ApprovedBy=?, ApprovedDate=GETDATE(), CancelReason=? WHERE ImportRequestID=? AND Status='PENDING'")) {
                ps.setString(1, nextStatus); ps.setInt(2, managerId); ps.setString(3, reason); ps.setInt(4, requestId);
                if (ps.executeUpdate() != 1) throw new SQLException("Request state changed");
            }
            conn.commit();
            return true;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Unable to decide import request " + requestId, e); return false; }
    }

    public List<ImportReceipt> findImportReceipts(String status) {
        List<ImportReceipt> receipts = new ArrayList<>();
        String sql = "SELECT r.ImportReceiptID, r.HandoverID, r.ImportRequestID, r.ReceiptDate, r.Status, r.Note, r.CancelReason, " +
                "r.ApprovedDate, r.PaperSigned, supplier.SupplierName, warehouse.FullName AS WarehouseStaffName, manager.FullName AS ManagerName " +
                "FROM IMPORT_RECEIPTS r JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=r.ImportRequestID " +
                "JOIN SUPPLIERS supplier ON supplier.SupplierID=ir.SupplierID JOIN USERS warehouse ON warehouse.UserID=r.UserID " +
                "LEFT JOIN USERS manager ON manager.UserID=r.ApprovedBy WHERE (?='ALL' OR UPPER(r.Status)=?) " +
                "ORDER BY CASE WHEN r.Status='PENDING_MANAGER' THEN 0 ELSE 1 END, r.ReceiptDate DESC, r.ImportReceiptID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) {
                ImportReceipt receipt = new ImportReceipt(); receipt.setReceiptId(rs.getInt("ImportReceiptID"));
                receipt.setHandoverId(rs.getInt("HandoverID")); receipt.setRequestId(rs.getInt("ImportRequestID"));
                receipt.setReceiptDate(rs.getTimestamp("ReceiptDate")); receipt.setStatus(rs.getString("Status"));
                receipt.setNote(rs.getString("Note")); receipt.setCancelReason(rs.getString("CancelReason"));
                receipt.setApprovedDate(rs.getTimestamp("ApprovedDate")); receipt.setPaperSigned(rs.getBoolean("PaperSigned"));
                receipt.setSupplierName(rs.getString("SupplierName")); receipt.setWarehouseStaffName(rs.getString("WarehouseStaffName"));
                receipt.setManagerName(rs.getString("ManagerName")); receipts.add(receipt);
            } }
            for (ImportReceipt receipt : receipts) receipt.setItems(findReceiptItems(conn, receipt.getReceiptId()));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list import receipts", e); }
        return receipts;
    }

    public boolean decideImportReceipt(int receiptId, int managerId, String nextStatus, String reason) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            int requestId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT ImportRequestID, Status, PaperSigned FROM IMPORT_RECEIPTS WITH (UPDLOCK, HOLDLOCK) WHERE ImportReceiptID=?")) {
                ps.setInt(1, receiptId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"PENDING_MANAGER".equalsIgnoreCase(rs.getString("Status"))) throw new SQLException("Import receipt is no longer pending review");
                    if (!rs.getBoolean("PaperSigned")) throw new SQLException("Signed handover paper has not been confirmed");
                    requestId = rs.getInt("ImportRequestID");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM IMPORT_RECEIPT_DETAILS WHERE ImportReceiptID=?")) {
                ps.setInt(1, receiptId); try (ResultSet rs = ps.executeQuery()) { if (!rs.next() || rs.getInt(1) == 0) throw new SQLException("Receipt has no serial records"); }
            }
            if ("COMPLETED".equals(nextStatus)) {
                try (PreparedStatement duplicate = conn.prepareStatement("SELECT COUNT(*) FROM IMPORT_RECEIPT_DETAILS d JOIN INVENTORY i ON i.SerialNumber=d.SerialNumber WHERE d.ImportReceiptID=?")) {
                    duplicate.setInt(1, receiptId); try (ResultSet rs = duplicate.executeQuery()) { if (rs.next() && rs.getInt(1) > 0) throw new SQLException("A serial number already exists in inventory"); }
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO INVENTORY (ProductID, SerialNumber, Location, Status) SELECT ProductID, SerialNumber, Location, CASE WHEN Condition='BRAND_NEW' THEN 'IN_STOCK' ELSE 'DAMAGED' END FROM IMPORT_RECEIPT_DETAILS WHERE ImportReceiptID=?")) {
                    ps.setInt(1, receiptId); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPORT_RECEIPTS SET Status='COMPLETED', ApprovedBy=?, ApprovedDate=GETDATE(), CancelReason=NULL WHERE ImportReceiptID=? AND Status='PENDING_MANAGER'")) {
                    ps.setInt(1, managerId); ps.setInt(2, receiptId); if (ps.executeUpdate() != 1) throw new SQLException("Receipt state changed");
                }
                String completeRequest = "UPDATE IMPORT_REQUESTS SET Status='COMPLETED' WHERE ImportRequestID=? AND Status='APPROVED' " +
                        "AND (SELECT COALESCE(SUM(Quantity),0) FROM IMPORT_REQUEST_DETAILS WHERE ImportRequestID=?) <= " +
                        "(SELECT COUNT(*) FROM IMPORT_RECEIPT_DETAILS d JOIN IMPORT_RECEIPTS r ON r.ImportReceiptID=d.ImportReceiptID WHERE r.ImportRequestID=? AND r.Status='COMPLETED')";
                try (PreparedStatement ps = conn.prepareStatement(completeRequest)) { ps.setInt(1, requestId); ps.setInt(2, requestId); ps.setInt(3, requestId); ps.executeUpdate(); }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPORT_RECEIPTS SET Status='REJECTED', ApprovedBy=?, ApprovedDate=GETDATE(), CancelReason=? WHERE ImportReceiptID=? AND Status='PENDING_MANAGER'")) {
                    ps.setInt(1, managerId); ps.setString(2, reason); ps.setInt(3, receiptId); if (ps.executeUpdate() != 1) throw new SQLException("Receipt state changed");
                }
            }
            conn.commit(); return true;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "Unable to decide import receipt " + receiptId, e); return false; }
    }

    private List<ImportReceiptItem> findReceiptItems(Connection conn, int receiptId) throws SQLException {
        List<ImportReceiptItem> items = new ArrayList<>();
        String sql = "SELECT d.ProductID, p.ProductCode, p.ProductName, d.SerialNumber, d.Location, d.Condition " +
                "FROM IMPORT_RECEIPT_DETAILS d JOIN PRODUCTS p ON p.ProductID=d.ProductID WHERE d.ImportReceiptID=? ORDER BY p.ProductCode, d.SerialNumber";
        try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.setInt(1, receiptId); try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { ImportReceiptItem item = new ImportReceiptItem(); item.setProductId(rs.getInt("ProductID"));
                item.setProductCode(rs.getString("ProductCode")); item.setProductName(rs.getString("ProductName"));
                item.setSerialNumber(rs.getString("SerialNumber")); item.setLocation(rs.getString("Location"));
                item.setCondition(rs.getString("Condition")); items.add(item); }
        } }
        return items;
    }

    private List<ImportRequestItem> findRequestItems(Connection conn, int requestId) throws SQLException {
        List<ImportRequestItem> items = new ArrayList<>();
        String sql = "SELECT d.ProductID, p.ProductCode, p.ProductName, d.Quantity, d.UnitPrice " +
                "FROM IMPORT_REQUEST_DETAILS d JOIN PRODUCTS p ON p.ProductID=d.ProductID " +
                "WHERE d.ImportRequestID=? ORDER BY d.ImportRequestDetailID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportRequestItem item = new ImportRequestItem(); item.setProductId(rs.getInt("ProductID"));
                    item.setProductCode(rs.getString("ProductCode")); item.setProductName(rs.getString("ProductName"));
                    item.setQuantity(rs.getInt("Quantity")); item.setUnitPrice(rs.getBigDecimal("UnitPrice")); items.add(item);
                }
            }
        }
        return items;
    }
}
