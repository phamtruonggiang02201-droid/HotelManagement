/**
 * Logic xử lý cho trang Đặt phòng tại quầy (Walk-in Booking)
 */

let roomTypes = [];

document.addEventListener('DOMContentLoaded', () => {
    initDates();
    loadRoomTypes();
    initForm();
});

function initDates() {
    const checkInInput = document.getElementById('checkIn');
    const checkOutInput = document.getElementById('checkOut');

    const now = new Date();
    const todayStr = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0') + '-' + String(now.getDate()).padStart(2, '0');
    
    checkInInput.min = todayStr;
    checkInInput.value = todayStr;

    const tomorrow = new Date(now);
    tomorrow.setDate(now.getDate() + 1);
    const tomorrowStr = tomorrow.getFullYear() + '-' + String(tomorrow.getMonth() + 1).padStart(2, '0') + '-' + String(tomorrow.getDate()).padStart(2, '0');
    
    checkOutInput.min = tomorrowStr;
    checkOutInput.value = tomorrowStr;

    checkInInput.addEventListener('change', () => {
        const ci = new Date(checkInInput.value);
        const co = new Date(checkOutInput.value);
        
        const nextDay = new Date(ci);
        nextDay.setDate(ci.getDate() + 1);
        const nextDayStr = nextDay.getFullYear() + '-' + String(nextDay.getMonth() + 1).padStart(2, '0') + '-' + String(nextDay.getDate()).padStart(2, '0');
        
        checkOutInput.min = nextDayStr;
        if (co <= ci) {
            checkOutInput.value = nextDayStr;
        }
        calculateTotal();
    });

    checkOutInput.addEventListener('change', calculateTotal);
}

async function loadRoomTypes() {
    try {
        const response = await fetch('/api/room-types');
        const data = await response.json();
        roomTypes = data.content || [];
        renderRoomTypes();
    } catch (err) {
        console.error(err);
        toastService.error('Không thể tải danh sách loại phòng');
    }
}

function renderRoomTypes() {
    const container = document.getElementById('room-types-selection');
    if (roomTypes.length === 0) {
        container.innerHTML = '<div class="col-span-full text-center py-10 text-slate-400">Không có loại phòng nào khả dụng.</div>';
        return;
    }

    container.innerHTML = roomTypes.map(type => `
        <div class="bg-white border border-slate-100 p-6 rounded-[32px] flex flex-col gap-6 hover:border-indigo-300 hover:shadow-xl hover:shadow-indigo-500/5 transition-all group">
            <div class="flex items-center justify-between">
                <div class="flex items-center gap-5">
                    <div class="w-16 h-16 rounded-2xl overflow-hidden border-2 border-slate-50 shadow-sm shrink-0">
                        <img src="${type.roomImage || 'https://via.placeholder.com/150?text=Room'}" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500">
                    </div>
                    <div>
                        <h4 class="font-black text-slate-900 mb-1">${type.typeName}</h4>
                        <p class="text-indigo-600 font-bold text-sm tracking-tight">${new Intl.NumberFormat('vi-VN').format(type.price)}đ / đêm</p>
                    </div>
                </div>
                <div class="flex items-center gap-4 bg-slate-50 p-2 rounded-2xl border border-slate-100">
                    <button type="button" onclick="updateQty('${type.id}', -1)" class="w-10 h-10 rounded-xl bg-white shadow-sm flex items-center justify-center text-slate-400 hover:text-rose-500 hover:shadow-md transition-all font-bold">-</button>
                    <input type="number" id="qty-${type.id}" class="w-10 text-center bg-transparent font-black text-slate-900 outline-none" value="0" readonly>
                    <button type="button" onclick="updateQty('${type.id}', 1)" class="w-10 h-10 rounded-xl bg-white shadow-sm flex items-center justify-center text-slate-400 hover:text-indigo-600 hover:shadow-md transition-all font-bold">+</button>
                </div>
            </div>
            <!-- Room Selection Area -->
            <div id="rooms-for-${type.id}" class="hidden grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-3 pt-4 border-t border-slate-50">
                <!-- Loaded when qty > 0 -->
            </div>
        </div>
    `).join('');
    lucide.createIcons();
}

async function updateQty(id, delta) {
    const input = document.getElementById(`qty-${id}`);
    let val = parseInt(input.value) + delta;
    if (val < 0) val = 0;
    input.value = val;

    const roomsContainer = document.getElementById(`rooms-for-${id}`);
    if (val > 0) {
        roomsContainer.classList.remove('hidden');
        if (roomsContainer.innerHTML.trim() === "") {
            loadAvailableRooms(id);
        }
    } else {
        roomsContainer.classList.add('hidden');
        // Uncheck all rooms of this type if qty is 0
        roomsContainer.querySelectorAll('input[type="checkbox"]').forEach(cb => cb.checked = false);
    }
    
    calculateTotal();
}

