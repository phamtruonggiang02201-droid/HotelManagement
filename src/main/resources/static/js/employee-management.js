function loadEmployees(page = 0) {
    const tbody = document.getElementById('employee-manage-tbody');
    tbody.innerHTML = '<tr><td colspan="8" class="px-8 py-10 text-center text-slate-400">Đang tải dữ liệu nhân viên...</td></tr>';

    fetch(`/management/employees/api?page=${page}&size=10`)
        .then(res => res.json())
        .then(data => {
            tbody.innerHTML = '';
            if (!data.content || data.content.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" class="px-8 py-10 text-center text-slate-400">Không có nhân viên nào.</td></tr>';
                return;
            }

            data.content.forEach(emp => {
                const tr = document.createElement('tr');
                tr.className = 'hover:bg-slate-50/50 transition-colors';
                tr.innerHTML = `
                    <td class="px-8 py-6">
                        <span class="font-bold text-slate-900">${emp.username}</span>
                    </td>
                    <td class="px-8 py-6">${emp.fullName}</td>
                    <td class="px-8 py-6">${emp.email}</td>
                    <td class="px-8 py-6 text-slate-500 text-sm italic">${emp.jobTitle || '-'}</td>
                    <td class="px-8 py-6">
                        <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider ${getRoleBadgeClass(emp.roleName)}">
                            ${emp.roleName}
                        </span>
                    </td>
                    <td class="px-8 py-6 text-slate-400 text-sm">
                        ${new Date(emp.createdAt).toLocaleDateString('vi-VN')}
                    </td>
                    <td class="px-8 py-6">
                        <button onclick="toggleEmployeeStatus('${emp.id}', ${emp.status})" 
                                class="flex items-center gap-2 px-3 py-1 rounded-full transition-all hover:bg-slate-100 active:scale-95 group">
                            <span class="w-2 h-2 rounded-full ${emp.status ? 'bg-emerald-500' : 'bg-rose-500'} group-hover:animate-pulse"></span>
                            <span class="${emp.status ? 'text-emerald-600' : 'text-rose-600'} font-bold text-xs uppercase">
                                ${emp.status ? 'Hoạt động' : 'Đã khóa'}
                            </span>
                        </button>
                    </td>
                    <td class="px-8 py-6 text-right">
                        <div class="flex justify-end gap-2">
                            <a href="/management/employees/${emp.id}" class="p-2 hover:bg-slate-100 rounded-lg text-slate-400 hover:text-indigo-600 transition-all">
                                <i data-lucide="edit-3" class="w-5 h-5"></i>
                            </a>
                            <button onclick="deleteEmployee('${emp.id}')" class="p-2 hover:bg-rose-50 rounded-lg text-slate-400 hover:text-rose-600 transition-all">
                                <i data-lucide="trash-2" class="w-5 h-5"></i>
                            </button>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });

            renderPagination(data);
            lucide.createIcons();
        })
        .catch(err => {
            console.error(err);
            tbody.innerHTML = '<tr><td colspan="8" class="px-8 py-10 text-center text-rose-500 font-bold">Lỗi khi tải dữ liệu! Vui lòng thử lại.</td></tr>';
        });
}

function getRoleBadgeClass(role) {
    switch (role) {
        case 'ADMIN': return 'bg-rose-100 text-rose-600';
        case 'MANAGER': return 'bg-amber-100 text-amber-600';
        case 'RECEPTION': return 'bg-indigo-100 text-indigo-600';
        default: return 'bg-slate-100 text-slate-600';
    }
}

function renderPagination(data) {
    const container = document.getElementById('employee-pagination');
    container.innerHTML = '';

    if (data.totalPages <= 1) return;

    for (let i = 0; i < data.totalPages; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.className = `w-10 h-10 rounded-xl font-bold transition-all ${data.number === i ? 'bg-indigo-600 text-white shadow-lg' : 'bg-white border border-slate-100 text-slate-500 hover:bg-slate-50'}`;
        btn.onclick = () => loadEmployees(i);
        container.appendChild(btn);
    }
}

function deleteEmployee(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa nhân viên này? Thao tác này không thể hoàn tác.')) return;

    fetch(`/management/employees/api/${id}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(res => {
            showToast(res.message);
            loadEmployees(0);
        })
        .catch(err => {
            console.error(err);
            showToast('Lỗi khi xóa nhân viên!', 'error');
        });
}

async function toggleEmployeeStatus(id, currentStatus) {
    const newStatus = !currentStatus;
    const confirmMsg = newStatus ? 'Bạn có muốn mở khóa tài khoản nhân viên này?' : 'Bạn có muốn khóa tài khoản nhân viên này?';
    
    if (confirm(confirmMsg)) {
        try {
            const response = await fetch(`/management/api/accounts/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status: newStatus })
            });
            const data = await response.json();
            
            if (response.ok) {
                showToast(data.message);
                loadEmployees(0);
            } else {
                throw new Error(data.message || 'Có lỗi xảy ra');
            }
        } catch (error) {
            console.error('Error toggling employee status:', error);
            showToast(error.message, 'error');
        }
    }
}
