/**
 * Logic xử lý trang chi tiết Loại phòng
 */

document.addEventListener('DOMContentLoaded', () => {
    const mode = document.getElementById('form-mode').value;
    const id = document.getElementById('room-type-id').value;

    if (mode === 'edit' || mode === 'view') {
        loadRoomTypeDetails(id);
    }

    if (mode !== 'view') {
        initFormHandlers();
    }
});

async function loadRoomTypeDetails(id) {
    try {
        const response = await fetch(`/api/room-types/${id}`);
        if (!response.ok) throw new Error('Không thể tải thông tin loại phòng');
        
        const type = await response.json();
        
        // Fill basic info
        document.getElementById('typeName').value = type.typeName;
        document.getElementById('price').value = type.price;
        document.getElementById('capacity').value = type.capacity;
        document.getElementById('description').value = type.description || '';
        
        // Main image
        if (type.roomImage) {
            document.getElementById('roomImage').value = type.roomImage;
            document.getElementById('room-image-preview').src = type.roomImage;
            document.getElementById('room-image-preview').classList.remove('gray-scale', 'opacity-50');
        }
        
        // Gallery
        renderGallery(type.galleryImages || []);
        
    } catch (err) {
        console.error(err);
        toastService.error(err.message);
    }
}

function initFormHandlers() {
    const form = document.getElementById('room-type-form');
    form.addEventListener('submit', handleFormSubmit);

    // Main image upload
    document.getElementById('room-image-file').addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        const url = await uploadFile(file);
        if (url) {
            document.getElementById('roomImage').value = url;
            document.getElementById('room-image-preview').src = url;
            document.getElementById('room-image-preview').classList.remove('gray-scale', 'opacity-50');
            toastService.success('Đã tải ảnh chính lên!');
        }
    });

    // Gallery upload
    document.getElementById('gallery-images-file').addEventListener('change', async (e) => {
        const files = Array.from(e.target.files);
        for (const file of files) {
            const url = await uploadFile(file);
            if (url) {
                addImageToGallery(url);
            }
        }
        toastService.success(`Đã tải lên ${files.length} ảnh gallery!`);
    });
}

async function uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file);
    try {
        const response = await fetch('/api/media/upload', {
            method: 'POST',
            body: formData
        });
        const data = await response.json();
        if (response.ok) return data.url;
        throw new Error(data.message || 'Lỗi upload');
    } catch (err) {
        console.error(err);
        toastService.error('Không thể tải ảnh lên: ' + file.name);
        return null;
    }
}

function renderGallery(images) {
    const container = document.getElementById('gallery-container');
    const emptyState = document.getElementById('gallery-empty-state');
    const mode = document.getElementById('form-mode').value;

    if (images.length > 0) {
        emptyState.classList.add('hidden');
        images.forEach(url => addImageToGallery(url, mode === 'view'));
    }
}

function addImageToGallery(url, isReadOnly = false) {
    const container = document.getElementById('gallery-container');
    document.getElementById('gallery-empty-state').classList.add('hidden');

    const wrapper = document.createElement('div');
    wrapper.className = 'gallery-item-wrapper relative group aspect-square rounded-2xl overflow-hidden border border-slate-200 shadow-sm';
    
    let html = `<img src="${url}" class="gallery-item-img w-full h-full object-cover transition-transform duration-500 group-hover:scale-110">`;
    
    if (!isReadOnly) {
        html += `
            <button type="button" onclick="this.parentElement.remove(); checkGalleryEmpty();" 
                class="absolute top-2 right-2 bg-white/90 backdrop-blur text-rose-500 p-2 rounded-xl opacity-0 group-hover:opacity-100 transition-all shadow-sm">
                <i data-lucide="trash-2" class="w-4 h-4"></i>
            </button>
        `;
    } else {
        html += `
            <div class="absolute inset-0 bg-slate-900/40 opacity-0 group-hover:opacity-100 transition-all flex items-center justify-center cursor-pointer" onclick="window.open('${url}', '_blank')">
                <i data-lucide="maximize" class="w-6 h-6 text-white"></i>
            </div>
        `;
    }
    
    wrapper.innerHTML = html;
    container.appendChild(wrapper);
    lucide.createIcons();
}

function checkGalleryEmpty() {
    const items = document.querySelectorAll('.gallery-item-img');
    if (items.length === 0) {
        document.getElementById('gallery-empty-state').classList.remove('hidden');
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const mode = document.getElementById('form-mode').value;
    const id = document.getElementById('room-type-id').value;

    const galleryImages = Array.from(document.querySelectorAll('.gallery-item-img')).map(img => img.src);
    
    const data = {
        typeName: document.getElementById('typeName').value,
        price: document.getElementById('price').value,
        capacity: document.getElementById('capacity').value,
        description: document.getElementById('description').value,
        roomImage: document.getElementById('roomImage').value,
        galleryImages: galleryImages
    };

    const url = mode === 'create' ? '/api/room-types' : `/api/room-types/${id}`;
    const method = mode === 'create' ? 'POST' : 'PUT';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            toastService.success(mode === 'create' ? 'Thêm loại phòng thành công!' : 'Đã lưu thay đổi!');
            setTimeout(() => {
                window.location.href = '/management/room-types';
            }, 1000);
        } else {
            const err = await response.json();
            toastService.error(err.message || 'Có lỗi xảy ra khi lưu');
        }
    } catch (err) {
        console.error(err);
        toastService.error('Lỗi kết nối hệ thống');
    }
}
