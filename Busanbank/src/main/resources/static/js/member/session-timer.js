(function () {
    const TOTAL_SECONDS = 60 * 60;
    const WARN_AT = 10 * 60;
    let remaining = TOTAL_SECONDS;
    let intervalId = null;
    let warned = false;

    const timerEl = () => document.getElementById("session-timer");

    // 모달을 동적으로 생성 (한 번만)
    function ensureModal() {
        let modal = document.getElementById("session-warning-modal");
        if (modal) return modal;

        modal = document.createElement("div");
        modal.id = "session-warning-modal";
        // 전체 모달 박스
        modal.style.position = "fixed";
        modal.style.left = "50%";
        modal.style.top = "25%";
        modal.style.transform = "translateX(-50%)";
        modal.style.zIndex = "9999";
        modal.style.width = "450px";
        modal.style.borderRadius = "12px";
        modal.style.boxShadow = "0 6px 20px rgba(0,0,0,0.25)";
        modal.style.background = "#fff";
        modal.style.display = "none";
        modal.style.fontFamily = "'Pretendard', sans-serif";
        modal.style.overflow = "hidden";

        // 내용
        modal.innerHTML = `
        <!-- 상단 컬러 바 -->
        <div style="
            width: 100%;
            height: 10px;
            background: #662382;
        "></div>

        <!-- 본문 -->
        <div style="padding: 22px;">
            <div style="font-size: 20px; font-weight:700; margin-bottom:12px; color:#333;">
                세션 만료 알림
            </div>

            <div id="session-warning-msg" style="margin-bottom:20px; font-size:16px; color:#555;">
                10분 후 자동 로그아웃 됩니다.
            </div>

            <div style="text-align:right; margin-top:16px;">
                <button id="session-extend-btn" style="
                    margin-right:10px;
                    padding:8px 16px;
                    border-radius:6px;
                    border:none;
                    background:#662382;
                    color:#fff;
                    font-weight:600;
                    cursor:pointer;
                ">연장</button>

                <button id="session-close-btn" style="
                    padding:8px 16px;
                    border-radius:6px;
                    border:1px solid #ccc;
                    background:#f8f8f8;
                    color:#555;
                    cursor:pointer;
                ">닫기</button>
            </div>
        </div>
    `;


        document.body.appendChild(modal);

        // 버튼 이벤트
        document.getElementById("session-extend-btn").addEventListener("click", () => {
            remaining = TOTAL_SECONDS;
            warned = false; // 경고 리셋
            updateDisplay();
            hideModal();
        });
        document.getElementById("session-close-btn").addEventListener("click", () => {
            hideModal();
        });

        // 간단한 자동 닫힘(선택) - 예: 20초 후 자동 닫기
        return modal;
    }

    function showModal(message) {
        if (remaining <= 0) return;  // 이미 로그아웃 상태면 모달 띄우지 않음
        const modal = ensureModal();
        const msgEl = document.getElementById("session-warning-msg");
        if (msgEl) msgEl.textContent = message || "세션 만료가 곧 예정되어 있습니다.";
        modal.style.display = "block";
        modal.setAttribute("aria-hidden", "false");
    }

    function hideModal() {
        const modal = document.getElementById("session-warning-modal");
        if (!modal) return;
        modal.style.display = "none";
        modal.setAttribute("aria-hidden", "true");
    }

    // CSRF 토큰 가져오기
    function getCsrf() {
        const token = document.querySelector("meta[name='_csrf']")?.content;
        const header = document.querySelector("meta[name='_csrf_header']")?.content;
        return { token, header };
    }

    function format(sec) {
        const m = String(Math.floor(sec / 60)).padStart(2, "0");
        const s = String(sec % 60).padStart(2, "0");
        return `${m}:${s}`;
    }

    function updateDisplay() {
        const el = timerEl();
        if (el) el.textContent = format(remaining);
    }

    function doLogout() {
        clearInterval(intervalId); // ← 여기에 추가
        const csrf = getCsrf();
        const headers = { "Content-Type": "application/x-www-form-urlencoded" };
        if (csrf.token && csrf.header) headers[csrf.header] = csrf.token;

        fetch("/busanbank/member/logout", {
            method: "POST",
            headers: headers,
            body: ""
        }).finally(() => {
            window.location.href = "/busanbank/member/auto";
        });
    }

    function tick() {
        remaining--;
        updateDisplay();

        // 비차단 알림: alert() 대신 DOM 모달 사용
        if (!warned && remaining === WARN_AT) {
            warned = true;
            showModal("세션 시간이 30분 남았습니다. 계속 사용하려면 연장 버튼을 눌러주세요.");
            // 모달을 띄워도 타이머는 계속 돈다.
        }

        if (remaining <= 0) {
            clearInterval(intervalId);
            warned = true;  // 더 이상 경고하지 않음
            hideModal();
            doLogout();
        }
    }

    function start() {
        // 로그인 여부 체크
        const isLoggedIn = !!document.querySelector(".session-box");
        if (!isLoggedIn) return; // 로그인 안 되어 있으면 타이머 시작 X

        updateDisplay();
        intervalId = setInterval(tick, 1000);

        const extendBtn = document.querySelector(".session-box button");
        if (extendBtn) {
            extendBtn.addEventListener("click", () => {
                remaining = TOTAL_SECONDS;
                warned = false;
                updateDisplay();
                hideModal();
            });
        }

        ensureModal();
    }

    window.addEventListener("load", start);
})();
