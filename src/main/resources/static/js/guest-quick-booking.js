document.addEventListener('DOMContentLoaded', () => {
    guestApp.init();
});

const guestApp = {
    state: {
        rooms: [],
        services: [],
        categories: [],
        selectedBookingId: null,
        selectedRoomName: '',
        cart: [],
        selectedCategory: 'all'
    },

    async init() {
        lucide.createIcons();
        await this.loadInitialData();
        this.bindEvents();
    },

    async loadInitialData() {
        try {
            const [roomRes, serviceRes, catRes] = await Promise.all([
                fetch('/services/quick-order/api/occupied-rooms'),
                fetch('/api/services/all'),
                fetch('/api/categories/all')
            ]);

            if (roomRes.ok) this.state.rooms = await roomRes.json();
            if (serviceRes.ok) this.state.services = await serviceRes.json();
            if (catRes.ok) this.state.categories = await catRes.json();

            this.renderRooms();
            this.renderCategories();
            this.renderServices();
        } catch (err) {
            console.error('Fetch error:', err);
            showToast('Không thể kết nối máy chủ!', 'error');
        }
    },

    bindEvents() {
        document.getElementById('submit-order-btn').addEventListener('click', () => this.submitOrder());
    },

    renderRooms() {
        const container = document.getElementById('room-list');
        if (this.state.rooms.length === 0) {
            container.innerHTML = '<p class="col-span-full text-center py-10 text-slate-400 font-bold">Hiện không có phòng nào khả dụng.</p>';
            return;
        }

        container.innerHTML = this.state.rooms.map(room => `
            <div class="room-card bg-white p-6 rounded-[24px] border border-slate-100 cursor-pointer text-center hover:border-indigo-200 transition-all ${this.state.selectedBookingId === room.bookingId ? 'selected' : ''}"
                 onclick="guestApp.selectRoom('${room.bookingId}', '${room.roomName}')">
                <h4 class="text-2xl font-black text-slate-900 mb-1">${room.roomName}</h4>
                <p class="text-[10px] text-slate-400 font-bold uppercase truncate">${room.guestName}</p>
            </div>
        `).join('');
    },

    renderCategories() {
        const container = document.getElementById('category-box');
        const categoriesHtml = this.state.categories.map(cat => `
            <button class="category-btn px-6 py-3 rounded-2xl font-bold text-sm bg-white text-slate-600 border border-slate-100 shadow-sm transition-all ${this.state.selectedCategory === cat.id ? 'active-category' : ''}"
                    onclick="guestApp.filterCategory('${cat.id}')">
                ${cat.categoryName}
            </button>
        `).join('');
        
        container.innerHTML = `
            <button class="category-btn px-6 py-3 rounded-2xl font-bold text-sm bg-white text-slate-600 border border-slate-100 shadow-sm transition-all ${this.state.selectedCategory === 'all' ? 'active-category' : ''}"
                    onclick="guestApp.filterCategory('all')">
                Tất cả
            </button>
        ` + categoriesHtml;
    },

    renderServices() {
        const grid = document.getElementById('service-grid');
        let filtered = this.state.services.filter(s => s.isActive);

        if (this.state.selectedCategory !== 'all') {
            filtered = filtered.filter(s => s.categoryId === this.state.selectedCategory);
        }

        grid.innerHTML = filtered.map(service => `
            <div class="service-card bg-white p-6 rounded-[32px] border border-slate-100 flex justify-between items-center group">
                <div class="flex-1">
                    <span class="text-[9px] font-black uppercase text-indigo-500 tracking-widest">${service.categoryName}</span>
                    <h4 class="text-lg font-black text-slate-900 my-1">${service.serviceName}</h4>
                    <p class="text-indigo-600 font-black">${this.formatPrice(service.price)}</p>
                </div>
                <button onclick="guestApp.addToCart('${service.id}')"
                        class="w-12 h-12 rounded-2xl bg-slate-900 text-white flex items-center justify-center hover:bg-indigo-600 transition-all shadow-lg">
                    <i data-lucide="plus" class="w-6 h-6"></i>
                </button>
            </div>
        `).join('');
        lucide.createIcons();
    },

    selectRoom(bookingId, name) {
        this.state.selectedBookingId = bookingId;
        this.state.selectedRoomName = name;
        this.renderRooms();
        
        // Unlock step 2
        const s2 = document.getElementById('step-2');
        s2.classList.remove('opacity-30', 'pointer-events-none');
        
        // Update labels
        document.getElementById('display-room-badge').classList.remove('hidden');
        document.getElementById('room-name-label').textContent = 'Phòng ' + name;
        
        this.updateCart();
    },

    filterCategory(id) {
        this.state.selectedCategory = id;
        this.renderCategories();
        this.renderServices();
    },

    addToCart(id) {
        const service = this.state.services.find(s => s.id === id);
        const existing = this.state.cart.find(item => item.id === id);

        if (existing) {
            existing.quantity++;
        } else {
            this.state.cart.push({ ...service, quantity: 1 });
        }
        this.updateCart();
    },

    changeQty(id, delta) {
        const item = this.state.cart.find(i => i.id === id);
        if (item) {
            item.quantity += delta;
            if (item.quantity <= 0) {
                this.state.cart = this.state.cart.filter(i => i.id !== id);
            }
        }
        this.updateCart();
    },

    updateCart() {
        const container = document.getElementById('cart-items');
        const empty = document.getElementById('empty-cart-msg');
        const totalEl = document.getElementById('total-price');
        const btn = document.getElementById('submit-order-btn');

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
            const cost = item.price * item.quantity;
            total += cost;
            return `
                <div class="flex items-center gap-4">
                    <div class="flex-1">
                        <h5 class="font-bold text-slate-900 text-sm leading-tight">${item.serviceName}</h5>
                        <p class="text-[10px] text-slate-400 font-bold">${this.formatPrice(item.price)}</p>
                    </div>
                    <div class="flex items-center gap-3 bg-slate-50 p-1.5 rounded-xl border border-slate-100">
                        <button onclick="guestApp.changeQty('${item.id}', -1)" class="w-6 h-6 flex items-center justify-center bg-white rounded-lg hover:bg-rose-50 hover:text-rose-500 transition-all shadow-sm">
                            <i data-lucide="minus" class="w-3 h-3"></i>
                        </button>
                        <span class="w-4 text-center text-xs font-black">${item.quantity}</span>
                        <button onclick="guestApp.changeQty('${item.id}', 1)" class="w-6 h-6 flex items-center justify-center bg-white rounded-lg hover:bg-indigo-50 hover:text-indigo-600 transition-all shadow-sm">
                            <i data-lucide="plus" class="w-3 h-3"></i>
                        </button>
                    </div>
                </div>
            `;
        }).join('');

        totalEl.textContent = this.formatPrice(total);
        btn.disabled = !this.state.selectedBookingId;
        lucide.createIcons();
    },

    async submitOrder() {
        if (!this.state.selectedBookingId || this.state.cart.length === 0) return;

        const btn = document.getElementById('submit-order-btn');
        btn.disabled = true;
        btn.textContent = 'GỬI ĐƠN HÀNG...';

        try {
            const items = {};
            this.state.cart.forEach(i => items[i.id] = i.quantity);

            const res = await fetch('/services/quick-order/api/book', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    bookingId: this.state.selectedBookingId,
                    items: items
                })
            });

            if (res.ok) {
                showToast('Hệ thống đã nhận đơn của phòng ' + this.state.selectedRoomName + '!', 'success');
                this.state.cart = [];
                this.updateCart();
                // Scroll back to top
                window.scrollTo({ top: 0, behavior: 'smooth' });
            } else {
                showToast('Không thể gửi đơn, quý khách vui lòng thử lại!', 'error');
            }
        } catch (err) {
            showToast('Lỗi kết nối!', 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'XÁC NHẬN PHỤC VỤ';
        }
    },

    formatPrice(v) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
    }
};
