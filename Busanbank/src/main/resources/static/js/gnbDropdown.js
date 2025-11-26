/*
    수정일 : 2025/11/25
    수정자 : 천수빈
    내용 : GNB 드롭다운 제어 + 드롭다운 앞줄 정렬
*/

/*
    수정일 : 2025/11/25
    수정자 : 천수빈
    내용 : GNB 드롭다운 제어 + 드롭다운 앞줄 정렬
*/

document.querySelectorAll('.menu-item > a').forEach(menu => {
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
        const rect = item.getBoundingClientRect();   // 메뉴 좌표
        inner.style.position = "relative";
        inner.style.left = rect.left + "px";         // 이게 정렬 핵심
    });
});
