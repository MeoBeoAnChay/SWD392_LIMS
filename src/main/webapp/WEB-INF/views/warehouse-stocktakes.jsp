<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Danh sách kiểm kho | LIMS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeWarehouseMenu" value="stocktakes"/>
<%@ include file="warehouse-shell.jspf" %>
<main class="mx-auto w-full max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <div>
        <p class="text-xs font-bold uppercase tracking-wider text-violet-700">Warehouse / Kiểm kê</p>
        <h1 class="mt-1 text-2xl font-bold">Danh sách kiểm kho</h1>
        <p class="mt-1 text-sm text-slate-500">Các đợt Manager giao cho bạn. Ghi nhận serial, vị trí và tình trạng rồi gửi kết quả để duyệt.</p>
    </div>
    <c:if test="${not empty notice}"><div class="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800"><c:out value="${notice}"/></div></c:if>
    <c:if test="${not empty error}"><div class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"><c:out value="${error}"/></div></c:if>
    <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 px-5 py-4"><h2 class="font-semibold">Đợt được giao và trạng thái xử lý</h2></div>
        <div class="overflow-x-auto">
            <table class="min-w-full text-left text-sm">
                <thead class="bg-slate-50 text-xs uppercase text-slate-500"><tr><th class="px-4 py-3">Mã đợt</th><th class="px-4 py-3">Khu vực</th><th class="px-4 py-3">Ngày giao</th><th class="px-4 py-3">Ghi chú</th><th class="px-4 py-3">Trạng thái</th><th class="px-4 py-3">Thao tác</th></tr></thead>
                <tbody class="divide-y divide-slate-100">
                    <c:forEach var="check" items="${stocktakes}">
                        <tr class="hover:bg-slate-50">
                            <td class="whitespace-nowrap px-4 py-4 font-semibold">#${check.checkId}<span class="mt-1 block text-xs font-normal text-slate-500">Giao bởi <c:out value="${check.managerName}"/></span></td>
                            <td class="px-4 py-4"><c:out value="${empty check.locationFilter ? 'Toàn kho' : check.locationFilter}"/></td>
                            <td class="whitespace-nowrap px-4 py-4"><fmt:formatDate value="${check.checkDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td class="max-w-xs px-4 py-4 text-slate-500"><c:out value="${check.note}"/><c:if test="${check.status=='REJECTED' and not empty check.cancelReason}"><span class="mt-1 block text-xs text-rose-700">Lý do từ chối: <c:out value="${check.cancelReason}"/></span></c:if></td>
                            <td class="whitespace-nowrap px-4 py-4"><span class="rounded-full px-2.5 py-1 text-xs font-semibold ${check.status == 'ASSIGNED' ? 'bg-amber-50 text-amber-800' : check.status == 'PENDING_MANAGER' ? 'bg-indigo-50 text-indigo-700' : check.status == 'REJECTED' ? 'bg-rose-50 text-rose-700' : 'bg-emerald-50 text-emerald-700'}">${check.status == 'ASSIGNED' ? 'Được giao' : check.status == 'PENDING_MANAGER' ? 'Chờ Manager duyệt' : check.status == 'REJECTED' ? 'Cần kiểm tra lại' : check.status == 'CANCELLED' ? 'Đã hủy' : 'Đã duyệt'}</span></td>
                            <td class="whitespace-nowrap px-4 py-4"><c:choose><c:when test="${check.status=='ASSIGNED' or check.status=='REJECTED'}"><a href="${pageContext.request.contextPath}/app/warehouse/stocktake-form?checkId=${check.checkId}" class="inline-flex items-center gap-2 rounded-lg bg-violet-600 px-3 py-2 text-xs font-semibold text-white hover:bg-violet-700"><i class="fa-solid fa-clipboard-check"></i>Tiến hành kiểm kho</a></c:when><c:when test="${check.status=='PENDING_MANAGER'}"><span class="text-xs text-slate-500">Đã gửi kết quả</span></c:when><c:otherwise><span class="text-xs text-emerald-700">Hoàn tất</span></c:otherwise></c:choose></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty stocktakes}"><tr><td colspan="6" class="px-4 py-12 text-center text-slate-500"><i class="fa-regular fa-clipboard mb-2 block text-2xl text-slate-300"></i>Chưa có đợt kiểm kho nào được giao.</td></tr></c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
</div>
</div>
</body>
</html>
