const modal = document.querySelector('.modal');

const btnOpenModal = document.querySelector('.agree-btn');
const btnCloseModals = document.querySelectorAll('.close-modal');

btnOpenModal.addEventListener("click", () => {
  modal.style.display = "flex";
});

btnCloseModals.forEach(btn => {
    btn.addEventListener("click", () => {
        const formElements = modal.querySelectorAll('input, textarea, select');
        formElements.forEach(el => {
            if (el.tagName === 'SELECT') {
                el.selectedIndex = 0;
            } else if (el.type === 'date' || el.type === 'text') {
                el.value = '';
            }
        });
        modal.style.display = "none";
    });
});

const btnRegister = modal.querySelector('.register-btn');
