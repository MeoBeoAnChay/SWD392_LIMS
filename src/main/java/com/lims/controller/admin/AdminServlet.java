package com.lims.controller.admin;

import com.lims.controller.BaseServlet;

import com.lims.dao.AuditLogDAO;
import com.lims.dao.UserDAO;
import com.lims.model.Role;
import com.lims.model.User;
import com.lims.service.AuditLogService;
import com.lims.util.Constants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "AdminServlet", urlPatterns = {"/app/admin/users", "/app/admin/audit"})
public class AdminServlet extends BaseServlet {
    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User actor = getCurrentUser(req);
        if (actor == null || !userDAO.isActiveAdmin(actor.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin role required");
            return;
        }
        String tab = req.getServletPath().endsWith("/audit") ? "audit" : "users";
        req.setAttribute("tab", tab);
        req.setAttribute("users", userDAO.findAll());
        req.setAttribute("roles", userDAO.findAllRoles());
        if ("audit".equals(tab)) {
            String action = req.getParameter("action");
            String username = req.getParameter("username");
            req.setAttribute("actionFilter", action == null ? "" : action);
            req.setAttribute("usernameFilter", username == null ? "" : username);
            req.setAttribute("auditActions", Arrays.asList("LOGIN", "LOGOUT", "CREATE", "UPDATE", "UPDATE_ROLES", "APPROVE", "REJECT"));
            req.setAttribute("auditLogs", auditLogDAO.findRecent(action, username));
        }
        req.setAttribute("notice", req.getParameter("notice"));
        req.setAttribute("error", req.getParameter("error"));
        forward(req, resp, "admin/admin.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User actor = getCurrentUser(req);
        if (actor == null || !userDAO.isActiveAdmin(actor.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin role required");
            return;
        }
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String result;
        if ("create".equals(action)) result = createUser(req, actor);
        else if ("update".equals(action)) result = updateUser(req, actor);
        else if ("roles".equals(action)) result = updateRoles(req, actor);
        else result = "error=Thao+t%C3%A1c+kh%C3%B4ng+h%E1%BB%A3p+l%E1%BB%87";
        redirect(req, resp, "/app/admin/users?" + result);
    }

    private String createUser(HttpServletRequest req, User actor) {
        String username = value(req, "username");
        String fullName = value(req, "fullName");
        String email = value(req, "email");
        String password = req.getParameter("password");
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password == null || password.length() < 8) {
            return "error=Vui+l%C3%B2ng+nh%E1%BA%ADp+%C4%91%E1%BB%A7+th%C3%B4ng+tin%2C+m%E1%BA%ADt+kh%E1%BA%A9u+t%E1%BB%91i+thi%E1%BB%83u+8+k%C3%BD+t%E1%BB%B1";
        }
        User user = new User(0, username, null, fullName, email, value(req, "phone"), "ACTIVE", null);
        List<Integer> roleIds = roleIds(req.getParameterValues("roleIds"));
        if (roleIds.isEmpty()) return "error=H%C3%A3y+ch%E1%BB%8Dn+%C3%ADt+nh%E1%BA%A5t+m%E1%BB%99t+vai+tr%C3%B2";
        if (!userDAO.create(user, password, roleIds)) return "error=Kh%C3%B4ng+th%E1%BB%83+t%E1%BA%A1o+t%C3%A0i+kho%E1%BA%A3n%3A+username+ho%E1%BA%B7c+email+c%C3%B3+th%E1%BB%83+%C4%91%C3%A3+t%E1%BB%93n+t%E1%BA%A1i";
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "CREATE", "USERS", String.valueOf(user.getUserId()), null,
                safeUserSummary(user, roleIds), getClientIP(req), req.getHeader("User-Agent"));
        return "notice=%C4%90%C3%A3+t%E1%BA%A1o+t%C3%A0i+kho%E1%BA%A3n";
    }

