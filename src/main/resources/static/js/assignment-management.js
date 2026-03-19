const now = new Date();
let currentDate = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0');
const shifts = ["Sáng", "Chiều", "Tối"];
let employeeCache = [];
let areaCache = []; // Load từ DB thay vì hardcode

// Close dropdowns on outside click
document.addEventListener('click', (e) => {
    if (!e.target.closest('.assignment-dropdown-container')) {
        document.querySelectorAll('.assignment-dropdown-list').forEach(l => l.classList.add('hidden'));
    }
});

document.addEventListener('DOMContentLoaded', () => {
    const filterDate = document.getElementById('filter-date');
    if (filterDate) {
        filterDate.value = currentDate;
        filterDate.addEventListener('change', (e) => {
            currentDate = e.target.value;
            loadAssignments();
        });
    }
    
    // Load khu vực từ DB trước, sau đó load nhân viên và phân công
    loadAreas();
});

// ===== LOAD DỮ LIỆU =====

async function loadAreas() {
    try {
        const res = await fetch('/api/areas');
        const data = await res.json();
        areaCache = data.map(a => ({ id: a.id, name: a.areaName }));
    } catch (err) {
        console.error('Không thể tải khu vực từ DB, dùng mặc định:', err);
        // Fallback nếu chưa có khu vực nào trong DB
        areaCache = [
            { id: 'snh-chinh', name: 'Sảnh chính' },
            { id: 'nha-hang', name: 'Nhà hàng' },
            { id: 'tang-1', name: 'Tầng 1' },
            { id: 'tang-2', name: 'Tầng 2' },
            { id: 'tang-3', name: 'Tầng 3' },
            { id: 'ho-boi', name: 'Hồ bơi' },
            { id: 'bao-ve', name: 'Bảo vệ / Cổng' }
        ];
    }
    loadEmployees();
}

function loadEmployees() {
    fetch('/management/employees/api?size=200')
        .then(res => res.json())
        .then(data => {
            // Lấy tất cả nhân viên, không lọc cứng theo role
            employeeCache = data.content || [];
            loadAssignments();
        })
        .catch(err => {
            console.error("Error loading employees:", err);
            loadAssignments();
        });
}

function loadAssignments() {
    fetch(`/management/assignments/api/by-date?date=${currentDate}`)
        .then(res => res.json())
        .then(data => {
            renderGrid(data);
        })
        .catch(err => console.error(err));
}

// ===== RENDER BẢNG PHÂN CÔNG =====

