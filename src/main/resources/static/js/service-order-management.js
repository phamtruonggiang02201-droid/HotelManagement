let currentPage = 0;
const pageSize = 10;
let employeeCache = [];
let currentAssignmentId = null;
let currentEditId = null;
let currentCancelId = null;
let currentStatusUpdate = { id: null, status: null };


document.addEventListener('DOMContentLoaded', () => {
    loadServiceOrders();
    loadEmployees();

    // Event Listeners
    document.getElementById('order-search').addEventListener('input', debounce(() => {
        currentPage = 0;
        loadServiceOrders();
    }, 500));

    document.getElementById('status-filter').addEventListener('change', () => {
        currentPage = 0;
        loadServiceOrders();
    });
});

async function loadEmployees() {
    try {
        const response = await fetch('/management/employees/api?size=200');
        const data = await response.json();
        employeeCache = data.content || [];
        populateEmployeeSelect();
    } catch (error) {
        console.error('Error loading employees:', error);
    }
}

function populateEmployeeSelect() {
    const select = document.getElementById('employee-select');
    if (!select) return;
    
    select.innerHTML = '<option value="">Chọn nhân viên...</option>';
    employeeCache.forEach(emp => {
        const option = document.createElement('option');
        option.value = emp.id;
        option.textContent = `${emp.fullName} (${emp.jobTitle || emp.roleName})`;
        select.appendChild(option);
    });
}

