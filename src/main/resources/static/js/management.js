/**
 * Logic quản lý Room và Service cho Dashboard
 */

document.addEventListener('DOMContentLoaded', () => {
    // Tải dữ liệu ban đầu
    loadManagementData();

    // Form Submits - Sử dụng optional chaining để tránh lỗi trên các trang không có form
    document.getElementById('room-type-form')?.addEventListener('submit', handleRoomTypeSubmit);
    document.getElementById('room-form')?.addEventListener('submit', handleRoomSubmit);
    document.getElementById('service-form')?.addEventListener('submit', handleServiceSubmit);
});

async function loadManagementData() {
    // Chỉ load types/categories nếu các element tương ứng tồn tại (tránh fetch thừa)
    if (document.getElementById('room-type-id')) {
        loadRoomTypes();
    }
    if (document.getElementById('service-category-id')) {
        loadServiceCategories();
    }
}

// --- ROOM TYPE MANAGEMENT ---

async function loadRoomTypesForManage() {
    try {
        const response = await fetch('/management/api/room-types');
        const types = await response.json();
        const tbody = document.getElementById('room-type-manage-tbody');
        if (!tbody) return;

        tbody.innerHTML = types.map(type => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">${type.typeName}</td>
                <td class="px-8 py-6 text-sm">${type.description || 'N/A'}</td>
                <td class="px-8 py-6 font-bold text-cyan-600">${type.capacity} người</td>
                <td class="px-8 py-6 text-right">
                    <button onclick="editRoomType('${type.id}')" class="text-indigo-600 hover:text-indigo-900 mx-2"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteRoomType('${type.id}')" class="text-rose-600 hover:text-rose-900 mx-2"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </td>
            </tr>
        `).join('');
        lucide.createIcons();
    } catch (error) { console.error(error); }
}

function openRoomTypeModal() {
    document.getElementById('room-type-modal-title').innerText = 'Thêm loại phòng mới';
    document.getElementById('room-type-form').reset();
    document.getElementById('room-type-id-input').value = '';
    toggleModal('room-type-manage-modal', true);
}

async function editRoomType(id) {
    try {
        const response = await fetch('/management/api/room-types');
        const types = await response.json();
        const type = types.find(t => t.id === id);
        if (type) {
            document.getElementById('room-type-modal-title').innerText = 'Chỉnh sửa loại phòng';
            document.getElementById('room-type-id-input').value = type.id;
            document.getElementById('room-type-name').value = type.typeName;
            document.getElementById('room-type-capacity').value = type.capacity;
            document.getElementById('room-type-description').value = type.description || '';
            toggleModal('room-type-manage-modal', true);
        }
    } catch (err) { console.error(err); }
}

async function handleRoomTypeSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('room-type-id-input').value;
    const data = {
        typeName: document.getElementById('room-type-name').value,
        capacity: document.getElementById('room-type-capacity').value,
        description: document.getElementById('room-type-description').value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/management/api/room-types/${id}` : '/management/api/room-types';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (response.ok) {
            toggleModal('room-type-manage-modal', false);
            loadRoomTypesForManage();
            toastService.info('Tính năng đang được phát triển');
        } else {
            const err = await response.json();
            toastService.error('Lỗi: ' + (err.message || 'Không thể lưu'));
        }
    } catch (err) { console.error(err); }
}

async function deleteRoomType(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa loại phòng này? Thao tác này có thể thất bại nếu có phòng đang sử dụng loại này.')) return;
    try {
        const response = await fetch(`/management/api/room-types/${id}`, { method: 'DELETE' });
        if (response.ok) {
            toastService.success('Xóa loại phòng thành công!');
            loadRoomTypesForManage();
        } else {
            const err = await response.json();
            toastService.error('Lỗi: ' + err.message);
        }
    } catch (err) { console.error(err); }
}

// --- ROOM MANAGEMENT ---

async function loadRoomsForManage() {
    try {
        const response = await fetch('/management/api/rooms');
        const rooms = await response.json();
        const tbody = document.getElementById('room-manage-tbody');
        if (!tbody) return;

        tbody.innerHTML = rooms.map(room => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">${room.roomName}</td>
                <td class="px-8 py-6">${room.roomType.typeName}</td>
                <td class="px-8 py-6">${new Intl.NumberFormat('vi-VN').format(room.price)}</td>
                <td class="px-8 py-6">
                    <span class="px-3 py-1 rounded-full text-[10px] font-black uppercase ${getStatusClass(room.status)}">
                        ${room.status}
                    </span>
                </td>
                <td class="px-8 py-6 text-right">
                    <button onclick="editRoom('${room.id}')" class="text-indigo-600 hover:text-indigo-900 mx-2"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteRoom('${room.id}')" class="text-rose-600 hover:text-rose-900 mx-2"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </td>
            </tr>
        `).join('');
        lucide.createIcons();
    } catch (error) {
        console.error('Error loading rooms:', error);
    }
}

async function loadRoomTypes() {
    try {
        const response = await fetch('/management/api/room-types');
        const types = await response.json();
        const select = document.getElementById('room-type-id');
        if (select) {
            select.innerHTML = types.map(t => `<option value="${t.id}">${t.typeName}</option>`).join('');
        }
    } catch (err) { console.error(err); }
}

function getStatusClass(status) {
    switch (status) {
        case 'AVAILABLE': return 'bg-emerald-100 text-emerald-600';
        case 'OCCUPIED': return 'bg-amber-100 text-amber-600';
        case 'MAINTENANCE': return 'bg-slate-100 text-slate-600';
        default: return 'bg-slate-100 text-slate-600';
    }
}

function openRoomModal() {
    document.getElementById('room-modal-title').innerText = 'Thêm phòng mới';
    document.getElementById('room-form').reset();
    document.getElementById('room-id').value = '';
    toggleModal('room-manage-modal', true);
}

async function editRoom(id) {
    try {
        const response = await fetch('/management/api/rooms');
        const rooms = await response.json();
        const room = rooms.find(r => r.id === id);
        if (room) {
            document.getElementById('room-modal-title').innerText = 'Chỉnh sửa phòng';
            document.getElementById('room-id').value = room.id;
            document.getElementById('room-name').value = room.roomName;
            document.getElementById('room-type-id').value = room.roomType.id;
            document.getElementById('room-price').value = room.price;
            document.getElementById('room-status').value = room.status;
            toggleModal('room-manage-modal', true);
        }
    } catch (err) { console.error(err); }
}

async function handleRoomSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('room-id').value;
    const data = {
        roomName: document.getElementById('room-name').value,
        roomTypeId: document.getElementById('room-type-id').value,
        price: document.getElementById('room-price').value,
        status: document.getElementById('room-status').value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/management/api/rooms/${id}` : '/management/api/rooms';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (response.ok) {
            toggleModal('room-manage-modal', false);
            loadRoomsForManage();
            toastService.success('Lưu thông tin phòng thành công!');
        } else {
            const err = await response.json();
            toastService.error('Lỗi: ' + (err.message || 'Không thể lưu'));
        }
    } catch (err) { console.error(err); }
}

async function deleteRoom(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa phòng này?')) return;
    try {
        const response = await fetch(`/management/api/rooms/${id}`, { method: 'DELETE' });
        if (response.ok) {
            loadRoomsForManage();
            toastService.success('Xóa phòng thành công!');
        } else {
            const err = await response.json();
            toastService.error('Lỗi: ' + err.message);
        }
    } catch (err) { console.error(err); }
}

// --- SERVICE MANAGEMENT ---

async function loadServicesForManage() {
    try {
        const response = await fetch('/management/services/api');
        const services = await response.json();
        const tbody = document.getElementById('service-manage-tbody');
        if (!tbody) return;

        tbody.innerHTML = services.map(srv => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">${srv.serviceName}</td>
                <td class="px-8 py-6"><span class="bg-indigo-50 text-indigo-600 px-3 py-1 rounded-lg text-[10px] uppercase font-black">${srv.categoryName}</span></td>
                <td class="px-8 py-6">${new Intl.NumberFormat('vi-VN').format(srv.price)}</td>
                <td class="px-8 py-6 text-right">
                    <button onclick="editService('${srv.id}')" class="text-indigo-600 hover:text-indigo-900 mx-2"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteService('${srv.id}')" class="text-rose-600 hover:text-rose-900 mx-2"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </td>
            </tr>
        `).join('');
        lucide.createIcons();
    } catch (error) {
        console.error('Error loading services:', error);
    }
}

async function loadServiceCategories() {
    try {
        const response = await fetch('/management/services/api/categories');
        const categories = await response.json();
        const select = document.getElementById('service-category-id');
        if (select) {
            select.innerHTML = categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join('');
        }
    } catch (err) { console.error('Error loading categories:', err); }
}

function openServiceModal() {
    document.getElementById('service-modal-title').innerText = 'Thêm dịch vụ mới';
    document.getElementById('service-form').reset();
    document.getElementById('service-id').value = '';
    toggleModal('service-manage-modal', true);
}

async function editService(id) {
    try {
        const response = await fetch('/management/services/api');
        const services = await response.json();
        const srv = services.find(s => s.id === id);
        if (srv) {
            document.getElementById('service-modal-title').innerText = 'Chỉnh sửa dịch vụ';
            document.getElementById('service-id').value = srv.id;
            document.getElementById('service-name-input').value = srv.serviceName;
            document.getElementById('service-price-input').value = srv.price;
            // Lookup category ID by name for the select (simplified logic)
            const catSelect = document.getElementById('service-category-id');
            for (let opt of catSelect.options) {
                if (opt.text === srv.categoryName) {
                    catSelect.value = opt.value;
                    break;
                }
            }
            toggleModal('service-manage-modal', true);
        }
    } catch (err) { console.error(err); }
}

async function handleServiceSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('service-id').value;
    const data = {
        serviceName: document.getElementById('service-name-input').value,
        categoryId: document.getElementById('service-category-id').value,
        price: document.getElementById('service-price-input').value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/management/services/api/${id}` : '/management/services/api';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (response.ok) {
            toggleModal('service-manage-modal', false);
            toastService.success('Lưu dịch vụ thành công!');
            loadServicesForManage();
        } else {
            const err = await response.json();
            toastService.error('Lỗi: ' + (err.message || 'Không thể lưu'));
        }
    } catch (err) { console.error(err); }
}

async function deleteService(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa dịch vụ này?')) return;
    try {
        const response = await fetch(`/management/services/api/${id}`, { method: 'DELETE' });
        if (response.ok) {
            loadServicesForManage();
            toastService.success('Xóa dịch vụ thành công!');
        }
    } catch (err) { console.error(err); }
}
