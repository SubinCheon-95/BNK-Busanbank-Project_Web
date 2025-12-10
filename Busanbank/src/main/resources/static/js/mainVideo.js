/*
    수정일 : 2025/11/24
    수정자 : 천수빈
    내용 : 딸깍은행 메인 영상 출력 + 영상 타입별 타이틀/서브 자동 변경
*/

document.addEventListener("DOMContentLoaded", () => {
    const videos = {
        main: document.getElementById("video-main"),
        btc: document.getElementById("video-btc"),
        gold: document.getElementById("video-gold"),
        oil: document.getElementById("video-oil")
    };

    // 텍스트 매핑
    const videoTexts = {
        main: {
            title: "",
            sub: ""
        },
        btc: {
            title: "BTC 비트코인 연동 예금",
            sub: "미래 흐름을 읽는 디지털 자산 기반 코인 예금"
        },
        gold: {
            title: "GOLD 금 시세 연동 예금",
            sub: "시간이 증명한, 변함없는 금의 자산 가치에 투자하는 방법"
        },
        oil: {
            title: "OIL 크루드오일 연동 예금",
            sub: "우리의 생활을 움직이는 오일에 쉽게 투자하다"
        }
    };

    const textBox = document.querySelector(".hero-text-box");
    const titleEl = document.querySelector(".hero-title");
    const subEl = document.querySelector(".hero-sub");

    // 텍스트 업데이트
    function updateText(type) {
        titleEl.textContent = videoTexts[type].title;
        subEl.textContent = videoTexts[type].sub;

        if (type === "main") {
            textBox.style.opacity = 0;
        } else {
            textBox.style.opacity = 1;
        }
    }

    // 영상 표시 + 텍스트 업데이트
    function showVideo(key) {
        Object.values(videos).forEach(v => v.classList.remove("active"));
        videos[key].classList.add("active");

        updateText(key);
    }

    // 카드 호버 이벤트
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

    // 초기 메인 영상 세팅
    showVideo("main");
});