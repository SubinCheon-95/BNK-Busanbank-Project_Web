/*
    날짜 : 2025/11/17
    이름 : 천수빈
    내용 : 돋보기 검색 기능
*/

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

/* ===========================
   🔍 키워드 검색 기능
=========================== */

const searchInput = document.querySelector(".search-box input");
const searchSubmitBtn = document.querySelector(".search-submit");

// 검색 실행 함수
function goSearch() {
    const keyword = searchInput.value.trim();

    if (keyword.length === 0) {
        alert("검색어를 입력하세요.");
        searchInput.focus();
        return;
    }

    // 검색 URL로 이동
    window.location.href = `/busanbank/prod/search?keyword=` + encodeURIComponent(keyword);
}

// 버튼 클릭 시 실행
searchSubmitBtn.addEventListener("click", goSearch);

// 엔터키 입력 시 실행
searchInput.addEventListener("keypress", function (e) {
    if (e.key === "Enter") {
        goSearch();
    }
});
