<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử xuất kho | LIMS Warehouse</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeWarehouseMenu" value="export-history"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto w-full max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Header -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-indigo-700">
                <i class="fa-solid fa-clock-rotate-left"></i>
                <span>Warehouse / Xuất kho</span>
            </div>
            <h1 class="mt-1 text-2xl font-bold tracking-tight text-slate-900">Lịch sử Xuất kho</h1>
            <p class="mt-1 text-sm text-slate-500">Theo dõi các phiếu xuất đã soạn theo Serial, chuyển duyệt Manager hoặc đã hoàn tất.</p>
        </div>
        <div class="flex items-center gap-2.5">
            <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" 
               class="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-indigo-700">
                <i class="fa-solid fa-truck-ramp-box"></i>
                <span>Đơn chờ soạn xuất</span>
            </a>
        </div>
    </div>

    <!-- Alert Notices -->
    <c:if test="${not empty notice}">
        <div class="flex items-center gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-xs font-medium text-emerald-800 shadow-sm">
            <i class="fa-solid fa-circle-check text-emerald-600 text-sm"></i>
            <span><c:out value="${notice}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-800 shadow-sm">
            <i class="fa-solid fa-circle-exclamation text-rose-600 text-sm"></i>
            <span><c:out value="${error}"/></span>
        </div>
    </c:if>

    <!-- Search Box (Search Export History by Serial Number / Customer) -->
    <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <form method="get" action="${pageContext.request.contextPath}/app/warehouse/export-history" class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <!-- Search by Serial Number -->
            <div class="lg:col-span-2">
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-1">
                    <i class="fa-solid fa-barcode text-indigo-600 mr-1"></i> Tra cứu theo Serial Number
                </label>
                <div class="relative">
                    <input type="text" name="serial" value="<c:out value="${filterSerial}"/>" 
                           placeholder="Nhập Serial Number để tìm phiếu xuất..." 
                           class="w-full rounded-xl border border-slate-300 py-2.5 pl-3.5 pr-10 text-xs font-mono focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                    <c:if test="${not empty filterSerial}">
                        <a href="${pageContext.request.contextPath}/app/warehouse/export-history" class="absolute right-3 top-2.5 text-xs text-slate-400 hover:text-slate-600">
                            <i class="fa-solid fa-xmark"></i>
                        </a>
                    </c:if>
                </div>
            </div>

            <!-- Filter Status -->
            <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-1">
                    <i class="fa-solid fa-filter text-slate-500 mr-1"></i> Trạng thái duyệt
                </label>
                <select name="status" class="w-full rounded-xl border border-slate-300 py-2.5 px-3 text-xs bg-white focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                    <option value="">-- Tất cả trạng thái --</option>
                    <option value="COMPLETED" ${filterStatus == 'COMPLETED' ? 'selected' : ''}>Đã xuất kho (COMPLETED)</option>
                    <option value="PENDING_MANAGER" ${filterStatus == 'PENDING_MANAGER' ? 'selected' : ''}>Chờ Manager duyệt (PENDING)</option>
                    <option value="REJECTED" ${filterStatus == 'REJECTED' ? 'selected' : ''}>Manager từ chối (REJECTED)</option>
                </select>
            </div>

            <!-- Submit Button -->
            <div class="flex items-end gap-2">
                <button type="submit" class="flex-1 rounded-xl bg-slate-900 py-2.5 px-4 text-xs font-semibold text-white shadow-sm transition hover:bg-slate-800">
                    <i class="fa-solid fa-magnifying-glass mr-1.5"></i> Tìm kiếm
                </button>
                <c:if test="${not empty filterSerial or not empty filterStatus or not empty filterKeyword}">
                    <a href="${pageContext.request.contextPath}/app/warehouse/export-history" 
                       class="rounded-xl border border-slate-300 py-2.5 px-3 text-xs font-semibold text-slate-600 hover:bg-slate-50" title="Đặt lại">
                        <i class="fa-solid fa-rotate-left"></i>
                    </a>
                </c:if>
            </div>
        </form>
    </div>

    <!-- Export Receipts Table -->
    <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="flex items-center justify-between border-b border-slate-100 px-5 py-4">
            <div>
                <h2 class="text-sm font-bold text-slate-800">Danh sách Phiếu xuất kho</h2>
                <p class="mt-0.5 text-xs text-slate-500">Hiển thị <b>${fn:length(exportReceipts)}</b> phiếu xuất kho đã soạn.</p>
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-100 text-left text-xs">
                <thead class="bg-slate-50/75 text-[11px] font-bold uppercase tracking-wider text-slate-500">
                    <tr>
                        <th class="px-5 py-3.5">Phiếu xuất</th>
                        <th class="px-5 py-3.5">Yêu cầu / Khách hàng</th>
                        <th class="px-5 py-3.5">Ngày lập</th>
                        <th class="px-5 py-3.5">Serial đã soạn</th>
                        <th class="px-5 py-3.5">Trạng thái duyệt</th>
                        <th class="px-5 py-3.5 text-right">Thao tác</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-slate-700">
                    <c:forEach var="r" items="${exportReceipts}">
                        <tr class="hover:bg-slate-50/70 transition">
                            <!-- Receipt ID -->
                            <td class="whitespace-nowrap px-5 py-4">
                                <span class="font-bold text-slate-900 text-sm">#PX-${r.receiptId}</span>
                                <div class="mt-0.5 text-[11px] text-slate-500">
                                    Đơn xuất: <span class="font-semibold text-indigo-700">#${r.requestId}</span>
                                </div>
                            </td>

                            <!-- Customer / Request -->
                            <td class="px-5 py-4">
                                <div class="font-semibold text-slate-900"><c:out value="${r.customerName}"/></div>
                                <div class="mt-0.5 text-[11px] text-slate-500">Soạn bởi: <c:out value="${r.warehouseName}"/></div>
                            </td>

                            <!-- Date -->
                            <td class="whitespace-nowrap px-5 py-4">
                                <div class="text-slate-900 font-medium">
                                    <fmt:formatDate value="${r.receiptDate}" pattern="dd/MM/yyyy HH:mm"/>
                                </div>
                            </td>

                            <!-- Serials Count & Preview -->
                            <td class="px-5 py-4">
                                <span class="font-bold text-slate-900">${fn:length(r.items)} máy</span>
                                <div class="mt-1 flex flex-wrap gap-1 max-w-xs">
                                    <c:forEach var="item" items="${r.items}" begin="0" end="2">
                                        <span class="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-[10px] text-slate-600 border border-slate-200">
                                            <c:out value="${item.serialNumber}"/>
                                        </span>
                                    </c:forEach>
                                    <c:if test="${fn:length(r.items) > 3}">
                                        <span class="text-[10px] text-slate-400 self-center">+${fn:length(r.items) - 3} máy</span>
                                    </c:if>
                                </div>
                            </td>

                            <!-- Status -->
                            <td class="whitespace-nowrap px-5 py-4">
                                <c:choose>
                                    <c:when test="${r.status == 'COMPLETED'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-emerald-100 px-2.5 py-1 text-xs font-semibold text-emerald-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-emerald-600"></span> Đã xuất kho
                                        </span>
                                    </c:when>
                                    <c:when test="${r.status == 'PENDING_MANAGER'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-2.5 py-1 text-xs font-semibold text-amber-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-amber-600"></span> Chờ Manager duyệt
                                        </span>
                                    </c:when>
                                    <c:when test="${r.status == 'REJECTED'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-rose-100 px-2.5 py-1 text-xs font-semibold text-rose-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-rose-600"></span> Bị từ chối
                                        </span>
                                        <c:if test="${not empty r.cancelReason}">
                                            <div class="mt-1 max-w-xs text-[11px] text-rose-700 truncate" title="<c:out value="${r.cancelReason}"/>">
                                                <c:out value="${r.cancelReason}"/>
                                            </div>
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rounded bg-slate-100 px-2.5 py-1 font-semibold text-xs">${r.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- Actions -->
                            <td class="whitespace-nowrap px-5 py-4 text-right">
                                <a href="${pageContext.request.contextPath}/app/warehouse/export-receipt-detail?receiptId=${r.receiptId}" 
                                   class="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 font-semibold text-slate-700 shadow-sm transition hover:border-indigo-400 hover:bg-indigo-50 hover:text-indigo-800">
                                    <i class="fa-solid fa-eye text-indigo-600"></i>
                                    <span>Xem chi tiết</span>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty exportReceipts}">
                        <tr>
                            <td colspan="6" class="px-5 py-12 text-center text-slate-400">
                                <i class="fa-solid fa-box-open text-3xl text-slate-300"></i>
                                <p class="mt-2 text-sm font-medium text-slate-500">Chưa có phiếu xuất kho nào trong lịch sử.</p>
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</main>
</div>
</div>
</body>
</html>