async function loadServiceOrders() {
    const keyword = document.getElementById('order-search').value;
    const status = document.getElementById('status-filter').value;
    const tbody = document.getElementById('order-table-body');
    const emptyState = document.getElementById('empty-state');

    try {
        const response = await fetch(`/management/service-orders/api?keyword=${keyword}&status=${status}&page=${currentPage}&size=${pageSize}`);
        const data = await response.json();

        tbody.innerHTML = '';
        
        if (!data.content || data.content.length === 0) {
            emptyState.classList.remove('hidden');
            renderPagination(0, 0);
            return;
        }

        emptyState.classList.add('hidden');
        data.content.forEach(order => {
            const row = document.createElement('tr');
            row.className = 'group hover:bg-slate-50/50 transition-colors border-b border-slate-50';
            
            const statusStyle = getStatusStyle(order.status);
            const dateStr = new Date(order.createdAt).toLocaleString('vi-VN');

            // Render details list with assignment info
            const detailsHtml = order.details.map(d => {
                let assignmentHtml = '';
                const isAssigned = d.assignmentId && d.staffName !== 'Chưa có' && d.staffName !== 'Đang chờ...';
                
                if (isAssigned) {
                    assignmentHtml = `
                        <div class="flex items-center gap-1.5 mt-0.5 text-[10px] font-bold text-indigo-500">
                            <i data-lucide="user" class="w-3 h-3"></i>
                            <span>${d.staffName}</span>
                            <span class="px-1.5 py-0.5 bg-indigo-50 rounded-md text-[8px] uppercase tracking-tighter">${d.status}</span>
                        </div>
                    `;
                } else if (order.status !== 'CANCELLED' && order.status !== 'COMPLETED') {
                    // Show assignment buttons if unassigned
                    const canAssign = window.currentUserRole === 'ADMIN' || window.currentUserRole === 'MANAGER';
                    const canClaim = !isAssigned && (window.currentUserRole !== 'GUEST' && window.currentUserRole !== 'CUSTOMER');

                    assignmentHtml = `
                        <div class="flex gap-2 mt-1">
                            ${canAssign ? `
                                <button onclick="openAssignModal('${d.assignmentId}')" 
                                    class="text-[9px] font-black uppercase tracking-widest text-indigo-600 hover:text-indigo-800 flex items-center gap-1">
                                    <i data-lucide="user-plus" class="w-2.5 h-2.5"></i> Giao việc
                                </button>
                            ` : ''}
                            ${canClaim && !canAssign ? `
                                <button onclick="handleClaimTask('${d.assignmentId}')" 
                                    class="text-[9px] font-black uppercase tracking-widest text-emerald-600 hover:text-emerald-800 flex items-center gap-1">
                                    <i data-lucide="hand" class="w-2.5 h-2.5"></i> Nhận việc
                                </button>
                            ` : ''}
                        </div>
                    `;
                }

                return `
                    <div class="py-2 border-b border-slate-50 last:border-0 group/item">
                        <div class="flex justify-between items-center">
                            <span class="text-sm font-medium text-slate-700">${d.serviceName} <span class="text-slate-400 font-bold">x${d.quantity}</span></span>
                            ${order.status === 'ORDERED' ? `
                                <button onclick="openQuantityModal('${d.id}', ${d.quantity}, '${d.serviceName}')" 
                                    class="p-1 px-2 text-indigo-500 hover:bg-white rounded-lg border border-slate-100 transition-all flex items-center gap-1 shadow-sm">
                                    <i data-lucide="edit-2" class="w-3 h-3"></i>
                                    <span class="text-[9px] font-black uppercase">Sửa</span>
                                </button>
                            ` : ''}
                        </div>
                        ${assignmentHtml}
                    </div>
                `;

            }).join('');

            row.innerHTML = `
                <td class="px-8 py-6 align-top min-w-[200px]">
                    <div class="font-black text-slate-900 mb-1 leading-tight">${order.guestName}</div>
                    <span class="px-2.5 py-1 bg-indigo-50 text-indigo-700 text-[10px] font-black uppercase rounded-lg border border-indigo-100 shadow-sm flex items-center gap-1.5 w-fit">
                        <i data-lucide="map-pin" class="w-3 h-3"></i>
                        ${order.roomName || 'N/A'}
                    </span>
                </td>
                <td class="px-8 py-6 align-top">
                    <div class="space-y-0 text-slate-600">
                        ${detailsHtml}
                    </div>
                </td>
                <td class="px-8 py-6 font-black text-slate-900 align-top">
                    ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(order.totalAmount)}
                </td>
                <td class="px-8 py-6 text-xs font-bold text-slate-400 align-top leading-relaxed uppercase tracking-tighter">
                    ${dateStr.split(',').join('<br>')}
                </td>
                <td class="px-8 py-6 text-center align-top">
                    <span class="px-3 py-1.5 ${statusStyle.bg} ${statusStyle.text} text-[10px] font-black uppercase rounded-xl inline-block shadow-sm">
                        ${statusStyle.label}
                    </span>
                </td>
                <td class="px-8 py-6 text-right align-top">
                    <div class="flex justify-end gap-2 opactiy-0 group-hover:opacity-100 transition-opacity">
                        ${renderActionButtons(order)}
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });

        renderPagination(data.totalPages, data.totalElements);
        lucide.createIcons();
    } catch (error) {
        console.error('Error loading service orders:', error);
    }
}

function getStatusStyle(status) {
    switch (status) {
        case 'ORDERED': return { label: 'Mới đặt', bg: 'bg-amber-100', text: 'text-amber-600' };
        case 'DELIVERED': return { label: 'Đã giao', bg: 'bg-blue-100', text: 'text-blue-600' };
        case 'CANCELLED': return { label: 'Đã hủy', bg: 'bg-rose-100', text: 'text-rose-600' };
        case 'COMPLETED': return { label: 'Hoàn thành', bg: 'bg-emerald-100', text: 'text-emerald-600' };
        default: return { label: status, bg: 'bg-slate-100', text: 'text-slate-600' };
    }
}

function renderActionButtons(order) {
    if (order.status === 'COMPLETED' || order.status === 'CANCELLED') return '';

    let buttons = '';
    
    if (order.status === 'ORDERED') {
        buttons += `
            <div class="flex items-center gap-2">
                <button onclick="updateOrderStatus('${order.id}', 'DELIVERED')" title="Xác nhận đã giao"
                    class="p-2.5 bg-blue-50 text-blue-600 rounded-xl hover:bg-blue-600 hover:text-white transition-all shadow-sm group/btn relative">
                    <i data-lucide="truck" class="w-4 h-4"></i>
                    <span class="absolute -top-10 left-1/2 -translate-x-1/2 px-2 py-1 bg-slate-900 text-white text-[8px] font-black rounded opacity-0 group-hover/btn:opacity-100 transition-opacity whitespace-nowrap z-10 shadow-xl">Đã giao</span>
                </button>
                <button onclick="openCancelModal('${order.id}')" title="Hủy đơn"
                    class="p-2.5 bg-rose-50 text-rose-600 rounded-xl hover:bg-rose-600 hover:text-white transition-all shadow-sm group/btn relative">
                    <i data-lucide="x" class="w-4 h-4"></i>
                    <span class="absolute -top-10 left-1/2 -translate-x-1/2 px-2 py-1 bg-slate-900 text-white text-[8px] font-black rounded opacity-0 group-hover/btn:opacity-100 transition-opacity whitespace-nowrap z-10 shadow-xl">Hủy đơn</span>
                </button>
            </div>
        `;
    } else if (order.status === 'DELIVERED') {
        buttons += `
            <button onclick="updateOrderStatus('${order.id}', 'COMPLETED')" title="Hoàn thành đơn"
                class="p-2.5 bg-emerald-50 text-emerald-600 rounded-xl hover:bg-emerald-600 hover:text-white transition-all shadow-sm group/btn relative">
                <i data-lucide="check-check" class="w-4 h-4"></i>
                <span class="absolute -top-10 left-1/2 -translate-x-1/2 px-2 py-1 bg-slate-900 text-white text-[8px] font-black rounded opacity-0 group-hover/btn:opacity-100 transition-opacity whitespace-nowrap z-10 shadow-xl">Hoàn thành</span>
            </button>
        `;
    }
    
    return buttons;
}

// ===== MODAL QUANTITY =====
function openQuantityModal(id, currentQty, serviceName) {
    currentEditId = id;
    document.getElementById('qty-modal-service-name').textContent = serviceName;
    document.getElementById('new-quantity').value = currentQty;
    
    const modal = document.getElementById('quantity-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    setTimeout(() => {
        modal.querySelector('.bg-white').classList.remove('scale-95', 'opacity-0');
    }, 10);
    lucide.createIcons();
}

function closeQuantityModal() {
    const modal = document.getElementById('quantity-modal');
    modal.querySelector('.bg-white').classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        currentEditId = null;
    }, 300);
}

function updateQtyInput(delta) {
    const input = document.getElementById('new-quantity');
    let val = parseInt(input.value) + delta;
    if (val < 1) val = 1;
    input.value = val;
}

async function confirmUpdateQuantity() {
    const newQty = parseInt(document.getElementById('new-quantity').value);
    
    if (isNaN(newQty) || newQty <= 0) {
        toastService.error('Vui lòng nhập số lượng hợp lệ.');
        return;
    }

    try {
        const response = await fetch(`/management/service-orders/api/${currentEditId}/quantity`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: newQty })
        });

        if (response.ok) {
            closeQuantityModal();
            loadServiceOrders();
            toastService.success('Đã cập nhật số lượng thành công');
        } else {
            const error = await response.json();
            toastService.error(error.message || 'Cập nhật thất bại');
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
    }
}

// ===== MODAL CANCEL =====
function openCancelModal(id) {
    currentCancelId = id;
    const modal = document.getElementById('cancel-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    setTimeout(() => {
        modal.querySelector('.bg-white').classList.remove('scale-95', 'opacity-0');
    }, 10);
    lucide.createIcons();
}

function closeCancelModal() {
    const modal = document.getElementById('cancel-modal');
    modal.querySelector('.bg-white').classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        currentCancelId = null;
    }, 300);
}

async function confirmCancelOrder() {
    try {
        const response = await fetch(`/management/service-orders/api/${currentCancelId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: 'CANCELLED' })
        });

        if (response.ok) {
            closeCancelModal();
            loadServiceOrders();
            toastService.success('Đã hủy đơn dịch vụ thành công');
        } else {
            const error = await response.json();
            toastService.error(error.message || 'Hủy thất bại');
        }
    } catch (error) {
        console.error('Error cancelling order:', error);
    }
}