function renderGrid(data) {
    const tbody = document.getElementById('assignment-grid-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (areaCache.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="px-8 py-16 text-center">
                    <div class="flex flex-col items-center gap-4">
                        <div class="w-16 h-16 bg-slate-50 rounded-3xl flex items-center justify-center text-slate-300">
                            <i data-lucide="map-pin" class="w-8 h-8"></i>
                        </div>
                        <div>
                            <p class="font-black text-slate-700 text-sm">Chưa có khu vực nào</p>
                            <p class="text-slate-400 text-xs mt-1">Đại ca vui lòng thêm khu vực tại <a href="/management/areas" class="text-indigo-600 underline">trang quản lý khu vực</a></p>
                        </div>
                    </div>
                </td>
            </tr>
        `;
        lucide.createIcons();
        return;
    }

    areaCache.forEach(area => {
        const tr = document.createElement('tr');
        tr.className = 'border-b border-slate-50 hover:bg-slate-50/50 transition-all';
        
        // Cột Khu vực
        const areaTd = document.createElement('td');
        areaTd.className = 'px-8 py-6 font-black text-slate-700 bg-slate-50/30';
        areaTd.innerHTML = `
            <div class="flex items-center gap-3">
                <div class="w-8 h-8 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
                    <i data-lucide="map-pin" class="w-4 h-4"></i>
                </div>
                <span>${area.name}</span>
            </div>
        `;
        tr.appendChild(areaTd);

        // Cột Ca
        shifts.forEach(shift => {
            const td = document.createElement('td');
            td.className = 'px-4 py-4 min-w-[220px] border-l border-slate-50 relative';
            
            const assignmentsInCell = data.filter(as => as.area === area.name && as.shift === shift);
            
            assignmentsInCell.forEach(as => {
                td.appendChild(createMiniCard(as));
            });
            
            // Custom Multi-select Dropdown
            const dropdownContainer = document.createElement('div');
            dropdownContainer.className = 'assignment-dropdown-container mt-2 relative';
            
            const toggleBtn = document.createElement('button');
            toggleBtn.className = 'w-full px-4 py-3 rounded-2xl border border-slate-100 bg-slate-50 text-left text-[11px] font-black text-indigo-600 hover:bg-white hover:shadow-lg hover:shadow-indigo-50 transition-all flex items-center justify-between group cursor-pointer';
            toggleBtn.innerHTML = `
                <span>+ Gán nhân viên</span>
                <i data-lucide="chevron-down" class="w-3.5 h-3.5 text-indigo-300 group-hover:text-indigo-600 transition-all"></i>
            `;
            
            const listContainer = document.createElement('div');
            listContainer.className = 'absolute left-0 right-0 mt-2 bg-white rounded-2xl shadow-2xl border border-slate-100 z-50 p-2 hidden max-h-72 overflow-y-auto animate-in fade-in zoom-in duration-200 assignment-dropdown-list';
            
            // Lọc nhân viên chưa được gán trong ngày (bất kể ca)
            const assignedTodayIds = data.map(as => as.employeeId);
            const availableEmps = employeeCache.filter(emp => !assignedTodayIds.includes(emp.id));
            
            if (availableEmps.length === 0) {
                listContainer.innerHTML = '<div class="px-4 py-4 text-[10px] font-bold text-slate-400 text-center uppercase tracking-widest">Hết nhân viên trống</div>';
            } else {
                // Header tìm kiếm
                const searchBox = document.createElement('input');
                searchBox.type = 'text';
                searchBox.placeholder = 'Tìm nhân viên...';
                searchBox.className = 'w-full px-4 py-2.5 mb-2 rounded-xl border border-slate-100 bg-slate-50 text-xs font-bold text-slate-700 focus:ring-2 focus:ring-indigo-500 outline-none transition-all';
                searchBox.onclick = e => e.stopPropagation();
                searchBox.oninput = (e) => {
                    const keyword = e.target.value.toLowerCase();
                    listContainer.querySelectorAll('label.emp-item').forEach(item => {
                        const name = item.querySelector('span').textContent.toLowerCase();
                        item.style.display = name.includes(keyword) ? '' : 'none';
                    });
                };
                listContainer.appendChild(searchBox);

                availableEmps.forEach(emp => {
                    const item = document.createElement('label');
                    item.className = 'emp-item flex items-center gap-3 px-4 py-2.5 hover:bg-slate-50 rounded-xl cursor-pointer transition-colors group select-none mb-1';
                    item.innerHTML = `
                        <input type="checkbox" value="${emp.id}" class="w-4 h-4 rounded border-slate-200 text-indigo-600 focus:ring-indigo-500 transition-all cursor-pointer">
                        <div class="flex flex-col">
                            <span class="text-xs font-black text-slate-700 group-hover:text-indigo-600 transition-colors">${emp.fullName}</span>
                            <span class="text-[9px] font-bold text-slate-400 uppercase tracking-tighter">${emp.jobTitle || emp.roleName || 'Nhân viên'}</span>
                        </div>
                    `;
                    listContainer.appendChild(item);
                });
                
                const confirmBtn = document.createElement('button');
                confirmBtn.className = 'w-full mt-2 bg-indigo-600 text-white py-3 rounded-xl text-[10px] font-black uppercase tracking-widest shadow-lg shadow-indigo-100 hover:bg-indigo-700 transition-all active:scale-95';
                confirmBtn.textContent = 'Xác nhận gán';
                confirmBtn.onclick = (e) => {
                    e.stopPropagation();
                    const selectedIds = Array.from(listContainer.querySelectorAll('input:checked')).map(cb => cb.value);
                    if (selectedIds.length > 0) {
                        batchAssign(selectedIds, area.name, shift);
                    }
                    listContainer.classList.add('hidden');
                };
                listContainer.appendChild(confirmBtn);
            }

            toggleBtn.onclick = (e) => {
                e.stopPropagation();
                document.querySelectorAll('.assignment-dropdown-list').forEach(l => {
                    if (l !== listContainer) l.classList.add('hidden');
                });
                listContainer.classList.toggle('hidden');
                lucide.createIcons();
            };
            
            dropdownContainer.appendChild(toggleBtn);
            dropdownContainer.appendChild(listContainer);
            td.appendChild(dropdownContainer);
            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });

    lucide.createIcons();
}

function createMiniCard(as) {
    const div = document.createElement('div');
    div.className = 'bg-white p-4 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all group relative mb-3';
    div.innerHTML = `
        <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
                <div class="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center font-black text-white text-[10px] shadow-sm">
                    ${(as.employeeFullName || '?').split(' ').pop().charAt(0)}
                </div>
                <div>
                    <span class="font-black text-slate-900 text-xs block truncate max-w-[100px]">${as.employeeFullName}</span>
                    <span class="text-[9px] text-slate-400 font-bold uppercase">${as.jobTitle || 'Nhân viên'}</span>
                </div>
            </div>
            <button onclick="deleteAssignment('${as.id}')" class="p-1.5 hover:bg-rose-50 rounded-lg text-slate-300 hover:text-rose-500 opacity-0 group-hover:opacity-100 transition-all">
                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
            </button>
        </div>
    `;
    return div;
}

// ===== PHÂN CÔNG =====

function batchAssign(empIds, areaName, shift) {
    const promises = empIds.map(id => {
        const formData = {
            employeeId: id,
            workDate: currentDate,
            area: areaName,
            shift: shift,
            notes: ""
        };
        return fetch('/management/assignments/api', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        }).then(res => res.json());
    });

    Promise.all(promises)
        .then(results => {
            const successCount = results.filter(r => r.id).length;
            if (successCount > 0) {
                toastService.success(`Đã gán thành công ${successCount} nhân viên!`);
            }
            loadAssignments();
        })
        .catch(err => {
            console.error(err);
            toastService.error('Đã xảy ra lỗi khi gán hàng loạt!');
        });
}

function deleteAssignment(id) {
    if (!confirm('Xóa phân công này?')) return;
    fetch(`/management/assignments/api/${id}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(res => {
            toastService.success(res.message);
            loadAssignments();
        });
}

// ===== ĐIỀU HƯỚNG NGÀY =====

function changeDate(days) {
    const date = new Date(currentDate);
    date.setDate(date.getDate() + days);
    currentDate = date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0') + '-' + String(date.getDate()).padStart(2, '0');
    const filterDate = document.getElementById('filter-date');
    if (filterDate) filterDate.value = currentDate;
    loadAssignments();
}

function setToToday() {
    const today = new Date();
    currentDate = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0') + '-' + String(today.getDate()).padStart(2, '0');
    const filterDate = document.getElementById('filter-date');
    if (filterDate) filterDate.value = currentDate;
    loadAssignments();
}

// ===== SAO CHÉP LỊCH =====

function copyToNextDay() {
    fetch(`/management/assignments/api/by-date?date=${currentDate}`)
        .then(res => res.json())
        .then(data => {
            if (data.length === 0) {
                toastService.error('Ngày hiện tại chưa có phân công nào để sao chép!');
                return;
            }

            const next = new Date(currentDate);
            next.setDate(next.getDate() + 1);
            const nextDateStr = next.getFullYear() + '-' + String(next.getMonth() + 1).padStart(2, '0') + '-' + String(next.getDate()).padStart(2, '0');

            if (!confirm(`Sao chép toàn bộ phân công ngày ${currentDate} sang ngày ${nextDateStr}?`)) return;

            const promises = data.map(as => {
                const formData = {
                    employeeId: as.employeeId,
                    workDate: nextDateStr,
                    area: as.area,
                    shift: as.shift,
                    notes: as.notes || ""
                };
                return fetch('/management/assignments/api', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(formData)
                }).then(res => res.json());
            });

            Promise.all(promises).then(results => {
                const successCount = results.filter(r => r.id).length;
                toastService.success(`Đã sao chép thành công ${successCount} phân công sang ngày mai!`);
                currentDate = nextDateStr;
                const filterDate = document.getElementById('filter-date');
                if (filterDate) filterDate.value = currentDate;
                loadAssignments();
            });
        })
        .catch(err => {
            console.error(err);
            toastService.error('Lỗi khi sao chép lịch trực!');
        });
}

