<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="com.lims.util.Constants" %>
<%@ page import="com.lims.model.User" %>
<%
    User currentUser = (User) session.getAttribute(Constants.SESSION_USER);
    if (currentUser != null) {
        response.sendRedirect(request.getContextPath() + currentUser.getDefaultRedirectUrl());
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
