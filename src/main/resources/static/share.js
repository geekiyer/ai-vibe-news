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
}

function closeShareModal() {
    document.querySelector('.modal-overlay').classList.add('hidden');
}

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