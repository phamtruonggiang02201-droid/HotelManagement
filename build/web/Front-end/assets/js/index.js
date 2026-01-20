
/**
 * LuxeStay Hotel Management System
 * Enhanced Main Logic for index.html
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- TRẠNG THÁI ---
    let isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

    // --- DỮ LIỆU GIẢ LẬP TÀI KHOẢN ---
    let userProfile = {
        fullname: "Nguyễn Văn An",
        email: "an.nguyen@luxestay.vn",
        phone: "0987 654 321",
        password: "password123"
    };

    // --- KHỞI TẠO ---
    const refreshIcons = () => {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    };
    refreshIcons();

    // --- CẬP NHẬT UI ---
    const updateUI = () => {
        if (isLoggedIn) {
            document.body.classList.add('logged-in');
            renderProfile();
        } else {
            document.body.classList.remove('logged-in');
            const activeSection = document.querySelector('section.active');
            if (activeSection && activeSection.classList.contains('auth-only')) {
                navigateTo('home-section');
            }
        }
        refreshIcons();
    };

    // --- RENDER THÔNG TIN TÀI KHOẢN ---
    const renderProfile = () => {
        const displayFullname = document.getElementById('display-fullname');
        const displayEmail = document.getElementById('display-email');
        const displayPhone = document.getElementById('display-phone');
        
        if (displayFullname) displayFullname.innerText = userProfile.fullname;
        if (displayEmail) displayEmail.innerText = userProfile.email;
        if (displayPhone) displayPhone.innerText = userProfile.phone;
    };

    // --- ĐIỀU HƯỚNG ---
    const navigateTo = (targetId) => {
        const sections = document.querySelectorAll('main section[id]');
        const navLinks = document.querySelectorAll('.nav-link');

        const targetSection = document.getElementById(targetId);
        if (targetSection && targetSection.classList.contains('auth-only') && !isLoggedIn) {
            window.location.href = 'auth.html';
            return;
        }

        navLinks.forEach(l => {
            l.classList.remove('sidebar-item-active');
            if (l.getAttribute('data-target') === targetId) {
                l.classList.add('sidebar-item-active');
            }
        });

        sections.forEach(sec => {
            sec.classList.remove('active');
            if (sec.id === targetId) {
                sec.classList.add('active');
            }
        });

        window.scrollTo({ top: 0, behavior: 'smooth' });
        refreshIcons();
    };

    const setupNavigation = () => {
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const targetId = link.getAttribute('data-target');
                if (targetId) navigateTo(targetId);
            });
        });
    };

    // --- XỬ LÝ ĐĂNG XUẤT ---
    const setupLogout = () => {
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => {
                if (confirm("Bạn có chắc chắn muốn đăng xuất?")) {
                    localStorage.removeItem('isLoggedIn');
                    isLoggedIn = false;
                    updateUI();
                    navigateTo('home-section');
                    window.location.reload();
                }
            });
        }
    };

    // --- LOGIC LỊCH LÀM VIỆC ---
    const setupScheduleViews = () => {
        const staffBtn = document.getElementById('view-staff-btn');
        const managerBtn = document.getElementById('view-manager-btn');
        const staffView = document.getElementById('staff-schedule-view');
        const managerView = document.getElementById('manager-schedule-view');

        if (staffBtn && managerBtn) {
            staffBtn.addEventListener('click', () => {
                staffBtn.classList.add('bg-indigo-600', 'text-white');
                staffBtn.classList.remove('text-slate-400');
                managerBtn.classList.remove('bg-indigo-600', 'text-white');
                managerBtn.classList.add('text-slate-400');
                staffView.classList.remove('hidden');
                managerView.classList.add('hidden');
                refreshIcons();
            });

            managerBtn.addEventListener('click', () => {
                managerBtn.classList.add('bg-indigo-600', 'text-white');
                managerBtn.classList.remove('text-slate-400');
                staffBtn.classList.remove('bg-indigo-600', 'text-white');
                staffBtn.classList.add('text-slate-400');
                managerView.classList.remove('hidden');
                staffView.classList.add('hidden');
                refreshIcons();
            });
        }
    };

    // --- LOGIC MODALS & HỒ SƠ ---
    const setupAccountModals = () => {
        const editProfileModal = document.getElementById('edit-profile-modal');
        const changePasswordModal = document.getElementById('change-password-modal');

        // Mở Modal Sửa Profile
        document.getElementById('open-edit-profile-btn')?.addEventListener('click', () => {
            document.getElementById('edit-fullname').value = userProfile.fullname;
            document.getElementById('edit-email').value = userProfile.email;
            document.getElementById('edit-phone').value = userProfile.phone;
            editProfileModal.classList.add('active');
        });

        // Mở Modal Đổi Mật Khẩu
        document.getElementById('open-change-password-btn')?.addEventListener('click', () => {
            document.getElementById('form-change-password').reset();
            changePasswordModal.classList.add('active');
        });

        // Xử lý lưu Profile
        document.getElementById('form-edit-profile')?.addEventListener('submit', (e) => {
            e.preventDefault();
            userProfile.fullname = document.getElementById('edit-fullname').value;
            userProfile.email = document.getElementById('edit-email').value;
            userProfile.phone = document.getElementById('edit-phone').value;
            
            alert("Cập nhật hồ sơ thành công!");
            editProfileModal.classList.remove('active');
            renderProfile();
        });

        // Xử lý đổi Mật khẩu
        document.getElementById('form-change-password')?.addEventListener('submit', (e) => {
            e.preventDefault();
            const oldPass = document.getElementById('old-password').value;
            const newPass = document.getElementById('new-password').value;
            const confirmPass = document.getElementById('confirm-new-password').value;

            if (oldPass !== userProfile.password) {
                alert("Mật khẩu cũ không chính xác!");
                return;
            }
            if (newPass !== confirmPass) {
                alert("Mật khẩu mới nhập lại không khớp!");
                return;
            }

            userProfile.password = newPass;
            alert("Đổi mật khẩu thành công!");
            changePasswordModal.classList.remove('active');
        });

        // Đóng modals
        document.addEventListener('click', (e) => {
            if (e.target.closest('.close-modal')) {
                const modal = e.target.closest('.modal');
                if (modal) modal.classList.remove('active');
            }
        });
    };

    const setupGeneralModals = () => {
        const roomModal = document.getElementById('room-modal');
        document.addEventListener('click', (e) => {
            if (e.target.closest('.room-detail-btn')) {
                roomModal.classList.add('active');
            }
            if (e.target.closest('.close-modal') || e.target === roomModal) {
                const modal = e.target.closest('.modal') || e.target;
                if (modal && modal.id === 'room-modal') modal.classList.remove('active');
            }
        });
    };

    const setupStarRating = () => {
        const stars = document.querySelectorAll('.star-btn');
        stars.forEach(star => {
            star.addEventListener('click', () => {
                const rating = parseInt(star.getAttribute('data-rating'));
                stars.forEach((s, index) => {
                    if (index < rating) s.classList.add('star-active');
                    else s.classList.remove('star-active');
                });
            });
        });
    };

    // --- KHỞI CHẠY ---
    setupNavigation();
    setupLogout();
    setupAccountModals();
    setupGeneralModals();
    setupStarRating();
    setupScheduleViews();
    updateUI();
});
