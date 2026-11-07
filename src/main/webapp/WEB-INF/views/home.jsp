<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cổng làm việc | LIMS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 min-h-screen text-slate-800 flex flex-col">

    <!-- Top Navigation -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-sm backdrop-blur">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-indigo-600 text-white flex items-center justify-center font-bold text-lg shadow-md shadow-indigo-600/30">
                    <i class="fa-solid fa-laptop"></i>
                </div>
                <div>
                    <h1 class="text-base font-bold text-slate-900 leading-tight">LIMS</h1>
                    <p class="text-[11px] text-slate-500 font-medium">Laptop Inventory Management System</p>
                </div>
            </div>

            <div class="flex items-center gap-3">
                <div class="hidden sm:flex items-center gap-3 pr-2">
                    <div class="w-9 h-9 rounded-full bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-xs uppercase">
                        ${fn:substring(sessionScope.LOGIN_USER.fullName, 0, 2)}
                    </div>
                    <div class="text-right">
                        <p class="text-xs font-bold text-slate-800 leading-tight">${sessionScope.LOGIN_USER.fullName}</p>
                        <p class="text-[11px] text-slate-400">@${sessionScope.LOGIN_USER.username}</p>
                    </div>
                </div>

                <a href="${pageContext.request.contextPath}/logout"
                   class="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-rose-600 transition hover:bg-rose-50 hover:border-rose-200 shadow-sm">
                    <i class="fa-solid fa-arrow-right-from-bracket"></i>
                    <span>Đăng xuất</span>
                </a>
            </div>
        </div>
    </header>

    <!-- Main Content -->
    <main class="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <!-- Hero Welcome Card -->
        <div class="relative overflow-hidden rounded-3xl bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white p-6 sm:p-8 shadow-xl">
            <div class="relative z-10 max-w-2xl">
                <span class="inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1 text-xs font-medium backdrop-blur border border-white/10 text-indigo-200">
                    <i class="fa-solid fa-sparkles text-[10px]"></i> Cổng điều hướng phân quyền (Role-Based Portal)
                </span>
                <h2 class="mt-4 text-2xl sm:text-3xl font-extrabold tracking-tight">
                    Xin chào, ${sessionScope.LOGIN_USER.fullName}!
                </h2>
                <p class="mt-2 text-sm text-slate-300 leading-relaxed">
                    Hệ thống quản lý kho máy tính xách tay LIMS. Bạn được phân bổ các vai trò dưới đây, vui lòng chọn không gian làm việc tương ứng để bắt đầu tác nghiệp.
                </p>
                <div class="mt-4 flex flex-wrap gap-2">
                    <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                        <span class="inline-flex items-center gap-1.5 rounded-lg bg-white/10 px-2.5 py-1 text-xs font-semibold text-white backdrop-blur border border-white/10">
                            <i class="fa-solid fa-shield text-[10px] text-indigo-400"></i> ${r.roleName}
                        </span>
                    </c:forEach>
                </div>
            </div>
            <!-- Background Decorative circles -->
            <div class="absolute -right-10 -bottom-10 w-72 h-72 rounded-full bg-indigo-500/10 blur-3xl pointer-events-none"></div>
            <div class="absolute right-32 top-0 w-48 h-48 rounded-full bg-blue-500/10 blur-2xl pointer-events-none"></div>
        </div>

        <!-- Role Workspace Hubs Grid -->
        <div>
            <div class="flex items-center justify-between mb-5">
                <div>
                    <h3 class="text-lg font-bold text-slate-900">Không gian làm việc của bạn</h3>
                    <p class="text-xs text-slate-500">Truy cập trực tiếp vào các phân hệ nghiệp vụ theo quyền được cấp</p>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <!-- 1. Manager Workspace Card -->
                <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                    <c:if test="${r.roleName eq 'Manager'}">
                        <div class="group relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-indigo-300">
                            <div>
                                <div class="flex items-center justify-between">
                                    <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-indigo-50 text-indigo-600 text-xl shadow-inner group-hover:bg-indigo-600 group-hover:text-white transition">
                                        <i class="fa-solid fa-chart-pie"></i>
                                    </span>
                                    <span class="rounded-full bg-indigo-50 px-2.5 py-1 text-[11px] font-bold text-indigo-700 ring-1 ring-inset ring-indigo-600/20">
                                        Manager
                                    </span>
                                </div>
                                <h4 class="mt-4 text-base font-bold text-slate-900">Quản lý & Kho Trưởng</h4>
                                <p class="mt-1 text-xs text-slate-500 leading-relaxed">
                                    Theo dõi tổng quan tồn kho, vị trí lưu trữ, phê duyệt đơn mua, duyệt nhập/xuất kho và kiểm kê.
                                </p>
                                <div class="mt-4 space-y-1.5 border-t border-slate-100 pt-3 text-xs text-slate-600">
                                    <a href="${pageContext.request.contextPath}/app/manager/dashboard" class="flex items-center gap-2 py-1 hover:text-indigo-600">
                                        <i class="fa-solid fa-chart-simple w-4 text-slate-400"></i> Dashboard & Tồn kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/manager/import-requests" class="flex items-center gap-2 py-1 hover:text-indigo-600">
                                        <i class="fa-solid fa-clipboard-check w-4 text-slate-400"></i> Phê duyệt Nhập / Xuất kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/manager/catalog?tab=products" class="flex items-center gap-2 py-1 hover:text-indigo-600">
                                        <i class="fa-solid fa-cubes w-4 text-slate-400"></i> Quản lý Master Data & Danh mục
                                    </a>
                                </div>
                            </div>
                            <div class="mt-6 pt-4 border-t border-slate-100">
                                <a href="${pageContext.request.contextPath}/app/manager/dashboard" 
                                   class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-indigo-700">
                                    <span>Vào phân hệ Manager</span>
                                    <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                </a>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>

                <!-- 2. Warehouse Workspace Card -->
                <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                    <c:if test="${r.roleName eq 'Warehouse Staff'}">
                        <div class="group relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-amber-300">
                            <div>
                                <div class="flex items-center justify-between">
                                    <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-50 text-amber-600 text-xl shadow-inner group-hover:bg-amber-600 group-hover:text-white transition">
                                        <i class="fa-solid fa-warehouse"></i>
                                    </span>
                                    <span class="rounded-full bg-amber-50 px-2.5 py-1 text-[11px] font-bold text-amber-700 ring-1 ring-inset ring-amber-600/20">
                                        Warehouse Staff
                                    </span>
                                </div>
                                <h4 class="mt-4 text-base font-bold text-slate-900">Nhân viên Kho</h4>
                                <p class="mt-1 text-xs text-slate-500 leading-relaxed">
                                    Tiếp nhận hàng bàn giao, quét số serial nhập kho, soạn hàng xuất theo lệnh và kiểm kê thực tế.
                                </p>
                                <div class="mt-4 space-y-1.5 border-t border-slate-100 pt-3 text-xs text-slate-600">
                                    <a href="${pageContext.request.contextPath}/app/warehouse/dashboard" class="flex items-center gap-2 py-1 hover:text-amber-600">
                                        <i class="fa-solid fa-grip w-4 text-slate-400"></i> Bàn làm việc Kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/warehouse/handovers" class="flex items-center gap-2 py-1 hover:text-amber-600">
                                        <i class="fa-solid fa-truck-ramp-box w-4 text-slate-400"></i> Tiếp nhận bàn giao & Nhập kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" class="flex items-center gap-2 py-1 hover:text-amber-600">
                                        <i class="fa-solid fa-box-open w-4 text-slate-400"></i> Soạn hàng & Phiếu xuất kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/warehouse/stocktakes" class="flex items-center gap-2 py-1 hover:text-amber-600">
                                        <i class="fa-solid fa-clipboard-list w-4 text-slate-400"></i> Thực hiện kiểm kê
                                    </a>
                                </div>
                            </div>
                            <div class="mt-6 pt-4 border-t border-slate-100">
                                <a href="${pageContext.request.contextPath}/app/warehouse/dashboard" 
                                   class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-amber-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-amber-700">
                                    <span>Vào phân hệ Kho</span>
                                    <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                </a>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>

                <!-- 3. Purchasing Workspace Card -->
                <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                    <c:if test="${r.roleName eq 'Purchasing Staff'}">
                        <div class="group relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-emerald-300">
                            <div>
                                <div class="flex items-center justify-between">
                                    <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600 text-xl shadow-inner group-hover:bg-emerald-600 group-hover:text-white transition">
                                        <i class="fa-solid fa-cart-shopping"></i>
                                    </span>
                                    <span class="rounded-full bg-emerald-50 px-2.5 py-1 text-[11px] font-bold text-emerald-700 ring-1 ring-inset ring-emerald-600/20">
                                        Purchasing Staff
                                    </span>
                                </div>
                                <h4 class="mt-4 text-base font-bold text-slate-900">Nhân viên Mua hàng</h4>
                                <p class="mt-1 text-xs text-slate-500 leading-relaxed">
                                    Lập yêu cầu mua hàng máy tính, lập biên bản bàn giao hàng về kho và quản lý thông tin nhà cung cấp.
                                </p>
                                <div class="mt-4 space-y-1.5 border-t border-slate-100 pt-3 text-xs text-slate-600">
                                    <a href="${pageContext.request.contextPath}/app/purchasing/dashboard" class="flex items-center gap-2 py-1 hover:text-emerald-600">
                                        <i class="fa-solid fa-chart-line w-4 text-slate-400"></i> Báo cáo mua hàng
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/purchasing/requests" class="flex items-center gap-2 py-1 hover:text-emerald-600">
                                        <i class="fa-solid fa-file-invoice-dollar w-4 text-slate-400"></i> Đơn mua hàng (PO)
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/purchasing/handovers" class="flex items-center gap-2 py-1 hover:text-emerald-600">
                                        <i class="fa-solid fa-handshake w-4 text-slate-400"></i> Biên bản bàn giao về kho
                                    </a>
                                </div>
                            </div>
                            <div class="mt-6 pt-4 border-t border-slate-100">
                                <a href="${pageContext.request.contextPath}/app/purchasing/dashboard" 
                                   class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-emerald-700">
                                    <span>Vào phân hệ Mua hàng</span>
                                    <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                </a>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>

                <!-- 4. Sales Workspace Card -->
                <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                    <c:if test="${r.roleName eq 'Sales Staff'}">
                        <div class="group relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-violet-300">
                            <div>
                                <div class="flex items-center justify-between">
                                    <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-50 text-violet-600 text-xl shadow-inner group-hover:bg-violet-600 group-hover:text-white transition">
                                        <i class="fa-solid fa-bag-shopping"></i>
                                    </span>
                                    <span class="rounded-full bg-violet-50 px-2.5 py-1 text-[11px] font-bold text-violet-700 ring-1 ring-inset ring-violet-600/20">
                                        Sales Staff
                                    </span>
                                </div>
                                <h4 class="mt-4 text-base font-bold text-slate-900">Nhân viên Bán hàng</h4>
                                <p class="mt-1 text-xs text-slate-500 leading-relaxed">
                                    Tạo phiếu yêu cầu xuất kho giao cho khách hàng và theo dõi tiến độ duyệt/soạn hàng từ kho.
                                </p>
                                <div class="mt-4 space-y-1.5 border-t border-slate-100 pt-3 text-xs text-slate-600">
                                    <a href="${pageContext.request.contextPath}/app/sales/export-requests" class="flex items-center gap-2 py-1 hover:text-violet-600">
                                        <i class="fa-solid fa-list-check w-4 text-slate-400"></i> Danh sách yêu cầu xuất kho
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/sales/export-request-form" class="flex items-center gap-2 py-1 hover:text-violet-600">
                                        <i class="fa-solid fa-plus w-4 text-slate-400"></i> Tạo yêu cầu xuất mới
                                    </a>
                                </div>
                            </div>
                            <div class="mt-6 pt-4 border-t border-slate-100">
                                <a href="${pageContext.request.contextPath}/app/sales/export-requests" 
                                   class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-violet-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-violet-700">
                                    <span>Vào phân hệ Bán hàng</span>
                                    <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                </a>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>

                <!-- 5. Admin Workspace Card -->
                <c:forEach var="r" items="${sessionScope.LOGIN_USER.roles}">
                    <c:if test="${r.roleName eq 'Admin'}">
                        <div class="group relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-blue-300">
                            <div>
                                <div class="flex items-center justify-between">
                                    <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-50 text-blue-600 text-xl shadow-inner group-hover:bg-blue-600 group-hover:text-white transition">
                                        <i class="fa-solid fa-user-shield"></i>
                                    </span>
                                    <span class="rounded-full bg-blue-50 px-2.5 py-1 text-[11px] font-bold text-blue-700 ring-1 ring-inset ring-blue-600/20">
                                        Administrator
                                    </span>
                                </div>
                                <h4 class="mt-4 text-base font-bold text-slate-900">Quản trị Hệ thống</h4>
                                <p class="mt-1 text-xs text-slate-500 leading-relaxed">
                                    Quản lý người dùng, phân quyền truy cập chức năng và theo dõi nhật ký hoạt động hệ thống.
                                </p>
                                <div class="mt-4 space-y-1.5 border-t border-slate-100 pt-3 text-xs text-slate-600">
                                    <a href="${pageContext.request.contextPath}/app/admin/users" class="flex items-center gap-2 py-1 hover:text-blue-600">
                                        <i class="fa-solid fa-users w-4 text-slate-400"></i> Quản lý Người dùng & Quyền
                                    </a>
                                    <a href="${pageContext.request.contextPath}/app/admin/audit" class="flex items-center gap-2 py-1 hover:text-blue-600">
                                        <i class="fa-solid fa-clock-rotate-left w-4 text-slate-400"></i> Nhật ký kiểm toán Audit Log
                                    </a>
                                </div>
                            </div>
                            <div class="mt-6 pt-4 border-t border-slate-100">
                                <a href="${pageContext.request.contextPath}/app/admin/users" 
                                   class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-blue-700">
                                    <span>Vào Admin Console</span>
                                    <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                </a>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
            </div>
        </div>

        <!-- Account Profile Details Section -->
        <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h4 class="text-sm font-bold uppercase tracking-wider text-slate-500 mb-4">Hồ sơ tài khoản</h4>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
                <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                    <span class="text-slate-400 block mb-1">Tên đăng nhập</span>
                    <span class="font-bold text-slate-800 text-sm">${sessionScope.LOGIN_USER.username}</span>
                </div>
                <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                    <span class="text-slate-400 block mb-1">Họ và tên</span>
                    <span class="font-bold text-slate-800 text-sm">${sessionScope.LOGIN_USER.fullName}</span>
                </div>
                <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                    <span class="text-slate-400 block mb-1">Email</span>
                    <span class="font-bold text-slate-800 text-sm">${sessionScope.LOGIN_USER.email}</span>
                </div>
                <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                    <span class="text-slate-400 block mb-1">Điện thoại</span>
                    <span class="font-bold text-slate-800 text-sm">${empty sessionScope.LOGIN_USER.phone ? 'Chưa cập nhật' : sessionScope.LOGIN_USER.phone}</span>
                </div>
            </div>
        </div>
    </main>

    <!-- Footer -->
    <footer class="border-t border-slate-200 bg-white py-4 mt-auto">
        <div class="max-w-7xl mx-auto px-4 text-center text-xs text-slate-400">
            LIMS - Laptop Inventory Management System &copy; 2026. All rights reserved.
        </div>
    </footer>

</body>
</html>