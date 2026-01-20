
/**
 * LuxeStay Authentication Logic
 * Handling Login/Register page
 */

document.addEventListener('DOMContentLoaded', () => {
    // Khởi tạo icons
    if (window.lucide) {
        window.lucide.createIcons();
    }

    const loginFormContainer = document.getElementById('login-form');
    const registerFormContainer = document.getElementById('register-form');
    
    const goToRegisterBtn = document.getElementById('go-to-register');
    const goToLoginBtn = document.getElementById('go-to-login');
    
    const signInForm = document.getElementById('form-signin');
    const signUpForm = document.getElementById('form-signup');

    // Kiểm tra query param để biết mở tab nào
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('mode') === 'register') {
        loginFormContainer.classList.add('hidden');
        registerFormContainer.classList.remove('hidden');
    }

    // Chuyển đổi giữa Login và Register
    goToRegisterBtn.addEventListener('click', () => {
        loginFormContainer.classList.add('hidden');
        registerFormContainer.classList.remove('hidden');
    });

    goToLoginBtn.addEventListener('click', () => {
        registerFormContainer.classList.add('hidden');
        loginFormContainer.classList.remove('hidden');
    });

    // Xử lý Đăng nhập
    signInForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const submitBtn = e.target.querySelector('button');
        const originalText = submitBtn.innerText;
        
        submitBtn.disabled = true;
        submitBtn.innerText = "Đang xác thực...";

        // Giả lập API call
        setTimeout(() => {
            localStorage.setItem('isLoggedIn', 'true');
            window.location.href = 'index.html';
        }, 1000);
    });

    // Xử lý Đăng ký
    signUpForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const submitBtn = e.target.querySelector('button');
        submitBtn.disabled = true;
        submitBtn.innerText = "Đang khởi tạo...";

        setTimeout(() => {
            alert("Đăng ký thành công! Hãy dùng tài khoản này để đăng nhập.");
            registerFormContainer.classList.add('hidden');
            loginFormContainer.classList.remove('hidden');
            submitBtn.disabled = false;
            submitBtn.innerText = "Hoàn tất đăng ký";
        }, 1200);
    });
});
