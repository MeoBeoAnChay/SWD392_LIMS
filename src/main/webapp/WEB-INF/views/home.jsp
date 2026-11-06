<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang Chủ - LIMS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 min-h-screen text-slate-800">

    <!-- Top Navbar -->
    <header class="bg-white border-b border-slate-200 shadow-sm sticky top-0 z-10">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-blue-600 text-white flex items-center justify-center font-bold text-lg shadow">
                    <i class="fa-solid fa-laptop"></i>
                </div>
                <div>
                    <h1 class="text-lg font-bold text-slate-800 leading-tight">LIMS</h1>
                    <p class="text-xs text-slate-500">Laptop Inventory Management System</p>
                </div>
            </div>

            <div class="flex items-center gap-4">
                <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                    <c:if test="${r.roleName == 'Admin'}">
                        <a href="${pageContext.request.contextPath}/app/admin/users" class="rounded-lg bg-blue-50 px-3 py-2 text-sm font-medium text-blue-700 border border-blue-200"><i class="fa-solid fa-user-shield mr-1"></i> Quản trị</a>
                    </c:if>
                    <c:if test="${r.roleName == 'Purchasing Staff'}">
                        <a href="${pageContext.request.contextPath}/app/purchasing/dashboard" class="rounded-lg bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700 border border-emerald-200"><i class="fa-solid fa-cart-shopping mr-1"></i> Mua hàng</a>
                    </c:if>
                    <c:if test="${r.roleName == 'Sales Staff'}"><a href="${pageContext.request.contextPath}/app/sales/export-requests" class="rounded-lg bg-indigo-50 px-3 py-2 text-sm font-medium text-indigo-700 border border-indigo-200"><i class="fa-solid fa-file-export mr-1"></i> Yêu cầu xuất kho</a></c:if>
                    <c:if test="${r.roleName == 'Manager'}">
                        <a href="${pageContext.request.contextPath}/app/manager/export-requests" class="rounded-lg bg-indigo-50 px-3 py-2 text-sm font-medium text-indigo-700 border border-indigo-200"><i class="fa-solid fa-file-export mr-1"></i> Duyệt yêu cầu xuất</a>
                        <a href="${pageContext.request.contextPath}/app/manager/export-receipts" class="rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-700 border border-amber-200"><i class="fa-solid fa-box-open mr-1"></i> Duyệt phiếu xuất</a>
                        <a href="${pageContext.request.contextPath}/app/manager/import-requests" class="rounded-lg bg-indigo-50 px-3 py-2 text-sm font-medium text-indigo-700 border border-indigo-200"><i class="fa-solid fa-clipboard-check mr-1"></i> Duyệt đơn mua</a>
                        <a href="${pageContext.request.contextPath}/app/manager/import-receipts" class="rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-700 border border-amber-200"><i class="fa-solid fa-boxes-stacked mr-1"></i> Duyệt phiếu nhập</a>
                        <a href="${pageContext.request.contextPath}/app/manager/stocktakes" class="rounded-lg bg-violet-50 px-3 py-2 text-sm font-medium text-violet-700 border border-violet-200"><i class="fa-solid fa-clipboard-list mr-1"></i> Kiểm kê kho</a>
                    </c:if>
                    <c:if test="${r.roleName == 'Manager'}"><a href="${pageContext.request.contextPath}/app/manager/catalog?tab=products" class="rounded-lg bg-slate-100 px-3 py-2 text-sm font-medium text-slate-700 border border-slate-200"><i class="fa-solid fa-database mr-1"></i> Master Data</a></c:if>
                    <c:if test="${r.roleName == 'Warehouse Staff'}">
                        <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" class="rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-700 border border-amber-200"><i class="fa-solid fa-box-open mr-1"></i> Soạn hàng xuất</a>
                        <a href="${pageContext.request.contextPath}/app/warehouse/handovers" class="rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-700 border border-amber-200"><i class="fa-solid fa-warehouse mr-1"></i> Tiếp nhận hàng</a>
                        <a href="${pageContext.request.contextPath}/app/warehouse/stocktakes" class="rounded-lg bg-violet-50 px-3 py-2 text-sm font-medium text-violet-700 border border-violet-200"><i class="fa-solid fa-clipboard-list mr-1"></i> Kiểm kho</a>
                    </c:if>
                </c:forEach>
                <div class="text-right">
                    <p class="text-sm font-semibold text-slate-800">${sessionScope.LOGIN_USER.fullName}</p>
                    <p class="text-xs text-slate-500">
                        <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}" varStatus="status">
                            <span class="inline-block bg-blue-100 text-blue-700 px-2 py-0.5 rounded text-[11px] font-medium mr-1">${r.roleName}</span>
                        </c:forEach>
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/logout"
                   class="bg-rose-50 hover:bg-rose-100 text-rose-600 px-3 py-2 rounded-lg text-sm font-medium border border-rose-200 transition flex items-center gap-2">
                    <i class="fa-solid fa-arrow-right-from-bracket"></i>
                    <span>Đăng xuất</span>
                </a>
            </div>
        </div>
    </header>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 sm:p-8">
            <div class="flex items-center gap-4 pb-6 border-b border-slate-100">
                <div class="w-14 h-14 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center text-2xl shadow-inner">
                    <i class="fa-solid fa-circle-check"></i>
                </div>
                <div>
                    <h2 class="text-xl font-bold text-slate-800">Đăng nhập thành công!</h2>
                    <p class="text-sm text-slate-500 mt-1">Chào mừng <span class="font-semibold text-slate-700">${sessionScope.LOGIN_USER.fullName}</span> (@${sessionScope.LOGIN_USER.username}) đã vào hệ thống.</p>
                </div>
            </div>

            <!-- User Info Card -->
            <div class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="bg-slate-50 p-4 rounded-lg border border-slate-200 space-y-2 text-sm">
                    <p class="font-semibold text-slate-700 text-xs uppercase tracking-wider text-slate-400">Thông tin tài khoản</p>
                    <p><span class="text-slate-500">Tên đăng nhập:</span> <span class="font-medium">${sessionScope.LOGIN_USER.username}</span></p>
                    <p><span class="text-slate-500">Họ và tên:</span> <span class="font-medium">${sessionScope.LOGIN_USER.fullName}</span></p>
                    <p><span class="text-slate-500">Email:</span> <span class="font-medium">${sessionScope.LOGIN_USER.email}</span></p>
                    <p><span class="text-slate-500">Số điện thoại:</span> <span class="font-medium">${empty sessionScope.LOGIN_USER.phone ? 'Chưa cập nhật' : sessionScope.LOGIN_USER.phone}</span></p>
                </div>

                <div class="bg-slate-50 p-4 rounded-lg border border-slate-200 space-y-2 text-sm">
                    <p class="font-semibold text-slate-700 text-xs uppercase tracking-wider text-slate-400">Vai trò & Phân quyền (RBAC)</p>
                    <div class="flex flex-wrap gap-2 pt-1">
                        <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                            <div class="bg-white border border-slate-300 rounded px-2.5 py-1 text-xs">
                                <span class="font-semibold text-blue-700">${r.roleName}</span>
                                <c:if test="${not empty r.description}">
                                    <span class="text-slate-400 block text-[11px]">${r.description}</span>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
    </main>

</body>
</html>
