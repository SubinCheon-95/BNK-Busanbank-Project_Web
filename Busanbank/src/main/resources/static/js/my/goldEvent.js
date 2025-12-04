/*
    날짜 : 2025/12/01
    이름 : 오서정
    내용 : 금 관련 이벤트 스크립트 작성
*/
let goldExplosionAnim;

function setupLottie() {
    goldExplosionAnim = lottie.loadAnimation({
        container: document.getElementById('goldExplosion'),
        renderer: 'svg',
        loop: false,
        autoplay: false,
        path: '/busanbank/js/my/star_burst.json' // 네 JSON 경로
    });

    goldExplosionAnim.setSpeed(2.0);
}


document.addEventListener("DOMContentLoaded", () => {
    lottie.loadAnimation({
        container: document.getElementById("goldHeaderLottie"),
        renderer: "svg",
        loop: true,
        autoplay: true,
        path: "/busanbank/js/my/gold_plates.json"
    });

    setupLottie();
    /* ==============================
       오늘 금 시세 표시
    =============================== */
    function setupTodayPrice(price) {
        document.getElementById("todayPrice").innerText = Number(price).toFixed(2);
    }

    /* ==============================
       모달 열기
    =============================== */
    function openGoldModal() {
        const modal = document.getElementById("goldModal");

        fetch("/busanbank/my/event/status", { credentials: "include" })
            .then(res => res.json())
            .then(data => {

                console.log("STATUS =", data.todayStatus, "/", data.pastStatus);

                resetModalUI();
                setupModalUI(data);

                modal.classList.remove("hide");
            });
    }

    /* ==============================
       모달 닫기
    =============================== */
    function closeGoldModal() {
        document.getElementById("goldModal").classList.add("hide");
    }

    /* ==============================
       UI 초기화
    =============================== */
    function resetModalUI() {

        const pickBtn = document.getElementById("goldPickBtn");

        pickBtn.classList.remove("hide");
        pickBtn.innerText = "금 캐기";

        document.getElementById("miningAnimation").classList.add("hide");
        document.getElementById("resultBox").classList.add("hide");
        document.getElementById("alreadyMessage").classList.add("hide");

        document.querySelector(".result-title").classList.add("hide");
        document.querySelector(".result-title").innerText = "";

        document.querySelector(".sub-text").classList.remove("hide");

        document.querySelector(".error-amount").classList.add("hide");
        document.querySelector(".wait-text").classList.add("hide");
        document.querySelector(".range-box").classList.add("hide");
    }

    /* ==============================
       상태별 UI 구성
    =============================== */
    function setupModalUI(data) {

        setupTodayPrice(data.todayPrice);

        const pickBtn = document.getElementById("goldPickBtn");
        const resultBox = document.getElementById("resultBox");
        const alreadyMsg = document.getElementById("alreadyMessage");

        const min = data.min ?? data.minPrice;
        const max = data.max ?? data.maxPrice;

        const rangeTitle = document.getElementById("rangeTitle");
        const rangeMin = document.getElementById("rangeMin");
        const rangeMax = document.getElementById("rangeMax");

        /* -------------------------
           오늘 NONE
        ---------------------------*/
        if (data.todayStatus === "NONE") {

            // A1) 과거 SUCCESS → 오늘 재참여 불가
            if (data.pastStatus === "SUCCESS") {

                pickBtn.classList.add("hide");
                resultBox.classList.remove("hide");

                // 추가됨 (중요)
                document.querySelector(".sub-text").classList.add("hide");
                document.querySelector(".result-title").classList.remove("hide");

                document.querySelector(".result-title").innerText =
                    "🎉 예측 성공! 쿠폰을 받으셨습니다.";

                document.querySelector(".wait-text").classList.add("hide");

                rangeTitle.innerText = "지난 나의 예측 범위";
                rangeMin.innerText = min.toFixed(2);
                rangeMax.innerText = max.toFixed(2);
                document.querySelector(".range-box").classList.remove("hide");

                return;
            }

            // A2) 과거 FAIL → 오늘 재참여 가능
            if (data.pastStatus === "FAIL") {
                document.getElementById("goldHeaderLottie").classList.add("hide");
                pickBtn.classList.remove("hide");
                resultBox.classList.remove("hide");

                document.querySelector(".result-title").classList.remove("hide");
                document.querySelector(".result-title").innerText = "📉 예측 실패!";

                rangeTitle.innerText = "지난 나의 예측 범위";
                rangeMin.innerText = min.toFixed(2);
                rangeMax.innerText = max.toFixed(2);
                document.querySelector(".range-box").classList.remove("hide");

                alreadyMsg.innerHTML =
                    "<p>예측에 실패했어요 😢</p><p>오늘 다시 도전해보세요!</p>";
                alreadyMsg.classList.remove("hide");

                return;
            }

            return;
        }

        /* -------------------------
           오늘 WAIT → 이미 참여
        ---------------------------*/
        if (data.todayStatus === "WAIT") {

            pickBtn.classList.add("hide");
            resultBox.classList.remove("hide");
            document.querySelector(".sub-text").classList.add("hide");

            rangeTitle.innerText = "나의 예측 범위";
            rangeMin.innerText = min.toFixed(2);
            rangeMax.innerText = max.toFixed(2);
            document.querySelector(".range-box").classList.remove("hide");

            document.getElementById("errorRate").innerText = data.errorRate;
            document.getElementById("errorAmount").innerText = data.errorAmount.toFixed(2);

            document.querySelector(".error-amount").classList.remove("hide");
            document.querySelector(".wait-text").classList.remove("hide");

            return;
        }

        /* -------------------------
           오늘 FAIL → 오늘 재참여 가능
        ---------------------------*/
        if (data.todayStatus === "FAIL") {
            document.getElementById("goldHeaderLottie").classList.add("hide");
            pickBtn.classList.remove("hide");
            resultBox.classList.remove("hide");

            document.querySelector(".result-title").classList.remove("hide");
            document.querySelector(".result-title").innerText = "📉 예측 실패!";

            rangeTitle.innerText = "지난 나의 예측 범위";
            rangeMin.innerText = min.toFixed(2);
            rangeMax.innerText = max.toFixed(2);
            document.querySelector(".range-box").classList.remove("hide");

            return;
        }

        /* -------------------------
           오늘 SUCCESS
        ---------------------------*/
        if (data.todayStatus === "SUCCESS") {

            pickBtn.classList.add("hide");
            resultBox.classList.remove("hide");

            document.querySelector(".sub-text").classList.add("hide");
            document.querySelector(".result-title").classList.remove("hide");
            document.querySelector(".result-title").innerText =
                "🎉 예측 성공! 쿠폰이 지급되었습니다!";

            document.querySelector(".wait-text").classList.add("hide");
            alreadyMsg.classList.add("hide");

            document.getElementById("errorRate").innerText = data.errorRate;
            document.getElementById("errorAmount").innerText = data.errorAmount.toFixed(2);
            document.querySelector(".error-amount").classList.remove("hide");

            rangeTitle.innerText = "지난 나의 예측 범위";
            rangeMin.innerText = min.toFixed(2);
            rangeMax.innerText = max.toFixed(2);
            document.querySelector(".range-box").classList.remove("hide");

            return;
        }
    }

    /* ==============================
       금캐기 클릭
    =============================== */
    document.getElementById("goldPickBtn").onclick = () => {

        // 🔥 FAIL UI 즉시 숨기기 (중요!)
        document.querySelector(".result-title").classList.add("hide");
        document.querySelector(".error-amount").classList.add("hide");
        document.querySelector(".range-box").classList.add("hide");
        document.querySelector(".wait-text").classList.add("hide");
        document.getElementById("resultBox").classList.add("hide");
        document.getElementById("goldHeaderLottie").classList.add("hide");

        const alreadyMsg = document.getElementById("alreadyMessage");
        alreadyMsg.classList.add("hide");
        alreadyMsg.innerHTML = "";

        document.querySelector(".range-box").classList.add("hide");

        const pickBtn = document.getElementById("goldPickBtn");
        const miningAnimation = document.getElementById("miningAnimation");

        pickBtn.classList.add("hide");
        miningAnimation.classList.remove("hide");

        // 🔥 금가루 3번 펑!
        goldExplosion.classList.remove("hide");

        const totalFrames = goldExplosionAnim.animationData.op;
        const frameRate = goldExplosionAnim.animationData.fr;
        const duration = (totalFrames / frameRate) * 1000;  // 1회 재생 시간(ms)

        let count = 0;

        function playExplosion() {
            goldExplosionAnim.goToAndPlay(0, true);
            count++;

            if (count < 3) {
                setTimeout(playExplosion, duration);
            } else {
                // ⭐⭐⭐ 폭발 3번 끝난 직후 처리 ⭐⭐⭐
                setTimeout(() => {
                    goldExplosion.classList.add("hide");
                    miningAnimation.classList.add("hide");

                    // 👉 이때 fetch 실행!
                    fetch("/busanbank/my/event/gold", {
                        method: "POST",
                        credentials: "include"
                    })
                        .then(res => res.json())
                        .then(data => {

                            if (data.already) {
                                alreadyMsg.innerHTML =
                                    "<p>오늘 이미 금 캐기를 하셨습니다!</p><p>내일 다시 도전해주세요 ✨</p>";
                                alreadyMsg.classList.remove("hide");
                                return;
                            }

                            // WAIT 화면 표시
                            document.getElementById("resultBox").classList.remove("hide");
                            document.querySelector(".sub-text").classList.add("hide");

                            const min = data.min;
                            const max = data.max;

                            document.getElementById("errorRate").innerText = data.errorRate;
                            document.getElementById("errorAmount").innerText = data.errorAmount.toFixed(2);

                            document.getElementById("rangeTitle").innerText = "나의 예측 범위";
                            document.getElementById("rangeMin").innerText = min.toFixed(2);
                            document.getElementById("rangeMax").innerText = max.toFixed(2);

                            document.querySelector(".range-box").classList.remove("hide");
                            document.querySelector(".error-amount").classList.remove("hide");
                            document.querySelector(".wait-text").classList.remove("hide");
                        });

                }, duration);
            }
        }

        playExplosion();
    };


    /* ==============================
       전역 바인딩 (중요!)
    =============================== */
    window.openGoldModal = openGoldModal;
    window.closeGoldModal = closeGoldModal;

});
