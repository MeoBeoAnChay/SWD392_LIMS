<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tra cứu Laptop & Tình trạng máy | LIMS Warehouse</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeWarehouseMenu" value="inventory"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto w-full max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Header -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-amber-700">
                <i class="fa-solid fa-laptop"></i>
                <span>Warehouse / Quản lý Hiện vật Kho</span>
            </div>
            <h1 class="mt-1 text-2xl font-bold tracking-tight text-slate-900">Tra cứu Laptop khả dụng & Tình trạng vật lý</h1>
            <p class="mt-1 text-sm text-slate-500">Tra cứu theo Dòng sản phẩm, Serial Number, vị trí lưu trữ và cập nhật tình trạng máy hỏng/lỗi.</p>
        </div>
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/app/warehouse/dashboard" 
               class="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50">
                <i class="fa-solid fa-chart-pie text-slate-500"></i>
                <span>Dashboard Kho</span>
            </a>
            <a href="${pageContext.request.contextPath}/app/warehouse/handovers" 
               class="inline-flex items-center gap-2 rounded-xl bg-amber-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-amber-700">
                <i class="fa-solid fa-boxes-stacked"></i>
                <span>Nhập kho mới</span>
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
    <c:if test="${not empty smartNotice}">
        <div class="flex items-center gap-2.5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-xs font-semibold text-amber-900 shadow-sm">
            <i class="fa-solid fa-circle-info text-amber-600 text-sm"></i>
            <span><c:out value="${smartNotice}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-800 shadow-sm">
            <i class="fa-solid fa-circle-exclamation text-rose-600 text-sm"></i>
            <span><c:out value="${error}"/></span>
        </div>
    </c:if>

    <!-- Mini KPI Strip -->
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="flex items-center justify-between text-slate-500">
                <span class="text-xs font-bold uppercase tracking-wider">Khả dụng xuất bán</span>
                <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600"><i class="fa-solid fa-circle-check"></i></span>
            </div>
            <div class="mt-2 text-2xl font-black text-emerald-700">${inventoryMetrics.totalInStock}</div>
            <p class="text-[11px] text-slate-400">Trạng thái IN_STOCK</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="flex items-center justify-between text-slate-500">
                <span class="text-xs font-bold uppercase tracking-wider">Đang giữ cho đơn</span>
                <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-amber-50 text-amber-600"><i class="fa-solid fa-clock"></i></span>
            </div>
            <div class="mt-2 text-2xl font-black text-amber-600">${inventoryMetrics.totalReserved}</div>
            <p class="text-[11px] text-slate-400">Chờ Manager duyệt xuất</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="flex items-center justify-between text-slate-500">
                <span class="text-xs font-bold uppercase tracking-wider">Hỏng / Lỗi vật lý</span>
                <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-rose-50 text-rose-600"><i class="fa-solid fa-triangle-exclamation"></i></span>
            </div>
            <div class="mt-2 text-2xl font-black text-rose-600">${inventoryMetrics.totalDamaged}</div>
            <p class="text-[11px] text-slate-400">Cần xử lý bảo hành / trả NCC</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="flex items-center justify-between text-slate-500">
                <span class="text-xs font-bold uppercase tracking-wider">Vị trí lưu trữ</span>
                <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-50 text-indigo-600"><i class="fa-solid fa-map-pin"></i></span>
            </div>
            <div class="mt-2 text-2xl font-black text-indigo-700">${inventoryMetrics.occupiedLocations} / ${inventoryMetrics.totalLocations}</div>
            <p class="text-[11px] text-slate-400">Vị trí kệ đang có hàng</p>
        </div>
    </div>

    <!-- Search & Filter Card with Full Product List -->
    <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <form method="get" action="${pageContext.request.contextPath}/app/warehouse/inventory" id="searchForm" class="grid gap-3 sm:grid-cols-2 lg:grid-cols-12">
            <!-- 1. List Sản phẩm / Model Dropdown -->
            <div class="lg:col-span-4">
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    <i class="fa-solid fa-laptop text-amber-600 mr-1"></i> Dòng sản phẩm / Model
                </label>
                <select name="productId" id="filterProduct" onchange="filterLive()" 
                        class="w-full rounded-xl border border-slate-300 py-2.5 px-3 text-xs bg-white font-medium text-slate-800 focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20">
                    <option value="">-- Tất cả sản phẩm (${fn:length(products)} dòng máy) --</option>
                    <c:forEach var="p" items="${products}">
                        <option value="${p.productId}" ${filterProductId == p.productId ? 'selected' : ''}>
                            [<c:out value="${p.productCode}"/>] <c:out value="${p.productName}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- 2. Serial Number Input -->
            <div class="lg:col-span-3">
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    <i class="fa-solid fa-barcode text-amber-600 mr-1"></i> Serial Number
                </label>
                <div class="relative">
                    <input type="text" name="serial" id="filterSerial" value="<c:out value="${filterSerial}"/>" 
                           placeholder="Nhập hoặc quét Serial..." oninput="filterLive()"
                           class="w-full rounded-xl border border-slate-300 py-2.5 pl-3.5 pr-8 text-xs font-mono font-bold text-slate-900 focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20">
                    <c:if test="${not empty filterSerial}">
                        <a href="${pageContext.request.contextPath}/app/warehouse/inventory" class="absolute right-3 top-2.5 text-xs text-slate-400 hover:text-slate-600">
                            <i class="fa-solid fa-xmark"></i>
                        </a>
                    </c:if>
                </div>
            </div>

            <!-- 3. Vị trí lưu kho -->
            <div class="lg:col-span-2">
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    <i class="fa-solid fa-location-dot text-indigo-600 mr-1"></i> Vị trí kho
                </label>
                <select name="location" id="filterLocation" onchange="filterLive()" 
                        class="w-full rounded-xl border border-slate-300 py-2.5 px-3 text-xs bg-white text-slate-800 focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20">
                    <option value="">-- Tất cả vị trí --</option>
                    <c:forEach var="loc" items="${locations}">
                        <option value="${loc}" ${filterLocation == loc ? 'selected' : ''}>${loc}</option>
                    </c:forEach>
                </select>
            </div>

            <!-- 4. Trạng thái máy -->
            <div class="lg:col-span-2">
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1">
                    <i class="fa-solid fa-filter text-slate-500 mr-1"></i> Trạng thái
                </label>
                <select name="status" id="filterStatus" onchange="filterLive()" 
                        class="w-full rounded-xl border border-slate-300 py-2.5 px-3 text-xs bg-white text-slate-800 focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20">
                    <option value="">-- Tất cả trạng thái --</option>
                    <option value="IN_STOCK" ${filterStatus == 'IN_STOCK' ? 'selected' : ''}>Sẵn sàng xuất (IN_STOCK)</option>
                    <option value="RESERVED" ${filterStatus == 'RESERVED' ? 'selected' : ''}>Đang giữ cho đơn (RESERVED)</option>
                    <option value="DAMAGED" ${filterStatus == 'DAMAGED' ? 'selected' : ''}>Hỏng / Lỗi vật lý (DAMAGED)</option>
                    <option value="RETURNED" ${filterStatus == 'RETURNED' ? 'selected' : ''}>Đã trả NCC (RETURNED)</option>
                    <option value="EXPORTED" ${filterStatus == 'EXPORTED' ? 'selected' : ''}>Đã xuất kho (EXPORTED)</option>
                </select>
            </div>

            <!-- 5. Submit Button -->
            <div class="flex items-end gap-1.5 lg:col-span-1">
                <button type="submit" class="w-full rounded-xl bg-slate-900 py-2.5 px-2 text-xs font-semibold text-white shadow-sm transition hover:bg-slate-800" title="Tìm kiếm">
                    <i class="fa-solid fa-magnifying-glass"></i>
                </button>
                <c:if test="${not empty filterSerial or not empty filterLocation or not empty filterStatus or not empty filterKeyword or (not empty filterProductId and filterProductId > 0)}">
                    <a href="${pageContext.request.contextPath}/app/warehouse/inventory" 
                       class="rounded-xl border border-slate-300 py-2.5 px-2.5 text-xs font-semibold text-slate-600 hover:bg-slate-50" title="Đặt lại bộ lọc">
                        <i class="fa-solid fa-rotate-left"></i>
                    </a>
                </c:if>
            </div>
        </form>
    </div>

    <!-- Inventory Results Table -->
    <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="flex items-center justify-between border-b border-slate-100 px-5 py-4">
            <div>
                <h2 class="text-sm font-bold text-slate-800">Danh sách Laptop trong hệ thống</h2>
                <p class="mt-0.5 text-xs text-slate-500">
                    Tìm thấy <b id="matchCount">${fn:length(items)}</b> sản phẩm phù hợp điều kiện lọc.
                </p>
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-100 text-left text-xs" id="inventoryTable">
                <thead class="bg-slate-50/75 text-[11px] font-bold uppercase tracking-wider text-slate-500">
                    <tr>
                        <th class="px-5 py-3.5">Serial Number</th>
                        <th class="px-5 py-3.5">Thông tin Máy / Model</th>
                        <th class="px-5 py-3.5">Vị trí lưu kho</th>
                        <th class="px-5 py-3.5">Trạng thái hiện vật</th>
                        <th class="px-5 py-3.5">Cập nhật lúc</th>
                        <th class="px-5 py-3.5 text-right">Thao tác</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-slate-700" id="tableBody">
                    <c:forEach var="item" items="${items}">
                        <tr class="hover:bg-slate-50/70 transition item-row" 
                            data-serial="${fn:toLowerCase(item.serialNumber)}"
                            data-product-id="${item.productId}"
                            data-product-name="${fn:toLowerCase(item.productName)} ${fn:toLowerCase(item.productCode)}"
                            data-location="${item.location}"
                            data-status="${item.status}">
                            <!-- Serial -->
                            <td class="whitespace-nowrap px-5 py-4">
                                <div class="flex items-center gap-2">
                                    <span class="rounded bg-slate-100 px-2 py-1 font-mono font-bold text-slate-900 border border-slate-200">
                                        <c:out value="${item.serialNumber}"/>
                                    </span>
                                    <button type="button" onclick="navigator.clipboard.writeText('${item.serialNumber}'); alert('Đã sao chép Serial: ${item.serialNumber}')" 
                                            class="text-slate-400 hover:text-slate-600" title="Sao chép Serial">
                                        <i class="fa-regular fa-copy"></i>
                                    </button>
                                </div>
                            </td>

                            <!-- Product Info -->
                            <td class="px-5 py-4">
                                <div class="font-bold text-slate-900"><c:out value="${item.productName}"/></div>
                                <div class="mt-0.5 flex flex-wrap gap-1.5 text-[11px] text-slate-500">
                                    <span class="rounded bg-amber-50 px-1.5 py-0.5 font-semibold text-amber-700"><c:out value="${item.productCode}"/></span>
                                    <c:if test="${not empty item.brandName}">
                                        <span class="rounded bg-slate-100 px-1.5 py-0.5 font-medium text-slate-600"><c:out value="${item.brandName}"/></span>
                                    </c:if>
                                    <c:if test="${not empty item.cpu}">
                                        <span class="text-slate-400">· <c:out value="${item.cpu}"/> / <c:out value="${item.ram}"/> / <c:out value="${item.storage}"/></span>
                                    </c:if>
                                </div>
                            </td>

                            <!-- Location -->
                            <td class="whitespace-nowrap px-5 py-4">
                                <span class="inline-flex items-center gap-1.5 rounded-lg border border-indigo-200 bg-indigo-50/70 px-2.5 py-1 font-mono text-xs font-semibold text-indigo-700">
                                    <i class="fa-solid fa-map-pin text-[10px]"></i>
                                    <c:out value="${item.location}"/>
                                </span>
                            </td>

                            <!-- Physical Status -->
                            <td class="whitespace-nowrap px-5 py-4">
                                <c:choose>
                                    <c:when test="${item.status == 'IN_STOCK'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-emerald-100 px-3 py-1 font-semibold text-emerald-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-emerald-600"></span> Sẵn sàng xuất
                                        </span>
                                    </c:when>
                                    <c:when test="${item.status == 'RESERVED'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-amber-100 px-3 py-1 font-semibold text-amber-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-amber-600"></span> Đang giữ chờ xuất
                                        </span>
                                    </c:when>
                                    <c:when test="${item.status == 'DAMAGED'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-rose-100 px-3 py-1 font-semibold text-rose-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-rose-600"></span> Hỏng / Lỗi vật lý
                                        </span>
                                    </c:when>
                                    <c:when test="${item.status == 'RETURNED'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1 font-semibold text-slate-700">
                                            <span class="h-1.5 w-1.5 rounded-full bg-slate-500"></span> Đã hoàn trả NCC
                                        </span>
                                    </c:when>
                                    <c:when test="${item.status == 'EXPORTED'}">
                                        <span class="inline-flex items-center gap-1.5 rounded-full bg-blue-100 px-3 py-1 font-semibold text-blue-800">
                                            <span class="h-1.5 w-1.5 rounded-full bg-blue-600"></span> Đã xuất cho khách
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rounded bg-slate-100 px-2 py-1 font-semibold text-slate-700">${item.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <!-- Updated At -->
                            <td class="whitespace-nowrap px-5 py-4 text-slate-500">
                                <fmt:formatDate value="${item.updatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                            </td>

                            <!-- Action -->
                            <td class="whitespace-nowrap px-5 py-4 text-right">
                                <c:if test="${item.status != 'EXPORTED'}">
                                    <button type="button" 
                                            onclick="openUpdateModal('${item.inventoryId}', '${item.serialNumber}', '${item.productName}', '${item.location}', '${item.status}')"
                                            class="inline-flex items-center gap-1.5 rounded-lg border border-slate-300 bg-white px-3 py-1.5 font-semibold text-slate-700 shadow-sm transition hover:border-amber-400 hover:bg-amber-50 hover:text-amber-800">
                                        <i class="fa-solid fa-pen-to-square text-amber-600"></i>
                                        <span>Cập nhật</span>
                                    </button>
                                </c:if>
                                <c:if test="${item.status == 'EXPORTED'}">
                                    <span class="text-slate-400 italic">Đã xuất khỏi kho</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>

                    <tr id="emptyRow" class="${empty items ? '' : 'hidden'}">
                        <td colspan="6" class="px-5 py-12 text-center text-slate-400">
                            <i class="fa-solid fa-magnifying-glass text-3xl text-slate-300"></i>
                            <p class="mt-2 text-sm font-medium text-slate-500">Không tìm thấy máy nào thỏa mãn tiêu chí tìm kiếm.</p>
                            <p class="mt-0.5 text-xs text-slate-400">Thử kiểm tra lại Serial Number hoặc chọn xóa bộ lọc.</p>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</main>

