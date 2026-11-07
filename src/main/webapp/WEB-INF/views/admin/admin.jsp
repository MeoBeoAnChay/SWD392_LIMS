<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${tab == 'audit' ? 'Audit Log' : 'Quản trị Người dùng'} | LIMS Admin</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<%@ include file="shell.jspf" %>

<main class="mx-auto max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Header Banner -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <h1 class="text-2xl font-bold tracking-tight text-slate-900">
                <c:choose>
                    <c:when test="${tab == 'audit'}">Nhật ký Hoạt động Hệ thống (Audit Log)</c:when>
                    <c:otherwise>Quản lý Người dùng & Phân quyền (RBAC)</c:otherwise>
                </c:choose>
            </h1>
            <p class="mt-1 text-sm text-slate-500">
                <c:choose>
                    <c:when test="${tab == 'audit'}">Theo dõi chi tiết các thao tác dữ liệu, đăng nhập, phân quyền và duyệt đơn.</c:when>
                    <c:otherwise>Tạo tài khoản mới, cập nhật trạng thái hoạt động và gán vai trò nhân sự.</c:otherwise>
                </c:choose>
            </p>
        </div>

        <!-- Tab Switcher -->
        <div class="inline-flex rounded-xl bg-slate-200/70 p-1">
            <a href="${pageContext.request.contextPath}/app/admin/users" 
               class="inline-flex items-center gap-2 rounded-lg px-4 py-2 text-xs font-semibold transition ${tab == 'users' ? 'bg-white text-blue-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}">
                <i class="fa-solid fa-users"></i> Người dùng
            </a>
            <a href="${pageContext.request.contextPath}/app/admin/audit" 
               class="inline-flex items-center gap-2 rounded-lg px-4 py-2 text-xs font-semibold transition ${tab == 'audit' ? 'bg-white text-blue-700 shadow-sm' : 'text-slate-600 hover:text-slate-900'}">
                <i class="fa-solid fa-shield-halved"></i> Audit Log
            </a>
        </div>
    </div>

    <!-- Alert Messages -->
    <c:if test="${not empty param.notice}">
        <div class="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800 shadow-sm">
            <i class="fa-solid fa-circle-check text-emerald-600"></i>
            <span><c:out value="${param.notice}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="flex items-center gap-3 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800 shadow-sm">
            <i class="fa-solid fa-triangle-exclamation text-rose-600"></i>
            <span><c:out value="${param.error}"/></span>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${tab == 'users'}">
            <!-- Create User Card -->
            <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <div class="flex items-center gap-3 pb-4 border-b border-slate-100">
                    <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
                        <i class="fa-solid fa-user-plus"></i>
                    </span>
                    <div>
                        <h2 class="text-base font-bold text-slate-800">Tạo tài khoản mới</h2>
                        <p class="text-xs text-slate-500">Điền thông tin và tích chọn ít nhất 1 vai trò hệ thống</p>
                    </div>
                </div>

                <form method="post" action="${pageContext.request.contextPath}/app/admin/users" class="mt-5 grid gap-4 md:grid-cols-3">
                    <input type="hidden" name="action" value="create">
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">Tên đăng nhập <span class="text-rose-500">*</span></label>
                        <input required name="username" maxlength="50" placeholder="vd: nguyen_van_a" 
                               class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">Họ và tên <span class="text-rose-500">*</span></label>
                        <input required name="fullName" maxlength="100" placeholder="vd: Nguyễn Văn A" 
                               class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">Email liên hệ <span class="text-rose-500">*</span></label>
                        <input required type="email" name="email" maxlength="100" placeholder="vd: nva@lims.com" 
                               class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">Số điện thoại</label>
                        <input name="phone" maxlength="20" placeholder="vd: 0912345678" 
                               class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1.5">Mật khẩu khởi tạo <span class="text-rose-500">*</span></label>
                        <input required type="password" name="password" minlength="8" placeholder="Tối thiểu 8 ký tự" 
                               class="w-full rounded-xl border border-slate-300 px-3.5 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20">
                    </div>
                    <div class="md:col-span-3">
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-2">Vai trò chỉ định <span class="text-rose-500">*</span></label>
                        <div class="flex flex-wrap gap-3">
                            <c:forEach var="role" items="${roles}">
                                <label class="inline-flex cursor-pointer items-center gap-2 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-xs font-medium text-slate-700 transition hover:bg-slate-100 hover:border-blue-300">
                                    <input type="checkbox" name="roleIds" value="${role.roleId}" class="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500">
                                    <span><c:out value="${role.roleName}"/></span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="md:col-span-3 pt-2">
                        <button type="submit" class="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500/30">
                            <i class="fa-solid fa-plus"></i> Tạo tài khoản
                        </button>
                    </div>
                </form>
            </section>

            <!-- Users List Table -->
            <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
                <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
                    <div class="flex items-center gap-2.5">
                        <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
                            <i class="fa-solid fa-users text-sm"></i>
                        </span>
                        <h2 class="text-base font-bold text-slate-800">Danh sách nhân sự (${users.size()})</h2>
                    </div>
                </div>

                <div class="overflow-x-auto">
                    <table class="min-w-full text-left text-sm">
                        <thead class="bg-slate-50/80 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                            <tr>
                                <th class="px-5 py-3.5">Tài khoản</th>
                                <th class="px-5 py-3.5">Hồ sơ cá nhân</th>
                                <th class="px-5 py-3.5">Trạng thái</th>
                                <th class="px-5 py-3.5">Phân quyền vai trò</th>
                                <th class="px-5 py-3.5 text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
                        <c:forEach var="u" items="${users}">
                            <tr class="transition hover:bg-slate-50/60 align-top">
                                <td class="px-5 py-4">
                                    <div class="flex items-center gap-3">
                                        <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-100 font-bold text-slate-600 text-xs uppercase">
                                            ${fn:substring(u.username, 0, 2)}
                                        </div>
                                        <div>
                                            <div class="font-bold text-slate-900"><c:out value="${u.username}"/></div>
                                            <div class="text-[11px] text-slate-400">ID: #${u.userId}</div>
                                        </div>
                                    </div>
                                </td>
                                <td class="px-5 py-4 min-w-[280px]">
                                    <form id="profile-${u.userId}" method="post" action="${pageContext.request.contextPath}/app/admin/users" class="space-y-1.5">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="userId" value="${u.userId}">
                                        <input required name="fullName" value="${fn:escapeXml(u.fullName)}" placeholder="Họ và tên" 
                                               class="w-full rounded-lg border border-slate-200 px-2.5 py-1 text-xs focus:border-blue-500 focus:outline-none">
                                        <input required type="email" name="email" value="${fn:escapeXml(u.email)}" placeholder="Email" 
                                               class="w-full rounded-lg border border-slate-200 px-2.5 py-1 text-xs focus:border-blue-500 focus:outline-none">
                                        <input name="phone" value="${fn:escapeXml(u.phone)}" placeholder="Điện thoại" 
                                               class="w-full rounded-lg border border-slate-200 px-2.5 py-1 text-xs focus:border-blue-500 focus:outline-none">
                                    </form>
                                </td>
                                <td class="px-5 py-4">
                                    <select form="profile-${u.userId}" name="status" 
                                            class="rounded-lg border border-slate-200 bg-white px-2.5 py-1 text-xs font-semibold ${u.status == 'ACTIVE' ? 'text-emerald-700 bg-emerald-50/50' : 'text-rose-700 bg-rose-50/50'} focus:outline-none">
                                        <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                        <option value="INACTIVE" ${u.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                        <option value="LOCKED" ${u.status == 'LOCKED' ? 'selected' : ''}>LOCKED</option>
                                    </select>
                                </td>
                                <td class="px-5 py-4 min-w-[220px]">
                                    <form id="roles-${u.userId}" method="post" action="${pageContext.request.contextPath}/app/admin/users">
                                        <input type="hidden" name="action" value="roles">
                                        <input type="hidden" name="userId" value="${u.userId}">
                                        <div class="flex flex-wrap gap-1.5">
                                            <c:forEach var="role" items="${roles}">
                                                <label class="inline-flex cursor-pointer items-center gap-1.5 rounded-md border px-2 py-1 text-[11px] ${u.hasRole(role.roleName) ? 'bg-blue-50 border-blue-200 text-blue-700 font-semibold' : 'bg-slate-50 border-slate-200 text-slate-600'}">
                                                    <input type="checkbox" name="roleIds" value="${role.roleId}" ${u.hasRole(role.roleName) ? 'checked' : ''} class="h-3 w-3 rounded text-blue-600">
                                                    <span><c:out value="${role.roleName}"/></span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </form>
                                </td>
                                <td class="px-5 py-4 text-right space-y-1.5 whitespace-nowrap">
                                    <button form="profile-${u.userId}" class="inline-flex items-center gap-1 rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-medium text-white transition hover:bg-slate-800">
                                        <i class="fa-solid fa-floppy-disk text-[10px]"></i> Lưu thông tin
                                    </button>
                                    <button form="roles-${u.userId}" class="inline-flex items-center gap-1 rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-medium text-white transition hover:bg-blue-700">
                                        <i class="fa-solid fa-user-shield text-[10px]"></i> Lưu vai trò
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty users}">
                            <tr><td colspan="5" class="px-5 py-12 text-center text-slate-400">Chưa có người dùng nào trong hệ thống.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:when>

        <c:otherwise>
            <!-- Audit Log Section -->
            <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm space-y-5">
                <!-- Filter Toolbar -->
                <form method="get" action="${pageContext.request.contextPath}/app/admin/audit" class="flex flex-wrap items-end gap-3 rounded-xl bg-slate-50 p-4 border border-slate-200">
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">Tài khoản</label>
                        <input name="username" value="${fn:escapeXml(usernameFilter)}" placeholder="Username lọc..." 
                               class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs focus:border-blue-500 focus:outline-none">
                    </div>
                    <div>
                        <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">Hành động (Action)</label>
                        <select name="action" class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs focus:border-blue-500 focus:outline-none">
                            <option value="">-- Tất cả hành động --</option>
                            <c:forEach var="a" items="${auditActions}">
                                <option value="${a}" ${actionFilter == a ? 'selected' : ''}>${a}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <button type="submit" class="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-blue-700">
                        <i class="fa-solid fa-filter"></i> Lọc dữ liệu
                    </button>
                    <a href="${pageContext.request.contextPath}/app/admin/audit" class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-100">
                        Đặt lại
                    </a>
                    <span class="ml-auto text-xs text-slate-400 self-center">Hiển thị tối đa 200 bản ghi mới nhất</span>
                </form>

                <!-- Audit Log Table -->
                <div class="overflow-x-auto rounded-xl border border-slate-200">
                    <table class="min-w-full text-left text-sm">
                        <thead class="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200">
                            <tr>
                                <th class="px-4 py-3">Thời gian</th>
                                <th class="px-4 py-3">Người thực hiện</th>
                                <th class="px-4 py-3">Hành động</th>
                                <th class="px-4 py-3">Đối tượng</th>
                                <th class="px-4 py-3">Chi tiết thay đổi</th>
                                <th class="px-4 py-3">IP / Client</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100 text-xs">
                        <c:forEach var="log" items="${auditLogs}">
                            <tr class="hover:bg-slate-50/70 align-top">
                                <td class="whitespace-nowrap px-4 py-3 font-mono text-slate-500">
                                    <fmt:formatDate value="${log.timestamp}" pattern="dd/MM/yyyy HH:mm:ss"/>
                                </td>
                                <td class="px-4 py-3">
                                    <div class="font-bold text-slate-800"><c:out value="${log.username}"/></div>
                                    <div class="text-[10px] text-slate-400">ID: #${log.userId}</div>
                                </td>
                                <td class="px-4 py-3">
                                    <span class="inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-bold ring-1 ring-inset ${log.action == 'CREATE' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : log.action == 'UPDATE' or log.action == 'UPDATE_ROLES' ? 'bg-blue-50 text-blue-700 ring-blue-600/20' : log.action == 'APPROVE' ? 'bg-indigo-50 text-indigo-700 ring-indigo-600/20' : log.action == 'REJECT' or log.action == 'DELETE' ? 'bg-rose-50 text-rose-700 ring-rose-600/20' : 'bg-slate-100 text-slate-700 ring-slate-500/20'}">
                                        <c:out value="${log.action}"/>
                                    </span>
                                </td>
                                <td class="px-4 py-3 font-medium text-slate-700">
                                    <span class="font-mono text-slate-900"><c:out value="${log.tableName}"/></span>
                                    <c:if test="${not empty log.recordId}">
                                        <span class="text-slate-400">#<c:out value="${log.recordId}"/></span>
                                    </c:if>
                                </td>
                                <td class="max-w-md px-4 py-3 space-y-1">
                                    <c:if test="${not empty log.oldValues}">
                                        <div class="text-slate-500"><span class="font-semibold text-rose-600">Trước:</span> <code class="font-mono text-[11px] text-slate-700 bg-slate-100 px-1 py-0.5 rounded"><c:out value="${log.oldValues}"/></code></div>
                                    </c:if>
                                    <c:if test="${not empty log.newValues}">
                                        <div class="text-slate-500"><span class="font-semibold text-emerald-600">Sau:</span> <code class="font-mono text-[11px] text-slate-700 bg-slate-100 px-1 py-0.5 rounded"><c:out value="${log.newValues}"/></code></div>
                                    </c:if>
                                </td>
                                <td class="px-4 py-3 whitespace-nowrap">
                                    <div class="font-mono text-slate-600"><c:out value="${log.ipAddress}"/></div>
                                    <div class="max-w-[150px] truncate text-[10px] text-slate-400" title="${log.userAgent}"><c:out value="${log.userAgent}"/></div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty auditLogs}">
                            <tr><td colspan="6" class="px-4 py-10 text-center text-slate-400">Không có bản ghi nhật ký kiểm toán phù hợp.</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</main>
</div>
</div>
</body>
</html>