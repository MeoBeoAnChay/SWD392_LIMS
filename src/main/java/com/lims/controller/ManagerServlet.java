package com.lims.controller;

import com.lims.model.User;
import com.lims.service.ManagerService;
import com.lims.service.StocktakeService;
import com.lims.service.ExportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@WebServlet(name = "ManagerServlet", urlPatterns = {"/app/manager/import-requests", "/app/manager/import-receipts", "/app/manager/stocktakes", "/app/manager/export-requests", "/app/manager/export-receipts"})
public class ManagerServlet extends BaseServlet {
    private final ManagerService managerService = new ManagerService();
    private final StocktakeService stocktakeService = new StocktakeService();
    private final ExportService exportService = new ExportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User manager = requireManager(req, resp);
        if (manager == null) return;
        boolean receiptsTab = req.getServletPath().endsWith("/import-receipts");
        boolean stocktakesTab = req.getServletPath().endsWith("/stocktakes");
        boolean exportRequestsTab = req.getServletPath().endsWith("/export-requests");
        boolean exportReceiptsTab = req.getServletPath().endsWith("/export-receipts");
        String status = req.getParameter("status");
        if (status == null || status.isBlank()) status = stocktakesTab ? "ALL" : receiptsTab || exportReceiptsTab ? "PENDING_MANAGER" : "PENDING";
        try {
            req.setAttribute("user", manager);
            req.setAttribute("statusFilter", status.toUpperCase());
            req.setAttribute("notice", req.getParameter("notice"));
            req.setAttribute("error", req.getParameter("error"));
            if(exportRequestsTab){req.setAttribute("activeManagerMenu","exports");req.setAttribute("statusFilter",status.toUpperCase());req.setAttribute("requestStatuses",Arrays.asList("ALL","PENDING","APPROVED","REJECTED","COMPLETED"));req.setAttribute("staff",stocktakeService.staff());req.setAttribute("requests",exportService.managerRequests(status));forward(req,resp,"manager-export-requests.jsp");
            } else if(exportReceiptsTab){req.setAttribute("activeManagerMenu","export-receipts");req.setAttribute("statusFilter",status.toUpperCase());req.setAttribute("receiptStatuses",Arrays.asList("ALL","PENDING_MANAGER","COMPLETED","REJECTED"));req.setAttribute("receipts",exportService.receipts(status));forward(req,resp,"manager-export-receipts.jsp");
            } else if (stocktakesTab) {
                req.setAttribute("activeManagerMenu", "stocktakes");
                req.setAttribute("stocktakeStatuses", Arrays.asList("ALL", "ASSIGNED", "PENDING_MANAGER", "APPROVED", "REJECTED", "CANCELLED"));
                req.setAttribute("staff", stocktakeService.staff());
                req.setAttribute("stocktakes", stocktakeService.managerList(status));
                forward(req, resp, "manager-stocktakes.jsp");
            } else if (receiptsTab) {
                req.setAttribute("receiptStatuses", Arrays.asList("PENDING_MANAGER", "COMPLETED", "REJECTED", "ALL"));
                req.setAttribute("receipts", managerService.getImportReceipts(status));
                forward(req, resp, "manager-import-receipts.jsp");
            } else {
                req.setAttribute("requestStatuses", Arrays.asList("PENDING", "APPROVED", "REJECTED", "CANCELLED", "COMPLETED", "ALL"));
                req.setAttribute("requests", managerService.getImportRequests(status));
                forward(req, resp, "manager-import-requests.jsp");
            }
        } catch (IllegalArgumentException e) {
            redirectWith(req, resp, exportRequestsTab?"/app/manager/export-requests":exportReceiptsTab?"/app/manager/export-receipts":stocktakesTab ? "/app/manager/stocktakes" : receiptsTab ? "/app/manager/import-receipts" : "/app/manager/import-requests",
                    stocktakesTab ? "ALL" : receiptsTab||exportReceiptsTab ? "PENDING_MANAGER" : "PENDING", "error", e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        User manager = requireManager(req, resp);
        if (manager == null) return;
        boolean receiptsTab = req.getServletPath().endsWith("/import-receipts");
        boolean stocktakesTab = req.getServletPath().endsWith("/stocktakes");
        boolean exportRequestsTab = req.getServletPath().endsWith("/export-requests");
        boolean exportReceiptsTab = req.getServletPath().endsWith("/export-receipts");
        String decision = req.getParameter("decision");
        try {
            String path;
            String notice;
            if(exportRequestsTab){exportService.decideRequest(manager,req.getParameter("requestId"),decision,req.getParameter("assignedTo"),req.getParameter("reason"),getClientIP(req),req.getHeader("User-Agent"));path="/app/manager/export-requests";notice="APPROVE".equalsIgnoreCase(decision)?"Đã duyệt yêu cầu và giao nhân viên kho soạn hàng.":"Đã từ chối yêu cầu xuất kho.";
            } else if(exportReceiptsTab){exportService.decideReceipt(manager,req.getParameter("receiptId"),decision,req.getParameter("reason"),getClientIP(req),req.getHeader("User-Agent"));path="/app/manager/export-receipts";notice="APPROVE".equalsIgnoreCase(decision)?"Đã duyệt phiếu xuất; serial đã được đánh dấu đã xuất.":"Đã từ chối phiếu xuất; hàng được trả lại tồn khả dụng.";
            } else if (stocktakesTab) {
                if ("CREATE".equalsIgnoreCase(decision)) {
                    int id = stocktakeService.create(manager, req.getParameter("assignedTo"), req.getParameter("locationFilter"), req.getParameter("note"), getClientIP(req), req.getHeader("User-Agent"));
                    path = "/app/manager/stocktakes"; notice = "Đã tạo và giao đợt kiểm kho #" + id + ".";
                } else {
                    stocktakeService.decide(manager, req.getParameter("checkId"), decision, req.getParameter("reason"), getClientIP(req), req.getHeader("User-Agent"));
                    path = "/app/manager/stocktakes"; notice = "APPROVE".equalsIgnoreCase(decision) ? "Đã duyệt và cập nhật tồn kho theo kết quả kiểm kê." : "Đã từ chối kết quả; tồn kho chưa thay đổi.";
                }
            } else if (receiptsTab) {
                managerService.decideImportReceipt(manager, req.getParameter("receiptId"), decision, req.getParameter("reason"),
                        getClientIP(req), req.getHeader("User-Agent"));
                path = "/app/manager/import-receipts";
                notice = "APPROVE".equalsIgnoreCase(decision) ? "Đã duyệt phiếu nhập và cập nhật tồn kho theo Serial Number, vị trí." : "Đã từ chối phiếu nhập; chưa cập nhật tồn kho.";
            } else {
                managerService.decideImportRequest(manager, req.getParameter("requestId"), decision, req.getParameter("reason"),
                        getClientIP(req), req.getHeader("User-Agent"));
                path = "/app/manager/import-requests";
                notice = "APPROVED".equalsIgnoreCase(decision)
                        ? "Đã duyệt đơn mua hàng. Purchasing Staff có thể tạo phiếu bàn giao để chuyển hàng đến kho."
                        : "Đã từ chối đơn mua hàng.";
            }
            redirectWith(req, resp, path, "ALL", "notice", notice);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectWith(req, resp, exportRequestsTab?"/app/manager/export-requests":exportReceiptsTab?"/app/manager/export-receipts":stocktakesTab ? "/app/manager/stocktakes" : receiptsTab ? "/app/manager/import-receipts" : "/app/manager/import-requests",
                    stocktakesTab ? "ALL" : receiptsTab||exportReceiptsTab ? "PENDING_MANAGER" : "PENDING", "error", e.getMessage());
        }
    }

    private User requireManager(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null || !managerService.isActiveManager(user.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Manager role required");
            return null;
        }
        return user;
    }

    private void redirectWith(HttpServletRequest req, HttpServletResponse resp, String path, String status, String key, String value) throws IOException {
        redirect(req, resp, path + "?status=" + URLEncoder.encode(status, StandardCharsets.UTF_8)
                + "&" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
