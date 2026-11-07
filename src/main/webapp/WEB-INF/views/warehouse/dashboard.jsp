<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bàn làm việc Kho | LIMS Warehouse</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeWarehouseMenu" value="dashboard"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto w-full max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Header Title -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <h1 class="text-2xl font-bold tracking-tight text-slate-900">Bàn làm việc Nhân viên Kho</h1>
            <p class="mt-1 text-sm text-slate-500">Tiếp nhận hàng giao, quét serial nhập kho, soạn hàng xuất kho và kiểm kê theo phân công.</p>
        </div>
    </div>

    <!-- Alert Notices -->
    <c:if test="${not empty notice}">
        <div class="flex items-center gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-xs font-medium text-emerald-800 shadow-sm">
            <i class="fa-solid fa-circle-check text-emerald-600"></i>
            <span><c:out value="${notice}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-800 shadow-sm">
            <i class="fa-solid fa-circle-exclamation text-rose-600"></i>
            <span><c:out value="${error}"/></span>
        </div>
    </c:if>

    <!-- Top Action KPI Cards -->
    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <!-- 1. Assigned Handovers -->
        <a href="${pageContext.request.contextPath}/app/warehouse/handovers" 
           class="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-amber-300">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Bàn giao được giao</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-600 text-base group-hover:bg-amber-600 group-hover:text-white transition">
                    <i class="fa-solid fa-boxes-stacked"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-slate-900">${fn:length(handovers)}</p>
            <span class="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-amber-700">
                Tiếp nhận hàng về kho <i class="fa-solid fa-arrow-right text-[10px]"></i>
            </span>
        </a>

        <!-- 2. Assigned Export Orders -->
        <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" 
           class="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-indigo-300">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Đơn xuất chờ soạn</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600 text-base group-hover:bg-indigo-600 group-hover:text-white transition">
                    <i class="fa-solid fa-truck-ramp-box"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-indigo-600">${fn:length(exportRequests)}</p>
            <span class="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-indigo-700">
                Soạn hàng theo Serial <i class="fa-solid fa-arrow-right text-[10px]"></i>
            </span>
        </a>

        <!-- 3. Assigned Stocktakes -->
        <a href="${pageContext.request.contextPath}/app/warehouse/stocktakes" 
           class="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-violet-300">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Đợt kiểm kho</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-violet-50 text-violet-600 text-base group-hover:bg-violet-600 group-hover:text-white transition">
                    <i class="fa-solid fa-clipboard-list"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-slate-900">${fn:length(stocktakes)}</p>
            <span class="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-violet-700">
                Thực hiện kiểm đếm <i class="fa-solid fa-arrow-right text-[10px]"></i>
            </span>
        </a>

        <!-- 4. Sent Export Receipts -->
        <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" 
           class="group rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-md hover:border-emerald-300">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Phiếu xuất đã gửi</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 text-base group-hover:bg-emerald-600 group-hover:text-white transition">
                    <i class="fa-solid fa-paper-plane"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-emerald-600">${fn:length(exportReceipts)}</p>
            <span class="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-emerald-700">
                Theo dõi duyệt xuất <i class="fa-solid fa-arrow-right text-[10px]"></i>
            </span>
        </a>
    </div>

    <!-- Dashboard Inventory Metrics Section -->
    <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm space-y-5">
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 pb-4">
            <div class="flex items-center gap-3">
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-500/10 text-amber-600">
                    <i class="fa-solid fa-chart-pie text-lg"></i>
                </span>
                <div>
                    <h2 class="text-base font-bold text-slate-900">Chỉ số Tồn kho Hiện vật (Dashboard Inventory Metrics)</h2>
                    <p class="text-xs text-slate-500">Thống kê số lượng máy thực tế, khả dụng, giữ hàng và phân bổ vị trí kệ</p>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/app/warehouse/inventory" 
               class="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-3.5 py-2 text-xs font-semibold text-white shadow-sm hover:bg-slate-800 transition">
                <i class="fa-solid fa-laptop text-xs text-amber-400"></i>
                <span>Tra cứu theo Serial & Vị trí</span>
                <i class="fa-solid fa-arrow-right text-[10px]"></i>
            </a>
        </div>

        <!-- 4 Metric Cards -->
        <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <div class="rounded-xl border border-emerald-100 bg-emerald-50/50 p-4">
                <div class="flex items-center justify-between text-emerald-800">
                    <span class="text-xs font-bold uppercase tracking-wider">Khả dụng xuất bán</span>
                    <i class="fa-solid fa-circle-check text-emerald-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-emerald-700">${inventoryMetrics.totalInStock} máy</div>
                <p class="mt-0.5 text-[11px] text-emerald-600 font-medium">Trạng thái IN_STOCK</p>
            </div>

            <div class="rounded-xl border border-amber-100 bg-amber-50/50 p-4">
                <div class="flex items-center justify-between text-amber-800">
                    <span class="text-xs font-bold uppercase tracking-wider">Đang giữ cho đơn</span>
                    <i class="fa-solid fa-clock text-amber-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-amber-700">${inventoryMetrics.totalReserved} máy</div>
                <p class="mt-0.5 text-[11px] text-amber-600 font-medium">Chờ duyệt xuất kho</p>
            </div>

            <div class="rounded-xl border border-rose-100 bg-rose-50/50 p-4">
                <div class="flex items-center justify-between text-rose-800">
                    <span class="text-xs font-bold uppercase tracking-wider">Hỏng / Lỗi vật lý</span>
                    <i class="fa-solid fa-triangle-exclamation text-rose-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-rose-700">${inventoryMetrics.totalDamaged} máy</div>
                <p class="mt-0.5 text-[11px] text-rose-600 font-medium">Cần bảo hành / đổi trả</p>
            </div>

            <div class="rounded-xl border border-indigo-100 bg-indigo-50/50 p-4">
                <div class="flex items-center justify-between text-indigo-800">
                    <span class="text-xs font-bold uppercase tracking-wider">Vị trí lưu kho</span>
                    <i class="fa-solid fa-map-pin text-indigo-500"></i>
                </div>
                <div class="mt-2 text-2xl font-black text-indigo-700">${inventoryMetrics.occupiedLocations} / ${inventoryMetrics.totalLocations}</div>
                <p class="mt-0.5 text-[11px] text-indigo-600 font-medium">Kệ kho đang chứa hàng</p>
            </div>
        </div>

        <!-- Secondary Grid: Brand Breakdown & Low Stock Alert -->
        <div class="grid gap-4 lg:grid-cols-2 pt-2">
            <!-- Brand breakdown -->
            <div class="rounded-xl border border-slate-100 bg-slate-50/70 p-4">
                <h3 class="text-xs font-bold uppercase tracking-wider text-slate-700 mb-3 flex items-center gap-2">
                    <i class="fa-solid fa-tags text-slate-400"></i> Phân bổ tồn kho theo Thương hiệu
                </h3>
                <div class="space-y-2.5">
                    <c:forEach var="entry" items="${inventoryMetrics.brandStock}">
                        <div>
                            <div class="flex justify-between text-xs font-semibold text-slate-700 mb-1">
                                <span><c:out value="${entry.key}"/></span>
                                <span>${entry.value} máy</span>
                            </div>
                            <div class="h-2 w-full rounded-full bg-slate-200 overflow-hidden">
                                <div class="h-full bg-amber-500 rounded-full" style="width: ${inventoryMetrics.totalInStock > 0 ? (entry.value * 100 / inventoryMetrics.totalInStock) : 0}%"></div>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty inventoryMetrics.brandStock}">
                        <p class="text-xs text-slate-400 italic">Chưa có dữ liệu phân bổ thương hiệu.</p>
                    </c:if>
                </div>
            </div>

            <!-- Low stock alert -->
            <div class="rounded-xl border border-slate-100 bg-slate-50/70 p-4">
                <h3 class="text-xs font-bold uppercase tracking-wider text-slate-700 mb-3 flex items-center gap-2">
                    <i class="fa-solid fa-bell text-amber-500"></i> Cảnh báo Tồn kho thấp (<= 3 máy)
                </h3>
                <div class="divide-y divide-slate-200/60 text-xs">
                    <c:forEach var="low" items="${inventoryMetrics.lowStockList}">
                        <div class="flex items-center justify-between py-2">
                            <div class="min-w-0 pr-2">
                                <span class="font-bold text-slate-800 truncate block"><c:out value="${low.productName}"/></span>
                                <span class="text-[11px] font-mono text-slate-500"><c:out value="${low.productCode}"/></span>
                            </div>
                            <span class="inline-flex items-center gap-1 rounded-md bg-amber-100 px-2 py-0.5 text-xs font-bold text-amber-800 whitespace-nowrap">
                                Còn ${low.availableQty} máy
                            </span>
                        </div>
                    </c:forEach>
                    <c:if test="${empty inventoryMetrics.lowStockList}">
                        <p class="text-xs text-emerald-700 py-4 text-center font-medium">
                            <i class="fa-solid fa-check-circle mr-1"></i> Tất cả các model đều có số lượng tồn an toàn.
                        </p>
                    </c:if>
                </div>
            </div>
        </div>
    </section>

    <!-- Quick Navigation Shortcuts -->
    <div class="grid gap-3 sm:grid-cols-2">
        <a href="${pageContext.request.contextPath}/app/warehouse/import-history" 
           class="flex items-center justify-between rounded-xl border border-amber-200 bg-amber-50/60 p-4 transition hover:bg-amber-100/60">
            <div class="flex items-center gap-3">
                <i class="fa-solid fa-clock-rotate-left text-amber-700 text-lg"></i>
                <div>
                    <h3 class="text-xs font-bold uppercase tracking-wider text-amber-900">Lịch sử Nhập kho</h3>
                    <p class="text-[11px] text-amber-700">Tra cứu các phiếu nhập kho đã hoàn tất và lưu trữ</p>
                </div>
            </div>
            <i class="fa-solid fa-arrow-right text-xs text-amber-600"></i>
        </a>
        <a href="${pageContext.request.contextPath}/app/warehouse/export-history" 
           class="flex items-center justify-between rounded-xl border border-indigo-200 bg-indigo-50/60 p-4 transition hover:bg-indigo-100/60">
            <div class="flex items-center gap-3">
                <i class="fa-solid fa-clock-rotate-left text-indigo-700 text-lg"></i>
                <div>
                    <h3 class="text-xs font-bold uppercase tracking-wider text-indigo-900">Lịch sử Xuất kho</h3>
                    <p class="text-[11px] text-indigo-700">Xem lại các đợt xuất kho và biên bản giao hàng</p>
                </div>
            </div>
            <i class="fa-solid fa-arrow-right text-xs text-indigo-600"></i>
        </a>
    </div>

    <!-- Work Queues Grid -->
    <div class="grid gap-6 xl:grid-cols-2">
        <!-- 1. Handovers Queue -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 p-5">
                <div>
                    <h2 class="text-base font-bold text-slate-800">Biên bản Bàn giao chờ tiếp nhận</h2>
                    <p class="mt-0.5 text-xs text-slate-500">Đối chiếu hàng thực tế từ NCC, kiểm đếm và lập phiếu nhập kho.</p>
                </div>
                <a href="${pageContext.request.contextPath}/app/warehouse/handovers" class="text-xs font-bold text-amber-700 hover:underline">
                    Xem tất cả <i class="fa-solid fa-arrow-right text-[10px]"></i>
                </a>
            </div>
            <div class="divide-y divide-slate-100 text-xs">
                <c:forEach var="h" items="${handovers}">
                    <div class="flex flex-wrap items-center justify-between gap-3 p-4 hover:bg-slate-50/70 transition">
                        <div>
                            <span class="font-bold text-slate-900">Phiếu bàn giao #${h.handoverId} · Đơn mua #${h.requestId}</span>
                            <div class="mt-1 text-slate-500">
                                <span>NCC: <b><c:out value="${h.supplierName}"/></b></span> · 
                                <span>Số lượng: <b class="text-amber-700">${h.handoverQuantity} máy</b></span> · 
                                <span class="rounded bg-slate-100 px-1.5 py-0.5 font-semibold text-slate-700">${h.receiptStatus}</span>
                            </div>
                        </div>
                        <c:if test="${h.receiptStatus == 'WAITING' or h.receiptStatus == 'REJECTED'}">
                            <a href="${pageContext.request.contextPath}/app/warehouse/import-receipt-form?handoverId=${h.handoverId}" 
                               class="inline-flex items-center gap-1.5 rounded-xl bg-amber-600 px-3 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-amber-700">
                                <i class="fa-solid fa-barcode"></i> Quét & Lập phiếu
                            </a>
                        </c:if>
                    </div>
                </c:forEach>
                <c:if test="${empty handovers}">
                    <div class="p-8 text-center text-slate-400">Không có phiếu bàn giao nào đang chờ tiếp nhận.</div>
                </c:if>
            </div>
        </section>

        <!-- 2. Export Orders Queue -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 p-5">
                <div>
                    <h2 class="text-base font-bold text-slate-800">Đơn hàng xuất kho chờ soạn máy</h2>
                    <p class="mt-0.5 text-xs text-slate-500">Chọn đúng số Serial từ kho và lập phiếu gửi Quản lý phê duyệt.</p>
                </div>
                <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" class="text-xs font-bold text-indigo-700 hover:underline">
                    Xem tất cả <i class="fa-solid fa-arrow-right text-[10px]"></i>
                </a>
            </div>
            <div class="divide-y divide-slate-100 text-xs">
                <c:forEach var="r" items="${exportRequests}">
                    <div class="flex flex-wrap items-center justify-between gap-3 p-4 hover:bg-slate-50/70 transition">
                        <div>
                            <span class="font-bold text-slate-900">Yêu cầu xuất #${r.requestId}</span> · 
                            <span class="text-slate-600"><c:out value="${r.customerName}"/></span>
                            <div class="mt-1 text-slate-400">
                                <fmt:formatDate value="${r.requestDate}" pattern="dd/MM/yyyy HH:mm"/> · ${fn:length(r.items)} dòng sản phẩm
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/app/warehouse/export-form?requestId=${r.requestId}" 
                           class="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-3 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-indigo-700">
                            <i class="fa-solid fa-box-open"></i> Soạn Serial
                        </a>
                    </div>
                </c:forEach>
                <c:if test="${empty exportRequests}">
                    <div class="p-8 text-center text-slate-400">Không có đơn xuất kho nào đang chờ soạn hàng.</div>
                </c:if>
            </div>
        </section>

        <!-- 3. Stocktakes Queue -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 p-5">
                <div>
                    <h2 class="text-base font-bold text-slate-800">Đợt kiểm kê được giao</h2>
                    <p class="mt-0.5 text-xs text-slate-500">Đối chiếu Serial thực tế và ghi nhận vị trí, tình trạng máy.</p>
                </div>
                <a href="${pageContext.request.contextPath}/app/warehouse/stocktakes" class="text-xs font-bold text-violet-700 hover:underline">
                    Xem tất cả <i class="fa-solid fa-arrow-right text-[10px]"></i>
                </a>
            </div>
            <div class="divide-y divide-slate-100 text-xs">
                <c:forEach var="check" items="${stocktakes}">
                    <div class="flex flex-wrap items-center justify-between gap-3 p-4 hover:bg-slate-50/70 transition">
                        <div>
                            <span class="font-bold text-slate-900">Đợt kiểm kho #${check.checkId}</span> · 
                            <span class="rounded bg-violet-50 px-2 py-0.5 text-[11px] font-bold text-violet-700">${check.status}</span>
                            <div class="mt-1 text-slate-500">
                                Phạm vi: <b><c:out value="${empty check.locationFilter ? 'Toàn bộ kho' : check.locationFilter}"/></b> · 
                                <fmt:formatDate value="${check.checkDate}" pattern="dd/MM/yyyy"/>
                            </div>
                        </div>
                        <c:if test="${check.status == 'ASSIGNED' or check.status == 'REJECTED'}">
                            <a href="${pageContext.request.contextPath}/app/warehouse/stocktake-form?checkId=${check.checkId}" 
                               class="inline-flex items-center gap-1.5 rounded-xl bg-violet-600 px-3 py-1.5 text-xs font-semibold text-white shadow-sm hover:bg-violet-700">
                                <i class="fa-solid fa-clipboard-check"></i> Kiểm kho ngay
                            </a>
                        </c:if>
                    </div>
                </c:forEach>
                <c:if test="${empty stocktakes}">
                    <div class="p-8 text-center text-slate-400">Không có đợt kiểm kê nào được giao cho bạn.</div>
                </c:if>
            </div>
        </section>

        <!-- 4. Export Receipts Status Tracking -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="border-b border-slate-100 p-5">
                <h2 class="text-base font-bold text-slate-800">Phiếu xuất kho đã chuyển duyệt</h2>
                <p class="mt-0.5 text-xs text-slate-500">Theo dõi kết quả phê duyệt từ Quản lý kho.</p>
            </div>
            <div class="divide-y divide-slate-100 text-xs">
                <c:forEach var="receipt" items="${exportReceipts}">
                    <div class="p-4 hover:bg-slate-50/70 transition">
                        <div class="flex items-center justify-between">
                            <span class="font-bold text-slate-900">Phiếu xuất #${receipt.receiptId} (Yêu cầu #${receipt.requestId})</span>
                            <span class="inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-bold ring-1 ring-inset ${receipt.status == 'COMPLETED' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : receipt.status == 'PENDING_MANAGER' ? 'bg-amber-50 text-amber-700 ring-amber-600/20' : 'bg-rose-50 text-rose-700 ring-rose-600/20'}">
                                ${receipt.status}
                            </span>
                        </div>
                        <c:if test="${receipt.status == 'REJECTED'}">
                            <p class="mt-1.5 text-xs text-rose-600 bg-rose-50 p-2 rounded-lg">
                                <b>Lý do từ chối:</b> <c:out value="${receipt.cancelReason}"/>
                            </p>
                        </c:if>
                        <div class="mt-1.5 text-slate-400 flex flex-wrap gap-2">
                            <c:forEach var="item" items="${receipt.items}">
                                <span class="bg-slate-100 px-2 py-0.5 rounded text-[11px] text-slate-600 font-mono">
                                    <c:out value="${item.productCode}"/> / <c:out value="${item.serialNumber}"/>
                                </span>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>
                <c:if test="${empty exportReceipts}">
                    <div class="p-8 text-center text-slate-400">Chưa có phiếu xuất nào được gửi duyệt.</div>
                </c:if>
            </div>
        </section>
    </div>
</main>
</div>
</div>
</body>
</html>