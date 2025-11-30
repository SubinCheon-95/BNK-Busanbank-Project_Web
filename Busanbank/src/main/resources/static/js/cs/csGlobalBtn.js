/*
    수정일 : 2025/11/28
    수정자 : 천수빈
    내용 : 글로벌 버튼 위치에 맞춰 드롭다운 정렬
*/

const globalBtn = document.querySelector(".global-btn");
const globalDropdown = document.querySelector(".global-dropdown");
const globalIcon = document.querySelector(".global-icon");
const globalInner = document.querySelector(".global-inner"); // 25.11.28_수빈

// 버튼 눌렀을 때 열고 닫기
globalBtn.addEventListener("click", (e) => {
    e.stopPropagation();

    const isOpen = globalDropdown.classList.contains("show");

    // 1) 열기 전 모든 submenu 닫기
    document.querySelectorAll(".submenu-wrap").forEach(s => {
        s.style.display = "none";
    });

    document.querySelectorAll(".menu-item").forEach(m => {
        m.classList.remove("active");
    });

    // 2) 기존 글로벌 드롭다운 상태 모두 닫기
    globalDropdown.classList.remove("show");
    globalBtn.classList.remove("active");
    globalIcon.classList.remove("xi-caret-up-min");
    globalIcon.classList.add("xi-caret-down-min");

    // 3) 클릭한 경우에만 다시 열기
    if (!isOpen) {
        globalDropdown.classList.add("show");
        globalBtn.classList.add("active");

        globalIcon.classList.remove("xi-caret-down-min");
        globalIcon.classList.add("xi-caret-up-min");

        // 버튼 위치에 맞춰 드롭다운 정렬
        const rect = globalBtn.getBoundingClientRect();
        globalInner.style.position = "relative";
        globalInner.style.left = rect.left + "px";

        globalDropdown.style.display = "block";
    } else {
        globalDropdown.style.display = "none";
    }
});


// 드롭다운 내부 클릭 → 언어 선택 + 닫기
globalDropdown.addEventListener("click", (e) => {
    e.stopPropagation();

    // li → a 또는 div 로 바뀌었기 때문에 data-lang 기준으로 변경
    const langTarget = e.target.closest("[data-lang]");
    if (langTarget) {
        const lang = langTarget.dataset.lang;
        translatePage(lang); // 기존 번역 기능 100% 유지
    }

    // 닫기 처리
    globalDropdown.classList.remove("show");
    globalDropdown.style.display = "none";
    globalBtn.classList.remove("active");

    globalIcon.classList.remove("xi-caret-up-min");
    globalIcon.classList.add("xi-caret-down-min");
});


// 바깥 클릭 시 닫기
document.addEventListener("click", () => {
    globalDropdown.classList.remove("show");
    globalDropdown.style.display = "none";
    globalBtn.classList.remove("active");

    globalIcon.classList.remove("xi-caret-up-min");
    globalIcon.classList.add("xi-caret-down-min");
});
