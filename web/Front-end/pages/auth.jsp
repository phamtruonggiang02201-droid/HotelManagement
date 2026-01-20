<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác thực - LuxeStay</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://unpkg.com/lucide@latest"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: white; color: #1e293b; }
        .auth-card { animation: fadeInUp 0.5s cubic-bezier(0.22, 1, 0.36, 1); }
        @keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
    </style>
</head>
<body class="overflow-hidden">
    <div class="flex min-h-screen">
        <!-- Cột trái: Hình ảnh -->
        <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden">
            <img src="https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&q=80&w=1600" class="w-full h-full object-cover">
            <div class="absolute inset-0 bg-indigo-900/40 backdrop-blur-[2px] p-20 flex flex-col justify-end text-white">
                <div class="flex items-center gap-4 mb-8">
                    <div class="w-14 h-14 bg-white rounded-2xl flex items-center justify-center shadow-2xl">
                        <i data-lucide="crown" class="w-8 h-8 text-indigo-600"></i>
                    </div>
                    <h2 class="text-4xl font-black tracking-tight">LuxeStay</h2>
                </div>
                <h3 class="text-5xl font-extrabold leading-tight mb-6">Trải nghiệm sự <br>khác biệt từ đẳng cấp.</h3>
            </div>
        </div>

        <!-- Cột phải: Biểu mẫu -->
        <div class="w-full lg:w-1/2 flex items-center justify-center p-8 bg-slate-50 relative">
            <a href="index.jsp" class="absolute top-8 left-8 flex items-center gap-2 text-slate-400 hover:text-slate-900 transition-all font-bold text-sm uppercase tracking-widest">
                <i data-lucide="arrow-left" class="w-5 h-5"></i> Quay lại trang chủ
            </a>
            
            <div class="w-full max-w-md auth-card">
                <!-- Login Form -->
                <div id="login-form">
                    <div class="mb-10">
                        <h1 class="text-4xl font-black text-slate-900 mb-3">Đăng nhập</h1>
                        <p class="text-slate-500 font-medium">Bắt đầu quản trị và đặt phòng cao cấp.</p>
                    </div>
                    <form id="form-signin" class="space-y-6">
                        <div class="space-y-2">
                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Email</label>
                            <input type="email" placeholder="example@luxestay.com" required class="w-full bg-white border border-slate-200 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 transition-all font-medium">
                        </div>
                        <div class="space-y-2">
                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Mật khẩu</label>
                            <input type="password" placeholder="••••••••" required class="w-full bg-white border border-slate-200 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 transition-all font-medium">
                        </div>
                        <button type="submit" class="w-full bg-indigo-600 text-white py-5 rounded-2xl font-black shadow-xl shadow-indigo-600/20 hover:bg-indigo-700 transition-all transform hover:-translate-y-1">Vào hệ thống</button>
                    </form>
                    <div class="mt-10 text-center">
                        <p class="text-slate-500 font-medium">Bạn là thành viên mới? <button id="go-to-register" class="text-indigo-600 font-black hover:underline">Đăng ký ngay</button></p>
                    </div>
                </div>

                <!-- Register Form -->
                <div id="register-form" class="hidden">
                    <div class="mb-10">
                        <h1 class="text-4xl font-black text-slate-900 mb-3">Tạo tài khoản</h1>
                    </div>
                    <form id="form-signup" class="space-y-5">
                        <div class="space-y-2">
                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Tên đầy đủ</label>
                            <input type="text" placeholder="Nguyễn Văn An" required class="w-full bg-white border border-slate-200 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-medium">
                        </div>
                        <div class="space-y-2">
                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Email</label>
                            <input type="email" placeholder="example@luxestay.com" required class="w-full bg-white border border-slate-200 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-medium">
                        </div>
                        <div class="space-y-2">
                            <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Mật khẩu</label>
                            <input type="password" placeholder="••••••••" required class="w-full bg-white border border-slate-200 p-5 rounded-2xl outline-none focus:ring-4 focus:ring-indigo-100 font-medium">
                        </div>
                        <button type="submit" class="w-full bg-slate-900 text-white py-5 rounded-2xl font-black shadow-xl shadow-slate-900/20 hover:bg-indigo-600 transition-all transform hover:-translate-y-1">Hoàn tất đăng ký</button>
                    </form>
                    <div class="mt-10 text-center">
                        <p class="text-slate-500 font-medium">Đã có tài khoản? <button id="go-to-login" class="text-indigo-600 font-black hover:underline">Đăng nhập</button></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="<%= request.getContextPath() %>/Front-end/assets/js/auth.js"></script>
</body>
</html>
