<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - LIMS (Laptop Inventory Management System)</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-100 flex items-center justify-center min-h-screen p-4 font-sans">

    <div class="bg-white shadow-xl rounded-2xl w-full max-w-md p-8 border border-slate-200">
        <!-- Logo / Title -->
        <div class="text-center mb-8">
            <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 text-blue-600 mb-3 shadow-inner">
                <i class="fa-solid fa-laptop text-2xl"></i>
            </div>
            <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Hệ Thống LIMS</h1>
            <p class="text-sm text-slate-500 mt-1">Laptop Inventory Management System</p>
        </div>

        <!-- Success/Alert Notifications -->
        <c:if test="${param.status eq 'logged_out'}">
            <div class="mb-5 bg-emerald-50 border border-emerald-200 text-emerald-700 px-4 py-3 rounded-lg text-sm flex items-center gap-2">
                <i class="fa-solid fa-circle-check"></i>
                <span>Bạn đã đăng xuất khỏi hệ thống thành công.</span>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="mb-5 bg-rose-50 border border-rose-200 text-rose-700 px-4 py-3 rounded-lg text-sm flex items-center gap-2">
                <i class="fa-solid fa-triangle-exclamation"></i>
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <!-- Login Form -->
        <form action="${pageContext.request.contextPath}/login" method="POST" class="space-y-5">
            <div>
                <label for="username" class="block text-sm font-semibold text-slate-700 mb-1">Tên đăng nhập</label>
                <div class="relative">
                    <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                        <i class="fa-solid fa-user"></i>
                    </span>
                    <input type="text" id="username" name="username" value="${username}" required
                           placeholder="Nhập username (vd: admin)"
                           class="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-lg text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition">
                </div>
            </div>

            <div>
                <label for="password" class="block text-sm font-semibold text-slate-700 mb-1">Mật khẩu</label>
                <div class="relative">
                    <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                        <i class="fa-solid fa-lock"></i>
                    </span>
                    <input type="password" id="password" name="password" required
                           placeholder="Nhập mật khẩu"
                           class="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-lg text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition">
                </div>
            </div>

            <button type="submit"
                    class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 px-4 rounded-lg shadow transition duration-150 flex items-center justify-center gap-2">
                <i class="fa-solid fa-right-to-bracket"></i>
                <span>Đăng nhập</span>
            </button>
        </form>

        <!-- Test Account Hint Box -->
        <div class="mt-8 pt-6 border-t border-slate-200">
            <p class="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Tài khoản kiểm thử mẫu:</p>
            <div class="bg-slate-50 p-3 rounded-lg text-xs text-slate-600 space-y-1 border border-slate-200">
                <p><span class="font-semibold text-slate-700">Username:</span> <code class="bg-slate-200 px-1 py-0.5 rounded text-blue-700">admin</code> | <code class="bg-slate-200 px-1 py-0.5 rounded text-blue-700">manager</code> | <code class="bg-slate-200 px-1 py-0.5 rounded text-blue-700">warehouse</code></p>
                <p><span class="font-semibold text-slate-700">Mật khẩu mặc định:</span> <code class="bg-slate-200 px-1 py-0.5 rounded text-rose-700">admin123</code></p>
            </div>
        </div>
    </div>

</body>
</html>
