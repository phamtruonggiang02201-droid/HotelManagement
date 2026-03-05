let currentDate = new Date().toISOString().split('T')[0];
const areas = ["Sảnh chính", "Nhà hàng", "Tầng 1", "Tầng 2", "Tầng 3", "Hồ bơi", "Bảo vệ / Cổng"];
const shifts = ["Sáng", "Chiều", "Tối"];
let employeeCache = [];

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('filter-date').value = currentDate;
    loadEmployees(); // loadEmployees will trigger loadAssignments after done
    
    document.getElementById('filter-date').addEventListener('change', (e) => {
        currentDate = e.target.value;
        loadAssignments();
    });

    document.getElementById('assignForm').addEventListener('submit', handleAssign);
});

function loadAssignments() {
    fetch(`/management/assignments/api/by-date?date=${currentDate}`)
        .then(res => res.json())
        .then(data => {
            renderGrid(data);
        })
        .catch(err => console.error(err));
}

function renderGrid(data) {
    const tbody = document.getElementById('assignment-grid-body');
    tbody.innerHTML = '';

    areas.forEach(area => {
        const tr = document.createElement('tr');
        tr.className = 'border-b border-slate-50 hover:bg-slate-50/50 transition-all';
        
        // Area Column
        const areaTd = document.createElement('td');
        areaTd.className = 'px-8 py-6 font-black text-slate-700 bg-slate-50/30';
        areaTd.textContent = area;
        tr.appendChild(areaTd);

        // Shift Columns
        shifts.forEach(shift => {
            const td = document.createElement('td');
            td.className = 'px-4 py-4 min-w-[200px] border-l border-slate-50';
            
            const assignmentsInCell = data.filter(as => as.area === area && as.shift === shift);
            
            if (assignmentsInCell.length > 0) {
                assignmentsInCell.forEach(as => {
                    const card = createMiniCard(as);
                    td.appendChild(card);
                });
            }
            
            // Inline Assignment Dropdown (Sử dụng Select2 hoặc Select thuần nhưng đẹp)
            const selectContainer = document.createElement('div');
            selectContainer.className = 'mt-2';
            
            const select = document.createElement('select');
            select.className = 'w-full px-3 py-2.5 rounded-xl border border-slate-100 bg-slate-50 text-xs font-bold text-slate-500 focus:ring-2 focus:ring-indigo-500 outline-none appearance-none cursor-pointer hover:bg-white transition-all';
            
            const defaultOpt = document.createElement('option');
            defaultOpt.value = "";
            defaultOpt.textContent = "+ Gán nhân viên...";
            select.appendChild(defaultOpt);

            employeeCache.forEach(emp => {
                // Kiểm tra xem nhân viên này đã có lịch trong ngày này chưa (bất kể ca nào)
                const isAssigned = data.some(as => as.employeeId === emp.id);
                if (!isAssigned) {
                    const opt = document.createElement('option');
                    opt.value = emp.id;
                    opt.textContent = emp.fullName;
                    select.appendChild(opt);
                }
            });

            select.onchange = (e) => {
                if (e.target.value) {
                    quickAssign(e.target.value, area, shift);
                }
            };

            selectContainer.appendChild(select);
            td.appendChild(selectContainer);
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
                    ${as.employeeFullName.split(' ').pop().charAt(0)}
                </div>
                <div>
                    <span class="font-black text-slate-900 text-xs block truncate max-w-[100px]">${as.employeeFullName}</span>
                    <span class="text-[9px] text-slate-400 font-bold uppercase">${as.jobTitle || 'Lễ tân'}</span>
                </div>
            </div>
            <button onclick="deleteAssignment('${as.id}')" class="p-1.5 hover:bg-rose-50 rounded-lg text-slate-300 hover:text-rose-500 opacity-0 group-hover:opacity-100 transition-all">
                <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
            </button>
        </div>
    `;
    return div;
}

function loadEmployees() {
    fetch('/management/employees/api?size=100')
        .then(res => res.json())
        .then(data => {
            console.log("Raw Employee Data:", data);
            employeeCache = data.content.filter(emp => emp.roleName === 'RECEPTION');
            console.log("Filtered Receptionists:", employeeCache.length);
            
            // Cập nhật cả modal select nếu cần
            const select = document.getElementById('employeeId');
            if (select) {
                select.innerHTML = '<option value="">-- Chọn nhân viên --</option>';
                employeeCache.forEach(emp => {
                    const opt = document.createElement('option');
                    opt.value = emp.id;
                    opt.textContent = `${emp.fullName} (${emp.jobTitle || 'Lễ tân'})`;
                    select.appendChild(opt);
                });
            }
            loadAssignments(); // Re-render assignments once we have the employee names
        })
        .catch(err => {
            console.error("Error loading employees:", err);
            loadAssignments(); 
        });
}

function quickAssign(empId, area, shift) {
    const formData = {
        employeeId: empId,
        workDate: currentDate,
        area: area,
        shift: shift,
        notes: ""
    };

    fetch('/management/assignments/api', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    })
    .then(res => res.json())
    .then(res => {
        if (res.id) {
            showToast('Đã gán việc thành công!');
            loadAssignments();
        } else {
            showToast(res.message, 'error');
            loadAssignments(); // Reload to reset select
        }
    })
    .catch(err => {
        console.error(err);
        showToast('Đã xảy ra lỗi!', 'error');
    });
}

// Giữ lại các hàm cũ cho Modal nếu đại ca muốn dùng nâng cao (có note)
function handleAssign(e) {
    e.preventDefault();
    const formData = {
        employeeId: document.getElementById('employeeId').value,
        workDate: document.getElementById('workDate').value,
        area: document.getElementById('area').value,
        shift: document.getElementById('shift').value,
        notes: document.getElementById('notes').value
    };

    fetch('/management/assignments/api', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    })
    .then(res => res.json())
    .then(res => {
        if (res.id) {
            showToast('Phân công thành công!');
            closeAssignModal();
            loadAssignments();
        } else {
            showToast(res.message, 'error');
        }
    })
    .catch(err => {
        console.error(err);
        showToast('Đã xảy ra lỗi!', 'error');
    });
}

function deleteAssignment(id) {
    if (!confirm('Xóa phân công này?')) return;
    fetch(`/management/assignments/api/${id}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(res => {
            showToast(res.message);
            loadAssignments();
        });
}

function openAssignModal(area = '', shift = 'Sáng') {
    const modal = document.getElementById('assignModal');
    const content = modal.querySelector('div');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    
    document.getElementById('workDate').value = currentDate;
    if (area) document.getElementById('area').value = area;
    if (shift) document.getElementById('shift').value = shift;

    setTimeout(() => {
        content.classList.remove('scale-95', 'opacity-0');
    }, 10);
}

function closeAssignModal() {
    const modal = document.getElementById('assignModal');
    const content = modal.querySelector('div');
    content.classList.add('scale-95', 'opacity-0');
    setTimeout(() => {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }, 300);
}

function changeDate(days) {
    const date = new Date(currentDate);
    date.setDate(date.getDate() + days);
    currentDate = date.toISOString().split('T')[0];
    document.getElementById('filter-date').value = currentDate;
    loadAssignments();
}

function setToToday() {
    currentDate = new Date().toISOString().split('T')[0];
    document.getElementById('filter-date').value = currentDate;
    loadAssignments();
}
