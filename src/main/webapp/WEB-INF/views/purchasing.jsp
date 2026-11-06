<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yêu cầu mua hàng | LIMS</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="min-h-screen bg-slate-50 text-slate-800">
<header class="sticky top-0 z-10 border-b border-slate-200 bg-white shadow-sm">
    <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
        <a href="${pageContext.request.contextPath}/home" class="flex items-center gap-3 font-bold"><span class="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-600 text-white"><i class="fa-solid fa-laptop"></i></span>LIMS <span class="text-sm font-normal text-slate-500">Mua hàng</span></a>
        <div class="flex items-center gap-4 text-sm"><span><c:out value="${sessionScope.LOGIN_USER.fullName}"/></span><a class="text-rose-600" href="${pageContext.request.contextPath}/logout">Đăng xuất</a></div>
    </div>
</header>
<main class="mx-auto max-w-7xl space-y-6 px-4 py-8">
    <div><h1 class="text-2xl font-bold">Yêu cầu mua hàng</h1><p class="mt-1 text-sm text-slate-500">Tạo đơn mua laptop, theo dõi phê duyệt và ghi nhận bàn giao lô hàng.</p></div>
    <c:if test="${not empty param.notice}"><div class="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800"><c:out value="${param.notice}"/></div></c:if>
    <c:if test="${not empty param.error}"><div class="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800"><c:out value="${param.error}"/></div></c:if>

    <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-4"><h2 class="text-lg font-bold">Tạo yêu cầu nhập</h2><p class="text-sm text-slate-500">Yêu cầu mới được tạo ở trạng thái Pending. Đơn giá được lấy từ danh mục sản phẩm.</p></div>
        <c:choose><c:when test="${empty suppliers}"><p class="rounded-lg bg-amber-50 p-3 text-sm text-amber-800">Chưa có nhà cung cấp ACTIVE. Vui lòng liên hệ Manager.</p></c:when><c:when test="${empty products}"><p class="rounded-lg bg-amber-50 p-3 text-sm text-amber-800">Chưa có sản phẩm ACTIVE để đặt mua.</p></c:when><c:otherwise>
            <form id="create-form" method="post" action="${pageContext.request.contextPath}/app/purchasing/requests" class="space-y-4">
                <input type="hidden" name="action" value="create">
                <label class="block max-w-xl text-sm font-medium">Nhà cung cấp<select name="supplierId" required class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"><option value="">-- Chọn nhà cung cấp --</option><c:forEach var="supplier" items="${suppliers}"><option value="${supplier.supplierId}"><c:out value="${supplier.supplierName}"/></option></c:forEach></select></label>
                <div><div class="mb-2 flex items-center justify-between"><h3 class="text-sm font-semibold">Sản phẩm và số lượng</h3><button type="button" onclick="addLine(document.getElementById('create-lines'))" class="rounded-lg border border-blue-200 px-3 py-1.5 text-xs font-semibold text-blue-700">+ Thêm sản phẩm</button></div><div id="create-lines" class="space-y-2"></div></div>
                <button class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-700">Tạo yêu cầu Pending</button>
            </form>
        </c:otherwise></c:choose>
    </section>

    <section class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div class="border-b border-slate-100 p-5"><h2 class="text-lg font-bold">Yêu cầu của tôi</h2><p class="text-sm text-slate-500">Chỉ người tạo mới được sửa hoặc hủy yêu cầu của mình.</p></div>
        <div class="divide-y divide-slate-200">
            <c:forEach var="request" items="${requests}">
                <article class="p-5">
                    <div class="flex flex-wrap items-start justify-between gap-3">
                        <div><div class="flex flex-wrap items-center gap-2"><h3 class="text-base font-bold">Yêu cầu #${request.requestId}</h3><span class="rounded-full px-2.5 py-1 text-xs font-semibold ${request.status == 'PENDING' ? 'bg-amber-100 text-amber-800' : request.status == 'APPROVED' ? 'bg-emerald-100 text-emerald-800' : request.status == 'REJECTED' ? 'bg-rose-100 text-rose-800' : 'bg-slate-100 text-slate-700'}"><c:out value="${request.status}"/></span></div>
                            <p class="mt-1 text-sm text-slate-600"><span class="font-medium">Nhà cung cấp:</span> <c:out value="${request.supplierName}"/> · <fmt:formatDate value="${request.requestDate}" pattern="dd/MM/yyyy HH:mm"/></p>
                        </div>
                        <div class="text-right text-sm"><p><span class="text-slate-500">Số lượng đặt:</span> <b>${request.orderedQuantity}</b></p><p><span class="text-slate-500">Đã bàn giao:</span> <b>${request.handedOverQuantity}</b> · <span class="text-slate-500">Còn lại:</span> <b>${request.remainingQuantity}</b></p></div>
                    </div>
                    <div class="mt-3 overflow-x-auto"><table class="min-w-full text-left text-sm"><thead class="text-xs uppercase text-slate-400"><tr><th class="py-2 pr-4">Mã</th><th class="py-2 pr-4">Sản phẩm</th><th class="py-2 pr-4">Số lượng</th><th class="py-2">Đơn giá</th></tr></thead><tbody class="divide-y divide-slate-100"><c:forEach var="item" items="${request.items}"><tr><td class="py-2 pr-4"><c:out value="${item.productCode}"/></td><td class="py-2 pr-4"><c:out value="${item.productName}"/></td><td class="py-2 pr-4"><c:out value="${item.quantity}"/></td><td class="py-2"><fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.##"/> ₫</td></tr></c:forEach></tbody></table></div>
                    <c:if test="${not empty request.cancelReason}"><p class="mt-2 text-sm text-rose-700"><b>Lý do hủy:</b> <c:out value="${request.cancelReason}"/></p></c:if>

                    <c:if test="${not empty request.handovers}"><div class="mt-3 rounded-lg bg-slate-50 p-3"><h4 class="mb-2 text-xs font-bold uppercase tracking-wide text-slate-500">Phiếu bàn giao</h4><div class="space-y-1"><c:forEach var="handover" items="${request.handovers}"><p class="text-sm"><b>#${handover.handoverId}</b> · <c:out value="${handover.handoverQuantity}"/> máy · <c:out value="${handover.handoverBy}"/> · <fmt:formatDate value="${handover.handoverDate}" pattern="dd/MM/yyyy HH:mm"/><c:if test="${not empty handover.note}"> · <c:out value="${handover.note}"/></c:if></p></c:forEach></div></div></c:if>

                    <div class="mt-4 flex flex-wrap gap-3">
                        <c:if test="${request.status == 'PENDING'}">
                            <details class="w-full rounded-lg border border-slate-200 p-3"><summary class="cursor-pointer text-sm font-semibold text-blue-700">Sửa yêu cầu</summary>
                                <form method="post" action="${pageContext.request.contextPath}/app/purchasing/requests" class="mt-3 space-y-3"><input type="hidden" name="action" value="update"><input type="hidden" name="requestId" value="${request.requestId}">
                                    <label class="block max-w-xl text-sm font-medium">Nhà cung cấp<select name="supplierId" required class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"><c:forEach var="supplier" items="${suppliers}"><option value="${supplier.supplierId}" ${supplier.supplierId == request.supplierId ? 'selected' : ''}><c:out value="${supplier.supplierName}"/></option></c:forEach></select></label>
                                    <div class="purchase-lines space-y-2"><c:forEach var="item" items="${request.items}"><div class="purchase-line flex flex-wrap items-center gap-2"><select name="productIds" required class="min-w-64 flex-1 rounded-lg border border-slate-300 px-3 py-2"><c:forEach var="product" items="${products}"><option value="${product.productId}" ${product.productId == item.productId ? 'selected' : ''}><c:out value="${product.productCode}"/> — <c:out value="${product.productName}"/></option></c:forEach></select><input name="quantities" type="number" min="1" max="2147483647" required value="${item.quantity}" class="w-32 rounded-lg border border-slate-300 px-3 py-2" aria-label="Số lượng"><button type="button" onclick="removeLine(this)" class="rounded border border-rose-200 px-3 py-2 text-rose-600" aria-label="Xóa dòng"><i class="fa-solid fa-trash"></i></button></div></c:forEach></div>
                                    <div class="flex gap-2"><button type="button" onclick="addLine(this.form.querySelector('.purchase-lines'))" class="rounded-lg border border-blue-200 px-3 py-1.5 text-xs font-semibold text-blue-700">+ Thêm sản phẩm</button><button class="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white">Lưu thay đổi</button></div>
                                </form>
                            </details>
                            <details class="rounded-lg border border-rose-200 p-3"><summary class="cursor-pointer text-sm font-semibold text-rose-700">Hủy yêu cầu</summary><form method="post" action="${pageContext.request.contextPath}/app/purchasing/requests" class="mt-3 space-y-2"><input type="hidden" name="action" value="cancel"><input type="hidden" name="requestId" value="${request.requestId}"><label class="block text-sm">Lý do hủy<textarea name="reason" required maxlength="255" rows="2" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></textarea></label><button class="rounded-lg bg-rose-600 px-3 py-2 text-sm font-semibold text-white">Xác nhận hủy</button></form></details>
                        </c:if>
                        <c:if test="${request.status == 'APPROVED' and request.remainingQuantity > 0}"><details class="w-full rounded-lg border border-emerald-200 p-3"><summary class="cursor-pointer text-sm font-semibold text-emerald-700">Tạo phiếu bàn giao</summary><form method="post" action="${pageContext.request.contextPath}/app/purchasing/requests" class="mt-3 grid gap-3 md:grid-cols-3"><input type="hidden" name="action" value="handover"><input type="hidden" name="requestId" value="${request.requestId}"><label class="text-sm">Người bàn giao / đơn vị vận chuyển<input name="handoverBy" required maxlength="100" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label><label class="text-sm">Số lượng bàn giao<input name="quantity" type="number" min="1" max="${request.remainingQuantity}" required value="${request.remainingQuantity}" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label><label class="text-sm">Ghi chú<input name="note" maxlength="255" class="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"></label><div class="md:col-span-3"><button class="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-semibold text-white">Lập phiếu bàn giao</button></div></form></details></c:if>
                    </div>
                </article>
            </c:forEach>
            <c:if test="${empty requests}"><p class="p-8 text-center text-sm text-slate-500">Bạn chưa tạo yêu cầu nhập nào.</p></c:if>
        </div>
    </section>
