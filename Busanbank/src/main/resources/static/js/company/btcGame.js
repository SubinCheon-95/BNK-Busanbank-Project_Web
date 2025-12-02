/*
    수정일 : 2025/12/02
    수정자 : 윤종인
    내용 : 업 앤 다운(비트코인 이벤트) 업데이트
*/

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById("bitcoinModal");
    const openBtn = document.getElementById("openModal");

    // 페이지 로드 시 모달 보이게
    modal.classList.add("active");
    modal.classList.add("animate-slideUp");
    yesterdayAndTodayPrice();

    // 클릭하면 닫기
    openBtn.addEventListener("click", () => {
        modal.classList.remove("active");
        modal.classList.remove("animate-slideUp");
    });

    async function yesterdayAndTodayPrice() {
        try {
            const response = await fetch(`/busanbank/api/coin/history/btc/yesterdayAndToday?symbol=BTC`);
            const json = await response.json();
            console.log(json);

            const yesterday = json[0].price;
            const today = json[1].price;
            console.log(`yesterday = ${yesterday}, today = ${today}`);

            document.getElementById('priceDisplay').textContent = Math.round(yesterday) + ' USD';
        } catch (error) {
            console.error('가격 가져오기 실패:', error);
        }
    }

    let result = '';
    const upBtn = document.getElementById('up_btn');
    const downBtn = document.getElementById('down_btn');

    // 버튼 클릭 이벤트
    upBtn.addEventListener("click", () => checkResult("up"));
    downBtn.addEventListener("click", () => checkResult("down"));

    async function checkResult(choice) {
        try {
            const response = await fetch(`/busanbank/api/coin/history/btc/yesterdayAndToday?symbol=BTC`);
            const json = await response.json();
            console.log(json);

            const yesterday = json[0].price;
            const today = json[1].price;

            console.log(`yesterday = ${yesterday}, today = ${today}`);

            const actual = yesterday < today ? "up" : "down";

            if (choice === actual) {
                console.log("성공");

                fetch("/busanbank/company/btcEvent", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ result: "success" })
                })
                    .then(res => res.text())
                    .then(msg => {
                        console.log("서버 응답:", msg);
                        showSuccessModal(yesterday,today);
                    });
            } else {
                fetch("/busanbank/company/btcEvent", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ result: "fail" })
                })
                    .then(msg => {
                        console.log("실패");
                        showFailModal(yesterday, today);
                    });
            }

            document.getElementById('priceDisplay').textContent = Math.round(yesterday) + ' USD';

        } catch (error) {
            console.error('가격 가져오기 실패:', error);
        }
    }

    function showSuccessModal(yesterday, today) {
        const modal = document.getElementById('bitcoinModal');
        const priceChange = ((today - yesterday) / yesterday * 100).toFixed(2);

        modal.innerHTML = `
                    <!-- Close Button -->
                    <button onclick="location.reload()" class="absolute top-6 right-6 text-gray-400 hover:text-gray-600 transition">
                        <i class="fa-solid fa-xmark text-xl"></i>
                    </button>

                    <!-- Success Animation -->
                    <div class="text-center mb-6">
                        <div class="text-8xl mb-4 animate-bounce">🎉</div>
                        <div class="flex justify-center gap-4 mb-4">
                            <span class="text-5xl animate-pulse">✨</span>
                            <span class="text-5xl animate-pulse" style="animation-delay: 0.2s">🎊</span>
                            <span class="text-5xl animate-pulse" style="animation-delay: 0.4s">✨</span>
                        </div>
                    </div>

                    <!-- Success Message -->
                    <div class="text-center mb-6">
                        <h2 class="text-3xl font-bold text-green-600 mb-3">예측 성공!</h2>
                        <p class="text-gray-600 text-lg mb-4">축하합니다! 정확하게 예측하셨어요</p>

                        <div class="bg-gradient-to-br from-green-50 to-emerald-50 rounded-2xl p-6 mb-4">
                            <p class="text-gray-600 text-sm mb-2">가격 변동</p>
                            <p class="text-2xl font-bold text-green-600 mb-1">
                                ${Math.round(yesterday).toLocaleString()} → ${Math.round(today).toLocaleString()} USD
                            </p>
                            <p id="priceChangeDisplay" class="text-green-600 font-semibold">
                                <i class="fa-solid fa-arrow-up"></i>
                            </p>
                        </div>

                        <div class="bg-yellow-50 border-2 border-yellow-200 rounded-xl p-4 mb-4">
                            <p class="text-yellow-800 font-bold text-lg">🎁 리워드 지급 완료!</p>
                        </div>
                    </div>

                    <!-- Close Button -->
                    <button id="rewardBtn" class="w-full bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white py-4 rounded-xl text-lg font-bold transition-all duration-200 transform hover:scale-105">
                        확인
                    </button>

                    <style>
                        @keyframes pulse {
                            0%, 100% { transform: scale(1); opacity: 1; }
                            50% { transform: scale(1.1); opacity: 0.8; }
                        }
                        .animate-pulse {
                            animation: pulse 1s ease-in-out infinite;
                        }
                    </style>
                `;

        const priceChangeEl = document.getElementById("priceChangeDisplay");

        if (priceChange > 0) {
            priceChangeEl.innerHTML = `
                        <i class="fa-solid fa-arrow-up"></i>
                    `;
        } else if (priceChange < 0) {
            priceChangeEl.innerHTML = `
                        <i class="fa-solid fa-arrow-down"></i> ${priceChange}% 하락
                    `;
        }

        const rewardBtn = document.querySelector("#rewardBtn");
        rewardBtn.addEventListener("click", () => {
            modal.classList.remove("active");
        });
    }

    function showFailModal(yesterday, today) {
        const modal = document.getElementById('bitcoinModal');
        const priceChange = ((today - yesterday) / yesterday * 100).toFixed(2);
        const isUp = today > yesterday;

        modal.innerHTML = `
                    <!-- Close Button -->
                    <button onclick="location.reload()" class="absolute top-6 right-6 text-gray-400 hover:text-gray-600 transition">
                        <i class="fa-solid fa-xmark text-xl"></i>
                    </button>

                    <!-- Fail Animation -->
                    <div class="text-center mb-6">
                        <div class="text-8xl mb-4">😢</div>
                    </div>

                    <!-- Fail Message -->
                    <div class="text-center mb-6">
                        <h2 class="text-3xl font-bold text-gray-800 mb-3">아쉬워요!</h2>
                        <p class="text-gray-600 text-lg mb-4">다음 기회에 다시 도전해보세요</p>

                        <div class="bg-gradient-to-br from-gray-50 to-slate-50 rounded-2xl p-6 mb-4">
                            <p class="text-gray-600 text-sm mb-2">실제 가격 변동</p>
                            <p class="text-2xl font-bold text-gray-800 mb-1">
                                ${Math.round(yesterday).toLocaleString()} → ${Math.round(today).toLocaleString()} USD
                            </p>
                            <p class="${isUp ? 'text-green-600' : 'text-red-600'} font-semibold">
                                <i class="fa-solid fa-arrow-${isUp ? 'up' : 'down'}"></i> ${Math.abs(priceChange)}% ${isUp ? '상승' : '하락'}
                            </p>
                        </div>
                    </div>

                    <!-- Close Button -->
                    <button id="failBtn" class="w-full bg-gradient-to-r from-gray-500 to-slate-600 hover:from-gray-600 hover:to-slate-700 text-white py-4 rounded-xl text-lg font-bold transition-all duration-200 transform hover:scale-105">
                        확인
                    </button>
                `;

        const failBtn = document.querySelector("#failBtn");
        failBtn.addEventListener("click", () => {
            modal.classList.remove("active");
        });
    }
});
