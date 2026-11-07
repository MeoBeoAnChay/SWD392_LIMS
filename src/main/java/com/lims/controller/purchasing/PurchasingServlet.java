package com.lims.controller.purchasing;

import com.lims.controller.BaseServlet;

import com.lims.model.ImportRequest;
import com.lims.model.Supplier;
import com.lims.model.User;
import com.lims.service.PurchasingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "PurchasingServlet", urlPatterns = {
        "/app/purchasing/dashboard", "/app/purchasing/requests", "/app/purchasing/request-form",
        "/app/purchasing/handover-form", "/app/purchasing/handovers", "/app/purchasing/suppliers",
        "/app/purchasing/supplier-form", "/app/purchasing/profile"
})
public class PurchasingServlet extends BaseServlet {
    private final PurchasingService purchasingService = new PurchasingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requirePurchasingUser(req, resp);
        if (user == null) return;
        String path = req.getServletPath();
        req.setAttribute("user", user);
        req.setAttribute("notice", req.getParameter("notice"));
        req.setAttribute("error", req.getParameter("error"));
        try {
            if (path.endsWith("/dashboard")) {
                req.setAttribute("activeMenu", "dashboard");
                req.setAttribute("stats", purchasingService.getDashboard(user.getUserId()));
                forward(req, resp, "purchasing/dashboard.jsp");
            } else if (path.endsWith("/request-form")) {
                req.setAttribute("activeMenu", "orders");
                req.setAttribute("suppliers", purchasingService.getSuppliers());
                req.setAttribute("products", purchasingService.getProducts());
                String id = req.getParameter("requestId");
                ImportRequest request = id == null ? null : purchasingService.getRequest(user.getUserId(), parseId(id));
                if (id != null && (request == null || !"PENDING".equalsIgnoreCase(request.getStatus()))) {
                    redirectWith(req, resp, "/app/purchasing/requests", "error", "Chỉ có thể sửa yêu cầu của bạn khi trạng thái còn Pending.");
                    return;
                }
                req.setAttribute("importRequest", request);
                forward(req, resp, "purchasing/request-form.jsp");
            } else if (path.endsWith("/requests")) {
                req.setAttribute("activeMenu", "orders");
                String status = req.getParameter("status");
                req.setAttribute("statusFilter", status == null ? "" : status.toUpperCase());
                req.setAttribute("requestStatuses", java.util.Arrays.asList("PENDING", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED"));
                req.setAttribute("searchFilter", req.getParameter("search") == null ? "" : req.getParameter("search"));
                req.setAttribute("fromFilter", req.getParameter("from") == null ? "" : req.getParameter("from"));
                req.setAttribute("toFilter", req.getParameter("to") == null ? "" : req.getParameter("to"));
                req.setAttribute("requests", purchasingService.getRequests(user.getUserId(), status, req.getParameter("search"), req.getParameter("from"), req.getParameter("to")));
                forward(req, resp, "purchasing/requests.jsp");
            } else if (path.endsWith("/handover-form")) {
                req.setAttribute("activeMenu", "handovers");
                req.setAttribute("warehouseStaff", purchasingService.getWarehouseStaff());
                req.setAttribute("locations", purchasingService.getWarehouseLocations());
                String requestValue = req.getParameter("requestId");
                if (requestValue == null || requestValue.isBlank()) {
                    req.setAttribute("approvedRequests", purchasingService.getRequests(user.getUserId(), "APPROVED", "", null, null));
                    req.setAttribute("importRequest", null);
                } else {
                    int requestId = parseId(requestValue);
                    ImportRequest request = purchasingService.getRequest(user.getUserId(), requestId);
                    if (request == null || !"APPROVED".equalsIgnoreCase(request.getStatus()) || request.getRemainingQuantity() < 1) {
                        redirectWith(req, resp, "/app/purchasing/handover-form", "error", "Chỉ đơn Approved còn số lượng mới được tạo phiếu bàn giao.");
                        return;
                    }
                    req.setAttribute("importRequest", request);
                    req.setAttribute("handoverItems", purchasingService.getAvailableHandoverItems(requestId));
                }
                forward(req, resp, "purchasing/handover-form.jsp");
            } else if (path.endsWith("/handovers")) {
                req.setAttribute("activeMenu", "handovers");
                String printId = req.getParameter("printId");
                if (printId != null) {
                    com.lims.model.Handover handover = purchasingService.getHandover(user.getUserId(), parseId(printId));
                    if (handover == null) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                    req.setAttribute("handover", handover);
                    forward(req, resp, "purchasing/handover-print.jsp");
                } else {
                    req.setAttribute("handovers", purchasingService.getHandovers(user.getUserId()));
                    forward(req, resp, "purchasing/handovers.jsp");
                }
            } else if (path.endsWith("/supplier-form")) {
                req.setAttribute("activeMenu", "suppliers");
                String id = req.getParameter("supplierId");
                Supplier supplier = id == null ? new Supplier() : purchasingService.getSupplier(parseId(id));
                if (supplier == null) { redirectWith(req, resp, "/app/purchasing/suppliers", "error", "Không tìm thấy nhà cung cấp."); return; }
                req.setAttribute("supplier", supplier);
                forward(req, resp, "purchasing/supplier-form.jsp");
            } else if (path.endsWith("/suppliers")) {
                req.setAttribute("activeMenu", "suppliers");
                req.setAttribute("suppliers", purchasingService.getAllSuppliers());
                forward(req, resp, "purchasing/suppliers.jsp");
            } else {
                req.setAttribute("activeMenu", "account");
                forward(req, resp, "purchasing/profile.jsp");
            }
        } catch (IllegalArgumentException e) {
            redirectWith(req, resp, "/app/purchasing/requests", "error", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = requirePurchasingUser(req, resp);
        if (user == null) return;
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                int id = purchasingService.createRequest(user, req.getParameter("supplierId"), req.getParameterValues("productIds"),
                        req.getParameterValues("quantities"), req.getParameterValues("unitPrices"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/purchasing/requests", "notice", "Đã tạo yêu cầu nhập #" + id);
            } else if ("update".equals(action)) {
                purchasingService.updateRequest(user, req.getParameter("requestId"), req.getParameter("supplierId"),
                        req.getParameterValues("productIds"), req.getParameterValues("quantities"), req.getParameterValues("unitPrices"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/purchasing/requests", "notice", "Đã cập nhật yêu cầu nhập.");
            } else if ("cancel".equals(action)) {
                purchasingService.cancelRequest(user, req.getParameter("requestId"), req.getParameter("reason"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/purchasing/requests", "notice", "Đã hủy yêu cầu nhập.");
            } else if ("handover".equals(action)) {
                int handoverId = purchasingService.createHandover(user, req.getParameter("requestId"), req.getParameter("handoverBy"),
                        req.getParameter("receiverId"), req.getParameter("location"), req.getParameter("packageCount"),
                        req.getParameterValues("productIds"), req.getParameterValues("quantities"), req.getParameter("goodsIntact") != null,
                        req.getParameter("quantityDiscrepancy") != null, req.getParameter("damaged") != null,
                        req.getParameter("serialDiscrepancy") != null, req.getParameter("note"), getClientIP(req), req.getHeader("User-Agent"));
                redirect(req, resp, "/app/purchasing/handovers?printId=" + handoverId + "&notice="
                        + URLEncoder.encode("Đã tạo phiếu bàn giao.", StandardCharsets.UTF_8));
            } else if ("saveSupplier".equals(action)) {
                Supplier supplier = readSupplier(req);
                purchasingService.saveSupplier(user, supplier, getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/purchasing/suppliers", "notice", "Đã lưu thông tin nhà cung cấp.");
            } else if ("changePassword".equals(action)) {
                purchasingService.changePassword(user, req.getParameter("currentPassword"), req.getParameter("newPassword"),
                        req.getParameter("confirmPassword"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "/app/purchasing/profile", "notice", "Đã đổi mật khẩu.");
            } else {
                redirectWith(req, resp, "/app/purchasing/dashboard", "error", "Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            if ("handover".equals(action)) {
                String target = "/app/purchasing/handover-form?requestId=" + URLEncoder.encode(req.getParameter("requestId") == null ? "" : req.getParameter("requestId"), StandardCharsets.UTF_8)
                        + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
                redirect(req, resp, target);
            } else {
                String target = "saveSupplier".equals(action) ? "/app/purchasing/suppliers"
                        : "changePassword".equals(action) ? "/app/purchasing/profile" : "/app/purchasing/requests";
                redirectWith(req, resp, target, "error", e.getMessage());
            }
        }
    }

    private User requirePurchasingUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null || !purchasingService.isActivePurchasingStaff(user.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Purchasing Staff role required");
            return null;
        }
        return user;
    }

    private Supplier readSupplier(HttpServletRequest req) {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(parseId(req.getParameter("supplierId")));
        supplier.setSupplierName(req.getParameter("supplierName")); supplier.setContactName(req.getParameter("contactName"));
        supplier.setEmail(req.getParameter("email")); supplier.setPhone(req.getParameter("phone"));
        supplier.setAddress(req.getParameter("address")); supplier.setStatus(req.getParameter("status"));
        return supplier;
    }

    private int parseId(String value) {
        try { return Integer.parseInt(value); } catch (Exception e) { return -1; }
    }

    private void redirectWith(HttpServletRequest req, HttpServletResponse resp, String path, String key, String value) throws IOException {
        redirect(req, resp, path + "?" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
