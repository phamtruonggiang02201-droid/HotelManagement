/**
 * Account Detail Page Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    const mode = document.getElementById('form-mode')?.value;
    const accountId = document.getElementById('account-id')?.value;

    if (mode === 'edit' && accountId) {
        loadAccountData(accountId);
    }

    document.getElementById('account-form')?.addEventListener('submit', handleFormSubmit);
});

async function loadAccountData(id) {
    try {
        const response = await fetch(`/management/api/accounts/${id}`);
        if (!response.ok) {
            throw new Error('Không thể tải thông tin tài khoản');
        }
        const account = await response.json();
        populateForm(account);
    } catch (error) {
        console.error('Error loading account:', error);
        if (typeof toastService !== 'undefined') {
            toastService.error('Không thể tải thông tin tài khoản');
        }
    }
}

function populateForm(account) {
    document.getElementById('fullName').value = account.fullName || '';
    document.getElementById('email').value = account.email || '';
    document.getElementById('username').value = account.username || '';
    document.getElementById('roleName').value = account.roleName || '';
    document.getElementById('phone').value = account.phone || '';
    // Password is intentionally left blank for security
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const mode = document.getElementById('form-mode')?.value;
    const accountId = document.getElementById('account-id')?.value;

    const formData = {
        fullName: document.getElementById('fullName').value,
        email: document.getElementById('email').value,
        username: document.getElementById('username').value,
        password: document.getElementById('password').value,
        roleName: document.getElementById('roleName').value,
        phone: document.getElementById('phone').value,
    };

    const url = mode === 'create' ? '/management/api/accounts' : `/management/api/accounts/${accountId}`;
    const method = mode === 'create' ? 'POST' : 'PUT';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData),
        });
        const data = await response.json();

        if (response.ok) {
            if (typeof toastService !== 'undefined') {
                toastService.success(data.message);
            }
            // Redirect to list after a short delay
            setTimeout(() => {
                window.location.href = '/management/accounts';
            }, 1500);
        } else {
            if (typeof toastService !== 'undefined') {
                toastService.error(data.message || 'Có lỗi xảy ra');
            }
        }
    } catch (error) {
        console.error('Error saving account:', error);
        if (typeof toastService !== 'undefined') {
            toastService.error('Có lỗi xảy ra khi lưu tài khoản');
        }
    }
}
