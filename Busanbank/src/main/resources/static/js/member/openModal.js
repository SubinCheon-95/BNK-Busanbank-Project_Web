function openModal() {
    const modal = document.getElementById('chatModal');
    modal.style.display = 'flex';
}

function closeModal() {
    const modal = document.getElementById('chatModal');
    modal.style.display = 'none';
}

window.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('chatModal');
    modal.style.display = 'none';
});