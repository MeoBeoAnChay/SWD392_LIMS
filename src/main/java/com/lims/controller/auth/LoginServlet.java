package com.lims.controller.auth;

import com.lims.controller.BaseServlet;

import com.lims.model.User;
import com.lims.service.UserService;
import com.lims.util.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller handling user login and authentication requests.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser != null) {
            redirect(req, resp, currentUser.getDefaultRedirectUrl());
            return;
        }
        forward(req, resp, "auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("errorMessage", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            req.setAttribute("username", username);
            forward(req, resp, "auth/login.jsp");
            return;
        }

        User user = userService.authenticate(username, password, getClientIP(req), req.getHeader("User-Agent"));

        if (user != null) {
            HttpSession session = req.getSession(true);
            req.changeSessionId();
            session.setAttribute(Constants.SESSION_USER, user);
            redirect(req, resp, user.getDefaultRedirectUrl());
        } else {
            req.setAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác (hoặc tài khoản đã bị khóa).");
            req.setAttribute("username", username);
            forward(req, resp, "auth/login.jsp");
        }
    }
}
