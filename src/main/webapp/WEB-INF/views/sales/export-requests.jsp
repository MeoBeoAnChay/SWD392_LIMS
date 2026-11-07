<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh sách yêu cầu xuất | LIMS Sales</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
    <c:set var="activeSalesMenu" value="exports"/>
    <%@ include file="shell.jspf" %>

    <div class="space-y-6">
        <!-- Page Header -->
        <div class="flex flex-wrap items-end justify-between gap-4">
            <div>
                <h1 class="text-2xl font-bold text-slate-900">Danh sách đơn yêu cầu xuất kho</h1>
                <p class="mt-1 text-sm text-slate-500">
                    Theo dõi trạng thái xét duyệt của Manager và tiến độ soạn hàng của nhân viên kho.
                </p>
            </div>
            <div class="flex flex-wrap gap-2.5">
                <a href="${pageContext.request.contextPath}/app/sales/export-history" 
                   class="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50">
                    <i class="fa-solid fa-clock-rotate-left text-violet-600"></i>
                    <span>Xem lịch sử phiếu xuất</span>
                </a>
                <a href="${pageContext.request.contextPath}/app/sales/export-request-form" 
                   class="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-violet-700">
                    <i class="fa-solid fa-plus text-xs"></i>
                    <span>Tạo đơn yêu cầu mới</span>
                </a>
            </div>
        </div>

        <c:if test="${not empty notice}">
            <div class="rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800 shadow-sm flex items-center gap-3">
                <i class="fa-solid fa-circle-check text-emerald-600 text-base"></i>
                <div><c:out value="${notice}"/></div>
            </div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800 shadow-sm flex items-center gap-3">
                <i class="fa-solid fa-circle-exclamation text-rose-600 text-base"></i>
                <div><c:out value="${error}"/></div>
            </div>
        </c:if>

        <!-- Filters & Search Toolbar -->
        <div class="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <!-- Status Tabs -->
            <div class="flex flex-wrap items-center gap-1.5 text-xs">
                <a href="${pageContext.request.contextPath}/app/sales/export-requests?status=ALL${not empty searchKeyword ? '&search=' : ''}${searchKeyword}" 
                   class="rounded-xl px-3.5 py-2 font-semibold transition ${currentStatus == 'ALL' ? 'bg-violet-600 text-white shadow-sm' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}">
                    Tất cả
                </a>
                <a href="${pageContext.request.contextPath}/app/sales/export-requests?status=PENDING${not empty searchKeyword ? '&search=' : ''}${searchKeyword}" 
                   class="rounded-xl px-3.5 py-2 font-semibold transition ${currentStatus == 'PENDING' ? 'bg-amber-500 text-white shadow-sm' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}">
                    Chờ duyệt
                </a>
                <a href="${pageContext.request.contextPath}/app/sales/export-requests?status=APPROVED${not empty searchKeyword ? '&search=' : ''}${searchKeyword}" 
                   class="rounded-xl px-3.5 py-2 font-semibold transition ${currentStatus == 'APPROVED' ? 'bg-blue-600 text-white shadow-sm' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}">
                    Đã duyệt & Kho nhận
                </a>
                <a href="${pageContext.request.contextPath}/app/sales/export-requests?status=COMPLETED${not empty searchKeyword ? '&search=' : ''}${searchKeyword}" 
                   class="rounded-xl px-3.5 py-2 font-semibold transition ${currentStatus == 'COMPLETED' ? 'bg-emerald-600 text-white shadow-sm' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}">
                    Đã xuất hoàn thành
                </a>
                <a href="${pageContext.request.contextPath}/app/sales/export-requests?status=REJECTED${not empty searchKeyword ? '&search=' : ''}${searchKeyword}" 
                   class="rounded-xl px-3.5 py-2 font-semibold transition ${currentStatus == 'REJECTED' ? 'bg-rose-600 text-white shadow-sm' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}">
                    Bị từ chối
                </a>
            </div>

            <!-- Search Input -->
            <form method="get" action="${pageContext.request.contextPath}/app/sales/export-requests" class="flex items-center gap-2">
                <input type="hidden" name="status" value="${currentStatus}">
                <div class="relative">
                    <span class="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                        <i class="fa-solid fa-magnifying-glass text-xs"></i>
                    </span>
                    <input type="text" name="search" value="<c:out value="${searchKeyword}"/>" 
                           placeholder="Tìm khách hàng / model..."
                           class="w-56 sm:w-64 rounded-xl border border-slate-200 pl-8 pr-3 py-1.5 text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-violet-500/20 focus:border-violet-500">
                </div>
                <button type="submit" class="rounded-xl bg-slate-100 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-200 transition">
                    Tìm
                </button>
                <c:if test="${not empty searchKeyword}">
                    <a href="${pageContext.request.contextPath}/app/sales/export-requests?status=${currentStatus}" 
                       class="text-xs text-slate-400 hover:text-slate-600" title="Xóa bộ lọc tìm kiếm">
                        <i class="fa-solid fa-xmark"></i>
                    </a>
                </c:if>
            </form>
        </div>

        <!-- Table Container -->
        <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
                <h2 class="text-base font-bold text-slate-900">Danh sách đơn xuất kho</h2>
                <span class="rounded-full bg-violet-50 px-3 py-1 text-xs font-bold text-violet-700 ring-1 ring-inset ring-violet-700/10">
                    ${requests.size()} đơn
                </span>
            </div>

            <div class="overflow-x-auto">
                <table class="min-w-full text-left text-sm">
                    <thead class="bg-slate-50/75 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                        <tr>
                            <th class="px-5 py-3.5">Mã đơn / Ngày tạo</th>
                            <th class="px-5 py-3.5">Khách hàng</th>
                            <th class="px-5 py-3.5">Sản phẩm yêu cầu</th>
                            <th class="px-5 py-3.5">Thủ kho phụ trách</th>
                            <th class="px-5 py-3.5">Trạng thái</th>
                            <th class="px-5 py-3.5">Ghi chú / Phản hồi</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        <c:forEach var="req" items="${requests}">
                            <tr class="hover:bg-slate-50/60 transition">
                                <td class="whitespace-nowrap px-5 py-4">
                                    <div class="font-bold text-slate-900">#${req.requestId}</div>
                                    <div class="text-xs text-slate-500 mt-0.5">
                                        <fmt:formatDate value="${req.requestDate}" pattern="dd/MM/yyyy HH:mm"/>
                                    </div>
                                </td>

                                <td class="px-5 py-4">
                                    <div class="font-semibold text-slate-900"><c:out value="${req.customerName}"/></div>
                                </td>

                                <td class="px-5 py-4">
                                    <div class="space-y-1 max-w-sm">
                                        <c:forEach var="item" items="${req.items}">
                                            <div class="inline-flex items-center gap-1.5 rounded-lg bg-slate-100 px-2 py-0.5 text-xs text-slate-700">
                                                <span class="font-medium"><c:out value="${item.productCode}"/></span>
                                                <span class="text-slate-400">×</span>
                                                <span class="font-bold text-violet-700">${item.quantity}</span>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </td>

                                <td class="px-5 py-4 text-xs">
                                    <c:choose>
                                        <c:when test="${not empty req.assignedName}">
                                            <div class="flex items-center gap-1.5 text-slate-800 font-medium">
                                                <i class="fa-solid fa-user text-amber-500 text-[10px]"></i>
                                                <span><c:out value="${req.assignedName}"/></span>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-slate-400 italic">Chưa phân công</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="whitespace-nowrap px-5 py-4">
                                    <c:choose>
                                        <c:when test="${req.status == 'COMPLETED'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                                <i class="fa-solid fa-circle-check text-[10px]"></i> Đã xuất kho
                                            </span>
                                        </c:when>
                                        <c:when test="${req.status == 'APPROVED'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700 border border-blue-200">
                                                <i class="fa-solid fa-box text-[10px]"></i> Đã duyệt · Chờ soạn
                                            </span>
                                        </c:when>
                                        <c:when test="${req.status == 'PENDING'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700 border border-amber-200">
                                                <i class="fa-solid fa-clock text-[10px]"></i> Chờ Manager duyệt
                                            </span>
                                        </c:when>
                                        <c:when test="${req.status == 'REJECTED'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 border border-rose-200">
                                                <i class="fa-solid fa-xmark text-[10px]"></i> Bị từ chối
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">
                                                ${req.status}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="px-5 py-4 text-xs max-w-xs">
                                    <c:if test="${req.status == 'REJECTED' and not empty req.cancelReason}">
                                        <div class="text-rose-700 font-medium">
                                            <i class="fa-solid fa-circle-exclamation mr-1"></i><c:out value="${req.cancelReason}"/>
                                        </div>
                                    </c:if>
                                    <c:if test="${req.status == 'COMPLETED'}">
                                        <a href="${pageContext.request.contextPath}/app/sales/export-history" 
                                           class="inline-flex items-center gap-1 text-violet-600 hover:text-violet-800 font-semibold hover:underline">
                                            <span>Xem Serial máy</span>
                                            <i class="fa-solid fa-arrow-right text-[10px]"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${empty req.cancelReason and req.status != 'COMPLETED'}">
                                        <span class="text-slate-300">-</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty requests}">
                            <tr>
                                <td colspan="6" class="px-6 py-16 text-center text-slate-400">
                                    <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-400 mb-3">
                                        <i class="fa-regular fa-folder-open text-2xl"></i>
                                    </div>
                                    <p class="text-base font-semibold text-slate-700">Không tìm thấy đơn xuất kho nào</p>
                                    <p class="mt-1 text-xs text-slate-400">
                                        <c:choose>
                                            <c:when test="${not empty searchKeyword or currentStatus != 'ALL'}">
                                                Không có kết quả phù hợp với bộ lọc hiện tại. Hãy thử chọn bộ lọc khác.
                                            </c:when>
                                            <c:otherwise>
                                                Bạn chưa tạo đơn yêu cầu xuất kho nào trong hệ thống.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <div class="mt-4">
                                        <a href="${pageContext.request.contextPath}/app/sales/export-request-form" 
                                           class="inline-flex items-center gap-1.5 rounded-xl bg-violet-600 px-4 py-2 text-xs font-semibold text-white transition hover:bg-violet-700">
                                            <i class="fa-solid fa-plus text-[10px]"></i> Tạo đơn yêu cầu xuất mới
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main></div></div></body></html>
