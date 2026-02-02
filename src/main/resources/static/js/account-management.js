/**
 * Logic quản lý Tài khoản cho Dashboard
 */

document.addEventListener('DOMContentLoaded', () => {
    // Tải dữ liệu ban đầu
    if (document.getElementById('account-manage-tbody')) {
        loadAccountsForManage();
    }
});

async function loadAccountsForManage() {
    try {
        const response = await fetch('/management/api/accounts');
        const accounts = await response.json();
        const tbody = document.getElementById('account-manage-tbody');
        if (!tbody) return;

        tbody.innerHTML = accounts.map(acc => `
            <tr class="hover:bg-slate-50 transition-all duration-200">
                <td class="px-8 py-6 font-bold text-slate-900">${acc.username}</td>
                <td class="px-8 py-6">${acc.fullName || 'N/A'}</td>
                <td class="px-8 py-6 text-sm text-slate-500">${acc.email}</td>
                <td class="px-8 py-6">
                    <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase ${getRoleClass(acc.roleName)}">
                        ${acc.roleName}
                    </span>
                </td>
                <td class="px-8 py-6">
                    <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase ${acc.status ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'}">
                        ${acc.status ? 'ACTIVE' : 'LOCKED'}
                    </span>
                </td>
                <td class="px-8 py-6 text-center">
                    ${acc.emailVerified ?
                '<i data-lucide="check-circle-2" class="w-5 h-5 text-emerald-500 mx-auto"></i>' :
                '<i data-lucide="x-circle" class="w-5 h-5 text-slate-300 mx-auto"></i>'}
                </td>
                <td class="px-8 py-6 text-right">
                    <a href="/management/accounts/${acc.id}" class="text-indigo-600 hover:text-indigo-900 mx-2 transition-transform hover:scale-110 inline-block">
                        <i data-lucide="edit-3" class="w-5 h-5"></i>
                    </a>
                    <button onclick="deleteAccount('${acc.id}')" class="text-rose-600 hover:text-rose-900 mx-2 transition-transform hover:scale-110">
                        <i data-lucide="trash-2" class="w-5 h-5"></i>
                    </button>
                </td>
            </tr>
        `).join('');
        lucide.createIcons();
    } catch (error) {
        console.error('Error loading accounts:', error);
        if (typeof toastService !== 'undefined') {
            toastService.error('Không thể tải danh sách tài khoản');
        }
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