// ===== LOGIC GÁN VIỆC/NHẬN VIỆC =====

function openAssignModal(assignmentId) {
    if (!assignmentId || assignmentId === 'null') {
        toastService.error('Không tìm thấy bản ghi nhiệm vụ cho món này.');
        return;
    }
    currentAssignmentId = assignmentId;
    const modal = document.getElementById('assign-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    setTimeout(() => {
        modal.querySelector('.bg-white').classList.remove('scale-95', 'opacity-0');
    }, 10);
    lucide.createIcons();
}

function closeAssignModal() {
    const modal = document.getElementById('assign-modal');
    modal.querySelector('.bg-white').classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        currentAssignmentId = null;
    }, 300);
}

async function confirmTaskAssignment() {
    const employeeId = document.getElementById('employee-select').value;
    const shift = document.querySelector('input[name="shift"]:checked').value;

    if (!employeeId) {
        toastService.error('Vui lòng chọn nhân viên!');
        return;
    }

    try {
        const response = await fetch(`/management/assignments/api/${currentAssignmentId}/assign?employeeId=${employeeId}&shift=${shift}`, {
            method: 'PUT'
        });

        if (response.ok) {
            toastService.success('Đã gán nhiệm vụ thành công!');
            closeAssignModal();
            loadServiceOrders();
        } else {
            const error = await response.json();
            toastService.error(error.message || 'Gán nhiệm vụ thất bại');
        }
    } catch (error) {
        console.error('Error assigning task:', error);
        toastService.error('Có lỗi xảy ra');
    }
}

