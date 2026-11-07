package com.lims.controller.sales;

import com.lims.controller.BaseServlet;
import com.lims.model.User;
import com.lims.service.ExportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "SalesServlet", urlPatterns = {
        "/app/sales/export-requests",
        "/app/sales/export-request-form",
        "/app/sales/export-history"
})
public class SalesServlet extends BaseServlet {

    private final ExportService service = new ExportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User u = getCurrentUser(req);
        if (u == null || !service.sales(u.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sales Staff role required");
            return;
        }

        req.setAttribute("user", u);
        req.setAttribute("notice", req.getParameter("notice"));
        req.setAttribute("error", req.getParameter("error"));

        String path = req.getServletPath();
        if (path.endsWith("/export-request-form")) {
            req.setAttribute("activeSalesMenu", "export-request-form");
            req.setAttribute("customers", service.customers());
            req.setAttribute("products", service.products());
            forward(req, resp, "sales/export-request-form.jsp");
        } else if (path.endsWith("/export-history")) {
            req.setAttribute("activeSalesMenu", "export-history");
            req.setAttribute("receipts", service.receiptsBySalesStaff(u.getUserId()));
            forward(req, resp, "sales/export-history.jsp");
        } else {
            req.setAttribute("activeSalesMenu", "exports");
            String status = req.getParameter("status");
            String search = req.getParameter("search");
            req.setAttribute("currentStatus", status == null ? "ALL" : status.toUpperCase());
            req.setAttribute("searchKeyword", search == null ? "" : search.trim());
            req.setAttribute("requests", service.salesRequests(u.getUserId(), status, search));
            forward(req, resp, "sales/export-requests.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        User u = getCurrentUser(req);
        if (u == null || !service.sales(u.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Sales Staff role required");
            return;
        }

        try {
            int id = service.createRequest(
                    u,
                    req.getParameter("customerId"),
                    req.getParameterValues("productIds"),
                    req.getParameterValues("quantities"),
                    getClientIP(req),
                    req.getHeader("User-Agent")
            );
            go(req, resp, "notice", "Đã gửi yêu cầu xuất kho #" + id + " cho Manager duyệt.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            go(req, resp, "error", e.getMessage());
        }
    }

    private void go(HttpServletRequest req, HttpServletResponse resp, String k, String v) throws IOException {
        redirect(req, resp, "/app/sales/export-requests?" + k + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8));
    }
}
