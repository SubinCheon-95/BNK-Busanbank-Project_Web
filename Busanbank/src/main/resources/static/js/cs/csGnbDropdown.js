/*
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : 고객센터 GNB 드롭다운 (1차 카테고리 앞머리 기준)
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

// 호버 시 서브메뉴 위치 조정 (1차 카테고리 앞머리 기준)
document.querySelectorAll(".menu-item").forEach(item => {
    const submenu = item.querySelector(".submenu-wrap");
    const inner = item.querySelector(".submenu-inner");

    if (!submenu || !inner) return;

    item.addEventListener("mouseenter", () => {

        const menuRect = item.getBoundingClientRect();
        const scrollLeft = window.pageXOffset;

        const btnLeft = menuRect.left + scrollLeft;

        inner.style.position = "absolute";
        inner.style.left = btnLeft + "px";
    });
});

// 마우스 나가면 active 제거
document.addEventListener('click', (e) => {
    if (!e.target.closest('.gnb')) {
        document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));
    }
});