<!-- Modal Cập nhật Tình trạng Vật lý -->
<div id="updateModal" class="fixed inset-0 z-50 hidden items-center justify-center bg-slate-900/60 p-4 backdrop-blur-sm">
    <div class="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden border border-slate-200">
        <!-- Modal Header -->
        <div class="flex items-center justify-between border-b border-slate-100 bg-slate-50 px-6 py-4">
            <div class="flex items-center gap-2.5">
                <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-amber-500 text-white">
                    <i class="fa-solid fa-pen-to-square"></i>
                </span>
                <div>
                    <h3 class="text-sm font-bold text-slate-900">Cập nhật Tình trạng Vật lý & Vị trí</h3>
                    <p class="text-xs text-slate-500">Ghi nhận tình trạng thực tế của máy trong kho</p>
                </div>
            </div>
            <button type="button" onclick="closeUpdateModal()" class="rounded-lg p-1 text-slate-400 hover:text-slate-600">
                <i class="fa-solid fa-xmark text-lg"></i>
            </button>
        </div>

        <!-- Modal Form -->
        <form method="post" action="${pageContext.request.contextPath}/app/warehouse/update-condition" class="p-6 space-y-4">
            <input type="hidden" name="inventoryId" id="modalInventoryId">

            <!-- Serial & Product Display -->
            <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-200/80">
                <div class="text-[11px] font-bold uppercase tracking-wider text-slate-400">Máy đang xử lý</div>
                <div class="mt-1 font-mono font-bold text-slate-900 text-sm" id="modalSerialNumber"></div>
                <div class="mt-0.5 text-xs text-slate-600" id="modalProductName"></div>
            </div>

            <!-- Tình trạng vật lý mới -->
            <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                    Tình trạng hiện vật mới <span class="text-rose-500">*</span>
                </label>
                <select name="status" id="modalStatus" required 
                        class="w-full rounded-xl border border-slate-300 py-2.5 px-3 text-xs bg-white focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20">
                    <option value="IN_STOCK">Sẵn sàng xuất bán (IN_STOCK)</option>
                    <option value="DAMAGED">Hỏng / Lỗi vật lý - Trầy xước nặng (DAMAGED)</option>
                    <option value="RETURNED">Hoàn trả nhà cung cấp (RETURNED)</option>
                </select>
                <p class="mt-1 text-[11px] text-slate-400">Nếu đánh dấu DAMAGED, máy sẽ không được phép chọn để xuất cho khách.</p>
            </div>

            <!-- Vị trí lưu kho -->
            <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                    Vị trí lưu kho <span class="text-rose-500">*</span>
                </label>
                <select name="location" id="modalLocation" required 
                        class="w-full rounded-xl border border-slate-300 py-2.5 px-3 text-xs bg-white focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20">
                    <c:forEach var="loc" items="${locations}">
                        <option value="${loc}">${loc}</option>
                    </c:forEach>
                </select>
            </div>

            <!-- Ghi chú lý do -->
            <div>
                <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                    Ghi chú chi tiết lý do thay đổi (Tối đa 255 ký tự)
                </label>
                <textarea name="note" rows="2" maxlength="255" placeholder="Mô tả hiện trạng máy, lỗi phát sinh hoặc lý do đổi vị trí..." 
                          class="w-full rounded-xl border border-slate-300 p-2.5 text-xs focus:border-amber-500 focus:outline-none focus:ring-2 focus:ring-amber-500/20"></textarea>
            </div>

            <!-- Modal Actions -->
            <div class="mt-6 flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
                <button type="button" onclick="closeUpdateModal()" 
                        class="rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50">
                    Hủy bỏ
                </button>
                <button type="submit" 
                        class="rounded-xl bg-amber-600 px-5 py-2.5 text-xs font-semibold text-white shadow-sm hover:bg-amber-700">
                    <i class="fa-solid fa-check mr-1.5"></i> Lưu cập nhật
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function openUpdateModal(id, serial, name, loc, status) {
        document.getElementById('modalInventoryId').value = id;
        document.getElementById('modalSerialNumber').innerText = serial;
        document.getElementById('modalProductName').innerText = name;
        document.getElementById('modalLocation').value = loc;
        if (status === 'IN_STOCK' || status === 'DAMAGED' || status === 'RETURNED') {
            document.getElementById('modalStatus').value = status;
        } else {
            document.getElementById('modalStatus').value = 'IN_STOCK';
        }
        const modal = document.getElementById('updateModal');
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function closeUpdateModal() {
        const modal = document.getElementById('updateModal');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }

    // Client-side Instant Filter
    function filterLive() {
        const pId = document.getElementById('filterProduct').value;
        const serial = document.getElementById('filterSerial').value.trim().toLowerCase();
        const loc = document.getElementById('filterLocation').value;
        const status = document.getElementById('filterStatus').value;

        const rows = document.querySelectorAll('.item-row');
        let visibleCount = 0;

        rows.forEach(row => {
            const rowPId = row.getAttribute('data-product-id');
            const rowSerial = row.getAttribute('data-serial');
            const rowLoc = row.getAttribute('data-location');
            const rowStatus = row.getAttribute('data-status');

            let match = true;

            // If user enters a specific serial, we check if serial matches
            if (serial && !rowSerial.includes(serial)) {
                match = false;
            }

            if (match && pId && rowPId !== pId) {
                match = false;
            }

            if (match && loc && loc !== 'ALL' && rowLoc !== loc) {
                match = false;
            }

            // Only enforce status filter if serial is not specifically matching,
            // OR if both are present and match
            if (match && status && status !== 'ALL' && rowStatus !== status) {
                // If serial was typed, don't hide the row if it's the exact serial the user is looking for!
                if (!serial || rowSerial !== serial) {
                    match = false;
                }
            }

            if (match) {
                row.style.display = '';
                visibleCount++;
            } else {
                row.style.display = 'none';
            }
        });

        const emptyRow = document.getElementById('emptyRow');
        const matchCountElem = document.getElementById('matchCount');
        if (matchCountElem) matchCountElem.innerText = visibleCount;

        if (visibleCount === 0) {
            emptyRow.classList.remove('hidden');
        } else {
            emptyRow.classList.add('hidden');
        }
    }
</script>

</div>
</div>
</body>
</html>
