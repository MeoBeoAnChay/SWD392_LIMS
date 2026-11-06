package com.lims.service;

import com.lims.dao.PurchasingDAO;
import com.lims.dao.UserDAO;
import com.lims.model.Supplier;
import com.lims.model.ImportRequest;
import com.lims.model.ImportRequestItem;
import com.lims.model.User;
import com.lims.util.PasswordUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurchasingService {
    private final PurchasingDAO purchasingDAO;
    private final UserDAO userDAO;
    private final AuditLogService auditLogService;

    public PurchasingService() {
        this.purchasingDAO = new PurchasingDAO();
        this.userDAO = new UserDAO();
        this.auditLogService = new AuditLogService();
    }

    public boolean isActivePurchasingStaff(int userId) { return purchasingDAO.isActivePurchasingStaff(userId); }
    public List<ImportRequest> getRequests(int userId) { return purchasingDAO.findRequestsByCreator(userId); }
    public ImportRequest getRequest(int userId, int requestId) {
        return purchasingDAO.findRequestsByCreator(userId).stream().filter(request -> request.getRequestId() == requestId).findFirst().orElse(null);
    }
    public List<ImportRequest> getRequests(int userId, String status, String search, String from, String to) {
        String cleanStatus = status == null ? "" : status.trim().toUpperCase();
        if (!cleanStatus.isEmpty() && !Set.of("PENDING", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED").contains(cleanStatus))
            throw new IllegalArgumentException("Trạng thái lọc không hợp lệ.");
        return purchasingDAO.findRequestsByCreator(userId, cleanStatus, search, parseDate(from), parseDate(to));
    }
    public List<com.lims.model.SupplierOption> getSuppliers() { return purchasingDAO.findActiveSuppliers(); }
    public List<com.lims.model.ProductOption> getProducts() { return purchasingDAO.findActiveProducts(); }
    public List<Supplier> getAllSuppliers() { return purchasingDAO.findAllSuppliers(); }
    public Supplier getSupplier(int supplierId) { return purchasingDAO.findSupplier(supplierId); }
    public List<com.lims.model.Handover> getHandovers(int userId) { return purchasingDAO.findHandoversByCreator(userId); }
    public List<User> getWarehouseStaff() { return purchasingDAO.findActiveWarehouseStaff(); }
    public List<ImportRequestItem> getAvailableHandoverItems(int requestId) { return purchasingDAO.findAvailableHandoverItems(requestId); }
    public com.lims.model.Handover getHandover(int userId, int handoverId) { return purchasingDAO.findHandoverByCreator(userId, handoverId); }

    public Map<String, Integer> getDashboard(int userId) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        Map<String, Integer> counts = purchasingDAO.getRequestCounts(userId);
        stats.put("pending", counts.getOrDefault("PENDING", 0));
        stats.put("approved", counts.getOrDefault("APPROVED", 0));
        stats.put("rejected", counts.getOrDefault("REJECTED", 0));
        stats.put("cancelled", counts.getOrDefault("CANCELLED", 0));
        stats.put("completed", counts.getOrDefault("COMPLETED", 0));
        int[] handovers = purchasingDAO.getHandoverProgress(userId);
        stats.put("handoverTotal", handovers[0]); stats.put("handoverReceived", handovers[1]);
        return stats;
    }

    public void saveSupplier(User actor, Supplier supplier, String ipAddress, String userAgent) {
        validateSupplier(supplier);
        boolean saved;
        if (supplier.getSupplierId() <= 0) {
            supplier.setStatus("ACTIVE");
            saved = purchasingDAO.createSupplier(supplier);
            if (!saved) throw new IllegalStateException("Không thêm được nhà cung cấp. Có thể tên đã tồn tại.");
            auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "SUPPLIERS", supplier.getSupplierName(), null,
                    supplierSummary(supplier), ipAddress, userAgent);
        } else {
            Supplier before = purchasingDAO.findSupplier(supplier.getSupplierId());
            if (before == null) throw new IllegalArgumentException("Không tìm thấy nhà cung cấp.");
            saved = purchasingDAO.updateSupplier(supplier);
            if (!saved) throw new IllegalStateException("Không cập nhật được nhà cung cấp. Có thể tên đã tồn tại.");
            auditLogService.logAction(actor.getUserId(), actor.getUsername(), "UPDATE", "SUPPLIERS", String.valueOf(supplier.getSupplierId()),
                    supplierSummary(before), supplierSummary(supplier), ipAddress, userAgent);
        }
    }

    public void changePassword(User actor, String currentPassword, String newPassword, String confirmation,
                               String ipAddress, String userAgent) {
        if (currentPassword == null || !PasswordUtils.checkPassword(currentPassword, actor.getPasswordHash()))
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 72)
            throw new IllegalArgumentException("Mật khẩu mới phải dài từ 8 đến 72 ký tự.");
        if (!newPassword.equals(confirmation)) throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        String newHash = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt(10));
        if (!userDAO.changePassword(actor.getUserId(), actor.getPasswordHash(), newHash))
            throw new IllegalStateException("Không cập nhật được mật khẩu. Hãy đăng nhập lại rồi thử lại.");
        actor.setPasswordHash(newHash);
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CHANGE_PASSWORD", "USERS", String.valueOf(actor.getUserId()),
                "passwordHash=updated", "passwordHash=updated", ipAddress, userAgent);
    }

    private void validateSupplier(Supplier supplier) {
        supplier.setSupplierName(trim(supplier.getSupplierName()));
        supplier.setContactName(trim(supplier.getContactName()));
        supplier.setEmail(trim(supplier.getEmail())); supplier.setPhone(trim(supplier.getPhone()));
        supplier.setAddress(trim(supplier.getAddress()));
        if (supplier.getSupplierName().isEmpty() || supplier.getSupplierName().length() > 150)
            throw new IllegalArgumentException("Tên công ty là bắt buộc, tối đa 150 ký tự.");
        if (supplier.getEmail().isEmpty() || supplier.getEmail().length() > 100 || !supplier.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Email nhà cung cấp không hợp lệ.");
        if (supplier.getPhone().isEmpty() || supplier.getPhone().length() > 20)
            throw new IllegalArgumentException("Số điện thoại là bắt buộc, tối đa 20 ký tự.");
        if (supplier.getContactName().length() > 100 || supplier.getAddress().length() > 255)
            throw new IllegalArgumentException("Người đại diện tối đa 100 ký tự, địa chỉ tối đa 255 ký tự.");
        String status = supplier.getStatus() == null ? "ACTIVE" : supplier.getStatus().trim().toUpperCase();
        if (!Set.of("ACTIVE", "INACTIVE").contains(status)) throw new IllegalArgumentException("Trạng thái nhà cung cấp không hợp lệ.");
        supplier.setStatus(status);
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
    private Date parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Date.valueOf(LocalDate.parse(value.trim())); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Ngày lọc không hợp lệ."); }
    }
    private Object supplierSummary(Supplier supplier) {
        return Map.of("name", supplier.getSupplierName(), "contact", supplier.getContactName(), "email", supplier.getEmail(),
                "phone", supplier.getPhone(), "address", supplier.getAddress(), "status", supplier.getStatus());
    }

    public int createRequest(User actor, String supplierValue, String[] productValues, String[] quantityValues, String[] unitPriceValues,
                             String ipAddress, String userAgent) {
        int supplierId = positiveInt(supplierValue, "Vui lòng chọn nhà cung cấp.");
        Map<Integer, ImportRequestItem> items = parseItems(productValues, quantityValues, unitPriceValues);
        int requestId = purchasingDAO.createRequest(actor.getUserId(), supplierId, items);
        if (requestId < 1) throw new IllegalStateException("Không tạo được yêu cầu. Hãy kiểm tra nhà cung cấp và sản phẩm còn hoạt động.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "IMPORT_REQUESTS", String.valueOf(requestId),
                null, requestSummary(supplierId, items), ipAddress, userAgent);
        return requestId;
    }

    public void updateRequest(User actor, String requestValue, String supplierValue, String[] productValues, String[] quantityValues, String[] unitPriceValues,
                              String ipAddress, String userAgent) {
        int requestId = positiveInt(requestValue, "Yêu cầu không hợp lệ.");
        int supplierId = positiveInt(supplierValue, "Vui lòng chọn nhà cung cấp.");
        Map<Integer, ImportRequestItem> items = parseItems(productValues, quantityValues, unitPriceValues);
        ImportRequest oldRequest = findRequest(actor.getUserId(), requestId);
        if (oldRequest == null || !"PENDING".equalsIgnoreCase(oldRequest.getStatus()))
            throw new IllegalArgumentException("Chỉ được sửa yêu cầu của bạn khi trạng thái còn Pending.");
        if (!purchasingDAO.updatePendingRequest(requestId, actor.getUserId(), supplierId, items))
            throw new IllegalStateException("Không cập nhật được yêu cầu. Có thể trạng thái đã thay đổi hoặc dữ liệu không hợp lệ.");
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "UPDATE", "IMPORT_REQUESTS", String.valueOf(requestId),
                requestSummary(oldRequest.getSupplierId(), oldRequest), requestSummary(supplierId, items), ipAddress, userAgent);
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

    public int createHandover(User actor, String requestValue, String handoverBy, String receiverValue, String location,
                              String packageCountValue, String[] productValues, String[] quantityValues,
                              boolean goodsIntact, boolean quantityDiscrepancy, boolean damaged, boolean serialDiscrepancy,
                              String note, String ipAddress, String userAgent) {
        int requestId = positiveInt(requestValue, "Yêu cầu không hợp lệ.");
        String carrier = handoverBy == null ? "" : handoverBy.trim();
        if (carrier.isEmpty() || carrier.length() > 100) throw new IllegalArgumentException("Tên người bàn giao là bắt buộc, tối đa 100 ký tự.");
        int receiverId = positiveInt(receiverValue, "Vui lòng chọn nhân viên kho tiếp nhận.");
        String cleanLocation = location == null ? "" : location.trim();
        if (cleanLocation.isEmpty() || cleanLocation.length() > 255) throw new IllegalArgumentException("Địa điểm bàn giao là bắt buộc, tối đa 255 ký tự.");
        int packageCount = positiveInt(packageCountValue, "Số kiện/thùng phải lớn hơn 0.");
        if (productValues == null || quantityValues == null || productValues.length != quantityValues.length)
            throw new IllegalArgumentException("Chi tiết sản phẩm bàn giao không hợp lệ.");
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (int i = 0; i < productValues.length; i++) {
            try {
                int productId = Integer.parseInt(productValues[i]);
                int quantity = Integer.parseInt(quantityValues[i]);
                if (productId < 1 || quantity < 0) throw new NumberFormatException();
                quantities.merge(productId, quantity, Integer::sum);
            } catch (NumberFormatException e) { throw new IllegalArgumentException("Số lượng từng mặt hàng không hợp lệ."); }
        }
        String cleanNote = note == null ? "" : note.trim();
        if (cleanNote.length() > 1000) throw new IllegalArgumentException("Ghi chú sai lệch tối đa 1000 ký tự.");
        boolean hasDiscrepancy = quantityDiscrepancy || damaged || serialDiscrepancy;
        if (goodsIntact && hasDiscrepancy) throw new IllegalArgumentException("Không thể chọn hàng nguyên vẹn cùng lúc với tình trạng sai lệch.");
        if (hasDiscrepancy && cleanNote.isEmpty()) throw new IllegalArgumentException("Hãy mô tả sai lệch hoặc hư hỏng trong phần ghi chú.");
        int handoverId = purchasingDAO.createHandover(requestId, actor.getUserId(), carrier, receiverId, cleanLocation,
                packageCount, quantities, goodsIntact, quantityDiscrepancy, damaged, serialDiscrepancy, cleanNote);
        if (handoverId < 1)
            throw new IllegalStateException("Không tạo được phiếu bàn giao. Chỉ yêu cầu Approved và còn số lượng mới được bàn giao.");
        int totalQuantity = quantities.values().stream().mapToInt(Integer::intValue).sum();
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "HANDOVERS", String.valueOf(handoverId), null,
                Map.of("requestId", requestId, "handoverBy", carrier, "receiverId", receiverId, "location", cleanLocation,
                        "packageCount", packageCount, "quantity", totalQuantity, "items", quantities, "note", cleanNote), ipAddress, userAgent);
        return handoverId;
    }

    private ImportRequest findRequest(int userId, int requestId) {
        return purchasingDAO.findRequestsByCreator(userId).stream().filter(request -> request.getRequestId() == requestId).findFirst().orElse(null);
    }

    private Map<Integer, ImportRequestItem> parseItems(String[] productValues, String[] quantityValues, String[] unitPriceValues) {
        if (productValues == null || quantityValues == null || unitPriceValues == null
                || productValues.length != quantityValues.length || productValues.length != unitPriceValues.length)
            throw new IllegalArgumentException("Danh sách sản phẩm không hợp lệ.");
        Map<Integer, ImportRequestItem> items = new LinkedHashMap<>();
        for (int i = 0; i < productValues.length; i++) {
            String product = productValues[i] == null ? "" : productValues[i].trim();
            String quantity = quantityValues[i] == null ? "" : quantityValues[i].trim();
            String unitPrice = unitPriceValues[i] == null ? "" : unitPriceValues[i].trim();
            if (product.isEmpty() && quantity.isEmpty() && unitPrice.isEmpty()) continue;
            if (product.isEmpty() || quantity.isEmpty() || unitPrice.isEmpty())
                throw new IllegalArgumentException("Mỗi dòng cần có sản phẩm, số lượng và đơn giá mua.");
            int productId = positiveInt(product, "Sản phẩm không hợp lệ.");
            int count = positiveInt(quantity, "Số lượng phải là số nguyên lớn hơn 0.");
            BigDecimal price = parseUnitPrice(unitPrice);
            ImportRequestItem existing = items.get(productId);
            if (existing != null && existing.getUnitPrice().compareTo(price) != 0)
                throw new IllegalArgumentException("Một sản phẩm không thể có nhiều đơn giá trong cùng một yêu cầu.");
            try {
                if (existing == null) {
                    ImportRequestItem item = new ImportRequestItem(); item.setProductId(productId); item.setQuantity(count); item.setUnitPrice(price);
                    items.put(productId, item);
                } else existing.setQuantity(Math.addExact(existing.getQuantity(), count));
            }
            catch (ArithmeticException e) { throw new IllegalArgumentException("Tổng số lượng sản phẩm quá lớn."); }
        }
        if (items.isEmpty()) throw new IllegalArgumentException("Yêu cầu cần ít nhất một dòng sản phẩm.");
        return items;
    }

    private BigDecimal parseUnitPrice(String value) {
        try {
            BigDecimal price = new BigDecimal(value).setScale(2, RoundingMode.UNNECESSARY);
            if (price.signum() > 0 && price.precision() <= 18) return price;
        } catch (NumberFormatException | ArithmeticException ignored) { }
        throw new IllegalArgumentException("Đơn giá mua phải lớn hơn 0 và có tối đa 2 chữ số thập phân.");
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
