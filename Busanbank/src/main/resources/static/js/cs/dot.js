const typingEl = document.getElementById("typingIndicator");
const dotsEl = typingEl?.querySelector(".dots");

let dotsTimer = null;
let dotsStep = 0;

function showTyping() {
    if (!typingEl) return;
    typingEl.classList.remove("hidden");

    if (!dotsEl) return;
    if (dotsTimer) return; // 이미 돌고 있으면 중복 방지

    dotsStep = 0;
    dotsTimer = setInterval(() => {
        dotsStep = (dotsStep + 1) % 4; // 0~3
        dotsEl.textContent = ".".repeat(dotsStep);
    }, 350);
}

function hideTyping() {
    if (!typingEl) return;
    typingEl.classList.add("hidden");

    if (dotsTimer) {
        clearInterval(dotsTimer);
        dotsTimer = null;
    }
    if (dotsEl) dotsEl.textContent = "";
}
