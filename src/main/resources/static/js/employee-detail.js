document.getElementById('employee-form').addEventListener('submit', function (e) {
    e.preventDefault();

    const id = document.getElementById('employee-id').value;
    const mode = document.getElementById('form-mode').value;
    const formData = {
        fullName: document.getElementById('fullName').value,
        email: document.getElementById('email').value,
        username: document.getElementById('username').value,
        roleName: document.getElementById('roleName').value,
        jobTitle: document.getElementById('jobTitle').value,
        phone: document.getElementById('phone').value,
        dob: document.getElementById('dob').value,
        address: document.getElementById('address').value,
        idNumber: document.getElementById('idNumber').value,
        idType: document.getElementById('idType').value,
        nationality: document.getElementById('nationality').value
    };

    // Add status if edit mode
    if (mode === 'edit') {
        formData.status = document.getElementById('status').checked;
        
        const password = document.getElementById('password').value;
        if (password) formData.password = password;
    }

    const url = mode === 'create' ? '/management/employees/api' : `/management/employees/api/${id}`;
    const method = mode === 'create' ? 'POST' : 'PUT';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
    })
        .then(res => res.json())
        .then(res => {
            if (res.message.includes('thành công')) {
                toastService.success(res.message);
                setTimeout(() => {
                    window.location.href = '/management/employees';
                }, 1000);
            } else {
                toastService.error(res.message);
            }
        })
        .catch(err => {
            console.error(err);
            toastService.error('Đã xảy ra lỗi!');
        });
});

// Load existing data if in edit mode
document.addEventListener('DOMContentLoaded', () => {
    const mode = document.getElementById('form-mode').value;
    const id = document.getElementById('employee-id').value;
    const jobTitleSelect = document.getElementById('jobTitle');
    const roleNameSelect = document.getElementById('roleName');

    // Reusable function to map Job Title to Role
    function syncRole(jobTitle) {
        if (jobTitle === 'Lễ tân' || jobTitle === 'Lễ tân trưởng') {
            roleNameSelect.value = 'RECEPTION';
        } else if (jobTitle === 'Quản lý') {
            roleNameSelect.value = 'MANAGER';
        } else if (jobTitle === 'Quản trị viên') {
            roleNameSelect.value = 'ADMIN';
        }
    }

    // Auto-map Job Title to Role on change
    jobTitleSelect.addEventListener('change', function () {
        syncRole(this.value);
    });

    if (mode === 'edit' && id) {
        fetch(`/management/api/accounts/${id}`)
            .then(res => res.json())
            .then(data => {
                document.getElementById('fullName').value = data.fullName || '';
                document.getElementById('email').value = data.email || '';
                document.getElementById('username').value = data.username || '';
                document.getElementById('roleName').value = data.roleName || '';
                document.getElementById('jobTitle').value = data.jobTitle || '';
                
                // Sync role based on job title after loading
                syncRole(data.jobTitle);
                
                document.getElementById('phone').value = data.phone || '';
                document.getElementById('address').value = data.address || '';
                document.getElementById('idNumber').value = data.idNumber || '';
                document.getElementById('idType').value = data.idType || 'CCCD';
                document.getElementById('nationality').value = data.nationality || '';
                
                // Set status checkbox if exists
                const statusBtn = document.getElementById('status');
                if (statusBtn) {
                    statusBtn.checked = data.status === true;
                }
                
                // Handle Date of Birth (format yyyy-MM-dd for input type="date")
                if (data.dob) {
                    const dobDate = new Date(data.dob);
                    const formattedDob = dobDate.toLocaleDateString('en-CA');
                    document.getElementById('dob').value = formattedDob;
                }
            })
            .catch(err => console.error('Error fetching employee data:', err));
    }
});
