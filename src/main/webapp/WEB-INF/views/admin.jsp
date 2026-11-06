<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản trị hệ thống | LIMS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="min-h-screen bg-slate-50 text-slate-800">
<header class="sticky top-0 z-10 border-b border-slate-200 bg-white shadow-sm">
    <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <a href="${pageContext.request.contextPath}/home" class="flex items-center gap-3 font-bold"><span class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-600 text-white"><i class="fa-solid fa-laptop"></i></span>LIMS <span class="text-sm font-normal text-slate-500">Quản trị</span></a>
        <div class="flex items-center gap-4 text-sm"><span><c:out value="${sessionScope.LOGIN_USER.fullName}"/></span><a class="text-rose-600" href="${pageContext.request.contextPath}/logout">Đăng xuất</a></div>
    </div>
</header>
<main class="mx-auto max-w-7xl space-y-6 px-4 py-8">
    <div><h1 class="text-2xl font-bold">Quản trị hệ thống</h1><p class="mt-1 text-sm text-slate-500">Quản lý người dùng, vai trò và lịch sử hoạt động.</p></div>
    <nav class="flex gap-2 border-b border-slate-200">
        <a href="${pageContext.request.contextPath}/app/admin/users" class="border-b-2 px-4 py-3 text-sm font-semibold ${tab == 'users' ? 'border-blue-600 text-blue-700' : 'border-transparent text-slate-500'}">Người dùng & phân quyền</a>
        <a href="${pageContext.request.contextPath}/app/admin/audit" class="border-b-2 px-4 py-3 text-sm font-semibold ${tab == 'audit' ? 'border-blue-600 text-blue-700' : 'border-transparent text-slate-500'}">Audit log</a>
    </nav>
    <c:if test="${not empty param.notice}"><div class="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800"><c:out value="${param.notice}"/></div></c:if>
    <c:if test="${not empty param.error}"><div class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800"><c:out value="${param.error}"/></div></c:if>

    <c:choose>
        <c:when test="${tab == 'users'}">
            <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                <h2 class="mb-4 text-lg font-bold">Tạo tài khoản</h2>
                <form method="post" action="${pageContext.request.contextPath}/app/admin/users" class="grid gap-3 md:grid-cols-3">
                    <input type="hidden" name="action" value="create">
                    <label class="text-sm">Username<input required name="username" maxlength="50" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label>
                    <label class="text-sm">Họ và tên<input required name="fullName" maxlength="100" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label>
                    <label class="text-sm">Email<input required type="email" name="email" maxlength="100" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label>
                    <label class="text-sm">Điện thoại<input name="phone" maxlength="20" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label>
                    <label class="text-sm">Mật khẩu tạm thời<input required type="password" name="password" minlength="8" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label>
                    <fieldset class="text-sm"><legend>Vai trò</legend><div class="mt-1 flex flex-wrap gap-3"><c:forEach var="role" items="${roles}"><label class="flex items-center gap-1"><input type="checkbox" name="roleIds" value="${role.roleId}"><c:out value="${role.roleName}"/></label></c:forEach></div></fieldset>
                    <div class="md:col-span-3"><button class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">Tạo tài khoản</button></div>
                </form>
            </section>
            <section class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                <div class="border-b border-slate-100 p-5"><h2 class="text-lg font-bold">Danh sách người dùng</h2></div>
                <div class="overflow-x-auto"><table class="min-w-full text-left text-sm">
                    <thead class="bg-slate-50 text-xs uppercase text-slate-500"><tr><th class="px-4 py-3">Tài khoản</th><th class="px-4 py-3">Thông tin</th><th class="px-4 py-3">Trạng thái</th><th class="px-4 py-3">Vai trò</th><th class="px-4 py-3">Cập nhật</th></tr></thead>
                    <tbody class="divide-y divide-slate-100">
                    <c:forEach var="u" items="${users}">
                        <tr class="align-top"><td class="px-4 py-4"><div class="font-semibold"><c:out value="${u.username}"/></div><div class="mt-1 text-xs text-slate-500">ID: ${u.userId}</div></td>
                            <td class="min-w-64 px-4 py-4"><form id="profile-${u.userId}" method="post" action="${pageContext.request.contextPath}/app/admin/users" class="space-y-2"><input type="hidden" name="action" value="update"><input type="hidden" name="userId" value="${u.userId}"><input required name="fullName" value="${fn:escapeXml(u.fullName)}" placeholder="Họ tên" class="w-full rounded border border-slate-300 px-2 py-1"><input required type="email" name="email" value="${fn:escapeXml(u.email)}" placeholder="Email" class="w-full rounded border border-slate-300 px-2 py-1"><input name="phone" value="${fn:escapeXml(u.phone)}" placeholder="Điện thoại" class="w-full rounded border border-slate-300 px-2 py-1"></form></td>
                            <td class="px-4 py-4"><select form="profile-${u.userId}" name="status" class="rounded border border-slate-300 px-2 py-1"><option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option><option value="INACTIVE" ${u.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option><option value="LOCKED" ${u.status == 'LOCKED' ? 'selected' : ''}>LOCKED</option></select></td>
                            <td class="min-w-52 px-4 py-4"><form id="roles-${u.userId}" method="post" action="${pageContext.request.contextPath}/app/admin/users"><input type="hidden" name="action" value="roles"><input type="hidden" name="userId" value="${u.userId}"><div class="space-y-1"><c:forEach var="role" items="${roles}"><label class="flex items-center gap-2 text-xs"><input type="checkbox" name="roleIds" value="${role.roleId}" ${u.hasRole(role.roleName) ? 'checked' : ''}><c:out value="${role.roleName}"/></label></c:forEach></div></form></td>
                            <td class="space-y-2 px-4 py-4"><button form="profile-${u.userId}" class="block rounded bg-slate-800 px-3 py-1.5 text-xs font-semibold text-white">Lưu thông tin</button><button form="roles-${u.userId}" class="rounded bg-blue-600 px-3 py-1.5 text-xs font-semibold text-white">Lưu vai trò</button></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty users}"><tr><td colspan="5" class="px-4 py-8 text-center text-slate-500">Chưa có người dùng.</td></tr></c:if>
                    </tbody>
                </table></div>
            </section>
        </c:when>
        <c:otherwise>
            <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
                <h2 class="mb-4 text-lg font-bold">Lịch sử hoạt động</h2>
                <form method="get" action="${pageContext.request.contextPath}/app/admin/audit" class="mb-5 flex flex-wrap items-end gap-3">
                    <label class="text-sm">Tài khoản<input name="username" value="${fn:escapeXml(usernameFilter)}" class="mt-1 block rounded-lg border border-slate-300 px-3 py-2"></label>
                    <label class="text-sm">Hành động<select name="action" class="mt-1 block rounded-lg border border-slate-300 px-3 py-2"><option value="">Tất cả</option><c:forEach var="a" items="${auditActions}"><option value="${a}" ${actionFilter == a ? 'selected' : ''}>${a}</option></c:forEach></select></label>
                    <button class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white">Lọc</button><span class="text-xs text-slate-500">Tối đa 200 bản ghi mới nhất</span>
                </form>
                <div class="overflow-x-auto"><table class="min-w-full text-left text-sm">
                    <thead class="bg-slate-50 text-xs uppercase text-slate-500"><tr><th class="px-3 py-3">Thời gian</th><th class="px-3 py-3">Người dùng</th><th class="px-3 py-3">Hành động</th><th class="px-3 py-3">Đối tượng</th><th class="px-3 py-3">Chi tiết</th><th class="px-3 py-3">IP</th></tr></thead>
                    <tbody class="divide-y divide-slate-100"><c:forEach var="log" items="${auditLogs}"><tr class="align-top"><td class="whitespace-nowrap px-3 py-3"><fmt:formatDate value="${log.timestamp}" pattern="dd/MM/yyyy HH:mm:ss"/></td><td class="px-3 py-3"><c:out value="${log.username}"/><div class="text-xs text-slate-400">ID: ${log.userId}</div></td><td class="px-3 py-3"><span class="rounded bg-blue-50 px-2 py-1 text-xs font-semibold text-blue-700"><c:out value="${log.action}"/></span></td><td class="px-3 py-3"><c:out value="${log.tableName}"/> #<c:out value="${log.recordId}"/></td><td class="max-w-lg px-3 py-3 text-xs"><c:if test="${not empty log.oldValues}"><div><b>Cũ:</b> <c:out value="${log.oldValues}"/></div></c:if><c:if test="${not empty log.newValues}"><div><b>Mới:</b> <c:out value="${log.newValues}"/></div></c:if><c:if test="${not empty log.userAgent}"><div class="mt-1 text-slate-400"><c:out value="${log.userAgent}"/></div></c:if></td><td class="px-3 py-3"><c:out value="${log.ipAddress}"/></td></tr></c:forEach><c:if test="${empty auditLogs}"><tr><td colspan="6" class="px-3 py-8 text-center text-slate-500">Không có bản ghi phù hợp.</td></tr></c:if></tbody>
                </table></div>
            </section>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