async function handleClaimTask(assignmentId) {
    if (!assignmentId || assignmentId === 'null') {
        toastService.error('Không tìm thấy bản ghi nhiệm vụ.');
        return;
    }

    if (!confirm('Đại ca có chắc chắn muốn nhận nhiệm vụ này không?')) return;

    try {
        const response = await fetch(`/api/staff/tasks/${assignmentId}/claim`, {
            method: 'POST'
        });

        const res = await response.json();
        if (response.ok && res.success) {
            toastService.success('Đã nhận việc thành công! Cố lên đại ca!');
            loadServiceOrders();
        } else {
            toastService.error(res.message || 'Nhận việc thất bại');
        }
    } catch (error) {
        console.error('Error claiming task:', error);
        toastService.error('Có lỗi xảy ra');
    }
}

// ===== CÁC HÀM CŨ =====

async function editOrderQuantity(id, currentQty, serviceName) {
    const newQtyStr = prompt(`Cập nhật số lượng cho [${serviceName}] (Hiện tại: ${currentQty}):`, currentQty);
    
    if (newQtyStr === null) return;
    const newQty = parseInt(newQtyStr);
    
    if (isNaN(newQty) || newQty <= 0) {
        toastService.error('Vui lòng nhập số lượng hợp lệ.');
        return;
    }

    try {
        const response = await fetch(`/management/service-orders/api/${id}/quantity`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: newQty })
        });

        if (response.ok) {
            loadServiceOrders();
            toastService.success('Đã cập nhật số lượng thành công');
        } else {
            const error = await response.json();
            toastService.error(error.message || 'Cập nhật thất bại');
        }
    } catch (error) {
        console.error('Error updating quantity:', error);
    }
}

async function updateOrderStatus(id, newStatus) {
    const confirmMsg = newStatus === 'CANCELLED' ? 'Bạn có chắc chắn muốn hủy đơn dịch vụ này?' : 'Cập nhật trạng thái đơn hàng này?';
    if (!confirm(confirmMsg)) return;

    try {
        const response = await fetch(`/management/service-orders/api/${id}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        if (response.ok) {
            loadServiceOrders();
            toastService.success('Cập nhật trạng thái thành công');
        } else {
            const error = await response.json();
            toastService.error(error.message || 'Cập nhật thất bại');
        }
    } catch (error) {
        console.error('Error updating order status:', error);
    }
}

function renderPagination(totalPages, totalElements) {
    const controls = document.getElementById('pagination-controls');
    const info = document.getElementById('pagination-info');

    info.textContent = `Tổng cộng: ${totalElements} đơn dịch vụ`;
    controls.innerHTML = '';

    if (totalPages <= 1) return;

    // Previous
    const prevBtn = document.createElement('button');
    prevBtn.className = `w-10 h-10 rounded-xl flex items-center justify-center transition-all ${currentPage === 0 ? 'text-slate-300 cursor-not-allowed' : 'bg-white text-slate-600 hover:bg-indigo-600 hover:text-white shadow-sm font-bold'}`;
    prevBtn.innerHTML = '<i data-lucide="chevron-left" class="w-4 h-4"></i>';
    prevBtn.onclick = () => { if (currentPage > 0) { currentPage--; loadServiceOrders(); } };
    controls.appendChild(prevBtn);

    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement('button');
        btn.className = `w-10 h-10 rounded-xl flex items-center justify-center font-black transition-all ${currentPage === i ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/20' : 'bg-white text-slate-600 hover:bg-slate-100 shadow-sm'}`;
        btn.textContent = i + 1;
        btn.onclick = () => { currentPage = i; loadServiceOrders(); };
        controls.appendChild(btn);
    }

    // Next
    const nextBtn = document.createElement('button');
    nextBtn.className = `w-10 h-10 rounded-xl flex items-center justify-center transition-all ${currentPage === totalPages - 1 ? 'text-slate-300 cursor-not-allowed' : 'bg-white text-slate-600 hover:bg-indigo-600 hover:text-white shadow-sm font-bold'}`;
    nextBtn.innerHTML = '<i data-lucide="chevron-right" class="w-4 h-4"></i>';
    nextBtn.onclick = () => { if (currentPage < totalPages - 1) { currentPage++; loadServiceOrders(); } };
    controls.appendChild(nextBtn);
}

function handleExportServiceOrders() {
    const keyword = document.getElementById('order-search').value;
    const status = document.getElementById('status-filter').value;
    window.location.href = `/management/service-orders/api/export?keyword=${encodeURIComponent(keyword)}&status=${status}`;
}

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}
