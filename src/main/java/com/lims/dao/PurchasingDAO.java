package com.lims.dao;

import com.lims.model.Handover;
import com.lims.model.ImportRequest;
import com.lims.model.ImportRequestItem;
import com.lims.model.ProductOption;
import com.lims.model.SupplierOption;
import com.lims.util.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    public List<ProductOption> findActiveProducts() {
        List<ProductOption> products = new ArrayList<>();
        String sql = "SELECT ProductID, ProductCode, ProductName, UnitPrice FROM PRODUCTS WHERE Status='ACTIVE' ORDER BY ProductName";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) products.add(new ProductOption(rs.getInt("ProductID"), rs.getString("ProductCode"),
                    rs.getString("ProductName"), rs.getBigDecimal("UnitPrice")));
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to list active products", e); }
        return products;
    }

    public List<ImportRequest> findRequestsByCreator(int userId) {
        List<ImportRequest> requests = new ArrayList<>();
        String sql = "SELECT ir.ImportRequestID, ir.SupplierID, s.SupplierName, ir.RequestDate, ir.Status, ir.CancelReason " +
                "FROM IMPORT_REQUESTS ir JOIN SUPPLIERS s ON s.SupplierID=ir.SupplierID WHERE ir.CreatedBy=? ORDER BY ir.RequestDate DESC, ir.ImportRequestID DESC";
        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
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

    public int createRequest(int creatorId, int supplierId, Map<Integer, Integer> quantities) {
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
            insertItems(conn, requestId, quantities);
            conn.commit();
            return requestId;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to create import request", e); return -1; }
    }

    public boolean updatePendingRequest(int requestId, int creatorId, int supplierId, Map<Integer, Integer> quantities) {
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
            insertItems(conn, requestId, quantities);
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

    public boolean createHandover(int requestId, int creatorId, String handoverBy, int quantity, String note) {
        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            int ordered;
            try (PreparedStatement ps = conn.prepareStatement("SELECT Status FROM IMPORT_REQUESTS WITH (UPDLOCK, HOLDLOCK) WHERE ImportRequestID=? AND CreatedBy=?")) {
                ps.setInt(1, requestId); ps.setInt(2, creatorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"APPROVED".equalsIgnoreCase(rs.getString("Status"))) throw new SQLException("Only approved requests may be handed over");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(Quantity), 0) FROM IMPORT_REQUEST_DETAILS WHERE ImportRequestID=?")) {
                ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) { rs.next(); ordered = rs.getInt(1); }
            }
            int handedOver;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(HandoverQuantity), 0) FROM HANDOVERS WHERE ImportRequestID=?")) {
                ps.setInt(1, requestId); try (ResultSet rs = ps.executeQuery()) { rs.next(); handedOver = rs.getInt(1); }
            }
            if (quantity > ordered - handedOver) throw new SQLException("Handover quantity exceeds remaining request quantity");
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO HANDOVERS (ImportRequestID, HandoverBy, HandoverQuantity, Note) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, requestId); ps.setString(2, handoverBy); ps.setInt(3, quantity); ps.setString(4, note); ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Unable to create handover for request " + requestId, e); return false; }
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

    private void lockPendingRequest(Connection conn, int requestId, int creatorId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT ImportRequestID FROM IMPORT_REQUESTS WITH (UPDLOCK, HOLDLOCK) WHERE ImportRequestID=? AND CreatedBy=? AND Status='PENDING'")) {
            ps.setInt(1, requestId); ps.setInt(2, creatorId); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new SQLException("Request is not pending or not owned by user"); }
        }
    }

    private void insertItems(Connection conn, int requestId, Map<Integer, Integer> quantities) throws SQLException {
        if (quantities == null || quantities.isEmpty()) throw new SQLException("Request must contain at least one product");
        String productSql = "SELECT UnitPrice FROM PRODUCTS WHERE ProductID=? AND Status='ACTIVE'";
        String insertSql = "INSERT INTO IMPORT_REQUEST_DETAILS (ImportRequestID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
        try (PreparedStatement productPs = conn.prepareStatement(productSql); PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
            for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                productPs.setInt(1, entry.getKey());
                java.math.BigDecimal unitPrice;
                try (ResultSet rs = productPs.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Product is not active: " + entry.getKey());
                    unitPrice = rs.getBigDecimal("UnitPrice");
                }
                insertPs.setInt(1, requestId); insertPs.setInt(2, entry.getKey());
                insertPs.setInt(3, entry.getValue()); insertPs.setBigDecimal(4, unitPrice);
                insertPs.executeUpdate();
            }
        }
    }
}
