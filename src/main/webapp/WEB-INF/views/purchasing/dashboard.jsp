<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Báo cáo Mua hàng | LIMS Purchasing</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body class="bg-slate-50 text-slate-800">
<c:set var="activeMenu" value="dashboard"/>
<%@ include file="shell.jspf" %>

<section class="space-y-6">
    <!-- Header Title -->
    <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
            <h1 class="text-2xl font-bold tracking-tight text-slate-900">Tổng quan & Báo cáo Mua hàng</h1>
            <p class="mt-1 text-sm text-slate-500">Theo dõi tiến độ duyệt đơn mua (PO) và tiến độ bàn giao hàng hóa về kho.</p>
        </div>
        <a href="${pageContext.request.contextPath}/app/purchasing/request-form" 
           class="inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-semibold text-white shadow-sm transition hover:bg-emerald-700">
            <i class="fa-solid fa-plus"></i> Tạo đơn mua mới
        </a>
    </div>

    <!-- Status Cards Grid -->
    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        <!-- Chờ duyệt -->
        <a href="${pageContext.request.contextPath}/app/purchasing/requests?status=PENDING" 
           class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md hover:border-amber-300">
            <div class="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-slate-400">
                <span>Chờ duyệt</span>
                <i class="fa-solid fa-hourglass-half text-amber-500 text-sm"></i>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-amber-600">${stats.pending}</p>
            <p class="mt-1 text-xs text-slate-500">Đơn gửi Manager duyệt</p>
        </a>

        <!-- Đã duyệt -->
        <a href="${pageContext.request.contextPath}/app/purchasing/requests?status=APPROVED" 
           class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md hover:border-emerald-300">
            <div class="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-slate-400">
                <span>Đã duyệt</span>
                <i class="fa-solid fa-circle-check text-emerald-500 text-sm"></i>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-emerald-600">${stats.approved}</p>
            <p class="mt-1 text-xs text-slate-500">Sẵn sàng lập bàn giao</p>
        </a>

        <!-- Hoàn tất -->
        <a href="${pageContext.request.contextPath}/app/purchasing/requests?status=COMPLETED" 
           class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md hover:border-blue-300">
            <div class="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-slate-400">
                <span>Hoàn tất</span>
                <i class="fa-solid fa-clipboard-check text-blue-500 text-sm"></i>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-blue-600">${stats.completed}</p>
            <p class="mt-1 text-xs text-slate-500">Đã nhập kho 100%</p>
        </a>

        <!-- Bị từ chối -->
        <a href="${pageContext.request.contextPath}/app/purchasing/requests?status=REJECTED" 
           class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md hover:border-rose-300">
            <div class="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-slate-400">
                <span>Bị từ chối</span>
                <i class="fa-solid fa-circle-xmark text-rose-500 text-sm"></i>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-rose-600">${stats.rejected}</p>
            <p class="mt-1 text-xs text-slate-500">Manager từ chối duyệt</p>
        </a>

        <!-- Đã hủy -->
        <a href="${pageContext.request.contextPath}/app/purchasing/requests?status=CANCELLED" 
           class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md hover:border-slate-300">
            <div class="flex items-center justify-between text-xs font-bold uppercase tracking-wider text-slate-400">
                <span>Đã hủy</span>
                <i class="fa-solid fa-ban text-slate-400 text-sm"></i>
            </div>
            <p class="mt-3 text-3xl font-extrabold text-slate-700">${stats.cancelled}</p>
            <p class="mt-1 text-xs text-slate-500">Người lập tự hủy đơn</p>
        </a>
    </div>

    <!-- Handover Progress & Quick Actions -->
    <div class="grid gap-6 lg:grid-cols-2">
        <!-- Progress Card -->
        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm flex flex-col justify-between">
            <div>
                <div class="flex items-start justify-between">
                    <div>
                        <h2 class="text-base font-bold text-slate-800">Tiến độ Bàn giao Hàng về Kho</h2>
                        <p class="mt-0.5 text-xs text-slate-500">Tỷ lệ các phiếu bàn giao đã được nhân viên kho kiểm đếm và lập phiếu nhập.</p>
                    </div>
                    <span class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 text-base">
                        <i class="fa-solid fa-truck-ramp-box"></i>
                    </span>
                </div>

                <div class="mt-6 flex items-baseline gap-2">
                    <span class="text-4xl font-black text-slate-900">${stats.handoverReceived}</span>
                    <span class="text-base font-bold text-slate-400">/ ${stats.handoverTotal} phiếu hoàn thành</span>
                </div>

                <div class="mt-3 h-2.5 overflow-hidden rounded-full bg-slate-100">
                    <c:choose>
                        <c:when test="${stats.handoverTotal > 0}">
                            <div class="h-full rounded-full bg-emerald-500 transition-all duration-500" style="width:${100 * stats.handoverReceived / stats.handoverTotal}%"></div>
                        </c:when>
                        <c:otherwise>
                            <div class="h-full w-0"></div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="mt-6 pt-4 border-t border-slate-100">
                <a href="${pageContext.request.contextPath}/app/purchasing/handovers" 
                   class="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-700 hover:text-emerald-800">
                    <span>Xem toàn bộ danh sách biên bản bàn giao</span>
                    <i class="fa-solid fa-arrow-right text-[10px]"></i>
                </a>
            </div>
        </section>

        <!-- Quick Actions Card -->
        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 class="text-base font-bold text-slate-800 mb-4">Lối tắt tác nghiệp nhanh</h2>
            <div class="grid gap-3">
                <a href="${pageContext.request.contextPath}/app/purchasing/request-form" 
                   class="flex items-center justify-between rounded-xl border border-slate-200 p-3.5 transition hover:bg-slate-50 hover:border-emerald-300">
                    <div class="flex items-center gap-3">
                        <span class="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
                            <i class="fa-solid fa-file-circle-plus text-sm"></i>
                        </span>
                        <div>
                            <span class="text-xs font-bold text-slate-800 block">Lập đơn mua hàng mới (Purchase Order)</span>
                            <span class="text-[11px] text-slate-500">Tạo đơn đặt mua máy tính từ các nhà cung cấp</span>
                        </div>
                    </div>
                    <i class="fa-solid fa-chevron-right text-xs text-slate-400"></i>
                </a>

                <a href="${pageContext.request.contextPath}/app/purchasing/requests" 
                   class="flex items-center justify-between rounded-xl border border-slate-200 p-3.5 transition hover:bg-slate-50 hover:border-blue-300">
                    <div class="flex items-center gap-3">
                        <span class="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
                            <i class="fa-solid fa-magnifying-glass text-sm"></i>
                        </span>
                        <div>
                            <span class="text-xs font-bold text-slate-800 block">Theo dõi trạng thái các đơn mua</span>
                            <span class="text-[11px] text-slate-500">Kiểm tra đơn pending, approved hoặc đã hoàn thành</span>
                        </div>
                    </div>
                    <i class="fa-solid fa-chevron-right text-xs text-slate-400"></i>
                </a>

                <a href="${pageContext.request.contextPath}/app/purchasing/suppliers" 
                   class="flex items-center justify-between rounded-xl border border-slate-200 p-3.5 transition hover:bg-slate-50 hover:border-violet-300">
                    <div class="flex items-center gap-3">
                        <span class="flex h-9 w-9 items-center justify-center rounded-lg bg-violet-50 text-violet-600">
                            <i class="fa-solid fa-building text-sm"></i>
                        </span>
                        <div>
                            <span class="text-xs font-bold text-slate-800 block">Tra cứu danh bạ Nhà cung cấp</span>
                            <span class="text-[11px] text-slate-500">Xem thông tin liên hệ, địa chỉ, người phụ trách</span>
                        </div>
                    </div>
                    <i class="fa-solid fa-chevron-right text-xs text-slate-400"></i>
                </a>
            </div>
        </section>
    </div>
</section>
</main>
</div>
</div>
</body>
</html>