    private String updateUser(HttpServletRequest req, User actor) {
        int id = intParam(req, "userId");
        User before = findUser(id);
        if (before == null) return "error=Kh%C3%B4ng+t%C3%ACm+th%E1%BA%A5y+t%C3%A0i+kho%E1%BA%A3n";
        String status = value(req, "status");
        if (!Arrays.asList("ACTIVE", "INACTIVE", "LOCKED").contains(status)) return "error=Tr%E1%BA%A1ng+th%C3%A1i+kh%C3%B4ng+h%E1%BB%A3p+l%E1%BB%87";
        if (id == actor.getUserId() && !"ACTIVE".equals(status)) return "error=Kh%C3%B4ng+th%E1%BB%83+v%C3%B4+hi%E1%BB%87u+h%C3%B3a+t%C3%A0i+kho%E1%BA%A3n+%C4%91ang+s%E1%BB%AD+d%E1%BB%A5ng";
        if ("ACTIVE".equals(before.getStatus()) && !"ACTIVE".equals(status) && before.hasRole(Constants.ROLE_ADMIN)
                && userDAO.countActiveAdmins() <= 1) return "error=H%E1%BB%87+th%E1%BB%91ng+ph%E1%BA%A3i+c%C3%B3+%C3%ADt+nh%E1%BA%A5t+m%E1%BB%99t+Admin+ho%E1%BA%A1t+%C4%91%E1%BB%99ng";
        User updated = new User(id, before.getUsername(), null, value(req, "fullName"), value(req, "email"), value(req, "phone"), status, before.getCreatedAt());
        if (updated.getFullName().isEmpty() || updated.getEmail().isEmpty()) return "error=H%E1%BB%8D+t%C3%AAn+v%C3%A0+email+l%C3%A0+b%E1%BA%AFt+bu%E1%BB%99c";
        if (!userDAO.update(updated)) return "error=C%E1%BA%ADp+nh%E1%BA%ADt+th%E1%BA%A5t+b%E1%BA%A1i%2C+ki%E1%BB%83m+tra+email+tr%C3%B9ng";
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "UPDATE", "USERS", String.valueOf(id),
                safeUserSummary(before, null), safeUserSummary(updated, null), getClientIP(req), req.getHeader("User-Agent"));
        return "notice=%C4%90%C3%A3+c%E1%BA%ADp+nh%E1%BA%ADt+t%C3%A0i+kho%E1%BA%A3n";
    }

    private String updateRoles(HttpServletRequest req, User actor) {
        int id = intParam(req, "userId");
        User user = findUser(id);
        if (user == null) return "error=Kh%C3%B4ng+t%C3%ACm+th%E1%BA%A5y+t%C3%A0i+kho%E1%BA%A3n";
        List<Integer> ids = roleIds(req.getParameterValues("roleIds"));
        if (ids.isEmpty()) return "error=T%C3%A0i+kho%E1%BA%A3n+ph%E1%BA%A3i+c%C3%B3+%C3%ADt+nh%E1%BA%A5t+m%E1%BB%99t+vai+tr%C3%B2";
        List<Role> allRoles = userDAO.findAllRoles();
        boolean hasAdmin = allRoles.stream().anyMatch(role -> ids.contains(role.getRoleId()) && Constants.ROLE_ADMIN.equalsIgnoreCase(role.getRoleName()));
        if (id == actor.getUserId() && !hasAdmin) return "error=Kh%C3%B4ng+th%E1%BB%83+x%C3%B3a+quy%E1%BB%81n+Admin+c%E1%BB%A7a+t%C3%A0i+kho%E1%BA%A3n+%C4%91ang+d%C3%B9ng";
        if ("ACTIVE".equalsIgnoreCase(user.getStatus()) && user.hasRole(Constants.ROLE_ADMIN) && !hasAdmin
                && userDAO.countActiveAdmins() <= 1) return "error=H%E1%BB%87+th%E1%BB%91ng+ph%E1%BA%A3i+c%C3%B3+%C3%ADt+nh%E1%BA%A5t+m%E1%BB%99t+Admin+ho%E1%BA%A1t+%C4%91%E1%BB%99ng";
        String before = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.joining(", "));
        if (!userDAO.updateRoles(id, ids)) return "error=C%E1%BA%ADp+nh%E1%BA%ADt+vai+tr%C3%B2+th%E1%BA%A5t+b%E1%BA%A1i";
        List<String> after = allRoles.stream().filter(r -> ids.contains(r.getRoleId())).map(Role::getRoleName).collect(Collectors.toList());
        auditLogService.logAction(actor.getUserId(), actor.getUsername(), "UPDATE_ROLES", "USER_ROLES", String.valueOf(id), before, after,
                getClientIP(req), req.getHeader("User-Agent"));
        return "notice=%C4%90%C3%A3+c%E1%BA%ADp+nh%E1%BA%ADt+vai+tr%C3%B2";
    }

    private User findUser(int id) {
        return userDAO.findAll().stream().filter(user -> user.getUserId() == id).findFirst().orElse(null);
    }

    private List<Integer> roleIds(String[] values) {
        List<Integer> ids = new ArrayList<>();
        if (values != null) for (String value : values) try { ids.add(Integer.parseInt(value)); } catch (NumberFormatException ignored) { }
        return ids;
    }

    private int intParam(HttpServletRequest req, String name) {
        try { return Integer.parseInt(req.getParameter(name)); } catch (Exception e) { return -1; }
    }

    private String value(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private String safeUserSummary(User user, Object roles) {
        return "username=" + user.getUsername() + "; fullName=" + user.getFullName() + "; email=" + user.getEmail()
                + "; phone=" + user.getPhone() + "; status=" + user.getStatus() + (roles == null ? "" : "; roleIds=" + roles);
    }
}
