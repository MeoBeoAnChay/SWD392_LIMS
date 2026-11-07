package com.lims.controller.auth;

import com.lims.controller.BaseServlet;

import com.lims.model.User;
import com.lims.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller handling user logout and session invalidation.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(com.lims.util.Constants.SESSION_USER);
            userService.recordLogout(user, getClientIP(req), req.getHeader("User-Agent"));
            session.invalidate();
        }
        redirect(req, resp, "/login?status=logged_out");
    }
}
