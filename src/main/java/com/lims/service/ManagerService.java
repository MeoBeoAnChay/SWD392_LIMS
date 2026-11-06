package com.lims.service;

import com.lims.dao.ManagerDAO;
import com.lims.model.ImportRequest;
import com.lims.model.ImportReceipt;
import com.lims.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManagerService {
    private final ManagerDAO managerDAO = new ManagerDAO();
    private final AuditLogService auditLogService = new AuditLogService();

    public boolean isActiveManager(int userId) { return managerDAO.isActiveManager(userId); }

    public List<ImportRequest> getImportRequests(String status) {
        String cleanStatus = status == null || status.isBlank() ? "PENDING" : status.trim().toUpperCase();
        if (!Set.of("ALL", "PENDING", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED").contains(cleanStatus))
            throw new IllegalArgumentException("Bộ lọc trạng thái không hợp lệ.");
        return managerDAO.findImportRequests(cleanStatus);
    }

    public List<ImportReceipt> getImportReceipts(String status) {
        String cleanStatus = status == null || status.isBlank() ? "PENDING_MANAGER" : status.trim().toUpperCase();
        if (!Set.of("ALL", "PENDING_MANAGER", "COMPLETED", "REJECTED").contains(cleanStatus))
            throw new IllegalArgumentException("Bộ lọc trạng thái phiếu nhập không hợp lệ.");
        return managerDAO.findImportReceipts(cleanStatus);
    }

    public void decideImportRequest(User manager, String requestValue, String decision, String reason,
                                    String ipAddress, String userAgent) {
        int requestId;
        try { requestId = Integer.parseInt(requestValue); }
        catch (Exception e) { throw new IllegalArgumentException("Mã đơn mua hàng không hợp lệ."); }
        if (requestId < 1) throw new IllegalArgumentException("Mã đơn mua hàng không hợp lệ.");
        String nextStatus = decision == null ? "" : decision.trim().toUpperCase();
        if (!Set.of("APPROVED", "REJECTED").contains(nextStatus)) throw new IllegalArgumentException("Quyết định duyệt không hợp lệ.");
        String cleanReason = reason == null ? "" : reason.trim();
        if ("REJECTED".equals(nextStatus) && (cleanReason.isEmpty() || cleanReason.length() > 255))
            throw new IllegalArgumentException("Khi từ chối, vui lòng nhập lý do (tối đa 255 ký tự).");
        if (!"REJECTED".equals(nextStatus)) cleanReason = null;

        ImportRequest before = managerDAO.findImportRequest(requestId);
        if (before == null || !"PENDING".equalsIgnoreCase(before.getStatus()))
            throw new IllegalArgumentException("Chỉ đơn đang Pending mới được duyệt hoặc từ chối.");
        if (!managerDAO.decidePendingRequest(requestId, manager.getUserId(), nextStatus, cleanReason))
            throw new IllegalStateException("Không cập nhật được đơn; có thể trạng thái đã thay đổi.");

        Map<String, Object> oldValues = new LinkedHashMap<>(); oldValues.put("status", before.getStatus());
        Map<String, Object> newValues = new LinkedHashMap<>(); newValues.put("status", nextStatus);
        newValues.put("approvedBy", manager.getUserId()); newValues.put("reason", cleanReason);
        auditLogService.logAction(manager.getUserId(), manager.getUsername(), nextStatus.equals("APPROVED") ? "APPROVE" : "REJECT",
                "IMPORT_REQUESTS", String.valueOf(requestId), oldValues, newValues, ipAddress, userAgent);
    }

    public void decideImportReceipt(User manager, String receiptValue, String decision, String reason,
                                    String ipAddress, String userAgent) {
        int receiptId;
        try { receiptId = Integer.parseInt(receiptValue); }
        catch (Exception e) { throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ."); }
        if (receiptId < 1) throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ.");
        String action = decision == null ? "" : decision.trim().toUpperCase();
        if (!Set.of("APPROVE", "REJECT").contains(action)) throw new IllegalArgumentException("Quyết định duyệt phiếu nhập không hợp lệ.");
        String cleanReason = reason == null ? "" : reason.trim();
        if ("REJECT".equals(action) && (cleanReason.isEmpty() || cleanReason.length() > 255))
            throw new IllegalArgumentException("Khi từ chối phiếu nhập, vui lòng nhập lý do (tối đa 255 ký tự).");
        String nextStatus = "APPROVE".equals(action) ? "COMPLETED" : "REJECTED";
        ImportReceipt before = managerDAO.findImportReceipts("ALL").stream()
                .filter(receipt -> receipt.getReceiptId() == receiptId).findFirst().orElse(null);
        if (before == null || !"PENDING_MANAGER".equalsIgnoreCase(before.getStatus()))
            throw new IllegalArgumentException("Chỉ phiếu nhập đang chờ duyệt mới được xử lý.");
        if (!managerDAO.decideImportReceipt(receiptId, manager.getUserId(), nextStatus, "REJECT".equals(action) ? cleanReason : null))
            throw new IllegalStateException("Không cập nhật được phiếu nhập; kiểm tra serial hoặc trạng thái phiếu.");
        Map<String, Object> oldValues = new LinkedHashMap<>(); oldValues.put("status", before.getStatus());
        Map<String, Object> newValues = new LinkedHashMap<>(); newValues.put("status", nextStatus);
        newValues.put("approvedBy", manager.getUserId()); newValues.put("reason", "REJECT".equals(action) ? cleanReason : null);
        newValues.put("inventoryUpdated", "APPROVE".equals(action));
        auditLogService.logAction(manager.getUserId(), manager.getUsername(), "APPROVE".equals(action) ? "APPROVE" : "REJECT",
                "IMPORT_RECEIPTS", String.valueOf(receiptId), oldValues, newValues, ipAddress, userAgent);
    }
}
