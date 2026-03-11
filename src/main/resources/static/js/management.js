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
        const response = await fetch('/api/room-types');
        const data = await response.json();
        const types = data.content || [];
        const tbody = document.getElementById('room-type-manage-tbody');
        if (!tbody) return;

        tbody.innerHTML = types.map(type => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">
                    <div class="flex items-center gap-3">
                        <img src="${type.roomImage || 'https://via.placeholder.com/150?text=No+Image'}" 
                             class="w-12 h-12 rounded-xl object-cover border border-slate-100 shadow-sm">
                        <span>${type.typeName}</span>
                    </div>
                </td>
                <td class="px-8 py-6 text-sm">${type.description || 'N/A'}</td>
                <td class="px-8 py-6 font-bold text-indigo-600">${new Intl.NumberFormat('vi-VN').format(type.price)}</td>
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

// Paginated Room Type Loading
let currentRoomTypePage = 0;
let currentRoomTypeKeyword = '';

async function loadRoomTypesWithPagination(page = 0, keyword = '') {
    currentRoomTypePage = page;
    currentRoomTypeKeyword = keyword;
    try {
        const response = await fetch(`/api/room-types/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`);
        const data = await response.json();
        const tbody = document.getElementById('room-type-manage-tbody');
        if (!tbody) return;

        const types = data.content || [];
        tbody.innerHTML = types.map(type => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">
                    <div class="flex items-center gap-3">
                        <img src="${type.roomImage || 'https://via.placeholder.com/150?text=No+Image'}" 
                             class="w-12 h-12 rounded-xl object-cover border border-slate-100 shadow-sm">
                        <span>${type.typeName}</span>
                    </div>
                </td>
                <td class="px-8 py-6 text-sm">${type.description || 'N/A'}</td>
                <td class="px-8 py-6 font-bold text-indigo-600">${new Intl.NumberFormat('vi-VN').format(type.price)}</td>
                <td class="px-8 py-6 font-bold text-cyan-600">${type.capacity} người</td>
                <td class="px-8 py-6 text-right">
                    <button onclick="editRoomType('${type.id}')" class="text-indigo-600 hover:text-indigo-900 mx-2"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteRoomType('${type.id}')" class="text-rose-600 hover:text-rose-900 mx-2"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </td>
            </tr>
        `).join('');
        lucide.createIcons();
        renderRoomTypePagination(data);
    } catch (error) { console.error(error); }
}

function renderRoomTypePagination(data) {
    const container = document.getElementById('room-type-pagination');
    if (!container) return;

    const { number: currentPage, totalPages } = data;
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';
    html += `<button onclick="loadRoomTypesWithPagination(${currentPage - 1}, currentRoomTypeKeyword)" ${currentPage === 0 ? 'disabled' : ''} class="px-4 py-2 rounded-xl border ${currentPage === 0 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white text-cyan-600 hover:bg-cyan-50'}">Trước</button>`;

    for (let i = 0; i < totalPages; i++) {
        html += `<button onclick="loadRoomTypesWithPagination(${i}, currentRoomTypeKeyword)" class="px-4 py-2 rounded-xl border ${i === currentPage ? 'bg-cyan-600 text-white' : 'bg-white text-slate-700 hover:bg-cyan-50'}">${i + 1}</button>`;
    }

    html += `<button onclick="loadRoomTypesWithPagination(${currentPage + 1}, currentRoomTypeKeyword)" ${currentPage >= totalPages - 1 ? 'disabled' : ''} class="px-4 py-2 rounded-xl border ${currentPage >= totalPages - 1 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white text-cyan-600 hover:bg-cyan-50'}">Sau</button>`;
    container.innerHTML = html;
}


function openRoomTypeModal() {
    document.getElementById('room-type-modal-title').innerText = 'Thêm loại phòng mới';
    document.getElementById('room-type-form').reset();
    document.getElementById('room-type-id-input').value = '';
    updateImagePreview('');
    toggleModal('room-type-manage-modal', true);
}

async function editRoomType(id) {
    try {
        const response = await fetch('/api/room-types');
        const data = await response.json();
        const types = data.content || [];
        const type = types.find(t => t.id === id);
        if (type) {
            document.getElementById('room-type-modal-title').innerText = 'Chỉnh sửa loại phòng';
            document.getElementById('room-type-id-input').value = type.id;
            document.getElementById('room-type-name').value = type.typeName;
            document.getElementById('room-type-capacity').value = type.capacity;
            document.getElementById('room-type-price').value = type.price;
            document.getElementById('room-type-image').value = type.roomImage || '';
            document.getElementById('room-type-description').value = type.description || '';
            updateImagePreview(type.roomImage || '');
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
        price: document.getElementById('room-type-price').value,
        roomImage: document.getElementById('room-type-image').value,
        description: document.getElementById('room-type-description').value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/room-types/${id}` : '/api/room-types';

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
        const response = await fetch(`/api/room-types/${id}`, { method: 'DELETE' });
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
        const response = await fetch('/api/rooms');
        const data = await response.json();
        const rooms = data.content || [];
        const tbody = document.getElementById('room-manage-tbody');
        if (!tbody) return;

        tbody.innerHTML = rooms.map(room => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">${room.roomName}</td>
                <td class="px-8 py-6">${room.roomType.typeName}</td>
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

// Paginated Room Loading
let currentRoomPage = 0;
let currentRoomKeyword = '';

async function loadRoomsWithPagination(page = 0, keyword = '') {
    currentRoomPage = page;
    currentRoomKeyword = keyword;
    try {
        const response = await fetch(`/api/rooms/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`);
        const data = await response.json();
        const tbody = document.getElementById('room-manage-tbody');
        if (!tbody) return;

        const rooms = data.content || [];
        tbody.innerHTML = rooms.map(room => `
            <tr>
                <td class="px-8 py-6 font-bold text-slate-900">${room.roomName}</td>
                <td class="px-8 py-6">${room.roomType.typeName}</td>
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
        renderRoomPagination(data);
    } catch (error) {
        console.error('Error loading rooms:', error);
    }
}

function renderRoomPagination(data) {
    const container = document.getElementById('room-pagination');
    if (!container) return;

    const { number: currentPage, totalPages } = data;
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';
    html += `<button onclick="loadRoomsWithPagination(${currentPage - 1}, currentRoomKeyword)" ${currentPage === 0 ? 'disabled' : ''} class="px-4 py-2 rounded-xl border ${currentPage === 0 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white text-indigo-600 hover:bg-indigo-50'}">Trước</button>`;

    for (let i = 0; i < totalPages; i++) {
        html += `<button onclick="loadRoomsWithPagination(${i}, currentRoomKeyword)" class="px-4 py-2 rounded-xl border ${i === currentPage ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-indigo-50'}">${i + 1}</button>`;
    }

    html += `<button onclick="loadRoomsWithPagination(${currentPage + 1}, currentRoomKeyword)" ${currentPage >= totalPages - 1 ? 'disabled' : ''} class="px-4 py-2 rounded-xl border ${currentPage >= totalPages - 1 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white text-indigo-600 hover:bg-indigo-50'}">Sau</button>`;
    container.innerHTML = html;
}


async function loadRoomTypes() {
    try {
        const response = await fetch('/api/room-types');
        const data = await response.json();
        const types = data.content || [];
        const select = document.getElementById('room-type-id');
        if (select) {
            select.innerHTML = types.map(t => `<option value="${t.id}">${t.typeName}</option>`).join('');
        }
    } catch (err) { console.error(err); }
}

// Link image preview and file upload
const imageInput = document.getElementById('room-type-image');
if (imageInput) {
    imageInput.addEventListener('input', (e) => updateImagePreview(e.target.value));
}
const fileInput = document.getElementById('room-type-image-file');
if (fileInput) {
    fileInput.addEventListener('change', handleImageUpload);
}

async function handleImageUpload(e) {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    const uploadBtn = e.target.closest('.relative').querySelector('button');
    const originalIcon = uploadBtn.innerHTML;
    uploadBtn.innerHTML = '<div class="w-5 h-5 border-2 border-cyan-600 border-t-transparent animate-spin rounded-full"></div>';
    uploadBtn.disabled = true;

    try {
        const response = await fetch('/api/media/upload', {
            method: 'POST',
            body: formData
        });
        const data = await response.json();
        if (response.ok) {
            document.getElementById('room-type-image').value = data.url;
            updateImagePreview(data.url);
            toastService.success('Tải ảnh lên thành công!');
        } else {
            toastService.error('Lỗi: ' + (data.message || 'Không thể tải ảnh'));
        }
    } catch (err) {
        console.error(err);
        toastService.error('Có lỗi xảy ra khi tải ảnh!');
    } finally {
        uploadBtn.innerHTML = originalIcon;
        uploadBtn.disabled = false;
        e.target.value = ''; // Reset for same file re-selection
    }
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
        const response = await fetch('/api/rooms');
        const data = await response.json();
        const rooms = data.content || [];
        const room = rooms.find(r => r.id === id);
        if (room) {
            document.getElementById('room-modal-title').innerText = 'Chỉnh sửa phòng';
            document.getElementById('room-id').value = room.id;
            document.getElementById('room-name').value = room.roomName;
            document.getElementById('room-type-id').value = room.roomType.id;
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
        status: document.getElementById('room-status').value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/rooms/${id}` : '/api/rooms';

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
        const response = await fetch(`/api/rooms/${id}`, { method: 'DELETE' });
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
        const response = await fetch('/api/services');
        const data = await response.json();
        const services = data.content || [];
        renderServiceTable(services);
    } catch (error) {
        console.error('Error loading services:', error);
    }
}

function renderServiceTable(services) {
    const tbody = document.getElementById('service-manage-tbody');
    if (!tbody) return;

    tbody.innerHTML = services.map(srv => `
        <tr class="hover:bg-slate-50/50 transition-colors group">
            <td class="px-10 py-8 font-black text-slate-900">
                <div class="flex flex-col">
                    <span>${srv.serviceName}</span>
                    <span class="text-[10px] text-slate-400 uppercase tracking-tighter">ID: ${srv.id.substring(0, 8)}...</span>
                </div>
            </td>
            <td class="px-10 py-8">
                <span class="bg-indigo-50 text-indigo-600 px-4 py-2 rounded-xl text-[10px] uppercase font-black tracking-widest border border-indigo-100/50">
                    ${srv.categoryName}
                </span>
            </td>
            <td class="px-10 py-8">
                <div class="flex items-center gap-1">
                    <span class="text-slate-400 text-sm">₫</span>
                    <span class="text-lg font-black text-slate-900">${new Intl.NumberFormat('vi-VN').format(srv.price)}</span>
                </div>
            </td>
            <td class="px-10 py-8">
                <div class="flex items-center gap-2">
                    <span class="w-2 h-2 rounded-full ${srv.isActive ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)]' : 'bg-slate-300'}"></span>
                    <span class="text-[11px] uppercase tracking-widest ${srv.isActive ? 'text-emerald-600' : 'text-slate-400'}">
                        ${srv.isActive ? 'Hoạt động' : 'Bị khóa'}
                    </span>
                </div>
            </td>
            <td class="px-10 py-8 text-right">
                <div class="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-all transform translate-x-2 group-hover:translate-x-0">
                    <button onclick="toggleServiceStatus('${srv.id}')" 
                            title="${srv.isActive ? 'Khóa dịch vụ' : 'Mở khóa dịch vụ'}"
                            class="p-3 ${srv.isActive ? 'text-amber-500 hover:bg-amber-50' : 'text-emerald-500 hover:bg-emerald-50'} rounded-xl transition-all">
                        <i data-lucide="${srv.isActive ? 'lock' : 'unlock'}" class="w-5 h-5"></i>
                    </button>
                    <button onclick="editService('${srv.id}')" 
                            class="p-3 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
                            <i data-lucide="edit-3" class="w-5 h-5"></i>
                    </button>
                    <button onclick="deleteService('${srv.id}')" 
                            class="p-3 text-rose-500 hover:bg-rose-50 rounded-xl transition-all">
                            <i data-lucide="trash-2" class="w-5 h-5"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
    lucide.createIcons();
}

// Paginated Service Loading
let currentServicePage = 0;
let currentServiceKeyword = '';

async function loadServicesWithPagination(page = 0, keyword = '') {
    currentServicePage = page;
    currentServiceKeyword = keyword;
    try {
        const response = await fetch(`/api/services/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`);
        const data = await response.json();
        const services = data.content || [];
        renderServiceTable(services);
        renderServicePagination(data);
    } catch (error) {
        console.error('Error loading services:', error);
    }
}

function renderServicePagination(data) {
    const container = document.getElementById('service-pagination');
    if (!container) return;

    const { number: currentPage, totalPages } = data;
    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';
    html += `<button onclick="loadServicesWithPagination(${currentPage - 1}, currentServiceKeyword)" ${currentPage === 0 ? 'disabled' : ''} class="px-4 py-2 rounded-xl border ${currentPage === 0 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white text-rose-600 hover:bg-rose-50'}">Trước</button>`;

    for (let i = 0; i < totalPages; i++) {
        html += `<button onclick="loadServicesWithPagination(${i}, currentServiceKeyword)" class="px-4 py-2 rounded-xl border ${i === currentPage ? 'bg-rose-600 text-white' : 'bg-white text-slate-700 hover:bg-rose-50'}">${i + 1}</button>`;
    }

    html += `<button onclick="loadServicesWithPagination(${currentPage + 1}, currentServiceKeyword)" ${currentPage >= totalPages - 1 ? 'disabled' : ''} class="px-4 py-2 rounded-xl border ${currentPage >= totalPages - 1 ? 'bg-slate-100 text-slate-400 cursor-not-allowed' : 'bg-white text-rose-600 hover:bg-rose-50'}">Sau</button>`;
    container.innerHTML = html;
}


async function loadServiceCategories() {
    try {
        const response = await fetch('/api/categories');
        const data = await response.json();
        const categories = data.content || []; // Spring Data Page có content là array
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
        const response = await fetch('/api/services');
        const data = await response.json();
        const services = data.content || [];
        const srv = services.find(s => s.id === id);
        if (srv) {
            document.getElementById('service-modal-title').innerText = 'Chỉnh sửa dịch vụ';
            document.getElementById('service-id').value = srv.id;
            document.getElementById('service-name-input').value = srv.serviceName;
            document.getElementById('service-price-input').value = srv.price;
            document.getElementById('service-active-input').checked = srv.isActive;
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
        price: document.getElementById('service-price-input').value,
        isActive: document.getElementById('service-active-input').checked
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `/api/services/${id}` : '/api/services';

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
        const response = await fetch(`/api/services/${id}`, { method: 'DELETE' });
        if (response.ok) {
            loadServicesForManage();
            toastService.success('Xóa dịch vụ thành công!');
        }
    } catch (err) { console.error(err); }
}

async function toggleServiceStatus(id) {
    try {
        const response = await fetch(`/api/services/${id}/toggle-status`, { method: 'PUT' });
        if (response.ok) {
            toastService.success('Cập nhật trạng thái thành công!');
            loadServicesWithPagination(currentServicePage, currentServiceKeyword);
        } else {
            const err = await response.json();
            toastService.error(err.message || 'Lỗi khi cập nhật trạng thái');
        }
    } catch (err) { console.error(err); }
}

// --- IMAGE PREVIEW UTILITY ---
function updateImagePreview(url) {
    const preview = document.getElementById('room-type-image-preview');
    const placeholder = document.getElementById('room-type-image-placeholder');
    if (!preview || !placeholder) return;

    if (url && url.trim() !== '') {
        preview.src = url;
        preview.classList.remove('hidden');
        placeholder.classList.add('hidden');
    } else {
        preview.src = '';
        preview.classList.add('hidden');
        placeholder.classList.remove('hidden');
    }
}
