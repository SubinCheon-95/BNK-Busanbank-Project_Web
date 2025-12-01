/*
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : 은행소개 전용 Global 드롭다운
*/

document.addEventListener("DOMContentLoaded", () => {
    const csGlobalBtn = document.querySelector("header .global-btn");
    const csGlobalDropdown = document.querySelector("header .global-dropdown");
    const csGlobalInner = document.querySelector("header .global-inner");

    if (!csGlobalBtn || !csGlobalDropdown || !csGlobalInner) return;

    // 드롭다운 토글
    csGlobalBtn.addEventListener("click", (e) => {
        e.stopPropagation();

        const isOpen = csGlobalDropdown.classList.contains("show");

        // submenu 전부 닫기
        document.querySelectorAll(".menu-item").forEach(m => m.classList.remove("active"));
        document.querySelectorAll(".submenu-wrap").forEach(s => {
            s.style.display = "none";
        });

        // 먼저 닫기
        csGlobalDropdown.classList.remove("show");

        // 다시 열기
        if (!isOpen) {
            csGlobalDropdown.classList.add("show");

            // 버튼의 x좌표 계산
            const btnRect = csGlobalBtn.getBoundingClientRect();
            const scrollLeft = window.pageXOffset;

            const btnLeft = btnRect.left + scrollLeft;

            // inner 정렬
            csGlobalInner.style.position = "absolute";
            csGlobalInner.style.left = btnLeft + "px";
            csGlobalInner.style.top = "0";
        }
    });

    // 언어 선택
    csGlobalDropdown.addEventListener("click", (e) => {
        e.stopPropagation();

        const langTarget = e.target.closest("[data-lang]");
        if (langTarget) {
            const lang = langTarget.dataset.lang;
            translatePage(lang);
            csGlobalDropdown.classList.remove("show");
        }
    });

    // 외부 클릭 → 닫기
    document.addEventListener("click", () => {
        csGlobalDropdown.classList.remove("show");
    });
});
