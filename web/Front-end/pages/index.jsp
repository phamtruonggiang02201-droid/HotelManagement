<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LuxeStay - Quản trị & Đặt phòng Khách sạn</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8fafc; color: #1e293b; }
        .sidebar-item-active { background-color: #1e293b; color: white; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); }
        section { display: none; }
        section.active { display: block; animation: slideIn 0.4s ease-out; }
        @keyframes slideIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
        .custom-scrollbar::-webkit-scrollbar { width: 6px; }
        .custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
        .custom-scrollbar::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 10px; }
        .modal { display: none; position: fixed; inset: 0; z-index: 100; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); align-items: center; justify-content: center; }
        .modal.active { display: flex; }
        .star-active { color: #f59e0b !important; background-color: #fffbeb !important; border-color: #fef3c7 !important; }

        /* Sidebar visibility states */
        .auth-only { display: none; }
        body.logged-in .auth-only { display: flex; }
        body.logged-in .guest-only { display: none !important; }
    </style>
</head>
<body class="overflow-x-hidden">

    <div id="main-app-container">
        <div class="flex min-h-screen">
            <!-- SIDEBAR -->
            <aside class="w-64 h-screen bg-slate-900 text-slate-300 flex flex-col fixed left-0 top-0 z-50">
                <div class="p-8">
                    <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center">
                            <i data-lucide="crown" class="w-6 h-6 text-white"></i>
                        </div>
                        <h1 class="text-white text-xl font-bold tracking-tight">LuxeStay</h1>
                    </div>
                </div>

                <nav class="flex-1 px-4 space-y-1.5 overflow-y-auto custom-scrollbar" id="main-nav">
                    <a href="#" data-target="home-section" class="nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all sidebar-item-active">
                        <i data-lucide="layout-dashboard" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Trang chủ</span>
                    </a>
                    <a href="#" data-target="rooms-section" class="nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                        <i data-lucide="bed" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Phòng nghỉ</span>
                    </a>
                    <a href="#" data-target="services-section" class="nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                        <i data-lucide="coffee" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Dịch vụ</span>
                    </a>

                    <!-- Các mục chỉ hiện khi đã đăng nhập -->
                    <div class="auth-only pt-4 pb-2 px-3 text-[10px] font-bold text-slate-500 uppercase tracking-widest">Quản trị</div>
                    <a href="#" data-target="feedback-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                        <i data-lucide="message-square-heart" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Đánh giá</span>
                    </a>
                    <a href="#" data-target="booking-history-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                        <i data-lucide="clipboard-list" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Lịch sử booking</span>
                    </a>
                    <a href="#" data-target="stats-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                        <i data-lucide="bar-chart-big" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Thống kê</span>
                    </a>
                    <a href="#" data-target="manage-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                        <i data-lucide="settings-2" class="w-5 h-5"></i>
                        <span class="font-medium text-sm">Cấu hình</span>
                    </a>
                </nav>

                <div class="p-4 border-t border-slate-800 bg-slate-900/50">
                    <!-- Footer cho Khách -->
                    <div class="guest-only space-y-2">
                        <a href="auth.jsp" class="w-full flex items-center justify-center gap-3 p-3.5 rounded-xl bg-indigo-600 text-white font-bold text-xs uppercase tracking-widest hover:bg-indigo-700 transition-all">
                            Đăng nhập
                        </a>
                        <a href="auth.jsp?mode=register" class="w-full flex items-center justify-center gap-3 p-3.5 rounded-xl border border-slate-700 text-slate-300 font-bold text-xs uppercase tracking-widest hover:bg-slate-800 transition-all">
                            Đăng ký
                        </a>
                    </div>
                    
                    <!-- Footer cho Thành viên -->
                    <div class="auth-only flex flex-col space-y-1">
                        <a href="#" data-target="account-section" class="nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                            <i data-lucide="user-circle" class="w-5 h-5"></i>
                            <span class="font-medium text-sm">Tài khoản</span>
                        </a>
                        <button id="logout-btn" class="w-full flex items-center gap-3 p-3.5 rounded-xl text-rose-400 hover:bg-rose-500/10 transition-colors">
                            <i data-lucide="log-out" class="w-5 h-5"></i>
                            <span class="font-bold text-xs uppercase tracking-widest">Đăng xuất</span>
                        </button>
                    </div>
                </div>
            </aside>

            <!-- MAIN CONTENT -->
            <main class="flex-1 ml-64 min-w-0 pb-20">
                <header class="sticky top-0 z-40 bg-white/90 backdrop-blur-xl border-b border-slate-200 px-10 py-5 flex items-center justify-between">
                    <div class="flex items-center gap-4 bg-slate-50 px-5 py-2.5 rounded-2xl border border-slate-200 w-full max-w-lg">
                        <i data-lucide="search" class="w-4 h-4 text-slate-400"></i>
                        <input type="text" placeholder="Tìm kiếm nhanh..." class="bg-transparent border-none outline-none w-full text-sm font-medium">
                    </div>
                    <div class="flex items-center gap-4 ml-auto">
                        <!-- Hiển thị Avatar nếu đã đăng nhập -->
                        <div class="auth-only w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center text-indigo-600 font-bold shadow-sm">VA</div>
                        <!-- Hiển thị nút bắt đầu nếu là khách -->
                        <a href="auth.html" class="guest-only px-6 py-2.5 bg-slate-900 text-white rounded-xl text-xs font-bold hover:bg-indigo-600 transition-all">Bắt đầu ngay</a>
                    </div>
                </header>

                <div class="px-10 py-10 max-w-[1400px] mx-auto">
                    <!-- Các Section -->
                    <section id="home-section" class="active">
                        <div class="mb-10"><h1 class="text-4xl font-extrabold text-slate-900 tracking-tight leading-tight">Chào mừng đến với LuxeStay</h1></div>
                        <div class="relative h-[500px] rounded-[48px] overflow-hidden shadow-2xl mb-16">
                            <img src="https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&q=80&w=1600" class="w-full h-full object-cover">
                            <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/20 to-transparent p-16 flex flex-col justify-end">
                                <h2 class="text-white text-5xl font-black mb-6">Trải nghiệm kỳ nghỉ dưỡng <br>vô cực tại LuxeStay</h2>
                                <button onclick="document.querySelector('[data-target=\'rooms-section\']').click()" class="bg-indigo-600 text-white w-fit px-12 py-5 rounded-2xl font-bold hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-600/40">Xem danh sách phòng</button>
                            </div>
                        </div>
                    </section>

                    <section id="rooms-section">
                        <div class="mb-10"><h1 class="text-3xl font-black text-slate-900">Không gian nghỉ dưỡng</h1></div>
                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                            <div class="bg-white rounded-[40px] border border-slate-100 overflow-hidden shadow-sm hover:shadow-xl transition-all group">
                                <div class="relative h-64 overflow-hidden"><img src="https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&q=80&w=800" class="w-full h-full object-cover transition-transform group-hover:scale-110"></div>
                                <div class="p-8">
                                    <h3 class="text-2xl font-bold text-slate-900 mb-2">Ocean Premier King</h3>
                                    <div class="flex justify-between items-center pt-8 border-t border-slate-50">
                                        <p class="text-2xl font-black text-indigo-600">2.500k</p>
                                        <button class="room-detail-btn bg-slate-900 text-white px-8 py-3.5 rounded-2xl font-bold text-sm hover:bg-indigo-600">Chi tiết</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section id="services-section">
                        <div class="mb-10"><h1 class="text-3xl font-black text-slate-900">Dịch vụ đẳng cấp</h1></div>
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                            <div class="bg-white p-10 rounded-[40px] border border-slate-100 shadow-sm"><i data-lucide="waves" class="w-12 h-12 text-indigo-600 mb-6"></i><h3 class="text-xl font-bold mb-3 text-slate-900">Hồ bơi vô cực</h3><p class="text-slate-500 text-sm">Tầm nhìn tuyệt đối hướng biển.</p></div>
                        </div>
                    </section>

                    <!-- Protected Sections -->
                    <section id="feedback-section" class="auth-only">
                        <div class="max-w-4xl mx-auto">
                            <h1 class="text-3xl font-black text-slate-900 mb-4">Để lại đánh giá</h1>
                            <div class="bg-white p-12 rounded-[48px] border border-slate-100 shadow-sm">
                                <div class="flex gap-4 mb-8" id="star-rating-container">
                                    <button data-rating="1" class="star-btn w-12 h-12 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300"><i data-lucide="star" class="w-6 h-6 fill-current"></i></button>
                                    <button data-rating="2" class="star-btn w-12 h-12 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300"><i data-lucide="star" class="w-6 h-6 fill-current"></i></button>
                                    <button data-rating="3" class="star-btn w-12 h-12 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300"><i data-lucide="star" class="w-6 h-6 fill-current"></i></button>
                                    <button data-rating="4" class="star-btn w-12 h-12 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300"><i data-lucide="star" class="w-6 h-6 fill-current"></i></button>
                                    <button data-rating="5" class="star-btn w-12 h-12 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300"><i data-lucide="star" class="w-6 h-6 fill-current"></i></button>
                                </div>
                                <textarea rows="4" placeholder="Chia sẻ cảm nhận của bạn..." class="w-full bg-slate-50 border border-slate-100 p-6 rounded-3xl outline-none focus:ring-4 focus:ring-indigo-100 mb-8"></textarea>
                                <button class="w-full bg-indigo-600 text-white py-5 rounded-2xl font-black shadow-lg">Gửi phản hồi</button>
                            </div>
                        </div>
                    </section>

                    <section id="booking-history-section" class="auth-only">
                        <div class="mb-10"><h1 class="text-3xl font-black text-slate-900">Lịch sử đặt phòng</h1></div>
                        <div class="bg-white rounded-[40px] border border-slate-100 overflow-hidden shadow-sm">
                            <table class="w-full text-left">
                                <thead class="bg-slate-50 border-b border-slate-100"><tr><th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Booking ID</th><th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest text-right">Thành tiền</th></tr></thead>
                                <tbody class="divide-y divide-slate-50"><tr><td class="px-10 py-8 font-black text-slate-400">#LX-9902</td><td class="px-10 py-8 text-right font-black text-slate-900 text-lg">7.500.000đ</td></tr></tbody>
                            </table>
                        </div>
                    </section>

                    <section id="stats-section" class="auth-only">
                        <div class="mb-10"><h1 class="text-3xl font-black text-slate-900">Thống kê doanh thu</h1></div>
                        <div class="bg-white p-12 rounded-[48px] border border-slate-100 shadow-sm"><h2 class="text-5xl font-black text-indigo-600">420,500,000đ</h2></div>
                    </section>

                    <section id="manage-section" class="auth-only">
                        <div class="mb-10"><h1 class="text-3xl font-black text-slate-900">Quản trị Hệ thống</h1></div>
                        <div class="flex justify-end mb-8"><button class="bg-indigo-600 text-white px-8 py-3.5 rounded-2xl font-black text-sm">Thêm phòng mới</button></div>
                    </section>

                    <section id="account-section" class="auth-only">
                        <div class="max-w-2xl mx-auto text-center">
                            <div class="w-24 h-24 bg-indigo-600 rounded-full flex items-center justify-center text-white text-3xl font-black mx-auto mb-8 shadow-2xl shadow-indigo-600/20">VA</div>
                            <h1 class="text-3xl font-black text-slate-900 mb-8">Thông tin cá nhân</h1>
                            <div class="bg-white p-10 rounded-[40px] border border-slate-100 shadow-sm space-y-6">
                                <div class="text-left"><label class="text-[10px] font-black text-slate-400 uppercase">Họ và tên</label><p class="text-lg font-bold text-slate-900">Nguyễn Văn An</p></div>
                                <div class="text-left"><label class="text-[10px] font-black text-slate-400 uppercase">Email</label><p class="text-lg font-bold text-slate-900">an.nguyen@luxestay.vn</p></div>
                            </div>
                        </div>
                    </section>
                </div>
            </main>
        </div>
    </div>

    <!-- MODAL CHI TIẾT PHÒNG -->
    <div id="room-modal" class="modal"><div class="bg-white w-full max-w-4xl rounded-[48px] overflow-hidden p-12 relative shadow-2xl"><button class="close-modal absolute top-8 right-8 text-slate-400 hover:text-slate-900"><i data-lucide="x" class="w-8 h-8"></i></button><h2 class="text-4xl font-black text-slate-900 mb-4">Ocean Premier King</h2><button class="w-full bg-slate-900 text-white py-6 rounded-3xl font-black mt-10 hover:bg-indigo-600 transition-all">Đặt ngay</button></div></div>

    <script src="<%= request.getContextPath() %>/Front-end/assets/js/index.js"></script>
</body>
</html>
