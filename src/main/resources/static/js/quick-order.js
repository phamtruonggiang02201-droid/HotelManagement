document.addEventListener('DOMContentLoaded', () => {
    quickOrderApp.init();
});

const quickOrderApp = {
    state: {
        rooms: [],
        services: [],
        categories: [],
        selectedRoom: null,
        cart: [],
        keyword: '',
        selectedCategory: 'all'
    },

    async init() {
        lucide.createIcons();
        await Promise.all([
            this.loadOccupiedRooms(),
            this.loadServices(),
            this.loadCategories()
        ]);
        this.bindEvents();
    },

    bindEvents() {
        document.getElementById('service-search').addEventListener('input', (e) => {
            this.state.keyword = e.target.value;
            this.renderServiceGrid();
        });

        document.getElementById('confirm-order-btn').addEventListener('click', () => {
            this.submitOrder();
        });
    },

    async loadOccupiedRooms() {
        try {
            const response = await fetch('/management/service-orders/api/occupied-rooms');
            if (response.ok) {
                this.state.rooms = await response.json();
                this.renderRoomList();
            }
        } catch (error) {
            console.error('Error loading rooms:', error);
            toastService.error('Không thể tải danh sách phòng!');
        }
    },

    async loadServices() {
        try {
            const response = await fetch('/api/services/all');
            if (response.ok) {
                this.state.services = await response.json();
                this.renderServiceGrid();
            }
        } catch (error) {
            console.error('Error loading services:', error);
        }
    },

    async loadCategories() {
        try {
            const response = await fetch('/api/categories/all');
            if (response.ok) {
                this.state.categories = await response.json();
                this.renderCategories();
            }
        } catch (error) {
            console.error('Error loading categories:', error);
        }
    },

    renderRoomList() {
        const roomList = document.getElementById('room-list');
        if (this.state.rooms.length === 0) {
            roomList.innerHTML = `
                <div class="text-center p-8 text-slate-400">
                    <p class="text-xs">Hiện không có phòng nào đang ở.</p>
                </div>
            `;
            return;
        }

        roomList.innerHTML = this.state.rooms.map(booking => {
            const roomNames = booking.rooms?.map(r => r.roomName).join(', ') || 'N/A';
            const isActive = this.state.selectedRoom?.id === booking.id;
            
            return `
                <div class="quick-card p-4 rounded-2xl border border-slate-100 cursor-pointer hover:bg-slate-50 ${isActive ? 'selected' : ''}"
                     onclick="quickOrderApp.selectRoom('${booking.id}')">
                    <div class="flex justify-between items-center mb-1">
                        <span class="text-lg font-black text-slate-900">${roomNames}</span>
                        <span class="px-2 py-0.5 rounded-full bg-indigo-100 text-indigo-700 font-bold text-[9px] uppercase tracking-tighter">Đang ở</span>
                    </div>
                    <p class="text-xs text-slate-500 font-medium truncate">Khách: ${booking.guest?.fullName || 'N/A'}</p>
                </div>
            `;
        }).join('');
    },

    renderCategories() {
        const container = document.getElementById('category-tabs');
        const activeClass = 'custom-active-tab';
        
        const html = [
            `<span class="cursor-pointer pb-2 category-tab ${this.state.selectedCategory === 'all' ? activeClass : ''}" onclick="quickOrderApp.selectCategory('all')">Tất cả</span>`
        ];

        this.state.categories.forEach(cat => {
            html.push(`<span class="cursor-pointer pb-2 category-tab ${this.state.selectedCategory === cat.id ? activeClass : ''}" onclick="quickOrderApp.selectCategory('${cat.id}')">${cat.categoryName}</span>`);
        });

        container.innerHTML = html.join('');
    },

    renderServiceGrid() {
        const grid = document.getElementById('service-grid');
        let filtered = this.state.services;

        if (this.state.selectedCategory !== 'all') {
            filtered = filtered.filter(s => s.category?.id === this.state.selectedCategory);
        }

        if (this.state.keyword) {
            const k = this.state.keyword.toLowerCase();
            filtered = filtered.filter(s => s.serviceName.toLowerCase().includes(k));
        }

        grid.innerHTML = filtered.map(service => `
            <div class="quick-card p-4 rounded-2xl border border-slate-100 flex flex-col hover:shadow-md h-full">
                <div class="flex justify-between items-start mb-2">
                    <h4 class="font-bold text-slate-900 leading-tight">${service.serviceName}</h4>
                    <span class="text-indigo-600 font-black text-xs">${this.formatCurrency(service.price)}</span>
                </div>
                <p class="text-[10px] text-slate-400 mb-4">${service.category?.categoryName || 'Dịch vụ'}</p>
                <button onclick="quickOrderApp.addToCart('${service.id}')"
                    class="mt-auto bg-slate-100 text-slate-900 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest hover:bg-slate-900 hover:text-white transition-all">
                    Chọn món
                </button>
            </div>
        `).join('');
    },

    selectRoom(bookingId) {
        this.state.selectedRoom = this.state.rooms.find(b => b.id === bookingId);
        this.renderRoomList();
        
        // Update selection UI in Cart column
        const info = document.getElementById('selected-room-info');
        info.classList.remove('hidden');
        document.getElementById('display-room-name').textContent = this.state.selectedRoom.rooms?.map(r => r.roomName).join(', ') || 'N/A';
        document.getElementById('display-guest-name').textContent = `Khách: ${this.state.selectedRoom.guest?.fullName}`;
        
        this.updateCartUI();
    },

    selectCategory(catId) {
        this.state.selectedCategory = catId;
        this.renderCategories();
        this.renderServiceGrid();
    },

    addToCart(serviceId) {
        const service = this.state.services.find(s => s.id === serviceId);
        const existing = this.state.cart.find(item => item.id === serviceId);

        if (existing) {
            existing.quantity++;
        } else {
            this.state.cart.push({
                ...service,
                quantity: 1
            });
        }
        this.updateCartUI();
    },

    removeFromCart(serviceId) {
        const index = this.state.cart.findIndex(item => item.id === serviceId);
        if (index > -1) {
            if (this.state.cart[index].quantity > 1) {
                this.state.cart[index].quantity--;
            } else {
                this.state.cart.splice(index, 1);
            }
        }
        this.updateCartUI();
    },

    updateCartUI() {
        const container = document.getElementById('cart-items');
        const empty = document.getElementById('empty-cart');
        const totalEl = document.getElementById('cart-total');
        const btn = document.getElementById('confirm-order-btn');

        if (this.state.cart.length === 0) {
            empty.classList.remove('hidden');
            container.innerHTML = '';
            totalEl.textContent = '0đ';
            btn.disabled = true;
            return;
        }

        empty.classList.add('hidden');
        let total = 0;

        container.innerHTML = this.state.cart.map(item => {
            const itemTotal = item.price * item.quantity;
            total += itemTotal;
            return `
                <div class="flex items-center gap-4 group">
                    <div class="flex-1">
                        <h5 class="text-sm font-bold text-white">${item.serviceName}</h5>
                        <p class="text-[10px] text-slate-400">${this.formatCurrency(item.price)}</p>
                    </div>
                    <div class="flex items-center bg-white/10 rounded-lg p-1">
                        <button onclick="quickOrderApp.removeFromCart('${item.id}')" class="w-6 h-6 flex items-center justify-center hover:bg-white/20 rounded-md transition-all">
                            <i data-lucide="minus" class="w-3 h-3 text-white"></i>
                        </button>
                        <span class="w-8 text-center text-xs font-black">${item.quantity}</span>
                        <button onclick="quickOrderApp.addToCart('${item.id}')" class="w-6 h-6 flex items-center justify-center hover:bg-white/20 rounded-md transition-all">
                            <i data-lucide="plus" class="w-3 h-3 text-white"></i>
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        totalEl.textContent = this.formatCurrency(total);
        btn.disabled = !this.state.selectedRoom;
        
        lucide.createIcons();
    },

    async submitOrder() {
        if (!this.state.selectedRoom || this.state.cart.length === 0) return;

        const btn = document.getElementById('confirm-order-btn');
        const originalText = btn.textContent;
        btn.disabled = true;
        btn.textContent = 'Đang xử lý...';

        try {
            const items = {};
            this.state.cart.forEach(item => {
                items[item.id] = item.quantity;
            });

            const response = await fetch('/management/service-orders/api/quick', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    bookingId: this.state.selectedRoom.id,
                    roomId: this.state.selectedRoom.rooms?.[0]?.id, // Default to first room
                    items: items
                })
            });

            if (response.ok) {
                toastService.success('Đã đặt dịch vụ thành công cho ' + document.getElementById('display-room-name').textContent);
                this.state.cart = [];
                this.updateCartUI();
            } else {
                const err = await response.json();
                toastService.error('Lỗi: ' + (err.message || 'Không thể đặt đơn'));
            }
        } catch (error) {
            console.error('Error submitting order:', error);
            toastService.error('Có lỗi xảy ra khi gửi đơn hàng!');
        } finally {
            btn.disabled = false;
            btn.textContent = originalText;
        }
    },

    formatCurrency(value) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
    }
};
