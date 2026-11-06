package com.lims.controller;

import com.lims.model.User;
import com.lims.service.PurchasingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "PurchasingServlet", urlPatterns = {"/app/purchasing/requests"})
public class PurchasingServlet extends BaseServlet {
    private final PurchasingService purchasingService = new PurchasingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null || !purchasingService.isActivePurchasingStaff(user.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Purchasing Staff role required");
            return;
        }
        req.setAttribute("user", user);
        req.setAttribute("suppliers", purchasingService.getSuppliers());
        req.setAttribute("products", purchasingService.getProducts());
        req.setAttribute("requests", purchasingService.getRequests(user.getUserId()));
        req.setAttribute("notice", req.getParameter("notice"));
        req.setAttribute("error", req.getParameter("error"));
        forward(req, resp, "purchasing.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = getCurrentUser(req);
        if (user == null || !purchasingService.isActivePurchasingStaff(user.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Purchasing Staff role required");
            return;
        }
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                int id = purchasingService.createRequest(user, req.getParameter("supplierId"), req.getParameterValues("productIds"),
                        req.getParameterValues("quantities"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "notice", "Đã tạo yêu cầu nhập #" + id);
            } else if ("update".equals(action)) {
                purchasingService.updateRequest(user, req.getParameter("requestId"), req.getParameter("supplierId"),
                        req.getParameterValues("productIds"), req.getParameterValues("quantities"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "notice", "Đã cập nhật yêu cầu nhập.");
            } else if ("cancel".equals(action)) {
                purchasingService.cancelRequest(user, req.getParameter("requestId"), req.getParameter("reason"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "notice", "Đã hủy yêu cầu nhập.");
            } else if ("handover".equals(action)) {
                purchasingService.createHandover(user, req.getParameter("requestId"), req.getParameter("handoverBy"),
                        req.getParameter("quantity"), req.getParameter("note"), getClientIP(req), req.getHeader("User-Agent"));
                redirectWith(req, resp, "notice", "Đã tạo phiếu bàn giao.");
            } else {
                redirectWith(req, resp, "error", "Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectWith(req, resp, "error", e.getMessage());
        }
    }

    private void redirectWith(HttpServletRequest req, HttpServletResponse resp, String key, String value) throws IOException {
        redirect(req, resp, "/app/purchasing/requests?" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}
