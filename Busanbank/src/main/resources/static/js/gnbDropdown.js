/*
    수정일 : 2025/11/27
    수정자 : 천수빈
    내용 : GNB 드롭다운 제어 + 서브메뉴 없는 링크 예외 처리
*/

// ★ data-menu 속성이 있는 것만 드롭다운 처리 ★
document.querySelectorAll('.menu-item[data-menu] > a').forEach(menu => {
    menu.addEventListener('click', e => {
        e.preventDefault();

        const item = menu.parentElement;
        const isActive = item.classList.contains('active');

        // 모든 메뉴 닫기
        document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));

        // 클릭한 메뉴만 열기
        if (!isActive) item.classList.add('active');
    });
});

document.querySelector(".gnb").addEventListener("mouseenter", (e) => {
    const item = e.target.closest(".menu-item");
    if (!item) return;

    const submenu = item.querySelector(".submenu-wrap");
    const inner = item.querySelector(".submenu-inner");
    if (!submenu || !inner) return;

    const rect = item.getBoundingClientRect();
    inner.style.position = "relative";
    inner.style.left = rect.left + "px";
}, true);

document.querySelectorAll(".menu-item").forEach(item => {
    const submenu = item.querySelector(".submenu-wrap");
    const inner = item.querySelector(".submenu-inner");

    if (!submenu || !inner) return;

    item.addEventListener("mouseenter", () => {
        const rect = item.getBoundingClientRect();
        inner.style.position = "relative";
        inner.style.left = rect.left + "px";
    });
});