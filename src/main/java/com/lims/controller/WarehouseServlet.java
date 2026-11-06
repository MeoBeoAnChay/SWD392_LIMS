package com.lims.controller;

import com.lims.model.Handover;
import com.lims.model.User;
import com.lims.service.WarehouseService;
import com.lims.service.StocktakeService;
import com.lims.service.PurchasingService;
import com.lims.model.Stocktake;
import com.lims.service.ExportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "WarehouseServlet", urlPatterns = {
        "/app/warehouse/dashboard",
        "/app/warehouse/handovers", "/app/warehouse/import-history", "/app/warehouse/import-receipt-form", "/app/warehouse/import-receipts",
        "/app/warehouse/stocktakes", "/app/warehouse/stocktake-form", "/app/warehouse/export-requests", "/app/warehouse/export-history",
        "/app/warehouse/export-form", "/app/warehouse/export-receipts"
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
        if(req.getServletPath().endsWith("/dashboard")){
            req.setAttribute("activeWarehouseMenu","dashboard");
            java.util.List<Handover> allHandovers=warehouseService.getAssignedHandovers(user.getUserId());
            req.setAttribute("handovers",allHandovers);
            req.setAttribute("importHistory",allHandovers.stream().filter(h->!"WAITING".equals(h.getReceiptStatus())&& !"REJECTED".equals(h.getReceiptStatus())).toList());
            req.setAttribute("exportRequests",exportService.assignedRequests(user.getUserId()));
            req.setAttribute("exportReceipts",exportService.receiptsByStaff(user.getUserId()));
            req.setAttribute("stocktakes",stocktakeService.assigned(user.getUserId()));
            forward(req,resp,"warehouse-dashboard.jsp");
        } else if(req.getServletPath().endsWith("/export-form")){
            req.setAttribute("activeWarehouseMenu","exports");
            int id=parseId(req.getParameter("requestId"));com.lims.model.ExportRequest request=exportService.request(id);
            if(request==null||request.getAssignedTo()!=user.getUserId()||!"APPROVED".equals(request.getStatus())){redirectWith(req,resp,"/app/warehouse/export-requests","error","Yêu cầu không được giao cho bạn hoặc không còn chờ soạn hàng.");return;}
            req.setAttribute("exportRequest",request);req.setAttribute("availableSerials",exportService.available(id,user.getUserId()));forward(req,resp,"warehouse-export-form.jsp");
        } else if (req.getServletPath().endsWith("/stocktake-form")) {
            req.setAttribute("activeWarehouseMenu","stocktakes");
            int id=parseId(req.getParameter("checkId")); Stocktake check=stocktakeService.find(id);
            if(check==null||check.getAssignedTo()!=user.getUserId()||!("ASSIGNED".equals(check.getStatus())||"REJECTED".equals(check.getStatus()))){redirectWith(req,resp,"/app/warehouse/stocktakes","error","Đợt kiểm kho không được giao cho bạn hoặc đã gửi duyệt.");return;}
            req.setAttribute("check",check);req.setAttribute("products",purchasingService.getProducts());forward(req,resp,"warehouse-stocktake-form.jsp");
        } else if (req.getServletPath().endsWith("/import-receipt-form")) {
            req.setAttribute("activeWarehouseMenu","handovers");
            int handoverId = parseId(req.getParameter("handoverId"));
            Handover handover = warehouseService.getAvailableHandover(handoverId, user.getUserId());
            if (handover == null) { redirectWith(req, resp, "/app/warehouse/handovers", "error", "Không tìm thấy phiếu bàn giao được giao cho bạn hoặc phiếu đã gửi duyệt."); return; }
            req.setAttribute("handover", handover);
            forward(req, resp, "warehouse-import-receipt-form.jsp");
        } else if(req.getServletPath().endsWith("/export-history")){
            req.setAttribute("activeWarehouseMenu","exports");req.setAttribute("exportReceipts",exportService.receiptsByStaff(user.getUserId()));forward(req,resp,"warehouse-export-history.jsp");
        } else if(req.getServletPath().endsWith("/export-requests")){
            req.setAttribute("activeWarehouseMenu","exports");
            req.setAttribute("exportRequests",exportService.assignedRequests(user.getUserId()));forward(req,resp,"warehouse-export-requests.jsp");
        } else if(req.getServletPath().endsWith("/stocktakes")) {
            req.setAttribute("activeWarehouseMenu","stocktakes");
            req.setAttribute("stocktakes",stocktakeService.assigned(user.getUserId()));forward(req,resp,"warehouse-stocktakes.jsp");
        } else if(req.getServletPath().endsWith("/import-history")) {
            req.setAttribute("activeWarehouseMenu","handovers");
            req.setAttribute("handovers",warehouseService.getAssignedHandovers(user.getUserId()).stream().filter(h->!"WAITING".equals(h.getReceiptStatus())&& !"REJECTED".equals(h.getReceiptStatus())).toList());
            forward(req,resp,"warehouse-import-history.jsp");
        } else {
            req.setAttribute("activeWarehouseMenu","handovers");
            req.setAttribute("handovers", warehouseService.getAssignedHandovers(user.getUserId()).stream().filter(h->"WAITING".equals(h.getReceiptStatus())||"REJECTED".equals(h.getReceiptStatus())).toList());
            forward(req, resp, "warehouse-handovers.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        User user = requireWarehouseStaff(req, resp);
        if (user == null) return;
        try {
            if(req.getServletPath().endsWith("/export-receipts")){
                int receipt=exportService.createReceipt(user,req.getParameter("requestId"),req.getParameterValues("serialNumbers"),req.getParameter("note"),getClientIP(req),req.getHeader("User-Agent"));
                redirectWith(req,resp,"/app/warehouse/export-requests","notice","Đã gửi phiếu xuất #"+receipt+" cho Manager duyệt; hàng đang được giữ chờ duyệt.");return;
            }
            if(req.getServletPath().endsWith("/stocktakes")){
                stocktakeService.submit(user,parseId(req.getParameter("checkId")),req.getParameterValues("detailIds"),req.getParameterValues("productIds"),req.getParameterValues("serialNumbers"),req.getParameterValues("actualStatuses"),req.getParameterValues("actualLocations"),req.getParameterValues("reasons"),getClientIP(req),req.getHeader("User-Agent"));
                redirectWith(req,resp,"/app/warehouse/stocktakes","notice","Đã gửi kết quả kiểm kho cho Manager duyệt. Tồn kho chưa thay đổi.");return;
            }
            int receiptId = warehouseService.createImportReceipt(user, req.getParameter("handoverId"),
                    req.getParameterValues("productIds"), req.getParameterValues("serialNumbers"),
                    req.getParameterValues("locations"), req.getParameterValues("conditions"), req.getParameter("note"),
                    req.getParameter("paperSigned") != null, getClientIP(req), req.getHeader("User-Agent"));
            redirectWith(req, resp, "/app/warehouse/handovers", "notice", "Đã gửi phiếu nhập kho #" + receiptId + " cho Manager duyệt. Tồn kho chưa được cập nhật.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if(req.getServletPath().endsWith("/export-receipts")){String url="/app/warehouse/export-form?requestId="+req.getParameter("requestId")+"&error="+URLEncoder.encode(e.getMessage(),StandardCharsets.UTF_8);redirect(req,resp,url);return;}
            if(req.getServletPath().endsWith("/stocktakes")){String url="/app/warehouse/stocktake-form?checkId="+req.getParameter("checkId")+"&error="+URLEncoder.encode(e.getMessage(),StandardCharsets.UTF_8);redirect(req,resp,url);return;}
            String target = "/app/warehouse/import-receipt-form?handoverId=" + URLEncoder.encode(
                    req.getParameter("handoverId") == null ? "" : req.getParameter("handoverId"), StandardCharsets.UTF_8)
                    + "&error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            redirect(req, resp, target);
        }
    }

    private User requireWarehouseStaff(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null || !warehouseService.isActiveWarehouseStaff(user.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Warehouse Staff role required"); return null;
        }
        return user;
    }

    private int parseId(String value) { try { return Integer.parseInt(value); } catch (Exception e) { return -1; } }

    private void redirectWith(HttpServletRequest req, HttpServletResponse resp, String path, String key, String value) throws IOException {
        redirect(req, resp, path + "?" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
