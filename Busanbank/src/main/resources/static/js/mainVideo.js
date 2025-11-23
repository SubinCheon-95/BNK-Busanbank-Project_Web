/*
    수정일 : 2025/11/22
    수정자 : 천수빈
    내용 : 딸깍은행 메인 영상 출력
*/

// mainVideo.js
document.addEventListener("DOMContentLoaded", () => {
    const videos = {
        main: document.getElementById("video-main"),
        btc: document.getElementById("video-btc"),
        gold: document.getElementById("video-gold"),
        oil: document.getElementById("video-oil")
    };

    function showVideo(key) {
        Object.values(videos).forEach(v => v.classList.remove("active"));
        videos[key].classList.add("active");
    }

    const cards = document.querySelectorAll(".product-card");

    cards.forEach(card => {
        const target = card.dataset.video; // btc, gold, oil

        card.addEventListener("mouseenter", () => {
            showVideo(target);
            card.classList.add("card--active");
        });

        card.addEventListener("mouseleave", () => {
            showVideo("main");
            card.classList.remove("card--active");
        });
    });

    // 초기 메인 영상 강제 세팅
    showVideo("main");
});
