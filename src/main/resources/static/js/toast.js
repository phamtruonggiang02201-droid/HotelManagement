// TOAST SERVICE
window.toastService = {
    create(type, title, message) {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast-item toast-${type}`;

        const icons = {
            success: 'check-circle',
            error: 'alert-circle',
            info: 'info'
        };

        toast.innerHTML = `
            <div class="toast-icon">
                <i data-lucide="${icons[type]}" class="w-6 h-6"></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">${title}</div>
                <div class="toast-message">${message}</div>
            </div>
        `;

        container.appendChild(toast);
        if (window.lucide) {
            lucide.createIcons();
        }

        // Show Animation
        requestAnimationFrame(() => toast.classList.add('show'));

        // Auto remove
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 500);
        }, 5000);
    },
    success(message) { this.create('success', 'Thành công', message); },
    error(message) { this.create('error', 'Thất bại', message); },
    info(message) { this.create('info', 'Thông báo', message); }
};
