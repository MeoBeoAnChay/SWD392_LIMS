package com.lims.dao;

import com.lims.model.Handover;
import com.lims.model.ImportReceipt;
import com.lims.model.InventoryItem;
import com.lims.model.ImportReceiptItem;
import com.lims.model.ImportRequestItem;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
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
                "(SELECT TOP 1 r.ImportReceiptID FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID ORDER BY r.ImportReceiptID DESC) AS ImportReceiptID, " +
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
                handover.setRejectReason(rs.getString("RejectReason")); int irId = rs.getInt("ImportReceiptID"); if (irId > 0) handover.setImportReceiptId(irId); handovers.add(handover);
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

    public List<ImportReceipt> searchImportReceipts(int userId, String serial, String status, String keyword) {
        List<ImportReceipt> receipts = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT ir.ImportReceiptID, ir.ImportRequestID, ir.HandoverID, ir.UserID, ir.ReceiptDate, ir.Status, ir.Note, " +
                "ir.ApprovedBy, ir.ApprovedDate, ir.CancelReason, ir.PaperSigned, ir.PaperSignedAt, " +
                "s.SupplierName, w.FullName AS WarehouseStaffName, m.FullName AS ManagerName, h.HandoverLocation " +
                "FROM IMPORT_RECEIPTS ir " +
                "JOIN HANDOVERS h ON h.HandoverID=ir.HandoverID " +
                "JOIN IMPORT_REQUESTS req ON req.ImportRequestID=ir.ImportRequestID " +
                "JOIN SUPPLIERS s ON s.SupplierID=req.SupplierID " +
                "JOIN USERS w ON w.UserID=ir.UserID " +
                "LEFT JOIN USERS m ON m.UserID=ir.ApprovedBy " +
                "WHERE (ir.UserID=? OR h.ReceivedBy=?) "
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND ir.Status=? ");
            params.add(status.trim());
        }
        if (serial != null && !serial.isBlank()) {
            sql.append("AND EXISTS (SELECT 1 FROM IMPORT_RECEIPT_DETAILS ird WHERE ird.ImportReceiptID=ir.ImportReceiptID AND ird.SerialNumber LIKE ?) ");
            params.add("%" + serial.trim() + "%");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (s.SupplierName LIKE ? OR CAST(ir.ImportReceiptID AS NVARCHAR) LIKE ? OR CAST(ir.ImportRequestID AS NVARCHAR) LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append("ORDER BY ir.ReceiptDate DESC, ir.ImportReceiptID DESC");

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportReceipt r = new ImportReceipt();
                    r.setReceiptId(rs.getInt("ImportReceiptID"));
                    r.setRequestId(rs.getInt("ImportRequestID"));
                    r.setHandoverId(rs.getInt("HandoverID"));
                    r.setWarehouseStaffName(rs.getString("WarehouseStaffName"));
                    r.setSupplierName(rs.getString("SupplierName"));
                    r.setManagerName(rs.getString("ManagerName"));
                    r.setReceiptDate(rs.getTimestamp("ReceiptDate"));
                    r.setApprovedDate(rs.getTimestamp("ApprovedDate"));
                    r.setStatus(rs.getString("Status"));
                    r.setNote(rs.getString("Note"));
                    r.setCancelReason(rs.getString("CancelReason"));
                    r.setPaperSigned(rs.getBoolean("PaperSigned"));
                    receipts.add(r);
                }
            }
            for (ImportReceipt r : receipts) {
                r.setItems(findImportReceiptItems(conn, r.getReceiptId()));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to search import receipts", e);
        }
        return receipts;
    }

    public List<ImportReceiptItem> findImportReceiptItems(Connection conn, int receiptId) throws SQLException {
        List<ImportReceiptItem> items = new ArrayList<>();
        String sql = "SELECT ird.ProductID, p.ProductCode, p.ProductName, ird.SerialNumber, ird.Location, ird.Condition " +
                     "FROM IMPORT_RECEIPT_DETAILS ird " +
                     "JOIN PRODUCTS p ON p.ProductID=ird.ProductID " +
                     "WHERE ird.ImportReceiptID=? ORDER BY ird.ImportReceiptDetailID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportReceiptItem item = new ImportReceiptItem();
                    item.setProductId(rs.getInt("ProductID"));
                    item.setProductCode(rs.getString("ProductCode"));
                    item.setProductName(rs.getString("ProductName"));
                    item.setSerialNumber(rs.getString("SerialNumber"));
                    item.setLocation(rs.getString("Location"));
                    item.setCondition(rs.getString("Condition"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public ImportReceipt findImportReceiptById(int receiptId, int userId) {
        String sql = "SELECT ir.ImportReceiptID, ir.ImportRequestID, ir.HandoverID, ir.UserID, ir.ReceiptDate, ir.Status, ir.Note, " +
                "ir.ApprovedBy, ir.ApprovedDate, ir.CancelReason, ir.PaperSigned, ir.PaperSignedAt, " +
                "s.SupplierName, w.FullName AS WarehouseStaffName, m.FullName AS ManagerName, " +
                "h.HandoverBy, h.HandoverDate, h.HandoverLocation " +
                "FROM IMPORT_RECEIPTS ir " +
                "JOIN HANDOVERS h ON h.HandoverID=ir.HandoverID " +
                "JOIN IMPORT_REQUESTS req ON req.ImportRequestID=ir.ImportRequestID " +
                "JOIN SUPPLIERS s ON s.SupplierID=req.SupplierID " +
                "JOIN USERS w ON w.UserID=ir.UserID " +
                "LEFT JOIN USERS m ON m.UserID=ir.ApprovedBy " +
                "WHERE ir.ImportReceiptID=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, receiptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ImportReceipt r = new ImportReceipt();
                    r.setReceiptId(rs.getInt("ImportReceiptID"));
                    r.setRequestId(rs.getInt("ImportRequestID"));
                    r.setHandoverId(rs.getInt("HandoverID"));
                    r.setWarehouseStaffName(rs.getString("WarehouseStaffName"));
                    r.setSupplierName(rs.getString("SupplierName"));
                    r.setManagerName(rs.getString("ManagerName"));
                    r.setReceiptDate(rs.getTimestamp("ReceiptDate"));
                    r.setApprovedDate(rs.getTimestamp("ApprovedDate"));
                    r.setStatus(rs.getString("Status"));
                    r.setNote(rs.getString("Note"));
                    r.setCancelReason(rs.getString("CancelReason"));
                    r.setPaperSigned(rs.getBoolean("PaperSigned"));
                    r.setItems(findImportReceiptItems(conn, receiptId));
                    return r;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to find import receipt " + receiptId, e);
        }
        return null;
    }

    public List<InventoryItem> searchInventory(String serial, String location, String status, String keyword, int productId) {
        List<InventoryItem> list = executeInventoryQuery(serial, location, status, keyword, productId);
        // Smart fallback: If specific serial was searched, but strict filter returned 0 items,
        // search by serial alone so user can see the laptop and its actual status!
        if (list.isEmpty() && serial != null && !serial.isBlank()) {
            List<InventoryItem> fallback = executeInventoryQuery(serial, null, null, null, 0);
            if (!fallback.isEmpty()) {
                return fallback;
            }
        }
        return list;
    }

    public List<InventoryItem> searchInventory(String serial, String location, String status, String keyword) {
        return searchInventory(serial, location, status, keyword, 0);
    }

    private List<InventoryItem> executeInventoryQuery(String serial, String location, String status, String keyword, int productId) {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT i.InventoryID, i.ProductID, i.SerialNumber, i.Location, i.Status, i.UpdatedAt, " +
                "p.ProductCode, p.ProductName, p.UnitPrice, p.CPU, p.RAM, p.Storage, p.GPU, p.Screen, " +
                "b.BrandName, pl.LineName " +
                "FROM INVENTORY i " +
                "JOIN PRODUCTS p ON p.ProductID=i.ProductID " +
                "LEFT JOIN PRODUCT_LINES pl ON pl.ProductLineID=p.ProductLineID " +
                "LEFT JOIN BRANDS b ON b.BrandID=pl.BrandID " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();
        if (productId > 0) {
            sql.append("AND i.ProductID = ? ");
            params.add(productId);
        }
        if (serial != null && !serial.isBlank()) {
            sql.append("AND i.SerialNumber LIKE ? ");
            params.add("%" + serial.trim() + "%");
        }
        if (location != null && !location.isBlank() && !"ALL".equalsIgnoreCase(location)) {
            sql.append("AND i.Location = ? ");
            params.add(location.trim());
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append("AND i.Status = ? ");
            params.add(status.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (p.ProductName LIKE ? OR p.ProductCode LIKE ? OR b.BrandName LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        sql.append("ORDER BY i.UpdatedAt DESC, i.InventoryID DESC");

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryItem item = new InventoryItem();
                    item.setInventoryId(rs.getInt("InventoryID"));
                    item.setProductId(rs.getInt("ProductID"));
                    item.setSerialNumber(rs.getString("SerialNumber"));
                    item.setLocation(rs.getString("Location"));
                    item.setStatus(rs.getString("Status"));
                    item.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    item.setProductCode(rs.getString("ProductCode"));
                    item.setProductName(rs.getString("ProductName"));
                    item.setUnitPrice(rs.getBigDecimal("UnitPrice"));
                    item.setCpu(rs.getString("CPU"));
                    item.setRam(rs.getString("RAM"));
                    item.setStorage(rs.getString("Storage"));
                    item.setGpu(rs.getString("GPU"));
                    item.setScreen(rs.getString("Screen"));
                    item.setBrandName(rs.getString("BrandName"));
                    item.setLineName(rs.getString("LineName"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to execute inventory query", e);
        }
        return list;
    }

    public InventoryItem findInventoryItemById(int inventoryId) {
        String sql = "SELECT i.InventoryID, i.ProductID, i.SerialNumber, i.Location, i.Status, i.UpdatedAt, " +
                "p.ProductCode, p.ProductName, p.UnitPrice, p.CPU, p.RAM, p.Storage, p.GPU, p.Screen, " +
                "b.BrandName, pl.LineName " +
                "FROM INVENTORY i " +
                "JOIN PRODUCTS p ON p.ProductID=i.ProductID " +
                "LEFT JOIN PRODUCT_LINES pl ON pl.ProductLineID=p.ProductLineID " +
                "LEFT JOIN BRANDS b ON b.BrandID=pl.BrandID " +
                "WHERE i.InventoryID=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InventoryItem item = new InventoryItem();
                    item.setInventoryId(rs.getInt("InventoryID"));
                    item.setProductId(rs.getInt("ProductID"));
                    item.setSerialNumber(rs.getString("SerialNumber"));
                    item.setLocation(rs.getString("Location"));
                    item.setStatus(rs.getString("Status"));
                    item.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                    item.setProductCode(rs.getString("ProductCode"));
                    item.setProductName(rs.getString("ProductName"));
                    item.setUnitPrice(rs.getBigDecimal("UnitPrice"));
                    item.setCpu(rs.getString("CPU"));
                    item.setRam(rs.getString("RAM"));
                    item.setStorage(rs.getString("Storage"));
                    item.setGpu(rs.getString("GPU"));
                    item.setScreen(rs.getString("Screen"));
                    item.setBrandName(rs.getString("BrandName"));
                    item.setLineName(rs.getString("LineName"));
                    return item;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to find inventory item " + inventoryId, e);
        }
        return null;
    }

    public boolean updatePhysicalItemCondition(int inventoryId, String newStatus, String newLocation) {
        String sql = "UPDATE INVENTORY SET Status=?, Location=?, UpdatedAt=GETDATE() WHERE InventoryID=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, newLocation);
            ps.setInt(3, inventoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to update physical item condition " + inventoryId, e);
            return false;
        }
    }

    public List<String> findAllWarehouseLocations() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT LocationName FROM WAREHOUSE_LOCATIONS ORDER BY LocationName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("LocationName"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to fetch warehouse locations", e);
        }
        return list;
    }

    public Map<String, Object> getWarehouseInventoryMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        try (Connection conn = DBContext.getConnection()) {
            String statusSql = "SELECT Status, COUNT(*) FROM INVENTORY GROUP BY Status";
            int totalInStock = 0, totalReserved = 0, totalDamaged = 0, totalReturned = 0, totalExported = 0;
            try (PreparedStatement ps = conn.prepareStatement(statusSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String st = rs.getString(1);
                    int count = rs.getInt(2);
                    if ("IN_STOCK".equalsIgnoreCase(st)) totalInStock = count;
                    else if ("RESERVED".equalsIgnoreCase(st)) totalReserved = count;
                    else if ("DAMAGED".equalsIgnoreCase(st)) totalDamaged = count;
                    else if ("RETURNED".equalsIgnoreCase(st)) totalReturned = count;
                    else if ("EXPORTED".equalsIgnoreCase(st)) totalExported = count;
                }
            }
            metrics.put("totalInStock", totalInStock);
            metrics.put("totalReserved", totalReserved);
            metrics.put("totalDamaged", totalDamaged);
            metrics.put("totalReturned", totalReturned);
            metrics.put("totalExported", totalExported);
            metrics.put("totalPhysicalStock", totalInStock + totalReserved + totalDamaged + totalReturned);

            String locSql = "SELECT COUNT(DISTINCT Location) FROM INVENTORY WHERE Status IN ('IN_STOCK', 'RESERVED', 'DAMAGED', 'RETURNED')";
            try (PreparedStatement ps = conn.prepareStatement(locSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) metrics.put("occupiedLocations", rs.getInt(1));
            }
            String totalLocSql = "SELECT COUNT(*) FROM WAREHOUSE_LOCATIONS";
            try (PreparedStatement ps = conn.prepareStatement(totalLocSql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) metrics.put("totalLocations", rs.getInt(1));
            }

            String brandSql = "SELECT ISNULL(b.BrandName, 'Khác') AS BrandName, COUNT(*) AS Qty " +
                    "FROM INVENTORY i " +
                    "JOIN PRODUCTS p ON p.ProductID=i.ProductID " +
                    "LEFT JOIN PRODUCT_LINES pl ON pl.ProductLineID=p.ProductLineID " +
                    "LEFT JOIN BRANDS b ON b.BrandID=pl.BrandID " +
                    "WHERE i.Status='IN_STOCK' GROUP BY b.BrandName ORDER BY Qty DESC";
            Map<String, Integer> brandStock = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(brandSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    brandStock.put(rs.getString("BrandName"), rs.getInt("Qty"));
                }
            }
            metrics.put("brandStock", brandStock);

            String lowStockSql = "SELECT p.ProductID, p.ProductCode, p.ProductName, " +
                    "SUM(CASE WHEN i.Status='IN_STOCK' THEN 1 ELSE 0 END) AS AvailableQty " +
                    "FROM PRODUCTS p " +
                    "LEFT JOIN INVENTORY i ON i.ProductID=p.ProductID " +
                    "WHERE p.Status='ACTIVE' " +
                    "GROUP BY p.ProductID, p.ProductCode, p.ProductName " +
                    "HAVING SUM(CASE WHEN i.Status='IN_STOCK' THEN 1 ELSE 0 END) <= 3 " +
                    "ORDER BY AvailableQty ASC";
            List<Map<String, Object>> lowStockList = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(lowStockSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productCode", rs.getString("ProductCode"));
                    item.put("productName", rs.getString("ProductName"));
                    item.put("availableQty", rs.getInt("AvailableQty"));
                    lowStockList.add(item);
                }
            }
            metrics.put("lowStockList", lowStockList);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to compute warehouse inventory metrics", e);
        }
        return metrics;
    }

}
