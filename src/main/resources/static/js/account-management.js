/**
 * Logic quản lý Tài khoản cho Dashboard
 */

document.addEventListener('DOMContentLoaded', () => {
    // Tải dữ liệu ban đầu
    if (document.getElementById('account-manage-tbody')) {
        loadAccountsForManage(0);
    }
});

async function loadAccountsForManage(page = 0) {
    try {
        const tbody = document.getElementById('account-manage-tbody');
        if (!tbody) return;
        tbody.innerHTML = '<tr><td colspan="7" class="px-8 py-10 text-center text-slate-400 font-medium italic">Đang tải danh sách tài khoản...</td></tr>';

        const response = await fetch(`/management/api/accounts?page=${page}&size=10`);
        const data = await response.json();
        
        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="px-8 py-10 text-center text-slate-400 font-medium italic">Không có tài khoản nào được tìm thấy.</td></tr>';
            return;
        }

        tbody.innerHTML = data.content.map(acc => `
            <tr class="hover:bg-slate-50 transition-all duration-200">
                <td class="px-8 py-6 font-bold text-slate-900">${acc.username}</td>
                <td class="px-8 py-6 text-slate-600">${acc.fullName || 'N/A'}</td>
                <td class="px-8 py-6 text-sm text-slate-500 underline decoration-slate-200 underline-offset-4">${acc.email}</td>
                <td class="px-8 py-6">
                    <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${getRoleClass(acc.roleName)}">
                        ${acc.roleName}
                    </span>
                </td>
                <td class="px-8 py-6">
                    <button onclick="toggleAccountStatus('${acc.id}', ${acc.status})" 
                            class="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider transition-all hover:scale-105 active:scale-95 ${acc.status ? 'bg-emerald-100 text-emerald-600 hover:bg-emerald-200' : 'bg-rose-100 text-rose-600 hover:bg-rose-200'}">
                        ${acc.status ? 'HOẠT ĐỘNG' : 'ĐÃ KHÓA'}
                    </button>
                </td>
                <td class="px-8 py-6 text-center">
                    ${acc.emailVerified ?
                '<i data-lucide="shield-check" class="w-5 h-5 text-emerald-500 mx-auto"></i>' :
                '<i data-lucide="shield-alert" class="w-5 h-5 text-slate-300 mx-auto"></i>'}
                </td>
                <td class="px-8 py-6 text-right">
                    <div class="flex justify-end gap-2">
                        <a href="/management/accounts/${acc.id}" class="p-2 hover:bg-slate-100 rounded-lg text-slate-400 hover:text-indigo-600 transition-all">
                            <i data-lucide="edit-3" class="w-5 h-5"></i>
                        </a>
                        <button onclick="deleteAccount('${acc.id}')" class="p-2 hover:bg-rose-50 rounded-lg text-slate-400 hover:text-rose-600 transition-all">
                            <i data-lucide="trash-2" class="w-5 h-5"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
        
        renderAccountPagination(data);
        lucide.createIcons();
    } catch (error) {
        console.error('Error loading accounts:', error);
        const tbody = document.getElementById('account-manage-tbody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="7" class="px-8 py-10 text-center text-rose-500 font-bold">Lỗi khi tải danh sách tài khoản! Vui lòng thử lại.</td></tr>';
        }
        if (typeof toastService !== 'undefined') {
            toastService.error('Không thể tải danh sách tài khoản');
        }
    }
}

function renderAccountPagination(data) {
    const container = document.getElementById('account-pagination');
    if (!container) return;
    container.innerHTML = '';

    if (data.totalPages <= 1) return;

    for (let i = 0; i < data.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `w-10 h-10 rounded-xl font-bold transition-all ${data.number === i ? 'bg-indigo-600 text-white shadow-lg' : 'bg-white border border-slate-100 text-slate-500 hover:bg-slate-50'}`;
        btn.onclick = () => loadAccountsForManage(i);
        container.appendChild(btn);
    }
}

function getRoleClass(role) {
    switch (role) {
        case 'ADMIN': return 'bg-indigo-100 text-indigo-700';
        case 'MANAGER': return 'bg-cyan-100 text-cyan-700';
        case 'RECEPTION': return 'bg-emerald-100 text-emerald-700';
        case 'CUSTOMER': return 'bg-slate-100 text-slate-700';
        default: return 'bg-slate-100 text-slate-700';
    }
}

function openAccountModal() {
    if (typeof toastService !== 'undefined') {
        toastService.info('Tính năng thêm tài khoản mới đang được phát triển');
    }
}

function editAccount(id) {
    if (typeof toastService !== 'undefined') {
        toastService.info('Tính năng chỉnh sửa tài khoản đang được phát triển');
    }
}

async function deleteAccount(id) {
    if (confirm('Bạn có chắc muốn xóa tài khoản này?')) {
        try {
            const response = await fetch(`/management/api/accounts/${id}`, { method: 'DELETE' });
            const data = await response.json();
            if (response.ok) {
                if (typeof toastService !== 'undefined') {
                    toastService.success(data.message);
                }
                loadAccountsForManage();
            } else {
                if (typeof toastService !== 'undefined') {
                    toastService.error(data.message || 'Có lỗi xảy ra');
                }
            }
        } catch (error) {
            console.error('Error deleting account:', error);
            if (typeof toastService !== 'undefined') {
                toastService.error('Có lỗi xảy ra khi xóa tài khoản');
            }
        }
    }
}

async function toggleAccountStatus(id, currentStatus) {
    const newStatus = !currentStatus;
    const confirmMsg = newStatus ? 'Bạn có muốn mở khóa tài khoản này?' : 'Bạn có muốn khóa tài khoản này?';
    
    if (confirm(confirmMsg)) {
        try {
            const response = await fetch(`/management/api/accounts/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: newStatus })
            });
            const data = await response.json();
            
            if (response.ok) {
                if (typeof toastService !== 'undefined') {
                    toastService.success(data.message);
                } else {
                    alert(data.message);
                }
                loadAccountsForManage();
            } else {
                throw new Error(data.message || 'Có lỗi xảy ra');
            }
        } catch (error) {
            console.error('Error toggling status:', error);
            if (typeof toastService !== 'undefined') {
                toastService.error(error.message);
            } else {
                alert('Lỗi: ' + error.message);
            }
        }
    }
}
