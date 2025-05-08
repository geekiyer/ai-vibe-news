function toggleShareDialog(button) {
    const card = button.closest('.bg-white');
    const dialog = card.querySelector('.share-dialog');
    dialog.classList.toggle('hidden');
}

function closeShareDialog(button) {
    const dialog = button.closest('.share-dialog');
    dialog.classList.add('hidden');
}

function copyShareLink(button) {
    const card = button.closest('.bg-white');
    const heading = card.querySelector('.share-preview-title').textContent;
    const destination = window.location.href;
    const shareText = `${heading} - ${destination}`;

    navigator.clipboard.writeText(shareText).then(() => {
        button.textContent = 'Copied!';
        setTimeout(() => {
            button.textContent = 'Copy Link';
        }, 2000);
    }).catch(err => {
        console.error('Failed to copy text: ', err);
    });
}

function openShareModal(title, imageUrl) {
    const modal = document.querySelector('.modal-overlay');
    modal.classList.remove('hidden');
    modal.querySelector('.share-preview-title').textContent = title;
    modal.querySelector('.share-preview-image').src = imageUrl;
    modal.querySelector('.share-url-input').value = window.location.href;
}

function closeShareModal() {
    document.querySelector('.modal-overlay').classList.add('hidden');
}

// Close modal on Esc key
document.addEventListener('keydown', function(event) {
    const modal = document.querySelector('.modal-overlay');
    if (!modal.classList.contains('hidden') && event.key === 'Escape') {
        closeShareModal();
    }
});

// Close modal when clicking outside modal-content
document.querySelector('.modal-overlay').addEventListener('click', function(event) {
    if (event.target === this) {
        closeShareModal();
    }
});

function copyShareLink(button) {
    const modal = document.querySelector('.modal-overlay');
    const heading = modal.querySelector('.share-preview-title').textContent;
    const destination = window.location.href;
    const shareText = `${heading} - ${destination}`;

    navigator.clipboard.writeText(shareText).then(() => {
        button.textContent = 'Copied!';
        setTimeout(() => {
            button.textContent = 'Copy Link';
        }, 2000);
    }).catch(err => {
        console.error('Failed to copy text: ', err);
    });
}

async function fetchShareCounts() {
    const res = await fetch('/api/share-counts');
    const counts = await res.json();
    for (const key in counts) {
        const [articleId, platform] = key.split(':');
        const el = document.getElementById(`share-count-${articleId}-${platform}`);
        if (el) el.textContent = counts[key];
    }
}

function shareTo(platform, title, url, articleId, btn) {
    let shareUrl = '';
    const encodedTitle = encodeURIComponent(title);
    const encodedUrl = encodeURIComponent(url);

    if (platform === 'twitter') {
        shareUrl = `https://twitter.com/intent/tweet?text=${encodedTitle}&url=${encodedUrl}`;
    } else if (platform === 'linkedin') {
        shareUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${encodedUrl}`;
    } else if (platform === 'facebook') {
        shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`;
    }
    window.open(shareUrl, '_blank', 'noopener,noreferrer,width=600,height=400');

    // Increment share count
    fetch('/api/share', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ articleId, platform })
    }).then(res => res.json()).then(data => {
        const el = document.getElementById(`share-count-${articleId}-${platform}`);
        if (el) el.textContent = data.count;
    });
}

window.addEventListener('DOMContentLoaded', fetchShareCounts);