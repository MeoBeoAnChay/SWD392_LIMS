package com.lims.controller;

import com.google.gson.Gson;
import com.lims.util.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Base Abstract Servlet providing shared HTTP utility methods for all controllers.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Gson gson = new Gson();

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jspPath)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        req.getRequestDispatcher("/WEB-INF/views/" + jspPath).forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void sendJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    @SuppressWarnings("unchecked")
    protected <T> T getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (T) session.getAttribute(Constants.SESSION_USER) : null;
    }

    protected String getClientIP(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
