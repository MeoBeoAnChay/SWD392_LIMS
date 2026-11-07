<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý Tồn kho & Vị trí | LIMS Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeManagerMenu" value="inventory"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Header Title -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <h1 class="text-2xl font-bold tracking-tight text-slate-900">Quản lý Tồn kho & Vị trí Lưu trữ</h1>
            <p class="mt-1 text-sm text-slate-500">Tra cứu chi tiết từng máy tính theo Serial, phân bổ ô kệ và điều chuyển vị trí kho.</p>
        </div>
    </div>

    <!-- Alert Notices -->
    <c:if test="${not empty param.notice}">
        <div class="flex items-center gap-2.5 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-xs font-medium text-emerald-800 shadow-sm">
            <i class="fa-solid fa-circle-check text-emerald-600"></i>
            <span><c:out value="${param.notice}"/></span>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-800 shadow-sm">
            <i class="fa-solid fa-circle-exclamation text-rose-600"></i>
            <span><c:out value="${param.error}"/></span>
        </div>
    </c:if>

    <!-- Section 1: Warehouse Locations Management -->
    <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 p-5">
            <div class="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h2 class="text-base font-bold text-slate-800">Danh mục Vị trí Kệ kho (Warehouse Locations)</h2>
                    <p class="mt-0.5 text-xs text-slate-500">Thêm ô kệ mới hoặc đổi tên vị trí. Chỉ được xóa vị trí khi ô kệ đang trống.</p>
                </div>
                <!-- Add New Location Form -->
                <form method="post" action="${pageContext.request.contextPath}/app/manager/inventory" class="flex items-center gap-2">
                    <input type="hidden" name="action" value="saveLocation">
                    <input type="hidden" name="locationId" value="0">
                    <input required maxlength="50" name="locationName" placeholder="Mã vị trí (vd: A-01-01)" 
                           class="w-44 rounded-xl border border-slate-300 px-3 py-2 text-xs font-mono focus:border-indigo-500 focus:outline-none">
                    <button type="submit" class="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-3.5 py-2 text-xs font-semibold text-white shadow-sm hover:bg-indigo-700">
                        <i class="fa-solid fa-plus"></i> Thêm vị trí
                    </button>
                </form>
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="min-w-full text-left text-sm">
                <thead class="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                    <tr>
                        <th class="px-5 py-3">Vị trí kệ</th>
                        <th class="px-5 py-3 text-center">Tổng máy lưu trữ</th>
                        <th class="px-5 py-3 text-center">Khả dụng</th>
                        <th class="px-5 py-3 text-center">Giữ chỗ</th>
                        <th class="px-5 py-3 text-center">Hư hỏng</th>
                        <th class="px-5 py-3 text-right">Thao tác</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-xs">
                <c:forEach var="loc" items="${managedLocations}">
                    <tr class="hover:bg-slate-50/70 transition">
                        <td class="px-5 py-3 font-mono font-bold text-indigo-700">
                            <form id="location-${loc.LocationID}" method="post" action="${pageContext.request.contextPath}/app/manager/inventory" class="inline-flex items-center gap-2">
                                <input type="hidden" name="action" value="saveLocation">
                                <input type="hidden" name="locationId" value="${loc.LocationID}">
                                <input required maxlength="50" name="locationName" value="<c:out value='${loc.LocationName}'/>" 
                                       class="w-36 rounded-lg border border-slate-200 px-2 py-1 font-mono text-xs focus:border-indigo-500 focus:outline-none">
                                <button type="submit" class="text-xs font-semibold text-indigo-600 hover:text-indigo-800">
                                    <i class="fa-solid fa-pen-to-square"></i>
                                </button>
                            </form>
                        </td>
                        <td class="px-5 py-3 text-center font-bold text-slate-800">${loc.UnitCount}</td>
                        <td class="px-5 py-3 text-center font-semibold text-emerald-600">${loc.AvailableUnits}</td>
                        <td class="px-5 py-3 text-center text-amber-600">${loc.ReservedUnits}</td>
                        <td class="px-5 py-3 text-center text-rose-600">${loc.DamagedUnits}</td>
                        <td class="px-5 py-3 text-right">
                            <c:if test="${loc.UnitCount == 0}">
                                <form method="post" action="${pageContext.request.contextPath}/app/manager/inventory" class="inline">
                                    <input type="hidden" name="action" value="deleteLocation">
                                    <input type="hidden" name="locationId" value="${loc.LocationID}">
                                    <button type="submit" onclick="return confirm('Bạn có chắc muốn xóa vị trí ${loc.LocationName}?')" 
                                            class="inline-flex items-center gap-1 rounded-lg border border-rose-200 bg-rose-50 px-2.5 py-1 text-xs font-semibold text-rose-600 hover:bg-rose-100">
                                        <i class="fa-solid fa-trash-can text-[10px]"></i> Xóa
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${loc.UnitCount > 0}">
                                <span class="text-slate-400 text-[11px]">Đang chứa máy</span>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty managedLocations}">
                    <tr><td colspan="6" class="px-5 py-8 text-center text-slate-400">Chưa có vị trí nào được thiết lập. Hãy thêm vị trí ở trên.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>

    <!-- Quick Stats Summary -->
    <div class="grid gap-4 sm:grid-cols-3">
        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm flex items-center justify-between">
            <div>
                <span class="text-xs text-slate-400 block">Vị trí kệ đang dùng</span>
                <span class="text-2xl font-bold text-slate-800">${fn:length(locationStock)}</span>
            </div>
            <i class="fa-solid fa-cubes text-slate-300 text-2xl"></i>
        </div>
        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm flex items-center justify-between">
            <div>
                <span class="text-xs text-slate-400 block">Số dòng Model trong kho</span>
                <span class="text-2xl font-bold text-slate-800">${fn:length(productStock)}</span>
            </div>
            <i class="fa-solid fa-laptop text-slate-300 text-2xl"></i>
        </div>
        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm flex items-center justify-between">
            <div>
                <span class="text-xs text-slate-400 block">Số máy khớp bộ lọc</span>
                <span class="text-2xl font-bold text-indigo-600">${fn:length(inventory)}</span>
            </div>
            <i class="fa-solid fa-list-check text-slate-300 text-2xl"></i>
        </div>
    </div>

    <!-- Section 2: Detailed Inventory Serial Lookup & Location Change -->
    <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 p-5 space-y-4">
            <h2 class="text-base font-bold text-slate-800">Tra cứu danh sách Máy theo Serial & Đổi vị trí</h2>
            
            <!-- Filters Toolbar -->
            <form method="get" action="${pageContext.request.contextPath}/app/manager/inventory" class="grid gap-3 sm:grid-cols-2 lg:grid-cols-5 items-end">
                <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">Từ khóa (Serial, Model)</label>
                    <input name="q" value="<c:out value='${search}'/>" placeholder="Nhập serial hoặc model..." 
                           class="w-full rounded-xl border border-slate-300 px-3 py-2 text-xs focus:border-indigo-500 focus:outline-none">
                </div>
                <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">Vị trí kho</label>
                    <select name="location" class="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs focus:border-indigo-500 focus:outline-none">
                        <option value="">-- Tất cả vị trí --</option>
                        <c:forEach var="loc" items="${locations}">
                            <option value="<c:out value='${loc}'/>" ${selectedLocation == loc ? 'selected' : ''}><c:out value="${loc}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label class="block text-xs font-semibold text-slate-600 uppercase tracking-wider mb-1">Trạng thái máy</label>
                    <select name="status" class="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs focus:border-indigo-500 focus:outline-none">
                        <c:forEach var="s" items="${['ALL','IN_STOCK','RESERVED','DAMAGED','EXPORTED']}">
                            <option value="${s}" ${statusFilter == s ? 'selected' : ''}>
                                ${s == 'ALL' ? '-- Tất cả trạng thái --' : s}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="flex items-center gap-2">
                    <button type="submit" class="inline-flex items-center gap-1.5 rounded-xl bg-indigo-600 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-indigo-700">
                        <i class="fa-solid fa-filter"></i> Lọc
                    </button>
                    <button type="submit" name="format" value="csv" class="inline-flex items-center gap-1.5 rounded-xl border border-emerald-300 bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-700 hover:bg-emerald-100">
                        <i class="fa-solid fa-file-csv"></i> Xuất CSV
                    </button>
                    <a href="${pageContext.request.contextPath}/app/manager/inventory" class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-100">
                        Đặt lại
                    </a>
                </div>
            </form>
        </div>

        <div class="overflow-x-auto">
            <table class="min-w-full text-left text-sm">
                <thead class="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                    <tr>
                        <th class="px-5 py-3.5">Model sản phẩm</th>
                        <th class="px-5 py-3.5">Số Serial</th>
                        <th class="px-5 py-3.5">Vị trí hiện tại</th>
                        <th class="px-5 py-3.5">Trạng thái</th>
                        <th class="px-5 py-3.5">Cập nhật cuối</th>
                        <th class="px-5 py-3.5 text-right">Điều chuyển vị trí</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-xs">
                <c:forEach var="item" items="${inventory}">
                    <tr class="hover:bg-slate-50/70 transition">
                        <td class="px-5 py-3.5">
                            <span class="font-bold text-slate-900 block"><c:out value="${item.ProductCode}"/></span>
                            <span class="text-[11px] text-slate-500"><c:out value="${item.ProductName}"/> (<c:out value="${item.BrandName}"/> - <c:out value="${item.LineName}"/>)</span>
                        </td>
                        <td class="px-5 py-3.5 font-mono font-bold text-slate-800 text-xs">
                            <c:out value="${item.SerialNumber}"/>
                        </td>
                        <td class="px-5 py-3.5 font-mono font-semibold text-indigo-700">
                            <c:out value="${item.Location}"/>
                        </td>
                        <td class="px-5 py-3.5">
                            <span class="inline-flex items-center rounded-md px-2.5 py-0.5 text-[11px] font-bold ring-1 ring-inset ${item.Status == 'IN_STOCK' ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : item.Status == 'RESERVED' ? 'bg-amber-50 text-amber-700 ring-amber-600/20' : item.Status == 'DAMAGED' ? 'bg-rose-50 text-rose-700 ring-rose-600/20' : 'bg-slate-100 text-slate-700 ring-slate-500/20'}">
                                <c:out value="${item.Status}"/>
                            </span>
                        </td>
                        <td class="px-5 py-3.5 text-slate-500 whitespace-nowrap">
                            <fmt:formatDate value="${item.UpdatedAt}" pattern="dd/MM/yyyy HH:mm"/>
                        </td>
                        <td class="px-5 py-3.5 text-right">
                            <c:if test="${item.Status != 'EXPORTED'}">
                                <form method="post" action="${pageContext.request.contextPath}/app/manager/inventory" class="inline-flex items-center gap-1.5">
                                    <input type="hidden" name="inventoryId" value="${item.InventoryID}">
                                    <input type="hidden" name="q" value="<c:out value='${search}'/>">
                                    <input type="hidden" name="filterLocation" value="<c:out value='${selectedLocation}'/>">
                                    <input type="hidden" name="status" value="${statusFilter}">
                                    <select required name="location" class="rounded-lg border border-slate-300 bg-white px-2 py-1 font-mono text-xs focus:border-indigo-500 focus:outline-none">
                                        <c:forEach var="targetLocation" items="${locations}">
                                            <option value="<c:out value='${targetLocation}'/>" ${item.Location == targetLocation ? 'selected' : ''}>
                                                <c:out value="${targetLocation}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                    <button type="submit" class="rounded-lg bg-indigo-50 border border-indigo-200 px-2.5 py-1 text-xs font-semibold text-indigo-700 hover:bg-indigo-100">
                                        Lưu vị trí
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${item.Status == 'EXPORTED'}">
                                <span class="text-slate-400 text-xs">Đã xuất bán</span>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty inventory}">
                    <tr><td colspan="6" class="px-5 py-12 text-center text-slate-400">Không tìm thấy máy nào phù hợp với bộ lọc.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </section>
</main>
</div>
</div>
</body>
</html>