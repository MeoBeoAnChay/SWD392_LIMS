package com.lims.service;

import com.lims.dao.WarehouseDAO;
import com.lims.model.Handover;
import com.lims.model.ImportReceiptItem;
import com.lims.model.ImportReceipt;
import com.lims.model.InventoryItem;
import com.lims.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WarehouseService {
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final AuditLogService auditLogService = new AuditLogService();

    public boolean isActiveWarehouseStaff(int userId) { return warehouseDAO.isActiveWarehouseStaff(userId); }
    public List<Handover> getAssignedHandovers(int userId) { return warehouseDAO.findAssignedHandovers(userId); }
    public Handover getAvailableHandover(int handoverId, int userId) { return warehouseDAO.findAvailableHandover(handoverId, userId); }

    public int createImportReceipt(User actor, String handoverValue, String[] productValues, String[] serialValues,
                                  String[] locationValues, String[] conditionValues, String note, boolean paperSigned,
                                  String ipAddress, String userAgent) {
        int handoverId;
        try { handoverId = Integer.parseInt(handoverValue); }
        catch (Exception e) { throw new IllegalArgumentException("Phiếu bàn giao không hợp lệ."); }
        Handover handover = warehouseDAO.findAvailableHandover(handoverId, actor.getUserId());
        if (handover == null) throw new IllegalArgumentException("Phiếu bàn giao không được giao cho bạn, chưa được duyệt hoặc đã gửi phiếu nhập.");
        if (!paperSigned) throw new IllegalArgumentException("Vui lòng xác nhận phiếu bàn giao giấy đã được hai bên ký.");
        int n = productValues == null ? -1 : productValues.length;
        if (n < 1 || serialValues == null || locationValues == null || conditionValues == null
                || serialValues.length != n || locationValues.length != n || conditionValues.length != n)
            throw new IllegalArgumentException("Cần nhập đầy đủ Serial Number, vị trí và tình trạng cho từng máy.");
        Map<Integer, Integer> expected = new HashMap<>();
        handover.getItems().forEach(item -> expected.put(item.getProductId(), item.getQuantity()));
        Map<Integer, Integer> entered = new HashMap<>();
        List<ImportReceiptItem> items = new ArrayList<>(); Set<String> serials = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int productId;
            try { productId = Integer.parseInt(productValues[i]); }
            catch (Exception e) { throw new IllegalArgumentException("Model sản phẩm không hợp lệ."); }
            if (!expected.containsKey(productId)) throw new IllegalArgumentException("Phiếu nhập có sản phẩm không nằm trong phiếu bàn giao.");
            String serial = serialValues[i] == null ? "" : serialValues[i].trim();
            String location = locationValues[i] == null ? "" : locationValues[i].trim();
            String condition = conditionValues[i] == null ? "" : conditionValues[i].trim().toUpperCase();
            if (serial.isEmpty() || serial.length() > 100) throw new IllegalArgumentException("Serial Number là bắt buộc, tối đa 100 ký tự.");
            if (!serials.add(serial.toUpperCase())) throw new IllegalArgumentException("Serial Number bị lặp trong phiếu: " + serial);
            if (location.isEmpty() || location.length() > 50) throw new IllegalArgumentException("Vị trí kho là bắt buộc, tối đa 50 ký tự.");
            if (!Set.of("BRAND_NEW", "DEFECTIVE", "SCRATCHED").contains(condition)) throw new IllegalArgumentException("Tình trạng máy không hợp lệ.");
            ImportReceiptItem item = new ImportReceiptItem(); item.setProductId(productId); item.setSerialNumber(serial);
            item.setLocation(location); item.setCondition(condition); items.add(item); entered.merge(productId, 1, Integer::sum);
        }
        if (!expected.equals(entered)) throw new IllegalArgumentException("Số máy nhập theo từng model phải khớp số lượng trên phiếu bàn giao.");
        String cleanNote = note == null ? "" : note.trim();
        if (cleanNote.length() > 255) throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
        int receiptId = warehouseDAO.createImportReceipt(handoverId, actor.getUserId(), cleanNote, items);
        if (receiptId < 1) throw new IllegalStateException("Không tạo được phiếu nhập. Kiểm tra Serial Number và trạng thái phiếu bàn giao.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "IMPORT_RECEIPTS", String.valueOf(receiptId), null,
                Map.of("handoverId", handoverId, "itemCount", items.size(), "paperSigned", true, "note", cleanNote), ipAddress, userAgent);
        return receiptId;
    }

    public List<ImportReceipt> searchImportReceipts(int userId, String serial, String status, String keyword) {
        return warehouseDAO.searchImportReceipts(userId, serial, status, keyword);
    }

    public ImportReceipt getImportReceiptDetail(int receiptId, int userId) {
        return warehouseDAO.findImportReceiptById(receiptId, userId);
    }

    public List<InventoryItem> searchInventory(String serial, String location, String status, String keyword, int productId) {
        return warehouseDAO.searchInventory(serial, location, status, keyword, productId);
    }

    public List<InventoryItem> searchInventory(String serial, String location, String status, String keyword) {
        return warehouseDAO.searchInventory(serial, location, status, keyword, 0);
    }

    public InventoryItem findInventoryItem(int inventoryId) {
        return warehouseDAO.findInventoryItemById(inventoryId);
    }

    public List<String> getWarehouseLocations() {
        return warehouseDAO.findAllWarehouseLocations();
    }

    public Map<String, Object> getInventoryMetrics() {
        return warehouseDAO.getWarehouseInventoryMetrics();
    }

    public boolean updatePhysicalCondition(User actor, int inventoryId, String status, String location, String note, String ip, String agent) {
        InventoryItem existing = warehouseDAO.findInventoryItemById(inventoryId);
        if (existing == null) throw new IllegalArgumentException("Không tìm thấy máy trong kho.");
        String cleanStatus = status == null ? "" : status.trim().toUpperCase();
        if (!Set.of("IN_STOCK", "DAMAGED", "RETURNED").contains(cleanStatus)) {
            throw new IllegalArgumentException("Tình trạng vật lý chỉ có thể cập nhật: Sẵn sàng xuất (IN_STOCK), Hỏng/Lỗi (DAMAGED) hoặc Trả hàng NCC (RETURNED).");
        }
        String cleanLocation = location == null ? "" : location.trim();
        if (cleanLocation.isEmpty() || cleanLocation.length() > 50) {
            throw new IllegalArgumentException("Vị trí kho là bắt buộc và tối đa 50 ký tự.");
        }
        boolean ok = warehouseDAO.updatePhysicalItemCondition(inventoryId, cleanStatus, cleanLocation);
        if (ok) {
            Map<String, Object> oldVal = Map.of("status", existing.getStatus(), "location", existing.getLocation());
            Map<String, Object> newVal = Map.of("status", cleanStatus, "location", cleanLocation, "note", note == null ? "" : note.trim());
            auditLogService.logAction(actor.getUserId(), actor.getUsername(), "UPDATE", "INVENTORY", String.valueOf(inventoryId), oldVal, newVal, ip, agent);
        }
        return ok;
    }

}
