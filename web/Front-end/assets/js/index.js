
/**
 * LuxeStay Hotel Management System
 * Enhanced Vanilla JavaScript implementation
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Khởi tạo Lucide Icons
    const refreshIcons = () => {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    };
    refreshIcons();

    // 2. Logic điều hướng Section (SPA)
    const setupNavigation = () => {
        const navLinks = document.querySelectorAll('.nav-link');
        const sections = document.querySelectorAll('main section[id]');

        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                const targetId = link.getAttribute('data-target');
                if (!targetId) return;

                e.preventDefault();

                // Cập nhật trạng thái Active trên Sidebar
                navLinks.forEach(l => l.classList.remove('sidebar-item-active'));
                link.classList.add('sidebar-item-active');

                // Chuyển đổi hiển thị Section
                sections.forEach(sec => {
                    sec.classList.remove('active');
                    if (sec.id === targetId) {
                        sec.classList.add('active');
                    }
                });

                // Cuộn mượt lên đầu trang và làm mới icon
                window.scrollTo({ top: 0, behavior: 'smooth' });
                refreshIcons();
            });
        });
    };

    // 3. Logic Modal (Chi tiết phòng)
    const setupModals = () => {
        const modal = document.getElementById('room-modal');
        const detailBtns = document.querySelectorAll('.room-detail-btn');
        const closeBtns = document.querySelectorAll('.close-modal');

        detailBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                modal.classList.add('active');
            });
        });

        closeBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                modal.classList.remove('active');
            });
        });

        // Đóng khi click ra ngoài
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    };

    // 4. Logic Lịch làm việc (Switch Staff/Manager View)
    const setupScheduleViews = () => {
        const staffBtn = document.getElementById('view-staff-btn');
        const managerBtn = document.getElementById('view-manager-btn');
        const staffView = document.getElementById('staff-schedule-view');
        const managerView = document.getElementById('manager-schedule-view');

        if (staffBtn && managerBtn) {
            staffBtn.addEventListener('click', () => {
                staffBtn.className = "px-6 py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold transition-all";
                managerBtn.className = "px-6 py-2.5 text-slate-400 hover:text-slate-900 rounded-xl text-xs font-bold transition-all";
                staffView.classList.remove('hidden');
                managerView.classList.add('hidden');
                refreshIcons();
            });

            managerBtn.addEventListener('click', () => {
                managerBtn.className = "px-6 py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold transition-all";
                staffBtn.className = "px-6 py-2.5 text-slate-400 hover:text-slate-900 rounded-xl text-xs font-bold transition-all";
                managerView.classList.remove('hidden');
                staffView.classList.add('hidden');
                refreshIcons();
            });
        }
    };

    // 5. Logic Quản lý Phòng & Dịch vụ (CRUD View Switch)
    const setupManageViews = () => {
        const roomsBtn = document.getElementById('manage-rooms-btn');
        const servicesBtn = document.getElementById('manage-services-btn');
        const roomsView = document.getElementById('manage-rooms-view');
        const servicesView = document.getElementById('manage-services-view');

        if (roomsBtn && servicesBtn) {
            roomsBtn.addEventListener('click', () => {
                roomsBtn.className = "px-8 py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold transition-all";
                servicesBtn.className = "px-8 py-2.5 text-slate-400 hover:text-slate-900 rounded-xl text-xs font-bold transition-all";
                roomsView.classList.remove('hidden');
                servicesView.classList.add('hidden');
                refreshIcons();
            });

            servicesBtn.addEventListener('click', () => {
                servicesBtn.className = "px-8 py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold transition-all";
                roomsBtn.className = "px-8 py-2.5 text-slate-400 hover:text-slate-900 rounded-xl text-xs font-bold transition-all";
                servicesView.classList.remove('hidden');
                roomsView.classList.add('hidden');
                refreshIcons();
            });
        }
    };

    // 6. Logic Giới tính (Account Form)
    const setupGenderSelection = () => {
        const genderBtns = document.querySelectorAll('#account-section .flex.gap-4 button');
        genderBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                genderBtns.forEach(b => {
                    b.className = "flex-1 py-3.5 text-slate-400 font-black text-xs hover:text-slate-600";
                });
                btn.className = "flex-1 py-3.5 bg-white shadow-sm rounded-[20px] font-black text-xs text-indigo-600";
            });
        });
    };

    // 7. Logic Đánh giá sao (Feedback Star Rating)
    const setupStarRating = () => {
        const stars = document.querySelectorAll('.star-btn');
        stars.forEach(star => {
            star.addEventListener('click', () => {
                const rating = parseInt(star.getAttribute('data-rating'));
                
                stars.forEach((s, index) => {
                    if (index < rating) {
                        s.classList.add('star-active');
                    } else {
                        s.classList.remove('star-active');
                    }
                });
            });
        });
    };

    // Khởi chạy
    setupNavigation();
    setupModals();
    setupScheduleViews();
    setupManageViews();
    setupGenderSelection();
    setupStarRating();
});
