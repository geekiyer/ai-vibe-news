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