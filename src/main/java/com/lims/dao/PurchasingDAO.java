package com.lims.dao;

import com.lims.model.Handover;
import com.lims.model.ImportRequest;
import com.lims.model.ImportRequestItem;
import com.lims.model.ProductOption;
import com.lims.model.SupplierOption;
import com.lims.model.Supplier;
import com.lims.model.User;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurchasingDAO {
    private static final Logger LOGGER = Logger.getLogger(PurchasingDAO.class.getName());

    public boolean isActivePurchasingStaff(int userId) {
        String sql = "SELECT COUNT(*) FROM USERS u JOIN USER_ROLES ur ON ur.UserID=u.UserID JOIN ROLES r ON r.RoleID=ur.RoleID " +
                "WHERE u.UserID=? AND u.Status='ACTIVE' AND LOWER(r.RoleName)=LOWER(?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setString(2, "Purchasing Staff");
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to verify Purchasing Staff access", e);
            return false;
        }
    }

    public List<SupplierOption> findActiveSuppliers() {
        List<SupplierOption> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, SupplierName FROM SUPPLIERS WHERE Status='ACTIVE' ORDER BY SupplierName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) suppliers.add(new SupplierOption(rs.getInt("SupplierID"), rs.getString("SupplierName")));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list active suppliers", e); }
        return suppliers;
    }

    public List<Supplier> findAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT SupplierID, SupplierName, ContactName, Email, Phone, Address, Status FROM SUPPLIERS ORDER BY SupplierName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) suppliers.add(readSupplier(rs));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list suppliers", e); }
        return suppliers;
    }

    public Supplier findSupplier(int supplierId) {
        String sql = "SELECT SupplierID, SupplierName, ContactName, Email, Phone, Address, Status FROM SUPPLIERS WHERE SupplierID=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? readSupplier(rs) : null; }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to find supplier " + supplierId, e); return null; }
    }

    public boolean createSupplier(Supplier supplier) {
        String sql = "INSERT INTO SUPPLIERS (SupplierName, ContactName, Email, Phone, Address, Status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSupplier(ps, supplier); return ps.executeUpdate() == 1;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to create supplier", e); return false; }
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE SUPPLIERS SET SupplierName=?, ContactName=?, Email=?, Phone=?, Address=?, Status=? WHERE SupplierID=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSupplier(ps, supplier); ps.setInt(7, supplier.getSupplierId()); return ps.executeUpdate() == 1;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to update supplier " + supplier.getSupplierId(), e); return false; }
    }

    public Map<String, Integer> getRequestCounts(int userId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT Status, COUNT(*) AS Total FROM IMPORT_REQUESTS WHERE CreatedBy=? GROUP BY Status";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) counts.put(rs.getString("Status"), rs.getInt("Total")); }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to count import requests", e); }
        return counts;
    }

    public int[] getHandoverProgress(int userId) {
        String sql = "SELECT (SELECT COUNT(*) FROM HANDOVERS h JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID WHERE ir.CreatedBy=?) AS Total, " +
                "(SELECT COUNT(*) FROM HANDOVERS h JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID WHERE ir.CreatedBy=? " +
                "AND EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='COMPLETED')) AS Received";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, userId); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new int[]{rs.getInt("Total"), rs.getInt("Received")}; }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to count handover progress", e); }
        return new int[]{0, 0};
    }

    public List<Handover> findHandoversByCreator(int userId) {
        List<Handover> handovers = new ArrayList<>();
        String sql = "SELECT h.HandoverID, h.ImportRequestID, h.HandoverBy, h.HandoverDate, h.HandoverQuantity, h.Note, h.HandoverLocation, h.PackageCount, " +
                "h.ReceivedBy, receiver.FullName AS ReceiverName, s.SupplierName, " +
                "(SELECT COUNT(*) FROM IMPORT_RECEIPTS r JOIN IMPORT_RECEIPT_DETAILS d ON d.ImportReceiptID=r.ImportReceiptID WHERE r.HandoverID=h.HandoverID AND r.Status='COMPLETED') AS ReceivedQuantity, " +
                "CASE WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='COMPLETED') THEN 'COMPLETED' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='PENDING_MANAGER') THEN 'PENDING_APPROVAL' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID AND r.Status='REJECTED') THEN 'REJECTED' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS r WHERE r.HandoverID=h.HandoverID) THEN 'PROCESSING' ELSE 'WAITING' END AS ReceiptStatus " +
                "FROM HANDOVERS h JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID " +
                "LEFT JOIN USERS receiver ON receiver.UserID=h.ReceivedBy " +
                "WHERE ir.CreatedBy=? ORDER BY h.HandoverDate DESC, h.HandoverID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Handover handover = new Handover(); handover.setHandoverId(rs.getInt("HandoverID"));
                    handover.setRequestId(rs.getInt("ImportRequestID")); handover.setHandoverBy(rs.getString("HandoverBy"));
                    handover.setHandoverDate(rs.getTimestamp("HandoverDate")); handover.setHandoverQuantity(rs.getInt("HandoverQuantity"));
                    handover.setNote(rs.getString("Note")); handover.setSupplierName(rs.getString("SupplierName"));
                    handover.setLocation(rs.getString("HandoverLocation")); handover.setPackageCount((Integer) rs.getObject("PackageCount"));
                    handover.setReceiverId(rs.getInt("ReceivedBy")); handover.setReceiverName(rs.getString("ReceiverName"));
                    handover.setReceivedQuantity(rs.getInt("ReceivedQuantity")); handover.setReceiptStatus(rs.getString("ReceiptStatus"));
                    handovers.add(handover);
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list handovers", e); }
        return handovers;
    }

    public List<User> findActiveWarehouseStaff() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT DISTINCT u.UserID, u.Username, u.FullName FROM USERS u " +
                "JOIN USER_ROLES ur ON ur.UserID=u.UserID JOIN ROLES r ON r.RoleID=ur.RoleID " +
                "WHERE u.Status='ACTIVE' AND LOWER(r.RoleName)=LOWER('Warehouse Staff') ORDER BY u.FullName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { User user = new User(); user.setUserId(rs.getInt("UserID")); user.setUsername(rs.getString("Username")); user.setFullName(rs.getString("FullName")); users.add(user); }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list active warehouse staff", e); }
        return users;
    }

    public List<ImportRequestItem> findAvailableHandoverItems(int requestId) {
        List<ImportRequestItem> items = new ArrayList<>();
        String sql = "SELECT d.ProductID, p.ProductCode, p.ProductName, d.Quantity AS OrderedQuantity, " +
                "d.Quantity-COALESCE((SELECT SUM(hd.Quantity) FROM HANDOVER_DETAILS hd JOIN HANDOVERS h ON h.HandoverID=hd.HandoverID " +
                "WHERE h.ImportRequestID=d.ImportRequestID AND hd.ProductID=d.ProductID),0) AS AvailableQuantity " +
                "FROM IMPORT_REQUEST_DETAILS d JOIN PRODUCTS p ON p.ProductID=d.ProductID WHERE d.ImportRequestID=? ORDER BY d.ImportRequestDetailID";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) {
                ImportRequestItem item = new ImportRequestItem(); item.setProductId(rs.getInt("ProductID"));
                item.setProductCode(rs.getString("ProductCode")); item.setProductName(rs.getString("ProductName"));
                item.setQuantity(rs.getInt("OrderedQuantity")); item.setHandoverRemainingQuantity(rs.getInt("AvailableQuantity")); items.add(item);
            } }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list available handover items", e); }
        return items;
    }

    public Handover findHandoverByCreator(int userId, int handoverId) {
        String sql = "SELECT h.HandoverID, h.ImportRequestID, h.HandoverBy, h.HandoverDate, h.HandoverQuantity, h.Note, " +
                "h.HandoverLocation, h.PackageCount, h.ReceivedBy, h.GoodsIntact, h.QuantityDiscrepancy, h.Damaged, h.SerialDiscrepancy, " +
                "receiver.FullName AS ReceiverName, s.SupplierName, " +
                "(SELECT TOP 1 irc.ImportReceiptID FROM IMPORT_RECEIPTS irc WHERE irc.HandoverID=h.HandoverID ORDER BY irc.ReceiptDate DESC) AS ImportReceiptID, " +
                "CASE WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS irc WHERE irc.HandoverID=h.HandoverID AND irc.Status='COMPLETED') THEN 'COMPLETED' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS irc WHERE irc.HandoverID=h.HandoverID AND irc.Status='PENDING_MANAGER') THEN 'PENDING_APPROVAL' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS irc WHERE irc.HandoverID=h.HandoverID AND irc.Status='REJECTED') THEN 'REJECTED' " +
                "WHEN EXISTS (SELECT 1 FROM IMPORT_RECEIPTS irc WHERE irc.HandoverID=h.HandoverID) THEN 'PROCESSING' ELSE 'WAITING' END AS ReceiptStatus " +
                "FROM HANDOVERS h JOIN IMPORT_REQUESTS ir ON ir.ImportRequestID=h.ImportRequestID JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID " +
                "LEFT JOIN USERS receiver ON receiver.UserID=h.ReceivedBy WHERE h.HandoverID=? AND ir.CreatedBy=?";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, handoverId); ps.setInt(2, userId); try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Handover handover = new Handover(); handover.setHandoverId(rs.getInt("HandoverID")); handover.setRequestId(rs.getInt("ImportRequestID"));
                handover.setHandoverBy(rs.getString("HandoverBy")); handover.setHandoverDate(rs.getTimestamp("HandoverDate"));
                handover.setHandoverQuantity(rs.getInt("HandoverQuantity")); handover.setNote(rs.getString("Note"));
                handover.setLocation(rs.getString("HandoverLocation")); handover.setPackageCount((Integer) rs.getObject("PackageCount"));
                handover.setReceiverId(rs.getInt("ReceivedBy")); handover.setReceiverName(rs.getString("ReceiverName"));
                handover.setGoodsIntact(rs.getBoolean("GoodsIntact")); handover.setQuantityDiscrepancy(rs.getBoolean("QuantityDiscrepancy"));
                handover.setDamaged(rs.getBoolean("Damaged")); handover.setSerialDiscrepancy(rs.getBoolean("SerialDiscrepancy"));
                int receiptId = rs.getInt("ImportReceiptID"); if (!rs.wasNull()) handover.setImportReceiptId(receiptId);
                handover.setReceiptStatus(rs.getString("ReceiptStatus"));
                handover.setSupplierName(rs.getString("SupplierName")); handover.setItems(findHandoverItems(conn, handoverId));
                return handover;
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to load handover " + handoverId, e); return null; }
    }

    private List<ImportRequestItem> findHandoverItems(Connection conn, int handoverId) throws SQLException {
        List<ImportRequestItem> items = new ArrayList<>();
        String sql = "SELECT hd.ProductID, p.ProductCode, p.ProductName, hd.Quantity, hd.Note FROM HANDOVER_DETAILS hd JOIN PRODUCTS p ON p.ProductID=hd.ProductID WHERE hd.HandoverID=? ORDER BY hd.HandoverDetailID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.setInt(1, handoverId); try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { ImportRequestItem item = new ImportRequestItem(); item.setProductId(rs.getInt("ProductID")); item.setProductCode(rs.getString("ProductCode")); item.setProductName(rs.getString("ProductName")); item.setQuantity(rs.getInt("Quantity")); items.add(item); }
        } }
        return items;
    }

    public List<ProductOption> findActiveProducts() {
        List<ProductOption> products = new ArrayList<>();
        String sql = "SELECT p.ProductID,p.ProductCode,p.ProductName,p.UnitPrice FROM PRODUCTS p JOIN PRODUCT_LINES l ON l.ProductLineID=p.ProductLineID JOIN BRANDS b ON b.BrandID=l.BrandID WHERE p.Status='ACTIVE' AND l.Status='ACTIVE' AND b.Status='ACTIVE' ORDER BY p.ProductName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) products.add(new ProductOption(rs.getInt("ProductID"), rs.getString("ProductCode"),
                    rs.getString("ProductName"), rs.getBigDecimal("UnitPrice")));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list active products", e); }
        return products;
    }

    public List<ImportRequest> findRequestsByCreator(int userId) {
        return findRequestsByCreator(userId, "", "", null, null);
    }

    public List<ImportRequest> findRequestsByCreator(int userId, String status, String search, Date fromDate, Date toDate) {
        List<ImportRequest> requests = new ArrayList<>();
        String sql = "SELECT ir.ImportRequestID, ir.SupplierID, s.SupplierName, ir.RequestDate, ir.Status, ir.CancelReason " +
                "FROM IMPORT_REQUESTS ir JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID WHERE ir.CreatedBy=? " +
                "AND (?='' OR UPPER(ir.Status)=UPPER(?)) AND (?='' OR CAST(ir.ImportRequestID AS NVARCHAR(20)) LIKE ? OR s.SupplierName LIKE ?) " +
                "AND (? IS NULL OR ir.RequestDate>=?) AND (? IS NULL OR ir.RequestDate<DATEADD(DAY,1,?)) ORDER BY ir.RequestDate DESC, ir.ImportRequestID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String cleanStatus = status == null ? "" : status.trim();
            String cleanSearch = search == null ? "" : search.trim();
            ps.setInt(1, userId); ps.setString(2, cleanStatus); ps.setString(3, cleanStatus);
            ps.setString(4, cleanSearch); ps.setString(5, "%" + cleanSearch + "%"); ps.setString(6, "%" + cleanSearch + "%");
            if (fromDate == null) { ps.setNull(7, java.sql.Types.DATE); ps.setNull(8, java.sql.Types.DATE); }
            else { ps.setDate(7, fromDate); ps.setDate(8, fromDate); }
            if (toDate == null) { ps.setNull(9, java.sql.Types.DATE); ps.setNull(10, java.sql.Types.DATE); }
            else { ps.setDate(9, toDate); ps.setDate(10, toDate); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportRequest request = new ImportRequest();
                    request.setRequestId(rs.getInt("ImportRequestID"));
                    request.setSupplierId(rs.getInt("SupplierID"));
                    request.setSupplierName(rs.getString("SupplierName"));
                    request.setRequestDate(rs.getTimestamp("RequestDate"));
                    request.setStatus(rs.getString("Status"));
                    request.setCancelReason(rs.getString("CancelReason"));
                    requests.add(request);
                }
            }
            for (ImportRequest request : requests) {
                request.setItems(findItems(conn, request.getRequestId()));
                request.setHandovers(findHandovers(conn, request.getRequestId()));
                request.setOrderedQuantity(request.getItems().stream().mapToInt(ImportRequestItem::getQuantity).sum());
                request.setHandedOverQuantity(request.getHandovers().stream().mapToInt(Handover::getHandoverQuantity).sum());
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list import requests", e); }
        return requests;
    }

    public int createRequest(int creatorId, int supplierId, Map<Integer, ImportRequestItem> items) {
        String sql = "INSERT INTO IMPORT_REQUESTS (SupplierID, CreatedBy, Status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            requireActiveSupplier(conn, supplierId);
            int requestId;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, supplierId); ps.setInt(2, creatorId); ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Import request ID was not generated");
                    requestId = keys.getInt(1);
                }
            }
            insertItems(conn, requestId, items);
            conn.commit();
            return requestId;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to create import request", e); return -1; }
    }

    public boolean updatePendingRequest(int requestId, int creatorId, int supplierId, Map<Integer, ImportRequestItem> items) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            lockPendingRequest(conn, requestId, creatorId);
            requireActiveSupplier(conn, supplierId);
            try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPORT_REQUESTS SET SupplierID=? WHERE ImportRequestID=? AND CreatedBy=? AND Status='PENDING'")) {
                ps.setInt(1, supplierId); ps.setInt(2, requestId); ps.setInt(3, creatorId);
                if (ps.executeUpdate() != 1) throw new SQLException("Request is no longer pending");
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM IMPORT_REQUEST_DETAILS WHERE ImportRequestID=?")) {
                ps.setInt(1, requestId); ps.executeUpdate();
            }
            insertItems(conn, requestId, items);
            conn.commit();
            return true;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to update import request " + requestId, e); return false; }
    }

    public boolean cancelPendingRequest(int requestId, int creatorId, String reason) {
        String sql = "UPDATE IMPORT_REQUESTS SET Status='CANCELLED', CancelReason=? WHERE ImportRequestID=? AND CreatedBy=? AND Status='PENDING'";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason); ps.setInt(2, requestId); ps.setInt(3, creatorId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to cancel import request " + requestId, e); return false; }
    }

    public int createHandover(int requestId, int creatorId, String handoverBy, int receiverId, String location, int packageCount,
                              Map<Integer, Integer> quantities, boolean goodsIntact, boolean quantityDiscrepancy, boolean damaged,
                              boolean serialDiscrepancy, String note) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement("SELECT Status FROM IMPORT_REQUESTS WITH (UPDLOCK, HOLDLOCK) WHERE ImportRequestID=? AND CreatedBy=?")) {
                ps.setInt(1, requestId); ps.setInt(2, creatorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"APPROVED".equalsIgnoreCase(rs.getString("Status"))) throw new SQLException("Only approved requests may be handed over");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM USERS u JOIN USER_ROLES ur ON ur.UserID=u.UserID JOIN ROLES r ON r.RoleID=ur.RoleID WHERE u.UserID=? AND u.Status='ACTIVE' AND LOWER(r.RoleName)=LOWER('Warehouse Staff')")) {
                ps.setInt(1, receiverId); try (ResultSet rs = ps.executeQuery()) { if (!rs.next() || rs.getInt(1) == 0) throw new SQLException("Selected receiver is not an active Warehouse Staff member"); }
            }
            Map<Integer, Integer> available = new LinkedHashMap<>();
            try (PreparedStatement ps = conn.prepareStatement("SELECT d.ProductID, d.Quantity-COALESCE((SELECT SUM(hd.Quantity) FROM HANDOVER_DETAILS hd JOIN HANDOVERS h ON h.HandoverID=hd.HandoverID WHERE h.ImportRequestID=d.ImportRequestID AND hd.ProductID=d.ProductID),0) AS AvailableQuantity FROM IMPORT_REQUEST_DETAILS d WHERE d.ImportRequestID=?")) {
                ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) available.put(rs.getInt("ProductID"), rs.getInt("AvailableQuantity")); }
            }
            int totalQuantity = 0;
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                int requested = entry.getValue() == null ? 0 : entry.getValue();
                if (requested < 0 || requested > available.getOrDefault(entry.getKey(), -1)) throw new SQLException("Handover item quantity is invalid or exceeds remaining quantity");
                totalQuantity += requested;
            }
            if (totalQuantity < 1) throw new SQLException("Handover quantity must be positive");
            int handoverId;
            String insert = "INSERT INTO HANDOVERS (ImportRequestID, HandoverBy, HandoverQuantity, Note, ReceivedBy, HandoverLocation, PackageCount, GoodsIntact, QuantityDiscrepancy, Damaged, SerialDiscrepancy) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, requestId); ps.setString(2, handoverBy); ps.setInt(3, totalQuantity); ps.setString(4, note);
                ps.setInt(5, receiverId); ps.setString(6, location); ps.setInt(7, packageCount); ps.setBoolean(8, goodsIntact);
                ps.setBoolean(9, quantityDiscrepancy); ps.setBoolean(10, damaged); ps.setBoolean(11, serialDiscrepancy); ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Handover ID was not generated"); handoverId = keys.getInt(1); }
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO HANDOVER_DETAILS (HandoverID, ProductID, Quantity) VALUES (?, ?, ?)")) {
                for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) if (entry.getValue() > 0) {
                    ps.setInt(1, handoverId); ps.setInt(2, entry.getKey()); ps.setInt(3, entry.getValue()); ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            return handoverId;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to create handover for request " + requestId, e); return -1; }
    }

    private List<ImportRequestItem> findItems(Connection conn, int requestId) throws SQLException {
        List<ImportRequestItem> items = new ArrayList<>();
        String sql = "SELECT d.ProductID, p.ProductCode, p.ProductName, d.Quantity, d.UnitPrice FROM IMPORT_REQUEST_DETAILS d JOIN PRODUCTS p ON p.ProductID=d.ProductID WHERE d.ImportRequestID=? ORDER BY d.ImportRequestDetailID";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportRequestItem item = new ImportRequestItem();
                    item.setProductId(rs.getInt("ProductID")); item.setProductCode(rs.getString("ProductCode"));
                    item.setProductName(rs.getString("ProductName")); item.setQuantity(rs.getInt("Quantity"));
                    item.setUnitPrice(rs.getBigDecimal("UnitPrice")); items.add(item);
                }
            }
        }
        return items;
    }

    private List<Handover> findHandovers(Connection conn, int requestId) throws SQLException {
        List<Handover> handovers = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT HandoverID, HandoverBy, HandoverDate, HandoverQuantity, Note FROM HANDOVERS WHERE ImportRequestID=? ORDER BY HandoverDate DESC")) {
            ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Handover handover = new Handover(); handover.setHandoverId(rs.getInt("HandoverID"));
                    handover.setHandoverBy(rs.getString("HandoverBy")); handover.setHandoverDate(rs.getTimestamp("HandoverDate"));
                    handover.setHandoverQuantity(rs.getInt("HandoverQuantity")); handover.setNote(rs.getString("Note"));
                    handovers.add(handover);
                }
            }
        }
        return handovers;
    }

    private void requireActiveSupplier(Connection conn, int supplierId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM SUPPLIERS WHERE SupplierID=? AND Status='ACTIVE'")) {
            ps.setInt(1, supplierId); try (ResultSet rs = ps.executeQuery()) { if (!rs.next() || rs.getInt(1) != 1) throw new SQLException("Supplier is not active"); }
        }
    }

    private Supplier readSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier(); supplier.setSupplierId(rs.getInt("SupplierID"));
        supplier.setSupplierName(rs.getString("SupplierName")); supplier.setContactName(rs.getString("ContactName"));
        supplier.setEmail(rs.getString("Email")); supplier.setPhone(rs.getString("Phone"));
        supplier.setAddress(rs.getString("Address")); supplier.setStatus(rs.getString("Status"));
        return supplier;
    }

    private void bindSupplier(PreparedStatement ps, Supplier supplier) throws SQLException {
        ps.setString(1, supplier.getSupplierName()); ps.setString(2, supplier.getContactName());
        ps.setString(3, supplier.getEmail()); ps.setString(4, supplier.getPhone());
        ps.setString(5, supplier.getAddress()); ps.setString(6, supplier.getStatus());
    }

    private void lockPendingRequest(Connection conn, int requestId, int creatorId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT ImportRequestID FROM IMPORT_REQUESTS WITH (UPDLOCK, HOLDLOCK) WHERE ImportRequestID=? AND CreatedBy=? AND Status='PENDING'")) {
            ps.setInt(1, requestId); ps.setInt(2, creatorId); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new SQLException("Request is not pending or not owned by user"); }
        }
    }

    private void insertItems(Connection conn, int requestId, Map<Integer, ImportRequestItem> items) throws SQLException {
        if (items == null || items.isEmpty()) throw new SQLException("Request must contain at least one product");
        String productSql = "SELECT ProductID FROM PRODUCTS WHERE ProductID=? AND Status='ACTIVE'";
        String insertSql = "INSERT INTO IMPORT_REQUEST_DETAILS (ImportRequestID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
        try (PreparedStatement productPs = conn.prepareStatement(productSql); PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
            for (Map.Entry<Integer, ImportRequestItem> entry : items.entrySet()) {
                productPs.setInt(1, entry.getKey());
                try (ResultSet rs = productPs.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Product is not active: " + entry.getKey());
                }
                insertPs.setInt(1, requestId); insertPs.setInt(2, entry.getKey());
                insertPs.setInt(3, entry.getValue().getQuantity()); insertPs.setBigDecimal(4, entry.getValue().getUnitPrice());
                insertPs.executeUpdate();
            }
        }
    }

    public List<String> findAllWarehouseLocations() {
        List<String> locations = new ArrayList<>();
        String sql = "SELECT LocationName FROM WAREHOUSE_LOCATIONS ORDER BY LocationName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) locations.add(rs.getString("LocationName"));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Unable to list warehouse locations", e);
        }
        return locations;
    }
}