function applyForWholeWeek() {
    fetch(`/management/assignments/api/by-date?date=${currentDate}`)
        .then(res => res.json())
        .then(data => {
            if (data.length === 0) {
                toastService.error('Ngày hiện tại chưa có phân công nào để áp dụng!');
                return;
            }

            if (!confirm(`Áp dụng toàn bộ lịch trực này cho 7 ngày tiếp theo?`)) return;

            const promises = [];
            for (let i = 1; i <= 7; i++) {
                const day = new Date(currentDate);
                day.setDate(day.getDate() + i);
                const dayStr = day.getFullYear() + '-' + String(day.getMonth() + 1).padStart(2, '0') + '-' + String(day.getDate()).padStart(2, '0');

                data.forEach(as => {
                    const formData = {
                        employeeId: as.employeeId,
                        workDate: dayStr,
                        area: as.area,
                        shift: as.shift,
                        notes: as.notes || ""
                    };
                    promises.push(
                        fetch('/management/assignments/api', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(formData)
                        }).then(res => res.json())
                    );
                });
            }

            Promise.all(promises).then(results => {
                const successIds = results.filter(r => r.id).length;
                toastService.success(`Đã sao chép thành công ${successIds} lượt phân công cho 7 ngày tới!`);
                loadAssignments();
            });
        })
        .catch(err => {
            console.error(err);
            toastService.error('Lỗi khi áp dụng lịch tuần!');
        });
}
