(function () {
    const TOTAL_SECONDS = 60 * 60;
    let remaining = TOTAL_SECONDS;
    let intervalId = null;

    const timerEl = () => document.getElementById("session-timer");

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
        if (remaining <= 0) {
            clearInterval(intervalId);
            doLogout();
        }
    }

    function start() {
        updateDisplay();
        intervalId = setInterval(tick, 1000);

        const extendBtn = document.querySelector(".session-box button");
        if(extendBtn) {
            extendBtn.addEventListener("click", () => {
                remaining = TOTAL_SECONDS;
                updateDisplay();
            });
        }
    }

    window.addEventListener("load", start);
})();
