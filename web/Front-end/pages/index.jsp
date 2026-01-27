<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>LuxeStay - Khách sạn</title>
        <script src="https://cdn.tailwindcss.com"></script>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
        <script src="https://unpkg.com/lucide@latest"></script>
        <style>
            body {
                font-family: 'Inter', sans-serif;
                background-color: #f8fafc;
                color: #1e293b;
            }
            .sidebar-item-active {
                background-color: #1e293b;
                color: white;
                box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1);
            }
            section {
                display: none;
            }
            section.active {
                display: block;
                animation: slideIn 0.4s ease-out;
            }
            @keyframes slideIn {
                from {
                    opacity: 0;
                    transform: translateY(15px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
            .custom-scrollbar::-webkit-scrollbar {
                width: 6px;
            }
            .custom-scrollbar::-webkit-scrollbar-track {
                background: transparent;
            }
            .custom-scrollbar::-webkit-scrollbar-thumb {
                background: #e2e8f0;
                border-radius: 10px;
            }
            .modal {
                display: none;
                position: fixed;
                inset: 0;
                z-index: 100;
                background: rgba(15, 23, 42, 0.6);
                backdrop-filter: blur(4px);
                align-items: center;
                justify-content: center;
            }
            .modal.active {
                display: flex;
            }
            .star-active {
                color: #f59e0b !important;
                background-color: #fffbeb !important;
                border-color: #fef3c7 !important;
            }

            /* Sidebar visibility states */
            .auth-only {
                display: none;
            }
            body.logged-in .auth-only {
                display: flex;
            }
            body.logged-in .guest-only {
                display: none !important;
            }

            .card-hover {
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }
            .card-hover:hover {
                transform: translateY(-5px);
                box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
            }
        </style>
    </head>
    <body class="overflow-x-hidden">

        <div id="main-app-container">
            <div class="flex min-h-screen">
                <!-- SIDEBAR -->
                <aside class="w-64 h-screen bg-slate-900 text-slate-300 flex flex-col fixed left-0 top-0 z-50">
                    <div class="p-8">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-600/20">
                                <i data-lucide="crown" class="w-6 h-6 text-white"></i>
                            </div>
                            <h1 class="text-white text-xl font-bold tracking-tight">LuxeStay</h1>
                        </div>
                    </div>

                    <nav class="flex-1 px-4 space-y-1.5 overflow-y-auto custom-scrollbar" id="main-nav">
                        <!-- Public Links -->
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
                        <a href="#" data-target="feedback-section" class="nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                            <i data-lucide="message-square-heart" class="w-5 h-5"></i>
                            <span class="font-medium text-sm">Đánh giá</span>
                        </a>

                        <!-- Protected Links -->
                        <div class="auth-only pt-6 pb-2 px-3 text-[10px] font-bold text-slate-500 uppercase tracking-widest">Hệ thống & Quản trị</div>
                        <a href="#" data-target="booking-history-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                            <i data-lucide="clipboard-list" class="w-5 h-5"></i>
                            <span class="font-medium text-sm">Lịch sử booking</span>
                        </a>
                        <a href="#" data-target="schedule-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                            <i data-lucide="calendar-clock" class="w-5 h-5"></i>
                            <span class="font-medium text-sm">Lịch trực</span>
                        </a>
                        <a href="#" data-target="stats-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                            <i data-lucide="bar-chart-big" class="w-5 h-5"></i>
                            <span class="font-medium text-sm">Doanh thu</span>
                        </a>
                        <a href="#" data-target="manage-section" class="auth-only nav-link flex items-center gap-3 p-3.5 rounded-xl hover:bg-slate-800 hover:text-white transition-all">
                            <i data-lucide="settings-2" class="w-5 h-5"></i>
                            <span class="font-medium text-sm">Cấu hình</span>
                        </a>
                    </nav>

                    <div class="p-4 border-t border-slate-800 bg-slate-900/50">
                        <div class="guest-only space-y-2">
                            <a href="auth.jsp" class="w-full flex items-center justify-center gap-3 p-3.5 rounded-xl bg-indigo-600 text-white font-bold text-xs uppercase tracking-widest hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-600/20">
                                Đăng nhập
                            </a>
                            <a href="auth.jsp?mode=register" class="w-full flex items-center justify-center gap-3 p-3.5 rounded-xl border border-slate-700 text-slate-300 font-bold text-xs uppercase tracking-widest hover:bg-slate-800 transition-all">
                                Đăng ký
                            </a>
                        </div>

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
                        <div class="flex items-center gap-4 bg-slate-50 px-5 py-2.5 rounded-2xl border border-slate-200 w-full max-w-lg focus-within:ring-2 focus-within:ring-indigo-100 transition-all">
                            <i data-lucide="search" class="w-4 h-4 text-slate-400"></i>
                            <input type="text" placeholder="Tìm phòng, dịch vụ..." class="bg-transparent border-none outline-none w-full text-sm font-medium">
                        </div>
                        <div class="flex items-center gap-4 ml-auto">
                            <div class="auth-only flex items-center gap-3">
                                <div class="text-right hidden sm:block">
                                    <p class="text-xs font-bold text-slate-900">Quản trị viên</p>
                                    <p class="text-[10px] text-slate-400 font-medium">an.nguyen@luxestay.vn</p>
                                </div>
                                <div class="w-10 h-10 bg-indigo-100 rounded-xl flex items-center justify-center text-indigo-600 font-bold shadow-sm">VA</div>
                            </div>
                            <a href="auth.jsp" class="guest-only px-6 py-2.5 bg-slate-900 text-white rounded-xl text-xs font-bold hover:bg-indigo-600 transition-all shadow-lg shadow-slate-900/20">Bắt đầu ngay</a>
                        </div>
                    </header>

                    <div class="px-10 py-10 max-w-[1400px] mx-auto">
                        <!-- Section: Home -->
                        <section id="home-section" class="active">
                            <div class="mb-12">
                                <h1 class="text-4xl font-extrabold text-slate-900 tracking-tight leading-tight">Khám phá LuxeStay</h1>
                                <p class="text-slate-500 mt-2 font-medium">Trải nghiệm dịch vụ 5 sao đẳng cấp quốc tế.</p>
                            </div>

                            <!-- Hero Banner -->
                            <div class="relative h-[480px] rounded-[48px] overflow-hidden shadow-2xl mb-20 group">
                                <img src="https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&q=80&w=1600" class="w-full h-full object-cover transition-transform duration-1000 group-hover:scale-105">
                                <div class="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/40 to-transparent p-16 flex flex-col justify-end">
                                    <span class="bg-indigo-600 text-white px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest w-fit mb-4 shadow-lg">New Season Offer</span>
                                    <h2 class="text-white text-6xl font-black mb-6 leading-tight">Kỳ nghỉ hè <br>không giới hạn</h2>
                                    <button onclick="document.querySelector('[data-target=\'rooms-section\']').click()" class="bg-white text-slate-900 w-fit px-12 py-5 rounded-2xl font-black hover:bg-indigo-600 hover:text-white transition-all shadow-xl">Xem danh sách phòng</button>
                                </div>
                            </div>

                            <!-- Highlights Section -->
                            <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-20">
                                <!-- Featured Rooms -->
                                <div>
                                    <div class="flex justify-between items-center mb-8">
                                        <h3 class="text-2xl font-black text-slate-900">Phòng nghỉ nổi bật</h3>
                                        <button onclick="document.querySelector('[data-target=\'rooms-section\']').click()" class="text-indigo-600 text-xs font-black uppercase tracking-widest hover:underline">Xem tất cả -></button>
                                    </div>
                                    <div class="space-y-6">
                                        <div class="bg-white p-6 rounded-[32px] border border-slate-100 shadow-sm flex items-center gap-6 card-hover">
                                            <div class="w-32 h-32 rounded-2xl overflow-hidden flex-shrink-0">
                                                <img src="https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&q=80&w=400" class="w-full h-full object-cover">
                                            </div>
                                            <div class="flex-1">
                                                <div class="flex justify-between items-start mb-2">
                                                    <h4 class="font-bold text-slate-900 text-lg">Royal Suite Sea View</h4>
                                                    <span class="text-indigo-600 font-black">4.200k</span>
                                                </div>
                                                <p class="text-slate-500 text-xs font-medium mb-4 line-clamp-1">Phòng tổng thống với view panorama toàn bộ vịnh biển.</p>
                                                <button class="room-detail-btn text-[10px] font-black uppercase text-slate-400 hover:text-indigo-600 tracking-widest">Xem chi tiết</button>
                                            </div>
                                        </div>
                                        <div class="bg-white p-6 rounded-[32px] border border-slate-100 shadow-sm flex items-center gap-6 card-hover">
                                            <div class="w-32 h-32 rounded-2xl overflow-hidden flex-shrink-0">
                                                <img src="https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&q=80&w=400" class="w-full h-full object-cover">
                                            </div>
                                            <div class="flex-1">
                                                <div class="flex justify-between items-start mb-2">
                                                    <h4 class="font-bold text-slate-900 text-lg">Deluxe Garden View</h4>
                                                    <span class="text-indigo-600 font-black">1.800k</span>
                                                </div>
                                                <p class="text-slate-500 text-xs font-medium mb-4 line-clamp-1">Không gian xanh mát, yên tĩnh giữa lòng resort.</p>
                                                <button class="room-detail-btn text-[10px] font-black uppercase text-slate-400 hover:text-indigo-600 tracking-widest">Xem chi tiết</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Popular Services -->
                                <div>
                                    <div class="flex justify-between items-center mb-8">
                                        <h3 class="text-2xl font-black text-slate-900">Dịch vụ yêu thích</h3>
                                        <button onclick="document.querySelector('[data-target=\'services-section\']').click()" class="text-indigo-600 text-xs font-black uppercase tracking-widest hover:underline">Xem tất cả -></button>
                                    </div>
                                    <div class="grid grid-cols-2 gap-6">
                                        <div class="bg-white p-8 rounded-[40px] border border-slate-100 shadow-sm card-hover relative overflow-hidden">
                                            <div class="absolute top-0 right-0 p-4">
                                                <span class="bg-rose-50 text-rose-500 px-3 py-1 rounded-full text-[8px] font-black uppercase">Trending</span>
                                            </div>
                                            <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center mb-6">
                                                <i data-lucide="utensils" class="w-6 h-6"></i>
                                            </div>
                                            <h4 class="font-bold text-slate-900 mb-2">Fine Dining</h4>
                                            <p class="text-slate-400 text-[10px] font-medium leading-relaxed">Ẩm thực đa dạng từ các đầu bếp hàng đầu thế giới.</p>
                                        </div>
                                        <div class="bg-white p-8 rounded-[40px] border border-slate-100 shadow-sm card-hover relative overflow-hidden">
                                            <div class="w-12 h-12 bg-emerald-50 text-emerald-600 rounded-2xl flex items-center justify-center mb-6">
                                                <i data-lucide="sparkles" class="w-6 h-6"></i>
                                            </div>
                                            <h4 class="font-bold text-slate-900 mb-2">Lotus Spa</h4>
                                            <p class="text-slate-400 text-[10px] font-medium leading-relaxed">Thư giãn tâm hồn với liệu trình thảo mộc truyền thống.</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Top Feedbacks (5-Star) -->
                            <div>
                                <div class="flex justify-between items-center mb-10">
                                    <div>
                                        <h3 class="text-3xl font-black text-slate-900">Phản hồi 5 sao</h3>
                                        <p class="text-slate-400 text-sm font-medium mt-1">Câu chuyện từ những khách hàng hài lòng nhất.</p>
                                    </div>
                                    <button onclick="document.querySelector('[data-target=\'feedback-section\']').click()" class="bg-slate-50 text-slate-900 px-6 py-3 rounded-2xl text-xs font-bold hover:bg-slate-100 transition-all border border-slate-200">Xem tất cả đánh giá</button>
                                </div>
                                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                                    <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm relative">
                                        <div class="flex text-amber-400 gap-1 mb-6">
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                        </div>
                                        <p class="text-slate-600 text-sm font-medium italic leading-relaxed mb-8">"LuxeStay thực sự là một thiên đường nghỉ dưỡng. Tôi đặc biệt ấn tượng với sự tận tâm của đội ngũ nhân viên. Chắc chắn sẽ quay lại!"</p>
                                        <div class="flex items-center gap-4 border-t border-slate-50 pt-8">
                                            <div class="w-12 h-12 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-600 font-bold">HA</div>
                                            <div>
                                                <h5 class="font-bold text-slate-900 text-sm">Hoàng Anh</h5>
                                                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Khách hàng Premium</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm relative">
                                        <div class="flex text-amber-400 gap-1 mb-6">
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                        </div>
                                        <p class="text-slate-600 text-sm font-medium italic leading-relaxed mb-8">"Dịch vụ phòng tuyệt vời, món ăn tại Fine Dining Restaurant ngon nhất mà tôi từng thử tại một khách sạn. 10/10!"</p>
                                        <div class="flex items-center gap-4 border-t border-slate-50 pt-8">
                                            <div class="w-12 h-12 bg-rose-100 rounded-full flex items-center justify-center text-rose-600 font-bold">ML</div>
                                            <div>
                                                <h5 class="font-bold text-slate-900 text-sm">Mai Lan</h5>
                                                <p class="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Du khách tự do</p>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="bg-slate-900 p-10 rounded-[48px] shadow-2xl relative text-white">
                                        <i data-lucide="quote" class="absolute top-8 right-10 w-12 h-12 text-white/10"></i>
                                        <div class="flex text-amber-400 gap-1 mb-6">
                                            <i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i><i data-lucide="star" class="w-4 h-4 fill-current"></i>
                                        </div>
                                        <p class="text-slate-300 text-sm font-medium italic leading-relaxed mb-8">"Tôi đã ở nhiều resort 5 sao nhưng chưa nơi nào cho cảm giác 'như ở nhà' nhưng vẫn sang trọng như tại LuxeStay."</p>
                                        <div class="flex items-center gap-4 border-t border-white/10 pt-8">
                                            <div class="w-12 h-12 bg-white/10 rounded-full flex items-center justify-center text-white font-bold">QD</div>
                                            <div>
                                                <h5 class="font-bold text-white text-sm">Quốc Dũng</h5>
                                                <p class="text-[10px] text-slate-500 font-bold uppercase tracking-widest">Thành viên Elite</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <!-- Section: Rooms (All) -->
                        <section id="rooms-section">
                            <div class="mb-12">
                                <h1 class="text-3xl font-black text-slate-900">Danh sách Phòng nghỉ</h1>
                                <p class="text-slate-500 font-medium">Lựa chọn không gian hoàn hảo cho hành trình của bạn.</p>
                            </div>
                            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
                                <!-- Card mẫu -->
                                <div class="bg-white rounded-[48px] border border-slate-100 overflow-hidden shadow-sm hover:shadow-2xl transition-all group cursor-pointer">
                                    <div class="relative h-72 overflow-hidden">
                                        <img src="https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&q=80&w=800" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110">
                                        <div class="absolute top-6 left-6 flex flex-wrap gap-2">
                                            <span class="bg-white/90 backdrop-blur-md text-slate-900 px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest shadow-lg">Premium</span>
                                        </div>
                                        <button class="absolute bottom-6 right-6 w-12 h-12 bg-white rounded-full flex items-center justify-center text-slate-900 hover:bg-indigo-600 hover:text-white transition-all shadow-xl">
                                            <i data-lucide="heart" class="w-5 h-5"></i>
                                        </button>
                                    </div>
                                    <div class="p-10">
                                        <div class="flex justify-between items-start mb-4">
                                            <h3 class="text-2xl font-black text-slate-900">Ocean Premier King</h3>
                                            <p class="text-indigo-600 font-black text-xl">2.500k</p>
                                        </div>
                                        <div class="flex items-center gap-4 mb-8 text-slate-400">
                                            <div class="flex items-center gap-1.5"><i data-lucide="maximize" class="w-4 h-4"></i><span class="text-[10px] font-bold">45m²</span></div>
                                            <div class="flex items-center gap-1.5"><i data-lucide="users" class="w-4 h-4"></i><span class="text-[10px] font-bold">2 Khách</span></div>
                                            <div class="flex items-center gap-1.5"><i data-lucide="wifi" class="w-4 h-4"></i><span class="text-[10px] font-bold">Free Wifi</span></div>
                                        </div>
                                        <button class="room-detail-btn w-full bg-slate-900 text-white py-5 rounded-[24px] font-black hover:bg-indigo-600 transition-all shadow-xl shadow-slate-900/10">Xem chi tiết & Đặt</button>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <!-- Section: Services (All) -->
                        <section id="services-section">
                            <div class="mb-12">
                                <h1 class="text-3xl font-black text-slate-900">Dịch vụ & Tiện ích</h1>
                                <p class="text-slate-500 font-medium">Nâng tầm kỳ nghỉ với những tiện nghi chuẩn 5 sao quốc tế.</p>
                            </div>
                            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                                <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm text-center card-hover">
                                    <div class="w-16 h-16 bg-blue-50 text-blue-600 rounded-3xl flex items-center justify-center mx-auto mb-6">
                                        <i data-lucide="waves" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-xl font-bold mb-3 text-slate-900">Hồ bơi Vô cực</h3>
                                    <p class="text-slate-500 text-xs font-medium leading-relaxed">Tầm nhìn tuyệt đối hướng biển rộng lớn.</p>
                                </div>
                                <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm text-center card-hover">
                                    <div class="w-16 h-16 bg-amber-50 text-amber-600 rounded-3xl flex items-center justify-center mx-auto mb-6">
                                        <i data-lucide="coffee" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-xl font-bold mb-3 text-slate-900">Lounge & Bar</h3>
                                    <p class="text-slate-500 text-xs font-medium leading-relaxed">Thưởng thức cocktail thượng hạng bên bờ biển.</p>
                                </div>
                                <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm text-center card-hover">
                                    <div class="w-16 h-16 bg-rose-50 text-rose-600 rounded-3xl flex items-center justify-center mx-auto mb-6">
                                        <i data-lucide="dumbbell" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-xl font-bold mb-3 text-slate-900">Phòng Gym</h3>
                                    <p class="text-slate-500 text-xs font-medium leading-relaxed">Trang thiết bị hiện đại duy trì vóc dáng.</p>
                                </div>
                                <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm text-center card-hover">
                                    <div class="w-16 h-16 bg-emerald-50 text-emerald-600 rounded-3xl flex items-center justify-center mx-auto mb-6">
                                        <i data-lucide="map-pin" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-xl font-bold mb-3 text-slate-900">City Tours</h3>
                                    <p class="text-slate-500 text-xs font-medium leading-relaxed">Khám phá văn hóa địa phương đặc sắc.</p>
                                </div>
                            </div>
                        </section>

                        <!-- Section: Feedback (Full) -->
                        <section id="feedback-section">
                            <div class="max-w-4xl mx-auto">
                                <div class="mb-12">
                                    <h1 class="text-3xl font-black text-slate-900">Đánh giá & Phản hồi</h1>
                                    <p class="text-slate-500 font-medium mt-2">Sự góp ý của bạn giúp chúng tôi hoàn thiện hơn mỗi ngày.</p>
                                </div>

                                <!-- Submit Form (Auth only) -->
                                <div class="auth-only bg-white p-12 rounded-[56px] border border-slate-100 shadow-2xl mb-16 relative overflow-hidden">
                                    <div class="absolute top-0 right-0 w-32 h-32 bg-indigo-50 rounded-bl-[100px] -z-10"></div>
                                    <h3 class="text-2xl font-black mb-8 text-slate-900">Chia sẻ trải nghiệm của bạn</h3>
                                    <div class="space-y-10">
                                        <div class="space-y-4">
                                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Mức độ hài lòng</label>
                                            <div class="flex gap-4" id="star-rating-container">
                                                <button data-rating="1" class="star-btn w-14 h-14 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300 hover:bg-amber-50 transition-all"><i data-lucide="star" class="w-7 h-7 fill-current"></i></button>
                                                <button data-rating="2" class="star-btn w-14 h-14 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300 hover:bg-amber-50 transition-all"><i data-lucide="star" class="w-7 h-7 fill-current"></i></button>
                                                <button data-rating="3" class="star-btn w-14 h-14 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300 hover:bg-amber-50 transition-all"><i data-lucide="star" class="w-7 h-7 fill-current"></i></button>
                                                <button data-rating="4" class="star-btn w-14 h-14 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300 hover:bg-amber-50 transition-all"><i data-lucide="star" class="w-7 h-7 fill-current"></i></button>
                                                <button data-rating="5" class="star-btn w-14 h-14 rounded-2xl border border-slate-100 flex items-center justify-center text-slate-300 hover:bg-amber-50 transition-all"><i data-lucide="star" class="w-7 h-7 fill-current"></i></button>
                                            </div>
                                        </div>
                                        <div class="space-y-4">
                                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Nhận xét chi tiết</label>
                                            <textarea rows="5" placeholder="Bạn cảm thấy thế nào về kỳ nghỉ lần này?..." class="w-full bg-slate-50 border border-slate-100 p-8 rounded-[32px] outline-none focus:ring-4 focus:ring-indigo-100 transition-all font-medium"></textarea>
                                        </div>
                                        <button class="w-full bg-indigo-600 text-white py-6 rounded-[28px] font-black shadow-2xl shadow-indigo-600/30 hover:bg-indigo-700 transition-all transform hover:-translate-y-1">Gửi phản hồi ngay</button>
                                    </div>
                                </div>

                                <div class="guest-only p-12 bg-indigo-50 border border-indigo-100 rounded-[48px] mb-16 text-center">
                                    <div class="w-16 h-16 bg-white rounded-3xl flex items-center justify-center mx-auto mb-6 shadow-xl text-indigo-600">
                                        <i data-lucide="lock" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-xl font-bold text-slate-900 mb-2">Đăng nhập để gửi đánh giá</h3>
                                    <p class="text-slate-500 mb-8 font-medium">Chúng tôi rất mong nhận được những góp ý chân thành từ bạn.</p>
                                    <a href="auth.jsp" class="inline-block bg-indigo-600 text-white px-10 py-4 rounded-2xl font-black text-xs uppercase tracking-widest hover:bg-indigo-700 transition-all shadow-xl shadow-indigo-600/20">Đăng nhập ngay</a>
                                </div>

                                <!-- List -->
                                <div class="space-y-8">
                                    <div class="bg-white p-10 rounded-[48px] border border-slate-100 shadow-sm relative">
                                        <div class="flex items-center gap-6 mb-8">
                                            <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 font-black text-xl">TM</div>
                                            <div>
                                                <h4 class="font-black text-slate-900 text-lg">Trần Minh</h4>
                                                <div class="flex text-amber-400 gap-1 mt-1"><i data-lucide="star" class="w-3 h-3 fill-current"></i><i data-lucide="star" class="w-3 h-3 fill-current"></i><i data-lucide="star" class="w-3 h-3 fill-current"></i><i data-lucide="star" class="w-3 h-3 fill-current"></i><i data-lucide="star" class="w-3 h-3 fill-current"></i></div>
                                            </div>
                                            <span class="ml-auto text-[10px] font-bold text-slate-300 uppercase tracking-widest">2 ngày trước</span>
                                        </div>
                                        <p class="text-slate-600 leading-relaxed font-medium italic">"Mọi thứ đều hoàn hảo từ lúc check-in đến check-out. Nhân viên hỗ trợ nhiệt tình, buffet sáng đa dạng."</p>
                                    </div>
                                </div>
                            </div>
                        </section>

                        <!-- Section: Booking History -->
                        <section id="booking-history-section" >
                            <div class="mb-10"><h1 class="text-3xl font-black text-slate-900">Lịch sử booking</h1></div>
                            <div class="bg-white rounded-[40px] border border-slate-100 overflow-hidden shadow-sm">
                                <table class="w-full text-left">
                                    <thead class="bg-slate-50 border-b border-slate-100"><tr><th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Mã Đặt Phòng</th><th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest text-right">Tổng thanh toán</th></tr></thead>
                                    <tbody class="divide-y divide-slate-50"><tr><td class="px-10 py-8 font-black text-slate-400">#LX-9902</td><td class="px-10 py-8 text-right font-black text-slate-900 text-lg">7.500.000đ</td></tr></tbody>
                                </table>
                            </div>
                        </section>

                        <!-- Section: Schedule -->
                        <section id="schedule-section" >
                            <div class="flex flex-col lg:flex-row justify-between items-center mb-12 gap-6">
                                <div>
                                    <h1 class="text-3xl font-black text-slate-900">Lịch trình công tác</h1>
                                    <p class="text-slate-500 font-medium">Theo dõi và quản lý thời gian làm việc hiệu quả.</p>
                                </div>
                                <div class="flex bg-white p-1.5 rounded-[20px] border border-slate-100 shadow-sm">
                                    <button id="view-staff-btn" class="px-8 py-3 bg-indigo-600 text-white rounded-2xl text-xs font-black transition-all">Lịch của tôi</button>
                                    <button id="view-manager-btn" class="px-8 py-3 text-slate-400 hover:text-slate-900 rounded-2xl text-xs font-black transition-all">Phân ca trực</button>
                                </div>
                            </div>

                            <!-- Staff View -->
                            <div id="staff-schedule-view" class="grid grid-cols-1 md:grid-cols-2 gap-8">
                                <div class="bg-indigo-600 p-10 rounded-[56px] text-white shadow-2xl shadow-indigo-600/30 relative overflow-hidden">
                                    <i data-lucide="clock" class="absolute -bottom-10 -right-10 w-48 h-48 text-white/5"></i>
                                    <div class="flex items-center gap-6 mb-10">
                                        <div class="w-16 h-16 bg-white/10 backdrop-blur-md rounded-3xl flex items-center justify-center font-black text-2xl">15</div>
                                        <div>
                                            <h4 class="font-black text-2xl">Ca Sáng Hôm Nay</h4>
                                            <p class="text-indigo-100 text-[10px] font-bold uppercase tracking-widest mt-1 opacity-80">Check-in: 06:55 AM</p>
                                        </div>
                                    </div>
                                    <div class="space-y-4">
                                        <div class="flex justify-between items-center p-4 bg-white/10 rounded-2xl">
                                            <span class="text-[10px] font-bold uppercase tracking-widest opacity-80">Thời gian</span>
                                            <span class="font-black">07:00 - 15:00</span>
                                        </div>
                                        <div class="flex justify-between items-center p-4 bg-white/10 rounded-2xl">
                                            <span class="text-[10px] font-bold uppercase tracking-widest opacity-80">Vị trí</span>
                                            <span class="font-black text-sm">Tiền sảnh - Main Lobby</span>
                                        </div>
                                    </div>
                                </div>
                                <div class="bg-white p-10 rounded-[56px] border border-slate-100 shadow-sm flex flex-col justify-between opacity-80 card-hover">
                                    <div>
                                        <div class="flex items-center gap-6 mb-10">
                                            <div class="w-16 h-16 bg-slate-50 text-slate-400 rounded-3xl flex items-center justify-center font-black text-2xl">16</div>
                                            <div>
                                                <h4 class="font-black text-2xl text-slate-900">Ngày mai - Ca Chiều</h4>
                                                <p class="text-slate-400 text-[10px] font-bold uppercase tracking-widest mt-1">Pool Side Bar</p>
                                            </div>
                                        </div>
                                        <p class="text-slate-500 font-medium italic">"Vui lòng chuẩn bị đồng phục cho ca trực ngoài trời."</p>
                                    </div>
                                    <div class="mt-8 flex justify-between items-center">
                                        <span class="text-[10px] font-black text-indigo-600 uppercase tracking-widest">Sắp diễn ra</span>
                                        <span class="font-black text-slate-900">15:00 - 23:00</span>
                                    </div>
                                </div>
                            </div>

                            <!-- Manager View -->
                            <div id="manager-schedule-view" class="hidden bg-white rounded-[48px] border border-slate-100 shadow-sm overflow-hidden">
                                <div class="p-10 bg-slate-50 border-b border-slate-100 flex justify-between items-center">
                                    <div>
                                        <h3 class="font-black text-slate-900 uppercase text-xs tracking-widest">Điều phối nhân sự tuần 24</h3>
                                        <p class="text-[10px] text-slate-400 font-bold mt-1">Tổng cộng 42 nhân viên đang hoạt động</p>
                                    </div>
                                    <button class="bg-indigo-600 text-white px-8 py-3 rounded-2xl text-[10px] font-black uppercase shadow-lg shadow-indigo-600/20 hover:bg-indigo-700 transition-all">Gán ca trực mới</button>
                                </div>
                                <table class="w-full text-left">
                                    <thead class="bg-slate-50">
                                        <tr>
                                            <th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Nhân sự</th>
                                            <th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Thời gian</th>
                                            <th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Phòng ban</th>
                                            <th class="px-10 py-6 text-[10px] font-black text-slate-400 uppercase tracking-widest text-right">Hành động</th>
                                        </tr>
                                    </thead>
                                    <tbody class="divide-y divide-slate-50">
                                        <tr class="hover:bg-slate-50 transition-colors">
                                            <td class="px-10 py-8">
                                                <div class="flex items-center gap-4">
                                                    <div class="w-10 h-10 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 font-black text-xs">VA</div>
                                                    <span class="font-bold text-slate-900">Nguyễn Văn An</span>
                                                </div>
                                            </td>
                                            <td class="px-10 py-8 text-sm text-slate-600 font-medium">Ca Sáng (07:00 - 15:00)</td>
                                            <td class="px-10 py-8"><span class="bg-blue-50 text-blue-600 px-4 py-1.5 rounded-full text-[10px] font-black uppercase">Tiền sảnh</span></td>
                                            <td class="px-10 py-8 text-right"><button class="text-indigo-600 font-black text-[10px] uppercase hover:underline">Điều chỉnh</button></td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </section>

                        <!-- Section: Stats -->
                        <section id="stats-section" >
                            <div class="mb-12"><h1 class="text-3xl font-black text-slate-900">Thống kê Doanh thu</h1></div>
                            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                                <div class="lg:col-span-2 bg-white p-12 rounded-[56px] border border-slate-100 shadow-sm">
                                    <h2 class="text-slate-400 text-[10px] font-black uppercase tracking-widest mb-4">Doanh thu tháng này</h2>
                                    <p class="text-6xl font-black text-indigo-600 mb-10">420,500,000đ</p>
                                    <div class="h-48 bg-slate-50 rounded-[32px] flex items-end justify-between p-8 gap-2">
                                        <div class="w-full bg-indigo-100 rounded-t-xl h-1/4"></div>
                                        <div class="w-full bg-indigo-200 rounded-t-xl h-2/4"></div>
                                        <div class="w-full bg-indigo-300 rounded-t-xl h-1/4"></div>
                                        <div class="w-full bg-indigo-400 rounded-t-xl h-3/4"></div>
                                        <div class="w-full bg-indigo-600 rounded-t-xl h-full"></div>
                                    </div>
                                </div>
                                <div class="bg-slate-900 p-12 rounded-[56px] text-white shadow-2xl flex flex-col justify-between">
                                    <div>
                                        <h3 class="font-black text-2xl mb-2">Tăng trưởng</h3>
                                        <p class="text-slate-500 text-xs font-medium">So với tháng trước</p>
                                    </div>
                                    <div class="text-center">
                                        <p class="text-7xl font-black text-emerald-400">+12%</p>
                                    </div>
                                    <button class="w-full py-5 bg-white/10 hover:bg-white/20 transition-all rounded-2xl font-black text-xs uppercase tracking-widest">Xuất báo cáo PDF</button>
                                </div>
                            </div>
                        </section>

                        <!-- Section: Manage -->
                        <section id="manage-section" >
                            <div class="mb-12"><h1 class="text-3xl font-black text-slate-900">Cấu hình Hệ thống</h1></div>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-10">
                                <div class="bg-white p-12 rounded-[56px] border border-slate-100 shadow-sm card-hover">
                                    <div class="w-16 h-16 bg-indigo-50 text-indigo-600 rounded-3xl flex items-center justify-center mb-8">
                                        <i data-lucide="layout-grid" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-2xl font-black text-slate-900 mb-4">Quản lý Phòng</h3>
                                    <p class="text-slate-500 text-sm font-medium mb-10">Thêm, sửa, xóa hoặc cập nhật trạng thái các phòng nghỉ trong resort.</p>
                                    <button class="w-full py-5 bg-slate-900 text-white rounded-[24px] font-black text-xs uppercase tracking-widest">Mở trình quản lý</button>
                                </div>
                                <div class="bg-white p-12 rounded-[56px] border border-slate-100 shadow-sm card-hover">
                                    <div class="w-16 h-16 bg-amber-50 text-amber-600 rounded-3xl flex items-center justify-center mb-8">
                                        <i data-lucide="layers" class="w-8 h-8"></i>
                                    </div>
                                    <h3 class="text-2xl font-black text-slate-900 mb-4">Quản lý Dịch vụ</h3>
                                    <p class="text-slate-500 text-sm font-medium mb-10">Cập nhật danh mục dịch vụ, giá cả và khuyến mãi đi kèm.</p>
                                    <button class="w-full py-5 border-2 border-slate-100 hover:border-indigo-600 text-slate-900 rounded-[24px] font-black text-xs uppercase tracking-widest transition-all">Mở trình quản lý</button>
                                </div>
                            </div>
                        </section>

                        <!-- Section: Account -->
                        <section id="account-section" >
                            <div class="max-w-3xl mx-auto">
                                <div class="text-center mb-12">
                                    <div class="w-32 h-32 bg-indigo-600 rounded-[40px] flex items-center justify-center text-white text-4xl font-black mx-auto mb-6 shadow-2xl shadow-indigo-600/30 transform -rotate-6">VA</div>
                                    <h1 class="text-4xl font-black text-slate-900 mb-2">Hồ sơ cá nhân</h1>
                                    <p class="text-slate-500 font-medium">Quản lý thông tin tài khoản và bảo mật của bạn.</p>
                                </div>

                                <!-- Account Info Cards -->
                                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
                                    <div class="bg-white p-8 rounded-[48px] border border-slate-100 shadow-sm relative overflow-hidden group">
                                        <div class="absolute top-0 right-0 w-24 h-24 bg-indigo-50 rounded-bl-full -z-10 transition-all group-hover:scale-110"></div>
                                        <div class="flex items-center gap-4 mb-6">
                                            <div class="w-10 h-10 bg-white shadow-sm border border-slate-100 rounded-xl flex items-center justify-center text-indigo-600">
                                                <i data-lucide="user" class="w-5 h-5"></i>
                                            </div>
                                            <h3 class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Họ và tên</h3>
                                        </div>
                                        <p id="display-fullname" class="text-xl font-black text-slate-900">Nguyễn Văn An</p>
                                    </div>

                                    <div class="bg-white p-8 rounded-[48px] border border-slate-100 shadow-sm relative overflow-hidden group">
                                        <div class="absolute top-0 right-0 w-24 h-24 bg-emerald-50 rounded-bl-full -z-10 transition-all group-hover:scale-110"></div>
                                        <div class="flex items-center gap-4 mb-6">
                                            <div class="w-10 h-10 bg-white shadow-sm border border-slate-100 rounded-xl flex items-center justify-center text-emerald-600">
                                                <i data-lucide="mail" class="w-5 h-5"></i>
                                            </div>
                                            <h3 class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Địa chỉ Gmail</h3>
                                        </div>
                                        <p id="display-email" class="text-xl font-black text-slate-900">an.nguyen@luxestay.vn</p>
                                    </div>

                                    <div class="bg-white p-8 rounded-[48px] border border-slate-100 shadow-sm relative overflow-hidden group">
                                        <div class="absolute top-0 right-0 w-24 h-24 bg-amber-50 rounded-bl-full -z-10 transition-all group-hover:scale-110"></div>
                                        <div class="flex items-center gap-4 mb-6">
                                            <div class="w-10 h-10 bg-white shadow-sm border border-slate-100 rounded-xl flex items-center justify-center text-amber-600">
                                                <i data-lucide="phone" class="w-5 h-5"></i>
                                            </div>
                                            <h3 class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Số điện thoại</h3>
                                        </div>
                                        <p id="display-phone" class="text-xl font-black text-slate-900">0987 654 321</p>
                                    </div>

                                    <div class="bg-white p-8 rounded-[48px] border border-slate-100 shadow-sm relative overflow-hidden group">
                                        <div class="absolute top-0 right-0 w-24 h-24 bg-rose-50 rounded-bl-full -z-10 transition-all group-hover:scale-110"></div>
                                        <div class="flex items-center gap-4 mb-6">
                                            <div class="w-10 h-10 bg-white shadow-sm border border-slate-100 rounded-xl flex items-center justify-center text-rose-600">
                                                <i data-lucide="lock" class="w-5 h-5"></i>
                                            </div>
                                            <h3 class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Mật khẩu</h3>
                                        </div>
                                        <p class="text-xl font-black text-slate-900 tracking-[0.3em]">••••••••</p>
                                    </div>
                                </div>

                                <!-- Action Buttons -->
                                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <button id="open-edit-profile-btn" class="flex items-center justify-center gap-3 bg-slate-900 text-white py-6 rounded-[32px] font-black text-sm uppercase tracking-widest shadow-xl shadow-slate-900/20 hover:bg-indigo-600 transition-all">
                                        <i data-lucide="edit-3" class="w-5 h-5"></i> Chỉnh sửa hồ sơ
                                    </button>
                                    <button id="open-change-password-btn" class="flex items-center justify-center gap-3 bg-white border-2 border-slate-100 text-slate-900 py-6 rounded-[32px] font-black text-sm uppercase tracking-widest hover:border-indigo-600 hover:text-indigo-600 transition-all">
                                        <i data-lucide="key" class="w-5 h-5"></i> Đổi mật khẩu
                                    </button>
                                </div>
                            </div>
                        </section>
                    </div>
                </main>
            </div>
        </div>

        <!-- MODAL CHỈNH SỬA HỒ SƠ -->
        <div id="edit-profile-modal" class="modal">
            <div class="bg-white w-full max-w-xl rounded-[56px] p-12 relative shadow-2xl">
                <button class="close-modal absolute top-8 right-8 text-slate-400 hover:text-slate-900"><i data-lucide="x" class="w-8 h-8"></i></button>
                <h2 class="text-3xl font-black text-slate-900 mb-8">Chỉnh sửa hồ sơ</h2>
                <form id="form-edit-profile" class="space-y-6">
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Họ và tên mới</label>
                        <input type="text" id="edit-fullname" placeholder="Nhập tên mới" class="w-full bg-slate-50 border border-slate-100 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-bold">
                    </div>
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Gmail mới</label>
                        <input type="email" id="edit-email" placeholder="example@luxestay.vn" class="w-full bg-slate-50 border border-slate-100 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-bold">
                    </div>
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Số điện thoại mới</label>
                        <input type="tel" id="edit-phone" placeholder="0xxx xxx xxx" class="w-full bg-slate-50 border border-slate-100 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-bold">
                    </div>
                    <button type="submit" class="w-full bg-indigo-600 text-white py-5 rounded-[24px] font-black shadow-xl shadow-indigo-600/20 mt-4">Lưu thay đổi</button>
                </form>
            </div>
        </div>

        <!-- MODAL ĐỔI MẬT KHẨU -->
        <div id="change-password-modal" class="modal">
            <div class="bg-white w-full max-w-xl rounded-[56px] p-12 relative shadow-2xl">
                <button class="close-modal absolute top-8 right-8 text-slate-400 hover:text-slate-900"><i data-lucide="x" class="w-8 h-8"></i></button>
                <h2 class="text-3xl font-black text-slate-900 mb-8">Đổi mật khẩu</h2>
                <form id="form-change-password" class="space-y-6">
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Mật khẩu cũ</label>
                        <input type="password" id="old-password" placeholder="••••••••" required class="w-full bg-slate-50 border border-slate-100 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-bold">
                    </div>
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Mật khẩu mới</label>
                        <input type="password" id="new-password" placeholder="••••••••" required class="w-full bg-slate-50 border border-slate-100 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-bold">
                    </div>
                    <div class="space-y-2">
                        <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Nhập lại mật khẩu mới</label>
                        <input type="password" id="confirm-new-password" placeholder="••••••••" required class="w-full bg-slate-50 border border-slate-100 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-bold">
                    </div>
                    <button type="submit" class="w-full bg-rose-600 text-white py-5 rounded-[24px] font-black shadow-xl shadow-rose-600/20 mt-4">Xác nhận đổi mật khẩu</button>
                </form>
            </div>
        </div>

        <!-- MODAL CHI TIẾT PHÒNG -->
        <div id="room-modal" class="modal">
            <div class="bg-white w-full max-w-5xl rounded-[56px] overflow-hidden p-0 relative shadow-2xl flex flex-col md:flex-row h-[80vh]">
                <button class="close-modal absolute top-8 right-8 z-20 w-12 h-12 bg-white/20 backdrop-blur-md hover:bg-white/40 rounded-full flex items-center justify-center transition-all">
                    <i data-lucide="x" class="w-8 h-8 text-white"></i>
                </button>
                <div class="w-full md:w-1/2 h-full">
                    <img src="https://images.unsplash.com/photo-1611892440504-42a792e24d32?auto=format&fit=crop&q=80&w=800" class="w-full h-full object-cover">
                </div>
                <div class="w-full md:w-1/2 p-16 flex flex-col justify-between overflow-y-auto custom-scrollbar">
                    <div>
                        <span class="text-[10px] font-black uppercase text-indigo-600 tracking-widest mb-4 block">Ocean Suite</span>
                        <h2 class="text-4xl font-black text-slate-900 mb-6">Ocean Premier King</h2>
                        <p class="text-slate-500 font-medium leading-relaxed mb-10">Đắm chìm trong không gian nghỉ dưỡng vô cực với tầm nhìn panorama hướng trọn vịnh biển. Phòng được trang bị nội thất gỗ óc chó cao cấp và bồn tắm thủy lực riêng biệt.</p>

                        <div class="grid grid-cols-2 gap-6 mb-12">
                            <div class="flex items-center gap-4">
                                <i data-lucide="coffee" class="w-5 h-5 text-slate-400"></i>
                                <span class="text-xs font-bold text-slate-600">Buffet Sáng Free</span>
                            </div>
                            <div class="flex items-center gap-4">
                                <i data-lucide="wind" class="w-5 h-5 text-slate-400"></i>
                                <span class="text-xs font-bold text-slate-600">Điều hòa trung tâm</span>
                            </div>
                        </div>
                    </div>

                    <div class="pt-10 border-t border-slate-100 flex items-center justify-between gap-8">
                        <div>
                            <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest">Tổng phí/Đêm</p>
                            <p class="text-3xl font-black text-indigo-600">2.500.000đ</p>
                        </div>
                        <button class="flex-1 bg-slate-900 text-white py-6 rounded-[28px] font-black hover:bg-indigo-600 transition-all shadow-xl shadow-slate-900/10">Đặt ngay</button>
                    </div>
                </div>
            </div>
        </div>

        <script src="<%= request.getContextPath() %>/Front-end/assets/js/index.js"></script>
    </body>
</html>