async function loadAvailableRooms(roomTypeId) {
    const container = document.getElementById(`rooms-for-${roomTypeId}`);
    container.innerHTML = '<div class="col-span-full text-center py-4 text-[10px] font-bold text-slate-400">Đang tải phòng...</div>';
    
    try {
        const response = await fetch(`/bookings/api/available-rooms/${roomTypeId}?size=100`);
        const data = await response.json();
        const rooms = data.content || [];

        if (rooms.length === 0) {
            container.innerHTML = '<div class="col-span-full text-center py-4 text-[10px] font-bold text-rose-400">Hết phòng trống!</div>';
            return;
        }

        container.innerHTML = rooms.map(room => `
            <label class="relative flex flex-col items-center gap-2 p-3 bg-slate-50 rounded-2xl border border-slate-100 cursor-pointer hover:bg-white hover:border-indigo-300 transition-all group">
                <input type="checkbox" name="roomSelection" value="${room.id}" data-type-id="${roomTypeId}" 
                    onchange="validateRoomSelection('${roomTypeId}')"
                    class="peer absolute opacity-0 w-full h-full cursor-pointer z-10">
                <div class="w-8 h-8 rounded-lg bg-white flex items-center justify-center font-black text-[10px] text-slate-400 peer-checked:bg-indigo-600 peer-checked:text-white transition-all">
                    ${room.roomName}
                </div>
            </label>
        `).join('');
    } catch (err) {
        console.error(err);
        container.innerHTML = '<div class="col-span-full text-center py-4 text-xs font-bold text-rose-400">Lỗi khi tải phòng!</div>';
    }
}

function validateRoomSelection(roomTypeId) {
    const qty = parseInt(document.getElementById(`qty-${roomTypeId}`).value) || 0;
    const selected = document.querySelectorAll(`input[data-type-id="${roomTypeId}"]:checked`);
    
    if (selected.length > qty) {
        toastService.warning(`Đại ca chỉ được chọn tối đa ${qty} phòng cho loại này thôi ạ!`);
        event.target.checked = false;
    }
}

function calculateTotal() {
    const checkInInput = document.getElementById('checkIn').value;
    const checkOutInput = document.getElementById('checkOut').value;
    
    if (!checkInInput || !checkOutInput) return;

    const checkIn = new Date(checkInInput);
    const checkOut = new Date(checkOutInput);
    
    let nights = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24));
    if (isNaN(nights) || nights < 1) nights = 1;
    
    document.getElementById('summary-nights').textContent = `${nights} đêm`;
    
    let total = 0;
    roomTypes.forEach(type => {
        const qty = parseInt(document.getElementById(`qty-${type.id}`).value) || 0;
        total += type.price * qty * nights;
    });
    
    document.getElementById('summary-total').textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(total);
}

function initForm() {
    const form = document.getElementById('walk-in-form');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const roomSelections = roomTypes.map(type => ({
            roomTypeId: type.id,
            quantity: parseInt(document.getElementById(`qty-${type.id}`).value) || 0
        })).filter(s => s.quantity > 0);
        
        if (roomSelections.length === 0) {
            toastService.warning('Đại ca vui lòng chọn ít nhất 1 loại phòng nhé!');
            return;
        }

        // Kiểm tra xem đã chọn đủ số lượng phòng cụ thể chưa
        const selectedRoomIds = Array.from(document.querySelectorAll('input[name="roomSelection"]:checked')).map(cb => cb.value);
        const totalRequiredQty = roomSelections.reduce((sum, s) => sum + s.quantity, 0);

        if (selectedRoomIds.length < totalRequiredQty) {
            toastService.warning(`Đại ca vui lòng chọn đủ ${totalRequiredQty} phòng cụ thể cho khách nhé!`);
            return;
        }

        const data = {
            guestName: document.getElementById('guestName').value,
            guestPhone: document.getElementById('guestPhone').value,
            guestEmail: document.getElementById('guestEmail').value,
            checkIn: document.getElementById('checkIn').value,
            checkOut: document.getElementById('checkOut').value,
            roomSelections: roomSelections,
            roomIds: selectedRoomIds
        };

        const btn = document.getElementById('submit-btn');
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span>Đang tạo đơn & Check-in...</span> <i class="lucide-refresh-cw animate-spin w-5 h-5"></i>';

        try {
            const response = await fetch('/bookings/api/walk-in', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (response.ok) {
                toastService.success('Đã tạo đơn & Check-in thành công cho khách!');
                setTimeout(() => {
                    window.location.href = '/bookings/reception';
                }, 1500);
            } else {
                const err = await response.json();
                toastService.error(err.message || 'Có lỗi xảy ra khi xử lý.');
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        } catch (err) {
            console.error(err);
            toastService.error('Lỗi kết nối hệ thống!');
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    });
}
