package com.lims.controller.warehouse;

import com.lims.controller.BaseServlet;
import com.lims.model.ExportReceipt;
import com.lims.model.Handover;
import com.lims.model.ImportReceipt;
import com.lims.model.InventoryItem;
import com.lims.model.Stocktake;
import com.lims.model.User;
import com.lims.service.ExportService;
import com.lims.service.PurchasingService;
import com.lims.service.StocktakeService;
import com.lims.service.WarehouseService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@WebServlet(name = "WarehouseServlet", urlPatterns = {
        "/app/warehouse/dashboard",
        "/app/warehouse/handovers",
        "/app/warehouse/import-history",
        "/app/warehouse/import-receipt-form",
        "/app/warehouse/import-receipts",
        "/app/warehouse/import-receipt-detail",
        "/app/warehouse/stocktakes",
        "/app/warehouse/stocktake-form",
        "/app/warehouse/export-requests",
        "/app/warehouse/export-history",
        "/app/warehouse/export-form",
        "/app/warehouse/export-receipts",
        "/app/warehouse/export-receipt-detail",
        "/app/warehouse/inventory",
        "/app/warehouse/update-condition"
})
public class WarehouseServlet extends BaseServlet {
    private final WarehouseService warehouseService = new WarehouseService();
    private final StocktakeService stocktakeService = new StocktakeService();
    private final PurchasingService purchasingService = new PurchasingService();
    private final ExportService exportService = new ExportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireWarehouseStaff(req, resp);
        if (user == null) return;
        req.setAttribute("user", user);
        req.setAttribute("notice", req.getParameter("notice"));
        req.setAttribute("error", req.getParameter("error"));

        String path = req.getServletPath();

