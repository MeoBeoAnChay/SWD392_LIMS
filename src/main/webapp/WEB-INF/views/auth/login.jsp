<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | LIMS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 flex items-center justify-center min-h-screen p-4 font-sans text-slate-800">

    <div class="relative w-full max-w-md">
        <!-- Glow Effects -->
        <div class="absolute -top-12 -left-12 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-12 -right-12 w-64 h-64 bg-blue-500/20 rounded-full blur-3xl pointer-events-none"></div>

        <!-- Login Card -->
        <div class="relative bg-white/95 backdrop-blur-xl shadow-2xl rounded-3xl p-8 sm:p-10 border border-white/20">
            <!-- Brand Header -->
            <div class="text-center mb-8">
                <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-indigo-600 text-white shadow-lg shadow-indigo-600/30 mb-4">
                    <i class="fa-solid fa-laptop text-2xl"></i>
                </div>
                <h1 class="text-2xl font-black text-slate-900 tracking-tight">Hệ Thống LIMS</h1>
                <p class="text-xs font-medium text-slate-500 mt-1">Laptop Inventory Management System</p>
            </div>

            <!-- Notifications -->
            <c:if test="${param.status eq 'logged_out'}">
                <div class="mb-5 flex items-center gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-xs font-medium text-emerald-800">
                    <i class="fa-solid fa-circle-check text-emerald-600"></i>
                    <span>Bạn đã đăng xuất an toàn khỏi hệ thống.</span>
                </div>
            </c:if>
            <c:if test="${param.status eq 'session_expired'}">
                <div class="mb-5 flex items-center gap-2.5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-xs font-medium text-amber-800">
                    <i class="fa-solid fa-clock-rotate-left text-amber-600"></i>
                    <span>Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.</span>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div class="mb-5 flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-800">
                    <i class="fa-solid fa-circle-exclamation text-rose-600"></i>
                    <span>${errorMessage}</span>
                </div>
            </c:if>

            <!-- Form -->
            <form action="${pageContext.request.contextPath}/login" method="POST" class="space-y-4">
                <div>
                    <label for="username" class="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5">Tên đăng nhập</label>
                    <div class="relative">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                            <i class="fa-solid fa-user text-sm"></i>
                        </span>
                        <input type="text" id="username" name="username" value="${username}" required autofocus
                               placeholder="Tên tài khoản (vd: admin)"
                               class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition">
                    </div>
                </div>

                <div>
                    <label for="password" class="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5">Mật khẩu</label>
                    <div class="relative">
                        <span class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none text-slate-400">
                            <i class="fa-solid fa-lock text-sm"></i>
                        </span>
                        <input type="password" id="password" name="password" required
                               placeholder="Nhập mật khẩu"
                               class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition">
                    </div>
                </div>

                <div class="pt-2">
                    <button type="submit"
                            class="w-full inline-flex items-center justify-center gap-2 rounded-xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-600/30 transition hover:bg-indigo-700 active:scale-[0.99] focus:outline-none focus:ring-2 focus:ring-indigo-500/50">
                        <i class="fa-solid fa-right-to-bracket text-xs"></i>
                        <span>Đăng nhập hệ thống</span>
                    </button>
                </div>
            </form>

            <!-- Test Accounts Demo Box -->
            <div class="mt-8 pt-6 border-t border-slate-100">
                <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2">Tài khoản mẫu kiểm thử:</p>
                <div class="rounded-xl bg-slate-50 p-3 text-xs text-slate-600 space-y-1.5 border border-slate-200/70">
                    <div class="flex items-center justify-between">
                        <span class="text-slate-500">Quản trị:</span>
                        <code class="rounded bg-slate-200/80 px-1.5 py-0.5 font-mono text-indigo-700 font-semibold">admin / 123456</code>
                    </div>
                    <div class="flex items-center justify-between">
                        <span class="text-slate-500">Kho Trưởng:</span>
                        <code class="rounded bg-slate-200/80 px-1.5 py-0.5 font-mono text-indigo-700 font-semibold">manager / 123456</code>
                    </div>
                    <div class="flex items-center justify-between">
                        <span class="text-slate-500">Nhân viên kho:</span>
                        <code class="rounded bg-slate-200/80 px-1.5 py-0.5 font-mono text-indigo-700 font-semibold">warehouse / 123456</code>
                    </div>
                </div>
            </div>
        </div>
    </div>

</body>
</html>