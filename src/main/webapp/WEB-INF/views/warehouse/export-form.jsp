<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Soạn hàng & Lập phiếu xuất | LIMS Warehouse</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeWarehouseMenu" value="exports"/>
<%@ include file="shell.jspf" %>

<main class="mx-auto w-full max-w-5xl space-y-6 px-4 py-6 sm:px-6 sm:py-8">
    <!-- Breadcrumb & Back -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <div class="flex items-center gap-2 text-xs font-semibold text-slate-500">
                <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" class="hover:text-indigo-700 transition">
                    <i class="fa-solid fa-arrow-left mr-1"></i> Danh sách đơn chờ soạn
                </a>
                <span>/</span>
                <span class="text-slate-800">Đơn xuất #${exportRequest.requestId}</span>
            </div>
            <h1 class="mt-2 text-2xl font-bold tracking-tight text-slate-900">Soạn hàng & Quét Serial xuất kho</h1>
            <p class="mt-1 text-sm text-slate-500">Khách hàng: <b><c:out value="${exportRequest.customerName}"/></b> · Quét mã vạch hoặc chọn đúng số Serial theo từng Model.</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" 
           class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 transition">
            Hủy soạn hàng
        </a>
    </div>

    <!-- Alert Notices -->
    <c:if test="${not empty param.error}">
        <div class="flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-xs font-medium text-rose-800 shadow-sm">
            <i class="fa-solid fa-circle-exclamation text-rose-600 text-sm"></i>
            <span><c:out value="${param.error}"/></span>
        </div>
    </c:if>

    <!-- Quick Barcode / Serial Scanner Box -->
    <div class="rounded-2xl border border-indigo-200 bg-indigo-50/50 p-5 shadow-sm">
        <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-indigo-900">
            <i class="fa-solid fa-barcode text-indigo-600 text-base"></i>
            <span>Máy quét mã vạch / Quét nhanh Serial</span>
        </div>
        <div class="mt-2.5 flex flex-wrap gap-2.5">
            <div class="relative flex-1 min-w-[280px]">
                <input type="text" id="scannerInput" placeholder="Đặt con trỏ vào đây và quét máy (hoặc nhập Serial rồi nhấn Enter)..." 
                       class="w-full rounded-xl border border-indigo-300 bg-white py-2.5 pl-3.5 pr-10 text-xs font-mono font-bold text-slate-900 placeholder:font-sans placeholder:font-normal focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
                <span class="absolute right-3 top-2.5 text-xs text-indigo-400">
                    <i class="fa-solid fa-qrcode"></i>
                </span>
            </div>
            <button type="button" onclick="handleScan()" 
                    class="rounded-xl bg-indigo-600 px-5 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-indigo-700">
                <i class="fa-solid fa-plus mr-1"></i> Chọn Serial
            </button>
        </div>
        <div id="scanFeedback" class="mt-2 hidden text-xs font-semibold"></div>
    </div>

    <!-- Export Form -->
    <form id="exportForm" method="post" action="${pageContext.request.contextPath}/app/warehouse/export-receipts" class="space-y-6">
        <input type="hidden" name="requestId" value="${exportRequest.requestId}">

        <!-- Product Line Sections -->
        <c:forEach var="line" items="${exportRequest.items}" varStatus="lineStatus">
            <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm" id="section-${line.productId}">
                <!-- Section Header with Live Counter -->
                <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 bg-slate-50/75 px-5 py-3.5">
                    <div>
                        <div class="flex items-center gap-2">
                            <span class="rounded bg-amber-100 px-2 py-0.5 font-mono text-xs font-bold text-amber-800">
                                <c:out value="${line.productCode}"/>
                            </span>
                            <h2 class="text-sm font-bold text-slate-900"><c:out value="${line.productName}"/></h2>
                        </div>
                    </div>

                    <!-- Live Counter Badge -->
                    <div class="flex items-center gap-3">
                        <div class="text-right">
                            <span class="text-[11px] font-semibold uppercase text-slate-400">Tiến độ soạn:</span>
                            <span class="font-bold text-xs ml-1" id="counter-text-${line.productId}">0 / ${line.quantity} máy</span>
                        </div>
                        <span id="badge-status-${line.productId}" class="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-600">
                            <span class="h-1.5 w-1.5 rounded-full bg-slate-400"></span> Chưa đủ
                        </span>
                    </div>
                </div>

                <!-- Available Serials Grid -->
                <div class="p-5">
                    <p class="text-xs text-slate-500 mb-3">
                        Chọn đúng <b>${line.quantity}</b> serial máy từ các vị trí lưu kho bên dưới:
                    </p>

                    <c:set var="hasInventory" value="false"/>
                    <div class="grid gap-2.5 sm:grid-cols-2 lg:grid-cols-3">
                        <c:forEach var="serial" items="${availableSerials}">
                            <c:if test="${serial.productId == line.productId}">
                                <c:set var="hasInventory" value="true"/>
                                <label id="card-${serial.serialNumber}" 
                                       class="relative flex cursor-pointer items-start gap-3 rounded-xl border border-slate-200 p-3.5 text-xs transition hover:bg-slate-50 hover:border-slate-300">
                                    <input type="checkbox" name="serialNumbers" value="${serial.serialNumber}" 
                                           data-product-id="${line.productId}" 
                                           data-required="${line.quantity}"
                                           onchange="updateCounters()" 
                                           class="serial-checkbox mt-0.5 h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500">
                                    <div class="flex-1 min-w-0">
                                        <div class="font-mono font-bold text-slate-900">
                                            <c:out value="${serial.serialNumber}"/>
                                        </div>
                                        <div class="mt-1 flex items-center gap-1 text-[11px] text-slate-500">
                                            <i class="fa-solid fa-map-pin text-[10px] text-indigo-600"></i>
                                            <span>Vị trí: <b class="text-slate-700"><c:out value="${serial.location}"/></b></span>
                                        </div>
                                    </div>
                                </label>
                            </c:if>
                        </c:forEach>
                    </div>

                    <c:if test="${not hasInventory}">
                        <div class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-xs text-rose-700">
                            <i class="fa-solid fa-triangle-exclamation mr-1"></i>
                            Không có serial khả dụng nào trong kho cho model này. Vui lòng liên hệ Quản lý kiểm tra tồn kho.
                        </div>
                    </c:if>
                </div>
            </section>
        </c:forEach>

        <!-- Note Input -->
        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <label class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
                Ghi chú phiếu xuất kho (Tùy chọn)
            </label>
            <textarea name="note" rows="2" maxlength="255" placeholder="Ghi chú về tình trạng đóng gói, người nhận hàng..." 
                      class="w-full rounded-xl border border-slate-300 p-3 text-xs focus:border-indigo-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"></textarea>
        </div>

        <!-- Submit Actions -->
        <div class="flex items-center justify-end gap-3 pt-2">
            <a href="${pageContext.request.contextPath}/app/warehouse/export-requests" 
               class="rounded-xl border border-slate-300 bg-white px-5 py-3 text-xs font-semibold text-slate-700 hover:bg-slate-50">
                Hủy bỏ
            </a>
            <button type="submit" id="submitBtn" 
                    class="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-6 py-3 text-xs font-semibold text-white shadow-md shadow-indigo-600/20 transition hover:bg-indigo-700">
                <i class="fa-solid fa-paper-plane"></i>
                <span>Gửi phiếu xuất cho Manager duyệt</span>
            </button>
        </div>
    </form>
