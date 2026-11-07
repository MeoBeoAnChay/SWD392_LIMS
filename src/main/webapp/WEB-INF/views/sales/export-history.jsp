<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử đơn xuất kho | LIMS Sales</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
    <c:set var="activeSalesMenu" value="export-history"/>
    <%@ include file="shell.jspf" %>

    <div class="space-y-6">
        <!-- Page Header -->
        <div class="flex flex-wrap items-end justify-between gap-4">
            <div>
                <nav class="flex items-center gap-2 text-xs font-semibold text-violet-600 mb-1">
                    <a href="${pageContext.request.contextPath}/app/sales/export-requests" class="hover:underline">Bán hàng</a>
                    <span>/</span>
                    <span class="text-slate-500">Lịch sử xuất kho</span>
                </nav>
                <h1 class="text-2xl font-bold text-slate-900">Lịch sử đơn xuất kho của bạn</h1>
                <p class="mt-1 text-sm text-slate-500">
                    Theo dõi danh sách các phiếu xuất kho đã được thủ kho xử lý và thông tin chi tiết Serial máy bàn giao khách hàng.
                </p>
            </div>
            <div class="flex flex-wrap gap-2.5">
                <a href="${pageContext.request.contextPath}/app/sales/export-requests" 
                   class="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50">
                    <i class="fa-solid fa-list-check text-slate-500"></i>
                    <span>Danh sách yêu cầu</span>
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

        <!-- KPI Summary Cards -->
        <c:set var="totalCount" value="${receipts.size()}"/>
        <c:set var="completedCount" value="0"/>
        <c:set var="pendingCount" value="0"/>
        <c:set var="rejectedCount" value="0"/>
        <c:forEach var="item" items="${receipts}">
            <c:choose>
                <c:when test="${item.status == 'COMPLETED'}">
                    <c:set var="completedCount" value="${completedCount + 1}"/>
                </c:when>
                <c:when test="${item.status == 'PENDING_MANAGER'}">
                    <c:set var="pendingCount" value="${pendingCount + 1}"/>
                </c:when>
                <c:when test="${item.status == 'REJECTED'}">
                    <c:set var="rejectedCount" value="${rejectedCount + 1}"/>
                </c:when>
            </c:choose>
        </c:forEach>

        <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                <div class="flex items-center justify-between text-slate-500">
                    <span class="text-xs font-bold uppercase tracking-wider">Tổng phiếu xuất</span>
                    <i class="fa-solid fa-receipt text-slate-400"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-slate-900">${totalCount}</div>
                <p class="mt-0.5 text-[11px] text-slate-400">Đơn đã qua bước xuất kho</p>
            </div>
            <div class="rounded-2xl border border-emerald-100 bg-emerald-50/60 p-4 shadow-sm">
                <div class="flex items-center justify-between text-emerald-700">
                    <span class="text-xs font-bold uppercase tracking-wider">Đã hoàn thành</span>
                    <i class="fa-solid fa-circle-check text-emerald-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-emerald-800">${completedCount}</div>
                <p class="mt-0.5 text-[11px] text-emerald-600">Đã xuất kho thực tế</p>
            </div>
            <div class="rounded-2xl border border-indigo-100 bg-indigo-50/60 p-4 shadow-sm">
                <div class="flex items-center justify-between text-indigo-700">
                    <span class="text-xs font-bold uppercase tracking-wider">Chờ Manager duyệt</span>
                    <i class="fa-solid fa-clock text-indigo-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-indigo-800">${pendingCount}</div>
                <p class="mt-0.5 text-[11px] text-indigo-600">Thủ kho đã soạn xong</p>
            </div>
            <div class="rounded-2xl border border-rose-100 bg-rose-50/60 p-4 shadow-sm">
                <div class="flex items-center justify-between text-rose-700">
                    <span class="text-xs font-bold uppercase tracking-wider">Bị từ chối</span>
                    <i class="fa-solid fa-circle-xmark text-rose-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-rose-800">${rejectedCount}</div>
                <p class="mt-0.5 text-[11px] text-rose-600">Manager không duyệt xuất</p>
            </div>
        </div>

        <!-- History Records Table -->
        <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 px-6 py-4">
                <div>
                    <h2 class="text-base font-bold text-slate-900">Chi tiết lịch sử xuất kho</h2>
                    <p class="text-xs text-slate-500">Thông tin phiếu xuất, người phụ trách và mã Serial Number đã giao</p>
                </div>
                <span class="rounded-full bg-violet-50 px-3 py-1 text-xs font-bold text-violet-700 ring-1 ring-inset ring-violet-700/10">
                    ${receipts.size()} phiếu xuất
                </span>
            </div>

            <div class="overflow-x-auto">
                <table class="min-w-full text-left text-sm">
                    <thead class="bg-slate-50/75 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                        <tr>
                            <th class="px-5 py-3.5">Mã phiếu / Đơn yêu cầu</th>
                            <th class="px-5 py-3.5">Khách hàng nhận</th>
                            <th class="px-5 py-3.5">Thời gian</th>
                            <th class="px-5 py-3.5">Thủ kho & Duyệt bởi</th>
                            <th class="px-5 py-3.5">Thiết bị & Số Serial</th>
                            <th class="px-5 py-3.5">Trạng thái</th>
                            <th class="px-5 py-3.5">Ghi chú</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                        <c:forEach var="r" items="${receipts}">
                            <tr class="hover:bg-slate-50/60 transition">
                                <td class="whitespace-nowrap px-5 py-4">
                                    <div class="font-bold text-slate-900">#PX-${r.receiptId}</div>
                                    <div class="mt-0.5 text-xs text-slate-500">
                                        Thuộc đơn: <span class="font-semibold text-violet-700">#${r.requestId}</span>
                                    </div>
                                </td>

                                <td class="px-5 py-4">
                                    <div class="font-semibold text-slate-900"><c:out value="${r.customerName}"/></div>
                                </td>

                                <td class="whitespace-nowrap px-5 py-4 text-xs">
                                    <div class="text-slate-700">
                                        <span class="text-slate-400">Xuất lúc:</span>
                                        <fmt:formatDate value="${r.receiptDate}" pattern="dd/MM/yyyy HH:mm"/>
                                    </div>
                                    <c:if test="${not empty r.approvedDate}">
                                        <div class="mt-0.5 text-slate-500">
                                            <span class="text-slate-400">Duyệt lúc:</span>
                                            <fmt:formatDate value="${r.approvedDate}" pattern="dd/MM/yyyy HH:mm"/>
                                        </div>
                                    </c:if>
                                </td>

                                <td class="px-5 py-4 text-xs">
                                    <div class="flex items-center gap-1.5 text-slate-700">
                                        <i class="fa-solid fa-warehouse text-amber-500 text-[10px]"></i>
                                        <span class="font-medium"><c:out value="${r.warehouseName}"/></span>
                                    </div>
                                    <c:if test="${not empty r.managerName}">
                                        <div class="mt-1 flex items-center gap-1.5 text-slate-500">
                                            <i class="fa-solid fa-user-check text-indigo-500 text-[10px]"></i>
                                            <span><c:out value="${r.managerName}"/></span>
                                        </div>
                                    </c:if>
                                </td>

                                <td class="px-5 py-4">
                                    <div class="space-y-1.5 max-w-sm">
                                        <c:forEach var="item" items="${r.items}">
                                            <div class="inline-flex items-center gap-1.5 rounded-lg border border-slate-200 bg-slate-50 px-2 py-1 text-xs">
                                                <span class="font-medium text-slate-700"><c:out value="${item.productCode}"/>:</span>
                                                <code class="font-mono font-bold text-violet-700"><c:out value="${item.serialNumber}"/></code>
                                                <c:if test="${not empty item.location}">
                                                    <span class="text-[10px] text-slate-400">(vị trí: ${item.location})</span>
                                                </c:if>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </td>

                                <td class="whitespace-nowrap px-5 py-4">
                                    <c:choose>
                                        <c:when test="${r.status == 'COMPLETED'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700 border border-emerald-200">
                                                <i class="fa-solid fa-check text-[10px]"></i> Đã xuất kho
                                            </span>
                                        </c:when>
                                        <c:when test="${r.status == 'PENDING_MANAGER'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700 border border-indigo-200">
                                                <i class="fa-solid fa-hourglass-half text-[10px]"></i> Chờ Manager duyệt
                                            </span>
                                        </c:when>
                                        <c:when test="${r.status == 'REJECTED'}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-700 border border-rose-200">
                                                <i class="fa-solid fa-xmark text-[10px]"></i> Bị từ chối
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">
                                                ${r.status}
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td class="px-5 py-4 text-xs text-slate-500 max-w-xs">
                                    <c:if test="${r.status == 'REJECTED' and not empty r.cancelReason}">
                                        <div class="text-rose-700 font-medium">
                                            <i class="fa-solid fa-circle-exclamation mr-1"></i><c:out value="${r.cancelReason}"/>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty r.note}">
                                        <div class="mt-0.5 text-slate-600"><c:out value="${r.note}"/></div>
                                    </c:if>
                                    <c:if test="${empty r.note and empty r.cancelReason}">
                                        <span class="text-slate-300">-</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty receipts}">
                            <tr>
                                <td colspan="7" class="px-6 py-16 text-center text-slate-400">
                                    <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-100 text-slate-400 mb-3">
                                        <i class="fa-regular fa-clock text-2xl"></i>
                                    </div>
                                    <p class="text-base font-semibold text-slate-700">Chưa có phiếu xuất kho nào</p>
                                    <p class="mt-1 text-xs text-slate-400">
                                        Khi các đơn yêu cầu xuất kho của bạn được kho tiếp nhận và soạn hàng, lịch sử phiếu xuất sẽ hiển thị tại đây.
                                    </p>
                                    <div class="mt-4">
                                        <a href="${pageContext.request.contextPath}/app/sales/export-request-form" 
                                           class="inline-flex items-center gap-1.5 rounded-xl bg-violet-600 px-4 py-2 text-xs font-semibold text-white transition hover:bg-violet-700">
                                            <i class="fa-solid fa-plus text-[10px]"></i> Tạo đơn yêu cầu xuất kho
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
