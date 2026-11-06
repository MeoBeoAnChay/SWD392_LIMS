package com.lims.service;

import com.lims.dao.PurchasingDAO;
import com.lims.model.ImportRequest;
import com.lims.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PurchasingService {
    private final PurchasingDAO purchasingDAO;
    private final AuditLogService auditLogService;

    public PurchasingService() {
        this.purchasingDAO = new PurchasingDAO();
        this.auditLogService = new AuditLogService();
    }

    public boolean isActivePurchasingStaff(int userId) { return purchasingDAO.isActivePurchasingStaff(userId); }
    public List<ImportRequest> getRequests(int userId) { return purchasingDAO.findRequestsByCreator(userId); }
    public List<com.lims.model.SupplierOption> getSuppliers() { return purchasingDAO.findActiveSuppliers(); }
    public List<com.lims.model.ProductOption> getProducts() { return purchasingDAO.findActiveProducts(); }

    public int createRequest(User actor, String supplierValue, String[] productValues, String[] quantityValues,
                             String ipAddress, String userAgent) {
        int supplierId = positiveInt(supplierValue, "Vui lòng chọn nhà cung cấp.");
        Map<Integer, Integer> quantities = parseItems(productValues, quantityValues);
        int requestId = purchasingDAO.createRequest(actor.getUserId(), supplierId, quantities);
        if (requestId < 1) throw new IllegalStateException("Không tạo được yêu cầu. Hãy kiểm tra nhà cung cấp và sản phẩm còn hoạt động.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "IMPORT_REQUESTS", String.valueOf(requestId),
                null, requestSummary(supplierId, quantities), ipAddress, userAgent);
        return requestId;
    }

    public void updateRequest(User actor, String requestValue, String supplierValue, String[] productValues, String[] quantityValues,
                              String ipAddress, String userAgent) {
        int requestId = positiveInt(requestValue, "Yêu cầu không hợp lệ.");
        int supplierId = positiveInt(supplierValue, "Vui lòng chọn nhà cung cấp.");
        Map<Integer, Integer> quantities = parseItems(productValues, quantityValues);
        ImportRequest oldRequest = findRequest(actor.getUserId(), requestId);
        if (oldRequest == null || !"PENDING".equalsIgnoreCase(oldRequest.getStatus()))
            throw new IllegalArgumentException("Chỉ được sửa yêu cầu của bạn khi trạng thái còn Pending.");
        if (!purchasingDAO.updatePendingRequest(requestId, actor.getUserId(), supplierId, quantities))
            throw new IllegalStateException("Không cập nhật được yêu cầu. Có thể trạng thái đã thay đổi hoặc dữ liệu không hợp lệ.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "UPDATE", "IMPORT_REQUESTS", String.valueOf(requestId),
                requestSummary(oldRequest.getSupplierId(), oldRequest), requestSummary(supplierId, quantities), ipAddress, userAgent);
    }

    public void cancelRequest(User actor, String requestValue, String reason, String ipAddress, String userAgent) {
        int requestId = positiveInt(requestValue, "Yêu cầu không hợp lệ.");
        String cleanReason = reason == null ? "" : reason.trim();
        if (cleanReason.isEmpty() || cleanReason.length() > 255) throw new IllegalArgumentException("Lý do hủy là bắt buộc và tối đa 255 ký tự.");
        ImportRequest oldRequest = findRequest(actor.getUserId(), requestId);
        if (oldRequest == null || !"PENDING".equalsIgnoreCase(oldRequest.getStatus()))
            throw new IllegalArgumentException("Chỉ được hủy yêu cầu của bạn khi trạng thái còn Pending.");
        if (!purchasingDAO.cancelPendingRequest(requestId, actor.getUserId(), cleanReason))
            throw new IllegalStateException("Không hủy được yêu cầu; có thể trạng thái đã thay đổi.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CANCEL", "IMPORT_REQUESTS", String.valueOf(requestId),
                requestSummary(oldRequest.getSupplierId(), oldRequest), cleanReason, ipAddress, userAgent);
    }

    public void createHandover(User actor, String requestValue, String handoverBy, String quantityValue, String note,
                               String ipAddress, String userAgent) {
        int requestId = positiveInt(requestValue, "Yêu cầu không hợp lệ.");
        String carrier = handoverBy == null ? "" : handoverBy.trim();
        if (carrier.isEmpty() || carrier.length() > 100) throw new IllegalArgumentException("Tên người bàn giao là bắt buộc, tối đa 100 ký tự.");
        int quantity = positiveInt(quantityValue, "Số lượng bàn giao phải lớn hơn 0.");
        String cleanNote = note == null ? "" : note.trim();
        if (cleanNote.length() > 255) throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
        if (!purchasingDAO.createHandover(requestId, actor.getUserId(), carrier, quantity, cleanNote))
            throw new IllegalStateException("Không tạo được phiếu bàn giao. Chỉ yêu cầu Approved và còn số lượng mới được bàn giao.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "HANDOVERS", String.valueOf(requestId), null,
                Map.of("handoverBy", carrier, "quantity", quantity, "note", cleanNote), ipAddress, userAgent);
    }

    private ImportRequest findRequest(int userId, int requestId) {
        return purchasingDAO.findRequestsByCreator(userId).stream().filter(request -> request.getRequestId() == requestId).findFirst().orElse(null);
    }

    private Map<Integer, Integer> parseItems(String[] productValues, String[] quantityValues) {
        if (productValues == null || quantityValues == null || productValues.length != quantityValues.length)
            throw new IllegalArgumentException("Danh sách sản phẩm không hợp lệ.");
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (int i = 0; i < productValues.length; i++) {
            String product = productValues[i] == null ? "" : productValues[i].trim();
            String quantity = quantityValues[i] == null ? "" : quantityValues[i].trim();
            if (product.isEmpty() && quantity.isEmpty()) continue;
            if (product.isEmpty() || quantity.isEmpty()) throw new IllegalArgumentException("Mỗi dòng sản phẩm cần chọn sản phẩm và nhập số lượng.");
            int productId = positiveInt(product, "Sản phẩm không hợp lệ.");
            int count = positiveInt(quantity, "Số lượng phải là số nguyên lớn hơn 0.");
            try { quantities.merge(productId, count, Math::addExact); }
            catch (ArithmeticException e) { throw new IllegalArgumentException("Tổng số lượng sản phẩm quá lớn."); }
        }
        if (quantities.isEmpty()) throw new IllegalArgumentException("Yêu cầu cần ít nhất một dòng sản phẩm.");
        return quantities;
    }

    private int positiveInt(String value, String message) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.trim());
            if (parsed > 0) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException(message);
    }

    private Object requestSummary(int supplierId, Object lines) {
        return Map.of("supplierId", supplierId, "items", lines);
    }
}
