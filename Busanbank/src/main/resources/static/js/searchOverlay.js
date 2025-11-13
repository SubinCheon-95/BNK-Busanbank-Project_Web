/* 돋보기 검색 기능 활성화 */
const searchOverlay = document.querySelector(".search-overlay");

// 검색창 열기
const openSearchBtn = document.querySelector(".search-open-btn");

// 검색창 닫기
const closeSearchBtn = document.querySelector(".search-close");

openSearchBtn.addEventListener("click", () => {
    searchOverlay.classList.add("active");
    openSearchBtn.classList.add("active");
});

closeSearchBtn.addEventListener("click", () => {
    searchOverlay.classList.remove("active");
    openSearchBtn.classList.remove("active");
});
