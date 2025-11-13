/* 글로벌 버튼 드롭다운 기능 */

const globalBtn = document.querySelector(".global-btn");
const globalDropdown = document.querySelector(".global-dropdown");
const globalIcon = document.querySelector(".global-icon");

// 버튼 눌렀을 때 열고 닫기
globalBtn.addEventListener("click", (e) => {
    e.stopPropagation();

    globalDropdown.classList.toggle("show");     // 드롭다운 열기/닫기
    globalBtn.classList.toggle("active");        // 버튼 색상 바꾸기

    // 아이콘 방향 바꾸기
    if (globalDropdown.classList.contains("show")) {
        globalIcon.classList.remove("xi-caret-down-min");
        globalIcon.classList.add("xi-caret-up-min");
    } else {
        globalIcon.classList.remove("xi-caret-up-min");
        globalIcon.classList.add("xi-caret-down-min");
    }
});


// 드롭다운 클릭 시 닫힘
globalDropdown.addEventListener("click", (e) => {
    e.stopPropagation();
    const lang = e.target.closest("li")?.dataset.lang;
    if (lang) {
        console.log("선택된 언어:", lang);
    }

    globalDropdown.classList.remove("show");
    globalBtn.classList.remove("active");

    globalIcon.classList.remove("xi-caret-up-min");
    globalIcon.classList.add("xi-caret-down-min");
});

// 바깥 클릭 시 닫기
document.addEventListener("click", () => {
    globalDropdown.classList.remove("show");
    globalBtn.classList.remove("active");

    globalIcon.classList.remove("xi-caret-up-min");
    globalIcon.classList.add("xi-caret-down-min");
});
