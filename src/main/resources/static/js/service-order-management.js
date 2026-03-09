let currentPage = 0;
const pageSize = 10;

document.addEventListener('DOMContentLoaded', () => {
    loadServiceOrders();

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
            row.className = 'group hover:bg-slate-50/50 transition-colors';
            
            const statusStyle = getStatusStyle(order.status);
            const dateStr = new Date(order.createdAt).toLocaleString('vi-VN');

            row.innerHTML = `
                <td class="px-8 py-6">
                    <div class="font-black text-slate-900">${order.guestName}</div>
                    <div class="text-[10px] font-bold text-indigo-600 uppercase tracking-widest mt-1">Phòng: ${order.roomNames || 'Chưa gán'}</div>
                </td>
                <td class="px-8 py-6">
                    <div class="font-bold text-slate-700">${order.serviceName}</div>
                    <div class="text-xs text-slate-400">Số lượng: x${order.quantity}</div>
                </td>
                <td class="px-8 py-6 font-black text-slate-900">
                    ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(order.totalAmount)}
                </td>
                <td class="px-8 py-6 text-xs font-medium text-slate-500">
                    ${dateStr}
                </td>
                <td class="px-8 py-6 text-center">
                    <span class="px-3 py-1.5 ${statusStyle.bg} ${statusStyle.text} text-[10px] font-black uppercase rounded-xl inline-block shadow-sm">
                        ${statusStyle.label}
                    </span>
                </td>
                <td class="px-8 py-6 text-right">
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
            <button onclick="editOrderQuantity('${order.id}', ${order.quantity})" title="Sửa số lượng"
                class="p-2.5 bg-indigo-50 text-indigo-600 rounded-xl hover:bg-indigo-600 hover:text-white transition-all shadow-sm">
                <i data-lucide="edit-3" class="w-4 h-4"></i>
            </button>
            <button onclick="updateOrderStatus('${order.id}', 'DELIVERED')" title="Xác nhận đã giao"
                class="p-2.5 bg-blue-50 text-blue-600 rounded-xl hover:bg-blue-600 hover:text-white transition-all shadow-sm">
                <i data-lucide="truck" class="w-4 h-4"></i>
            </button>
            <button onclick="updateOrderStatus('${order.id}', 'CANCELLED')" title="Hủy đơn"
                class="p-2.5 bg-rose-50 text-rose-600 rounded-xl hover:bg-rose-600 hover:text-white transition-all shadow-sm">
                <i data-lucide="x" class="w-4 h-4"></i>
            </button>
        `;
    } else if (order.status === 'DELIVERED') {
        buttons += `
            <button onclick="updateOrderStatus('${order.id}', 'COMPLETED')" title="Hoàn thành đơn"
                class="p-2.5 bg-emerald-50 text-emerald-600 rounded-xl hover:bg-emerald-600 hover:text-white transition-all shadow-sm">
                <i data-lucide="check-check" class="w-4 h-4"></i>
            </button>
        `;
    }
    
    return buttons;
}

async function editOrderQuantity(id, currentQty) {
    const newQtyStr = prompt(`Cập nhật số lượng cho dịch vụ (Hiện tại: ${currentQty}):`, currentQty);
    
    if (newQtyStr === null) return; // Cancelled
    
    const newQty = parseInt(newQtyStr);
    
    if (isNaN(newQty) || newQty <= 0) {
        alert('Vui lòng nhập số lượng hợp lệ (số nguyên lớn hơn 0).');
        return;
    }

    if (newQty === currentQty) return;

    try {
        const response = await fetch(`/management/service-orders/api/${id}/quantity`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: newQty })
        });

        if (response.ok) {
            loadServiceOrders();
            if (typeof showToast !== 'undefined') {
                showToast('Đã cập nhật số lượng thành công');
            }
        } else {
            const error = await response.json();
            alert(error.message || 'Cập nhật thất bại');
        }
    } catch (error) {
        console.error('Error updating order quantity:', error);
        alert('Có lỗi xảy ra khi cập nhật số lượng');
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
        } else {
            const error = await response.json();
            alert(error.message || 'Cập nhật thất bại');
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

    // Page numbers (simple)
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

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}
