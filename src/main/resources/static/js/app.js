// ========== AI Photo Generator - Frontend ==========

const API_BASE = '/api/images';
let currentImageId = null;
let searchTimeout = null;
let dragCounter = 0;

// ========== DOM Elements ==========
const generateForm = document.getElementById('generateForm');
const promptInput = document.getElementById('prompt');
const charCount = document.getElementById('charCount');
const generateBtn = document.getElementById('generateBtn');
const gallery = document.getElementById('gallery');
const emptyState = document.getElementById('emptyState');
const loadingGallery = document.getElementById('loadingGallery');
const noResults = document.getElementById('noResults');
const skeletonGrid = document.getElementById('skeletonGrid');
const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const imageCount = document.getElementById('imageCount');
const previewModal = document.getElementById('previewModal');
const modalImage = document.getElementById('modalImage');
const modalPrompt = document.getElementById('modalPrompt');
const modalSize = document.getElementById('modalSize');
const modalStyle = document.getElementById('modalStyle');
const modalProvider = document.getElementById('modalProvider');
const modalDate = document.getElementById('modalDate');
const modalDownload = document.getElementById('modalDownload');
const modalDelete = document.getElementById('modalDelete');
const modalClose = document.getElementById('modalClose');
const themeToggle = document.getElementById('themeToggle');
const toastContainer = document.getElementById('toastContainer');
const copyPromptBtn = document.getElementById('copyPromptBtn');
const dropOverlay = document.getElementById('dropOverlay');
const generatingOverlay = document.getElementById('generatingSkeleton');

// ========== Initialization ==========
document.addEventListener('DOMContentLoaded', () => {
    loadGallery();
    setupEventListeners();
    initTheme();
    setupDragDrop();
});

// ========== Event Listeners ==========
function setupEventListeners() {
    // Form submission
    generateForm.addEventListener('submit', handleGenerate);

    // Character count + copy button visibility
    promptInput.addEventListener('input', () => {
        charCount.textContent = promptInput.value.length;
        copyPromptBtn.style.display = promptInput.value.length > 0 ? 'block' : 'none';
    });

    // Copy prompt button
    copyPromptBtn.addEventListener('click', () => {
        navigator.clipboard.writeText(promptInput.value).then(() => {
            showToast('Prompt copied to clipboard!', 'success');
            copyPromptBtn.innerHTML = '&#10003;';
            setTimeout(() => { copyPromptBtn.innerHTML = '&#128203;'; }, 1500);
        });
    });

    // Search with debounce
    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            if (searchInput.value.trim()) {
                searchImages(searchInput.value.trim());
                clearSearchBtn.style.display = 'block';
            } else {
                loadGallery();
                clearSearchBtn.style.display = 'none';
            }
        }, 300);
    });

    searchBtn.addEventListener('click', () => {
        if (searchInput.value.trim()) {
            searchImages(searchInput.value.trim());
        }
    });

    clearSearchBtn.addEventListener('click', () => {
        searchInput.value = '';
        clearSearchBtn.style.display = 'none';
        loadGallery();
    });

    // Prompt suggestions
    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            promptInput.value = chip.dataset.prompt;
            charCount.textContent = promptInput.value.length;
            copyPromptBtn.style.display = 'block';
            promptInput.focus();
        });
    });

    // Modal
    modalClose.addEventListener('click', closeModal);
    document.querySelector('.modal-backdrop').addEventListener('click', closeModal);
    modalDelete.addEventListener('click', handleDelete);
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeModal();
    });

    // Theme toggle
    themeToggle.addEventListener('click', toggleTheme);

    // Keyboard shortcut: Ctrl+Enter to generate
    promptInput.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            e.preventDefault();
            generateForm.dispatchEvent(new Event('submit'));
        }
    });
}

// ========== Drag & Drop ==========
function setupDragDrop() {
    document.addEventListener('dragenter', (e) => {
        e.preventDefault();
        dragCounter++;
        if (e.dataTransfer.types.includes('text/plain')) {
            dropOverlay.style.display = 'flex';
            document.querySelector('.generate-panel').classList.add('drag-active');
        }
    });

    document.addEventListener('dragleave', (e) => {
        e.preventDefault();
        dragCounter--;
        if (dragCounter <= 0) {
            dragCounter = 0;
            dropOverlay.style.display = 'none';
            document.querySelector('.generate-panel').classList.remove('drag-active');
        }
    });

    document.addEventListener('dragover', (e) => {
        e.preventDefault();
    });

    document.addEventListener('drop', (e) => {
        e.preventDefault();
        dragCounter = 0;
        dropOverlay.style.display = 'none';
        document.querySelector('.generate-panel').classList.remove('drag-active');

        const text = e.dataTransfer.getData('text/plain');
        if (text && text.length >= 3) {
            promptInput.value = text.substring(0, 500);
            charCount.textContent = promptInput.value.length;
            copyPromptBtn.style.display = 'block';
            showToast('Prompt dropped! Click Generate to create an image.', 'info');
        }
    });
}

