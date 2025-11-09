document.addEventListener('DOMContentLoaded', () => {

    const modal = document.getElementById("myModal");
    const detailBtns = document.querySelectorAll(".detailBtn");
    const closeBtn = modal.querySelector(".close");
    const form = document.getElementById("shopForm");

    detailBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            modal.style.display = "block";
        })
    });

    closeBtn.addEventListener("click", () => {
        modal.style.display = "none"
        form.reset();
    });
    window.addEventListener("click", (e) => {
        if (e.target === modal) modal.style.display = "none";
    });

    const modalContent = modal.querySelector(".modal-content");
    modalContent.addEventListener("click", (e) => {
        e.stopPropagation(); // 클릭 이벤트가 부모로 전달되지 않도록 막음
    });
});