        if (path.endsWith("/dashboard")) {
            req.setAttribute("activeWarehouseMenu", "dashboard");
            List<Handover> allHandovers = warehouseService.getAssignedHandovers(user.getUserId());
            req.setAttribute("handovers", allHandovers);
            req.setAttribute("importHistory", warehouseService.searchImportReceipts(user.getUserId(), null, null, null));
            req.setAttribute("exportRequests", exportService.assignedRequests(user.getUserId()));
            req.setAttribute("exportReceipts", exportService.receiptsByStaff(user.getUserId()));
            req.setAttribute("stocktakes", stocktakeService.assigned(user.getUserId()));
            req.setAttribute("inventoryMetrics", warehouseService.getInventoryMetrics());
            forward(req, resp, "warehouse/dashboard.jsp");

        } else if (path.endsWith("/inventory")) {
            req.setAttribute("activeWarehouseMenu", "inventory");
            String serial = req.getParameter("serial");
            String location = req.getParameter("location");
            String status = req.getParameter("status");
            String keyword = req.getParameter("keyword");
            int productId = parseId(req.getParameter("productId"));

            List<InventoryItem> items = warehouseService.searchInventory(serial, location, status, keyword, productId);
            req.setAttribute("items", items);
            req.setAttribute("products", purchasingService.getProducts());
            req.setAttribute("locations", warehouseService.getWarehouseLocations());
            req.setAttribute("filterSerial", serial);
            req.setAttribute("filterLocation", location);
            req.setAttribute("filterStatus", status);
            req.setAttribute("filterKeyword", keyword);
            req.setAttribute("filterProductId", productId);
            req.setAttribute("inventoryMetrics", warehouseService.getInventoryMetrics());

            if (serial != null && !serial.isBlank() && !items.isEmpty() && status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                if (!status.equalsIgnoreCase(items.get(0).getStatus())) {
                    req.setAttribute("smartNotice", "Tìm thấy máy mang Serial '" + serial + "' (Lưu ý: Máy hiện có trạng thái thực tế là '" + items.get(0).getStatus() + "')");
                }
            }
            forward(req, resp, "warehouse/inventory.jsp");

        } else if (path.endsWith("/import-history")) {
            req.setAttribute("activeWarehouseMenu", "import-history");
            String serial = req.getParameter("serial");
            String status = req.getParameter("status");
            String keyword = req.getParameter("keyword");

            List<ImportReceipt> receipts = warehouseService.searchImportReceipts(user.getUserId(), serial, status, keyword);
            req.setAttribute("receipts", receipts);
            req.setAttribute("filterSerial", serial);
            req.setAttribute("filterStatus", status);
            req.setAttribute("filterKeyword", keyword);
            forward(req, resp, "warehouse/import-history.jsp");

        } else if (path.endsWith("/import-receipt-detail")) {
            req.setAttribute("activeWarehouseMenu", "import-history");
            int receiptId = parseId(req.getParameter("receiptId"));
            ImportReceipt receipt = warehouseService.getImportReceiptDetail(receiptId, user.getUserId());
            if (receipt == null) {
                redirectWith(req, resp, "/app/warehouse/import-history", "error", "Không tìm thấy phiếu nhập kho #" + receiptId);
                return;
            }
            req.setAttribute("receipt", receipt);
            forward(req, resp, "warehouse/import-receipt-detail.jsp");

        } else if (path.endsWith("/export-form")) {
            req.setAttribute("activeWarehouseMenu", "exports");
            int id = parseId(req.getParameter("requestId"));
            com.lims.model.ExportRequest request = exportService.request(id);
            if (request == null || request.getAssignedTo() != user.getUserId() || !"APPROVED".equals(request.getStatus())) {
                redirectWith(req, resp, "/app/warehouse/export-requests", "error", "Yêu cầu không được giao cho bạn hoặc không còn chờ soạn hàng.");
                return;
            }
            req.setAttribute("exportRequest", request);
            req.setAttribute("availableSerials", exportService.available(id, user.getUserId()));
            forward(req, resp, "warehouse/export-form.jsp");

        } else if (path.endsWith("/export-history")) {
            req.setAttribute("activeWarehouseMenu", "export-history");
            String serial = req.getParameter("serial");
            String status = req.getParameter("status");
            String keyword = req.getParameter("keyword");

            List<ExportReceipt> allReceipts = exportService.receiptsByStaff(user.getUserId());
            if ((serial != null && !serial.isBlank()) || (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) || (keyword != null && !keyword.isBlank())) {
                final String sSearch = serial == null ? "" : serial.trim().toLowerCase();
                final String stSearch = status == null ? "" : status.trim().toUpperCase();
                final String kwSearch = keyword == null ? "" : keyword.trim().toLowerCase();

                allReceipts = allReceipts.stream().filter(r -> {
                    boolean match = true;
                    if (!sSearch.isEmpty()) {
                        match = r.getItems().stream().anyMatch(it -> it.getSerialNumber() != null && it.getSerialNumber().toLowerCase().contains(sSearch));
                    }
                    if (match && !stSearch.isEmpty() && !"ALL".equalsIgnoreCase(stSearch)) {
                        match = stSearch.equalsIgnoreCase(r.getStatus());
                    }
                    if (match && !kwSearch.isEmpty()) {
                        match = (r.getCustomerName() != null && r.getCustomerName().toLowerCase().contains(kwSearch)) ||
                                String.valueOf(r.getReceiptId()).contains(kwSearch) ||
                                String.valueOf(r.getRequestId()).contains(kwSearch);
                    }
                    return match;
                }).toList();
            }

            req.setAttribute("exportReceipts", allReceipts);
            req.setAttribute("filterSerial", serial);
            req.setAttribute("filterStatus", status);
            req.setAttribute("filterKeyword", keyword);
            forward(req, resp, "warehouse/export-history.jsp");

        } else if (path.endsWith("/export-receipt-detail")) {
            req.setAttribute("activeWarehouseMenu", "export-history");
            int receiptId = parseId(req.getParameter("receiptId"));
            ExportReceipt receipt = exportService.getReceiptDetail(receiptId);
            if (receipt == null) {
                redirectWith(req, resp, "/app/warehouse/export-history", "error", "Không tìm thấy phiếu xuất kho #" + receiptId);
                return;
            }
            req.setAttribute("receipt", receipt);
            forward(req, resp, "warehouse/export-receipt-detail.jsp");

        } else if (path.endsWith("/export-requests")) {
            req.setAttribute("activeWarehouseMenu", "exports");
            req.setAttribute("exportRequests", exportService.assignedRequests(user.getUserId()));
            forward(req, resp, "warehouse/export-requests.jsp");

        } else if (path.endsWith("/stocktake-form")) {
            req.setAttribute("activeWarehouseMenu", "stocktakes");
            int id = parseId(req.getParameter("checkId"));
            Stocktake check = stocktakeService.find(id);
            if (check == null || check.getAssignedTo() != user.getUserId() || !("ASSIGNED".equals(check.getStatus()) || "REJECTED".equals(check.getStatus()))) {
                redirectWith(req, resp, "/app/warehouse/stocktakes", "error", "Đợt kiểm kho không được giao cho bạn hoặc đã gửi duyệt.");
                return;
            }
            req.setAttribute("check", check);
            req.setAttribute("products", purchasingService.getProducts());
            forward(req, resp, "warehouse/stocktake-form.jsp");

        } else if (path.endsWith("/stocktakes")) {
            req.setAttribute("activeWarehouseMenu", "stocktakes");
            req.setAttribute("stocktakes", stocktakeService.assigned(user.getUserId()));
            forward(req, resp, "warehouse/stocktakes.jsp");

        } else if (path.endsWith("/import-receipt-form")) {
            req.setAttribute("activeWarehouseMenu", "handovers");
            int handoverId = parseId(req.getParameter("handoverId"));
            Handover handover = warehouseService.getAvailableHandover(handoverId, user.getUserId());
            if (handover == null) {
                redirectWith(req, resp, "/app/warehouse/handovers", "error", "Không tìm thấy phiếu bàn giao được giao cho bạn hoặc phiếu đã gửi duyệt.");
                return;
            }
            req.setAttribute("handover", handover);
            req.setAttribute("locations", warehouseService.getWarehouseLocations());
            forward(req, resp, "warehouse/import-receipt-form.jsp");

        } else {
            req.setAttribute("activeWarehouseMenu", "handovers");
            req.setAttribute("handovers", warehouseService.getAssignedHandovers(user.getUserId()).stream().filter(h -> "WAITING".equals(h.getReceiptStatus()) || "REJECTED".equals(h.getReceiptStatus())).toList());
            forward(req, resp, "warehouse/handovers.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        User user = requireWarehouseStaff(req, resp);
        if (user == null) return;

        String path = req.getServletPath();

        try {
            if (path.endsWith("/update-condition")) {
                int inventoryId = parseId(req.getParameter("inventoryId"));
                String status = req.getParameter("status");
                String location = req.getParameter("location");
                String note = req.getParameter("note");
                warehouseService.updatePhysicalCondition(user, inventoryId, status, location, note, getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/warehouse/inventory", "notice", "Đã cập nhật tình trạng vật lý cho máy thành công.");
                return;
            }

            if (path.endsWith("/export-receipts")) {
                int receipt = exportService.createReceipt(user, req.getParameter("requestId"), req.getParameterValues("serialNumbers"), req.getParameter("note"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/warehouse/export-requests", "notice", "Đã gửi phiếu xuất #" + receipt + " cho Manager duyệt; hàng đang được giữ chờ duyệt.");
                return;
            }

            if (path.endsWith("/stocktakes")) {
                stocktakeService.submit(user, parseId(req.getParameter("checkId")), req.getParameterValues("detailIds"), req.getParameterValues("productIds"), req.getParameterValues("serialNumbers"), req.getParameterValues("actualStatuses"), req.getParameterValues("actualLocations"), req.getParameterValues("reasons"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/warehouse/stocktakes", "notice", "Đã gửi kết quả kiểm kho cho Manager duyệt. Tồn kho chưa thay đổi.");
                return;
            }

            int receiptId = warehouseService.createImportReceipt(user, req.getParameter("handoverId"),
                    req.getParameterValues("productIds"), req.getParameterValues("serialNumbers"),
                    req.getParameterValues("locations"), req.getParameterValues("conditions"), req.getParameter("note"),
                    req.getParameter("paperSigned") != null, getClientIP(req), req.getHeader("User-Agent"));
            redirectWith(req, resp, "/app/warehouse/handovers", "notice", "Đã gửi phiếu nhập kho #" + receiptId + " cho Manager duyệt. Tồn kho chưa được cập nhật.");

        } catch (IllegalArgumentException | IllegalStateException e) {
            if (path.endsWith("/update-condition")) {
                redirectWith(req, resp, "/app/warehouse/inventory", "error", e.getMessage());
                return;
            }
            if (path.endsWith("/export-receipts")) {
                String url = "/app/warehouse/export-form?requestId=" + req.getParameter("requestId") + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                redirect(req, resp, url);
                return;
            }
            if (path.endsWith("/stocktakes")) {
                String url = "/app/warehouse/stocktake-form?checkId=" + req.getParameter("checkId") + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                redirect(req, resp, url);
                return;
            }
            String target = "/app/warehouse/import-receipt-form?handoverId=" + URLEncoder.encode(
                    req.getParameter("handoverId") == null ? "" : req.getParameter("handoverId"), StandardCharsets.UTF_8)
                    + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            redirect(req, resp, target);
        }
    }

    private User requireWarehouseStaff(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null || !warehouseService.isActiveWarehouseStaff(user.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Warehouse Staff role required");
            return null;
        }
        return user;
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private void redirectWith(HttpServletRequest req, HttpServletResponse resp, String path, String key, String value) throws IOException {
        redirect(req, resp, path + "?" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
