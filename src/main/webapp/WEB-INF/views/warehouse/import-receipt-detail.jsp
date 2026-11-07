<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chi tiết phiếu nhập #${receipt.receiptId} | LIMS Warehouse</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeWarehouseMenu" value="import-history"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto w-full max-w-5xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Breadcrumb & Back -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <div class="flex items-center gap-2 text-xs font-semibold text-slate-500">
                <a href="${pageContext.request.contextPath}/app/warehouse/import-history" class="hover:text-amber-700 transition">
                    <i class="fa-solid fa-arrow-left mr-1"></i> Lịch sử nhập kho
                </a>
                <span>/</span>
                <span class="text-slate-800">Phiếu nhập #${receipt.receiptId}</span>
            </div>
            <h1 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">Chi tiết Phiếu nhập kho #${receipt.receiptId}</h1>
            <p class="mt-1 text-xs text-slate-500">Đơn mua hàng PO #${receipt.requestId} · Biên bản bàn giao #${receipt.handoverId}</p>
        </div>
        <div class="flex items-center gap-2.5">
            <a href="${pageContext.request.contextPath}/app/warehouse/import-history" 
               class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 transition">
                Quay lại
            </a>
            <button type="button" onclick="window.print()" 
                    class="inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-slate-800 transition">
                <i class="fa-solid fa-print"></i> In phiếu nhập
            </button>
        </div>
    </div>

    <!-- Overview Metadata Card -->
    <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm space-y-6">
        <div class="flex flex-wrap items-center justify-between gap-4 border-b border-slate-100 pb-5">
            <div class="flex items-center gap-3">
                <span class="flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500 text-white text-xl shadow-md shadow-amber-500/20">
                    <i class="fa-solid fa-file-invoice"></i>
                </span>
                <div>
                    <div class="text-xs font-bold uppercase tracking-wider text-slate-400">Trạng thái phiếu nhập</div>
                    <div class="mt-0.5">
                        <c:choose>
                            <c:when test="${receipt.status == 'COMPLETED'}">
                                <span class="inline-flex items-center gap-1.5 rounded-full bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-800">
                                    <span class="h-2 w-2 rounded-full bg-emerald-600"></span> Đã nhập kho chính thức
                                </span>
                            </c:when>
                            <c:when test="${receipt.status == 'PENDING_MANAGER'}">
                                <span class="inline-flex items-center gap-1.5 rounded-full bg-indigo-100 px-3 py-1 text-xs font-bold text-indigo-800">
                                    <span class="h-2 w-2 rounded-full bg-indigo-600"></span> Đang chờ Quản lý duyệt
                                </span>
                            </c:when>
                            <c:when test="${receipt.status == 'REJECTED'}">
                                <span class="inline-flex items-center gap-1.5 rounded-full bg-rose-100 px-3 py-1 text-xs font-bold text-rose-800">
                                    <span class="h-2 w-2 rounded-full bg-rose-600"></span> Quản lý từ chối duyệt
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="rounded bg-slate-100 px-2.5 py-1 text-xs font-bold">${receipt.status}</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- Paper Signed Badge -->
            <div>
                <c:if test="${receipt.paperSigned}">
                    <span class="inline-flex items-center gap-1.5 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-xs font-semibold text-emerald-800">
                        <i class="fa-solid fa-signature text-emerald-600"></i>
                        <span>Đã ký biên bản giấy hai bên</span>
                    </span>
                </c:if>
            </div>
        </div>

        <!-- Info Grid -->
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 text-xs">
            <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                <span class="text-slate-400 block font-semibold uppercase tracking-wider text-[10px]">Nhà cung cấp</span>
                <span class="font-bold text-slate-900 text-sm mt-1 block"><c:out value="${receipt.supplierName}"/></span>
                <span class="text-slate-500 mt-0.5 block">Đơn mua PO #${receipt.requestId}</span>
            </div>

            <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                <span class="text-slate-400 block font-semibold uppercase tracking-wider text-[10px]">Người tiếp nhận nhập</span>
                <span class="font-bold text-slate-900 text-sm mt-1 block"><c:out value="${receipt.warehouseStaffName}"/></span>
                <span class="text-slate-500 mt-0.5 block">
                    Lúc <fmt:formatDate value="${receipt.receiptDate}" pattern="dd/MM/yyyy HH:mm"/>
                </span>
            </div>

            <div class="rounded-xl bg-slate-50 p-3.5 border border-slate-100">
                <span class="text-slate-400 block font-semibold uppercase tracking-wider text-[10px]">Phê duyệt quản lý</span>
                <span class="font-bold text-slate-900 text-sm mt-1 block">
                    <c:out value="${empty receipt.managerName ? 'Chưa duyệt' : receipt.managerName}"/>
                </span>
                <c:if test="${not empty receipt.approvedDate}">
                    <span class="text-slate-500 mt-0.5 block">
                        Duyệt lúc <fmt:formatDate value="${receipt.approvedDate}" pattern="dd/MM/yyyy HH:mm"/>
                    </span>
                </c:if>
            </div>
        </div>

        <!-- Rejection Notice -->
        <c:if test="${receipt.status == 'REJECTED' and not empty receipt.cancelReason}">
            <div class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-800">
                <div class="font-bold flex items-center gap-1.5 text-rose-900">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    <span>Lý do từ chối từ Quản lý:</span>
                </div>
                <p class="mt-1 pl-5"><c:out value="${receipt.cancelReason}"/></p>
            </div>
        </c:if>

        <!-- Staff Note -->
        <c:if test="${not empty receipt.note}">
            <div class="rounded-xl border border-slate-200 bg-slate-50/70 p-3.5 text-xs">
                <span class="font-semibold text-slate-600 block">Ghi chú của nhân viên kho:</span>
                <p class="mt-1 text-slate-700"><c:out value="${receipt.note}"/></p>
            </div>
        </c:if>
    </div>

    <!-- Items List Table -->
    <div class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div class="flex items-center justify-between border-b border-slate-100 px-5 py-4">
            <div>
                <h2 class="text-sm font-bold text-slate-800">Danh sách Serial máy đã quét tiếp nhận</h2>
                <p class="mt-0.5 text-xs text-slate-500">Tổng cộng <b>${fn:length(receipt.items)}</b> máy được sắp xếp vào kho.</p>
            </div>
        </div>

        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-100 text-left text-xs">
                <thead class="bg-slate-50/75 text-[11px] font-bold uppercase tracking-wider text-slate-500">
                    <tr>
                        <th class="px-5 py-3.5">STT</th>
                        <th class="px-5 py-3.5">Serial Number</th>
                        <th class="px-5 py-3.5">Thông tin Model</th>
                        <th class="px-5 py-3.5">Vị trí lưu kho</th>
                        <th class="px-5 py-3.5">Tình trạng tiếp nhận</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-slate-700">
                    <c:forEach var="item" items="${receipt.items}" varStatus="status">
                        <tr class="hover:bg-slate-50/70 transition">
                            <td class="px-5 py-3.5 text-slate-400 font-mono">${status.index + 1}</td>
                            <td class="px-5 py-3.5 font-mono font-bold text-slate-900">
                                <span class="rounded bg-slate-100 px-2 py-1 border border-slate-200">
                                    <c:out value="${item.serialNumber}"/>
                                </span>
                            </td>
                            <td class="px-5 py-3.5">
                                <div class="font-semibold text-slate-900"><c:out value="${item.productName}"/></div>
                                <div class="text-[11px] text-amber-700 font-medium"><c:out value="${item.productCode}"/></div>
                            </td>
                            <td class="px-5 py-3.5">
                                <span class="inline-flex items-center gap-1 rounded bg-indigo-50 px-2 py-1 font-mono font-semibold text-indigo-700 border border-indigo-200">
                                    <i class="fa-solid fa-map-pin text-[10px]"></i>
                                    <c:out value="${item.location}"/>
                                </span>
                            </td>
                            <td class="px-5 py-3.5">
                                <c:choose>
                                    <c:when test="${item.condition == 'BRAND_NEW'}">
                                        <span class="rounded-full bg-emerald-100 px-2.5 py-0.5 font-semibold text-emerald-800">
                                            Mới 100% nguyên seal
                                        </span>
                                    </c:when>
                                    <c:when test="${item.condition == 'SCRATCHED'}">
                                        <span class="rounded-full bg-amber-100 px-2.5 py-0.5 font-semibold text-amber-800">
                                            Trầy xước ngoại quan
                                        </span>
                                    </c:when>
                                    <c:when test="${item.condition == 'DEFECTIVE'}">
                                        <span class="rounded-full bg-rose-100 px-2.5 py-0.5 font-semibold text-rose-800">
                                            Lỗi / Hỏng vật lý
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="rounded bg-slate-100 px-2 py-0.5 font-semibold">${item.condition}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</main>
</div>
</div>
</body>
</html>