</main>

<template id="product-line-template"><div class="purchase-line flex flex-wrap items-center gap-2"><select name="productIds" required class="min-w-64 flex-1 rounded-lg border border-slate-300 px-3 py-2"><option value="">-- Chọn sản phẩm --</option><c:forEach var="product" items="${products}"><option value="${product.productId}"><c:out value="${product.productCode}"/> — <c:out value="${product.productName}"/></option></c:forEach></select><input name="quantities" type="number" min="1" max="2147483647" required placeholder="Số lượng" class="w-32 rounded-lg border border-slate-300 px-3 py-2" aria-label="Số lượng"><button type="button" onclick="removeLine(this)" class="rounded border border-rose-200 px-3 py-2 text-rose-600" aria-label="Xóa dòng"><i class="fa-solid fa-trash"></i></button></div></template>
<script>
    function addLine(container) {
        const template = document.getElementById('product-line-template');
        container.appendChild(template.content.cloneNode(true));
    }
    function removeLine(button) {
        const container = button.closest('.purchase-lines') || button.closest('#create-lines');
        const rows = container ? container.querySelectorAll('.purchase-line') : [];
        if (rows.length > 1) button.closest('.purchase-line').remove();
        else button.closest('.purchase-line').querySelector('select').value = '';
    }
    addLine(document.getElementById('create-lines'));
</script>
</body>
</html>