// ========== Image Generation ==========
async function handleGenerate(e) {
    e.preventDefault();

    const prompt = promptInput.value.trim();
    if (!prompt || prompt.length < 3) {
        showToast('Please enter a prompt (at least 3 characters)', 'error');
        promptInput.focus();
        return;
    }

    const requestData = {
        prompt: prompt,
        size: document.getElementById('size').value,
        style: document.getElementById('style').value,
        quality: document.getElementById('quality').value
    };

    setGenerating(true);
    generatingOverlay.style.display = 'block';

    try {
        const response = await fetch(`${API_BASE}/generate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Generation failed');
        }

        const result = await response.json();
        showToast('Image generated successfully!', 'success');

        // Clear form
        promptInput.value = '';
        charCount.textContent = '0';
        copyPromptBtn.style.display = 'none';

        // Reload gallery
        await loadGallery();
    } catch (error) {
        showToast(error.message || 'Failed to generate image', 'error');
        console.error('Generation error:', error);
    } finally {
        setGenerating(false);
        generatingOverlay.style.display = 'none';
    }
}

function setGenerating(isGenerating) {
    const btnText = generateBtn.querySelector('.btn-text');
    const btnLoading = generateBtn.querySelector('.btn-loading');

    generateBtn.disabled = isGenerating;
    btnText.style.display = isGenerating ? 'none' : 'inline';
    btnLoading.style.display = isGenerating ? 'inline' : 'none';
}

// ========== Gallery ==========
async function loadGallery() {
    // Show skeleton instead of spinner
    skeletonGrid.style.display = 'grid';
    gallery.innerHTML = '';
    emptyState.style.display = 'none';
    noResults.style.display = 'none';
    loadingGallery.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE}?size=50`);
        if (!response.ok) throw new Error('Failed to load gallery');

        const images = await response.json();

        // Brief delay for skeleton to be visible (feels smoother)
        await new Promise(r => setTimeout(r, 300));

        skeletonGrid.style.display = 'none';
        renderGallery(images);
        updateImageCount(images.length);
    } catch (error) {
        skeletonGrid.style.display = 'none';
        showToast('Failed to load gallery', 'error');
        console.error('Gallery error:', error);
    }
}

async function searchImages(query) {
    skeletonGrid.style.display = 'grid';
    gallery.innerHTML = '';
    emptyState.style.display = 'none';
    noResults.style.display = 'none';
    loadingGallery.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
        if (!response.ok) throw new Error('Search failed');

        const images = await response.json();

        await new Promise(r => setTimeout(r, 200));

        skeletonGrid.style.display = 'none';
        renderGallery(images);

        if (images.length === 0) {
            noResults.style.display = 'block';
        }
    } catch (error) {
        skeletonGrid.style.display = 'none';
        showToast('Search failed', 'error');
    }
}

function renderGallery(images) {
    gallery.innerHTML = '';

    if (images.length === 0) {
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    images.forEach((image, index) => {
        const card = document.createElement('div');
        card.className = 'image-card';
        card.style.animationDelay = `${index * 0.05}s`;

        // Card prompt with copy button
        const promptShort = image.prompt.length > 60
            ? image.prompt.substring(0, 60) + '...'
            : image.prompt;

        card.innerHTML = `
            <img src="${image.imageUrl}" alt="${escapeHtml(image.prompt)}" loading="lazy"
                 onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22200%22 height=%22200%22><rect fill=%22%23ddd%22 width=%22200%22 height=%22200%22/><text x=%2250%%22 y=%2250%%22 dominant-baseline=%22middle%22 text-anchor=%22middle%22 fill=%22%23999%22 font-size=%2214%22>No Image</text></svg>'">
            <div class="card-info">
                <p class="card-prompt" title="${escapeHtml(image.prompt)}">${escapeHtml(promptShort)}</p>
                <div class="card-meta">
                    <span>${formatDate(image.createdAt)}</span>
                    <span class="provider-tag">${image.provider || 'AI'}</span>
                </div>
            </div>
        `;
        card.addEventListener('click', () => openModal(image));
        gallery.appendChild(card);
    });
}

function updateImageCount(count) {
    imageCount.textContent = `${count} image${count !== 1 ? 's' : ''}`;
}

// ========== Modal ==========
function openModal(image) {
    currentImageId = image.id;
    modalImage.src = image.imageUrl;
    modalImage.alt = image.prompt;
    modalPrompt.textContent = image.prompt;
    modalSize.textContent = `Size: ${image.size}`;
    modalStyle.textContent = `Style: ${image.style}`;
    modalProvider.textContent = `Provider: ${image.provider || 'AI'}`;

    modalDate.textContent = formatDate(image.createdAt);
    modalDownload.href = image.imageUrl;
    modalDownload.download = `${image.id}.png`;
    previewModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    previewModal.style.display = 'none';
    document.body.style.overflow = '';
    currentImageId = null;
}

async function handleDelete() {
    if (!currentImageId) return;

    if (!confirm('Are you sure you want to delete this image?')) return;

    try {
        const response = await fetch(`${API_BASE}/${currentImageId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Delete failed');

        showToast('Image deleted', 'success');
        closeModal();
        await loadGallery();
    } catch (error) {
        showToast('Failed to delete image', 'error');
    }
}

// ========== Theme ==========
function initTheme() {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') {
        document.documentElement.setAttribute('data-theme', 'dark');
        themeToggle.textContent = '☀';
    }
}

function toggleTheme() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    if (isDark) {
        document.documentElement.removeAttribute('data-theme');
        themeToggle.textContent = '☾';
        localStorage.setItem('theme', 'light');
    } else {
        document.documentElement.setAttribute('data-theme', 'dark');
        themeToggle.textContent = '☀';
        localStorage.setItem('theme', 'dark');
    }
}

// ========== Toast Notifications ==========
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100px)';
        toast.style.transition = '0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ========== Utilities ==========
function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