</main>

<script>
    // Scanner handling
    const scannerInput = document.getElementById('scannerInput');
    scannerInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleScan();
        }
    });

    function handleScan() {
        const query = scannerInput.value.trim().toUpperCase();
        const feedback = document.getElementById('scanFeedback');
        if (!query) return;

        const checkboxes = document.querySelectorAll('.serial-checkbox');
        let found = false;

        checkboxes.forEach(cb => {
            if (cb.value.toUpperCase() === query) {
                found = true;
                if (cb.checked) {
                    feedback.className = 'mt-2 text-xs font-semibold text-amber-600';
                    feedback.innerHTML = '<i class="fa-solid fa-circle-info mr-1"></i> Serial <b>' + query + '</b> đã được chọn từ trước.';
                } else {
                    cb.checked = true;
                    feedback.className = 'mt-2 text-xs font-semibold text-emerald-600';
                    feedback.innerHTML = '<i class="fa-solid fa-circle-check mr-1"></i> Đã chọn thành công Serial: <b>' + query + '</b>';
                    
                    // Highlight card
                    const card = document.getElementById('card-' + cb.value);
                    if (card) {
                        card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        card.classList.add('ring-2', 'ring-indigo-500', 'bg-indigo-50/50');
                    }
                }
            }
        });

        if (!found) {
            feedback.className = 'mt-2 text-xs font-semibold text-rose-600';
            feedback.innerHTML = '<i class="fa-solid fa-circle-xmark mr-1"></i> Không tìm thấy Serial <b>' + query + '</b> trong danh sách khả dụng cho đơn này.';
        }

        feedback.classList.remove('hidden');
        scannerInput.value = '';
        scannerInput.focus();
        updateCounters();
    }

    function updateCounters() {
        const checkboxes = document.querySelectorAll('.serial-checkbox');
        const counts = {};
        const required = {};

        checkboxes.forEach(cb => {
            const pId = cb.getAttribute('data-product-id');
            const req = parseInt(cb.getAttribute('data-required'));
            required[pId] = req;
            if (!counts[pId]) counts[pId] = 0;
            if (cb.checked) {
                counts[pId]++;
                const card = document.getElementById('card-' + cb.value);
                if (card) card.classList.add('border-indigo-400', 'bg-indigo-50/30');
            } else {
                const card = document.getElementById('card-' + cb.value);
                if (card) card.classList.remove('border-indigo-400', 'bg-indigo-50/30');
            }
        });

        for (const pId in required) {
            const current = counts[pId] || 0;
            const target = required[pId];
            const textElem = document.getElementById('counter-text-' + pId);
            const badgeElem = document.getElementById('badge-status-' + pId);

            if (textElem) textElem.innerText = current + ' / ' + target + ' máy';

            if (badgeElem) {
                if (current === target) {
                    badgeElem.className = 'inline-flex items-center gap-1 rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-800';
                    badgeElem.innerHTML = '<span class="h-1.5 w-1.5 rounded-full bg-emerald-600"></span> Đã đủ số lượng';
                } else if (current > target) {
                    badgeElem.className = 'inline-flex items-center gap-1 rounded-full bg-rose-100 px-2.5 py-0.5 text-xs font-semibold text-rose-800';
                    badgeElem.innerHTML = '<span class="h-1.5 w-1.5 rounded-full bg-rose-600"></span> Chọn thừa ' + (current - target) + ' máy';
                } else {
                    badgeElem.className = 'inline-flex items-center gap-1 rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-semibold text-slate-600';
                    badgeElem.innerHTML = '<span class="h-1.5 w-1.5 rounded-full bg-slate-400"></span> Còn thiếu ' + (target - current) + ' máy';
                }
            }
        }
    }

    // Validate on submit
    document.getElementById('exportForm').addEventListener('submit', function(e) {
        const checkboxes = document.querySelectorAll('.serial-checkbox');
        const counts = {};
        const required = {};

        checkboxes.forEach(cb => {
            const pId = cb.getAttribute('data-product-id');
            const req = parseInt(cb.getAttribute('data-required'));
            required[pId] = req;
            if (!counts[pId]) counts[pId] = 0;
            if (cb.checked) counts[pId]++;
        });

        for (const pId in required) {
            const current = counts[pId] || 0;
            const target = required[pId];
            if (current !== target) {
                e.preventDefault();
                alert('Vui lòng chọn đúng chính xác ' + target + ' serial cho từng dòng sản phẩm! (Hiện tại: ' + current + '/' + target + ')');
                return false;
            }
        }
    });

    // Run initial counter update
    updateCounters();
</script>

</div>
</div>
</body>
</html>
