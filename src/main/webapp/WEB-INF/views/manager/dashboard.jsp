<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard Quản lý Kho | LIMS Manager</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeManagerMenu" value="dashboard"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto max-w-7xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Header Title -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <h1 class="text-2xl font-bold tracking-tight text-slate-900">Dashboard & Báo cáo Tồn kho</h1>
            <p class="mt-1 text-sm text-slate-500">Tổng quan tình hình tồn kho thực tế, phân bổ vị trí lưu trữ và tiến độ xét duyệt nghiệp vụ.</p>
        </div>
        <div class="flex items-center gap-3">
            <a href="${pageContext.request.contextPath}/app/manager/inventory" 
               class="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-indigo-700">
                <i class="fa-solid fa-boxes-stacked"></i> Tra cứu tồn kho chi tiết
            </a>
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

    <!-- Top Key Metrics Cards -->
    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <!-- Card 1: Total Units -->
        <article class="relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Tổng máy trong kho</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600 text-base">
                    <i class="fa-solid fa-boxes-stacked"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-slate-900"><c:out value="${stats.TotalUnits}" default="0"/></p>
            <p class="mt-1 text-xs text-slate-500"><span class="font-semibold text-indigo-600">${stats.ProductCount}</span> dòng máy hiện có trong kho</p>
        </article>

        <!-- Card 2: Available Units -->
        <article class="relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Sẵn sàng xuất kho</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 text-base">
                    <i class="fa-solid fa-circle-check"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-emerald-600"><c:out value="${stats.AvailableUnits}" default="0"/></p>
            <p class="mt-1 text-xs text-slate-500">Trạng thái khả dụng (IN_STOCK)</p>
        </article>

        <!-- Card 3: Active Locations -->
        <article class="relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Vị trí đang chứa máy</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-sky-50 text-sky-600 text-base">
                    <i class="fa-solid fa-location-dot"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-slate-900"><c:out value="${stats.LocationCount}" default="0"/></p>
            <p class="mt-1 text-xs text-slate-500">Khu vực / kệ có hàng lưu trữ</p>
        </article>

        <!-- Card 4: Pending Approvals -->
        <article class="relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
            <div class="flex items-center justify-between">
                <span class="text-xs font-bold uppercase tracking-wider text-slate-500">Chờ duyệt nghiệp vụ</span>
                <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-50 text-amber-600 text-base">
                    <i class="fa-solid fa-hourglass-half"></i>
                </span>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-amber-600">
                ${stats.PendingImports + stats.PendingExports + stats.PendingStocktakes}
            </p>
            <p class="mt-1 text-xs text-slate-500">
                Nhập: <b>${stats.PendingImports}</b> · Xuất: <b>${stats.PendingExports}</b> · Kiểm: <b>${stats.PendingStocktakes}</b>
            </p>
        </article>
    </div>

    <!-- Secondary Operational Stats -->
    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <span class="text-xs text-slate-400 block mb-1">Giữ chỗ cho đơn xuất</span>
            <div class="flex items-center justify-between">
                <span class="text-xl font-bold text-slate-800">${stats.ReservedUnits} máy</span>
                <span class="text-xs text-amber-600 bg-amber-50 px-2 py-0.5 rounded font-semibold">RESERVED</span>
            </div>
        </div>

        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <span class="text-xs text-slate-400 block mb-1">Máy lỗi / Chờ bảo hành</span>
            <div class="flex items-center justify-between">
                <span class="text-xl font-bold text-rose-600">${stats.DamagedUnits} máy</span>
                <span class="text-xs text-rose-600 bg-rose-50 px-2 py-0.5 rounded font-semibold">DAMAGED</span>
            </div>
        </div>

        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <span class="text-xs text-slate-400 block mb-1">Tổng máy đã xuất bán</span>
            <div class="flex items-center justify-between">
                <span class="text-xl font-bold text-slate-800">${stats.ExportedUnits} máy</span>
                <span class="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded font-semibold">EXPORTED</span>
            </div>
        </div>

        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <span class="text-xs text-slate-400 block mb-1">Biến động kho tháng này</span>
            <div class="flex items-center justify-between">
                <span class="text-sm font-bold text-slate-800">
                    <span class="text-emerald-600">+${stats.ImportedThisMonth} nhập</span> / 
                    <span class="text-orange-600">-${stats.ExportedThisMonth} xuất</span>
                </span>
                <i class="fa-solid fa-arrow-trend-up text-slate-300"></i>
            </div>
        </div>
    </div>

    <!-- Data Tables Grid -->
    <div class="grid gap-6 xl:grid-cols-2">
        <!-- Location Stock Table -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 p-5">
                <div>
                    <h2 class="text-base font-bold text-slate-800">Phân bổ tồn kho theo Vị trí</h2>
                    <p class="mt-0.5 text-xs text-slate-500">Chi tiết số lượng máy khả dụng, giữ chỗ và hư hỏng tại từng ô kệ.</p>
                </div>
                <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-50 text-indigo-600">
                    <i class="fa-solid fa-location-crosshairs text-xs"></i>
                </span>
            </div>
            <div class="overflow-x-auto">
                <table class="min-w-full text-left text-sm">
                    <thead class="bg-slate-50/80 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                        <tr>
                            <th class="px-4 py-3">Vị trí kệ</th>
                            <th class="px-4 py-3 text-center">Tổng máy</th>
                            <th class="px-4 py-3 text-center">Khả dụng</th>
                            <th class="px-4 py-3 text-center">Giữ chỗ</th>
                            <th class="px-4 py-3 text-center">Hư hỏng</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100 text-xs">
                    <c:forEach var="row" items="${locationStock}">
                        <tr class="hover:bg-slate-50/70 transition">
                            <td class="px-4 py-3 font-mono font-bold text-indigo-700">
                                <c:out value="${row.Location}"/>
                            </td>
                            <td class="px-4 py-3 text-center font-bold text-slate-800">${row.TotalUnits}</td>
                            <td class="px-4 py-3 text-center font-semibold text-emerald-600">${row.AvailableUnits}</td>
                            <td class="px-4 py-3 text-center text-amber-600">${row.ReservedUnits}</td>
                            <td class="px-4 py-3 text-center text-rose-600">${row.DamagedUnits}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty locationStock}">
                        <tr><td colspan="5" class="px-4 py-10 text-center text-slate-400">Chưa có dữ liệu vị trí kho.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <!-- Product Stock Table -->
        <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center justify-between border-b border-slate-100 p-5">
                <div>
                    <h2 class="text-base font-bold text-slate-800">Tồn kho theo Model sản phẩm</h2>
                    <p class="mt-0.5 text-xs text-slate-500">Số lượng máy thực tế và tình trạng theo từng dòng Laptop.</p>
                </div>
                <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-indigo-50 text-indigo-600">
                    <i class="fa-solid fa-laptop text-xs"></i>
                </span>
            </div>
            <div class="max-h-[26rem] overflow-auto">
                <table class="min-w-full text-left text-sm">
                    <thead class="sticky top-0 bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-100">
                        <tr>
                            <th class="px-4 py-3">Mã Model & Tên máy</th>
                            <th class="px-4 py-3 text-center">Tổng</th>
                            <th class="px-4 py-3 text-center">Khả dụng</th>
                            <th class="px-4 py-3 text-center">Giữ chỗ</th>
                            <th class="px-4 py-3 text-center">Hư</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100 text-xs">
                    <c:forEach var="row" items="${productStock}">
                        <tr class="hover:bg-slate-50/70 transition">
                            <td class="px-4 py-3">
                                <span class="font-bold text-slate-900 block"><c:out value="${row.ProductCode}"/></span>
                                <span class="text-[11px] text-slate-500"><c:out value="${row.ProductName}"/></span>
                            </td>
                            <td class="px-4 py-3 text-center font-bold text-slate-800">${row.TotalUnits}</td>
                            <td class="px-4 py-3 text-center font-semibold text-emerald-600">${row.AvailableUnits}</td>
                            <td class="px-4 py-3 text-center text-amber-600">${row.ReservedUnits}</td>
                            <td class="px-4 py-3 text-center text-rose-600">${row.DamagedUnits}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty productStock}">
                        <tr><td colspan="5" class="px-4 py-10 text-center text-slate-400">Chưa có sản phẩm nào trong kho.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</main>
</div>
</div>
</body>
</html>