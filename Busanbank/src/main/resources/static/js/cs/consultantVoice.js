(function () {
    const CTX = (window.CTX_PATH || "/").replace(/\/$/, "");

    const listEl = document.getElementById("voiceWaitingList");
    const countEl = document.getElementById("voiceWaitingCount");
    const btnRefresh = document.getElementById("btnVoiceRefresh");

    const voiceLabel = document.getElementById("currentVoiceSessionLabel");
    const btnHangup = document.getElementById("btnVoiceHangup");
    const frameWrap = document.getElementById("voiceFrameWrap");

    // ✅ 상담사(세션 로그인) 전용 엔드포인트
    const VOICE_BASE = "/cs/call/voice";

    // =========================
    // 상담사 Call WS (대기/배정 알림)
    // =========================
    let callWs = null;

    function connectCallAgentWs() {
        if (callWs && (callWs.readyState === WebSocket.OPEN || callWs.readyState === WebSocket.CONNECTING)) return;

        const consultantId = window.CONSULTANT_ID;
        if (!consultantId) {
            console.warn("CONSULTANT_ID not found");
            return;
        }

        const wsUrl =
            (location.protocol === "https:" ? "wss://" : "ws://") +
            location.host +
            `${CTX}/ws/call-agent?consultantId=${encodeURIComponent(consultantId)}`;

        callWs = new WebSocket(wsUrl);

        callWs.onopen = () => console.log("📡 CallAgent WS connected");

        callWs.onmessage = (evt) => {
            try {
                const msg = JSON.parse(evt.data);
                console.log("📨 CallAgent WS msg:", msg);

                switch (msg.type) {
                    case "VOICE_ENQUEUED":
                    case "VOICE_ACCEPTED":
                    case "VOICE_ENDED":
                    case "CALL_ASSIGNED":
                        refresh().catch(console.error);
                        break;
                }
            } catch (e) {
                console.error("WS parse error", e);
            }
        };

        callWs.onclose = () => {
            console.warn("🔌 CallAgent WS closed. retry...");
            setTimeout(connectCallAgentWs, 2000);
        };

        callWs.onerror = (e) => console.error("CallAgent WS error", e);
    }

    // =========================
    // 팝업/상태 관리
    // =========================
    let voicePopup = null;
    let popupWatchTimer = null;

    let currentSessionId = null;   // UI 표기용
    let acceptedSessionId = null;  // accept 성공한 세션만 end 대상 (지금 흐름: agent.html에서 accept하므로 여기서는 null 유지)

    function clearPopupWatch() {
        if (popupWatchTimer) {
            clearInterval(popupWatchTimer);
            popupWatchTimer = null;
        }
    }

    function closeVoicePopup() {
        clearPopupWatch();
        try {
            if (voicePopup && !voicePopup.closed) voicePopup.close();
        } catch (_) {}
        voicePopup = null;
    }

    // ✅ 팝업 차단 99% 회피 버전
    // - noopener/noreferrer 제거
    // - about:blank 대신 "" 사용
    function openBlankPopupSync() {
        const features = [
            "popup=yes",
            "width=1100",
            "height=760",
            "left=120",
            "top=80"
        ].join(",");

        // 이미 열려 있으면 재사용
        if (voicePopup && !voicePopup.closed) {
            try { voicePopup.focus(); } catch (_) {}
            return voicePopup;
        }

        // ✅ 핵심: 동기 click에서 바로 open
        voicePopup = window.open("", "voiceAgentPopup", features);

        if (!voicePopup) {
            alert("팝업이 차단되었습니다.\n- 주소창 오른쪽 팝업 아이콘에서 허용\n- 확장프로그램(광고차단/보안) 잠시 OFF\n- 사이트 설정에서 팝업 허용");
            return null;
        }

        // 빈 화면 표시(선택)
        try {
            voicePopup.document.open();
            voicePopup.document.write(
                "<!doctype html><html><head><title>연결 중...</title></head>" +
                "<body style='font-family:system-ui;padding:20px'>음성 상담 화면 여는 중...</body></html>"
            );
            voicePopup.document.close();
        } catch (_) {}

        return voicePopup;
    }

    function navigatePopupToAgent(sessionId) {
        if (!voicePopup || voicePopup.closed) return;

        // ✅ 절대경로로 이동 (CTX 꼬임 방지)
        const origin = window.location.origin;
        const url = `${origin}${CTX}/voice/agent.html?sessionId=${encodeURIComponent(sessionId)}`;

        try {
            voicePopup.location.replace(url);
            voicePopup.focus();
        } catch (e) {
            console.error("popup navigate failed", e);
        }
    }

    function startPopupWatch() {
        clearPopupWatch();
        popupWatchTimer = setInterval(async () => {
            if (!voicePopup || voicePopup.closed) {
                clearPopupWatch();
                voicePopup = null;

                // ✅ 현재 흐름은 agent.html에서 accept/end 처리하므로
                // 여기서는 자동 end 하지 말고 UI만 정리
                acceptedSessionId = null;
                currentSessionId = null;

                voiceLabel.textContent = "없음";
                btnHangup.disabled = true;
                frameWrap?.classList.remove("is-open");
            }
        }, 800);
    }

    // =========================
    // API 공통
    // =========================
    async function api(path, options = {}) {
        const res = await fetch(CTX + path, {
            credentials: "same-origin",
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {}),
            },
        });

        const ct = res.headers.get("content-type") || "";
        const body = ct.includes("application/json")
            ? await res.json().catch(() => ({}))
            : await res.text();

        if (!res.ok) {
            const msg =
                (typeof body === "string" && body) ||
                body?.message ||
                body?.error ||
                JSON.stringify(body);
            throw new Error(msg);
        }
        return body;
    }

    // =========================
    // 리스트 렌더링
    // =========================
    function render(list) {
        if (!listEl) return;

        listEl.innerHTML = "";
        if (countEl) countEl.textContent = `${list.length}건`;

        if (!list.length) {
            const li = document.createElement("li");
            li.innerHTML = `
        <div class="agent-session-main">
          <span class="agent-session-meta">대기중 전화가 없습니다.</span>
        </div>`;
            listEl.appendChild(li);
            return;
        }

        list.forEach((s) => {
            const li = document.createElement("li");
            li.dataset.sessionId = s.sessionId;

            li.innerHTML = `
        <div class="agent-session-main">
          <span class="agent-session-id">콜 #${s.sessionId}</span>
          <span class="agent-session-meta">${s.status ?? ""}</span>
        </div>
        <button type="button" class="agent-btn agent-btn-primary" data-accept>수락</button>
      `;

            // ✅ 절대 async로 만들지 말 것 (팝업 차단 원인)
            li.querySelector("[data-accept]").addEventListener("click", () => {
                const sid = s.sessionId;

                // 1) 클릭 즉시 팝업 오픈(동기)
                const popup = openBlankPopupSync();
                if (!popup) return;

                // 2) 팝업 닫힘 감시
                startPopupWatch();

                // 3) 팝업을 agent.html로 이동
                navigatePopupToAgent(sid);

                // 4) UI 표기만 갱신 (accept는 agent.html Join에서)
                currentSessionId = sid;
                acceptedSessionId = null;
                voiceLabel.textContent = sid;
                btnHangup.disabled = false;
                frameWrap?.classList.add("is-open");
            });

            listEl.appendChild(li);
        });
    }

    // =========================
    // 대기 목록 갱신
    // =========================
    async function refresh() {
        const data = await api(`${VOICE_BASE}/waiting`, { method: "GET" });
        render(Array.isArray(data) ? data : []);
    }

    btnRefresh?.addEventListener("click", () => refresh().catch(console.error));

    // =========================
    // 통화 종료(수동)
    // - "수락"을 agent.html에서 하기 때문에, 여기 end는 '현재 표시된 세션'을 그냥 end 처리
    // =========================
    btnHangup?.addEventListener("click", async () => {
        const sid = voiceLabel?.textContent;
        if (!sid || sid === "없음") return;

        try {
            const r = await api(`${VOICE_BASE}/${encodeURIComponent(sid)}/end`, { method: "POST" });
            if (r && typeof r === "object" && r.ok === false) {
                alert(`종료 실패: ${r.reason ?? "UNKNOWN"}`);
                return;
            }

            closeVoicePopup();
            acceptedSessionId = null;
            currentSessionId = null;

            voiceLabel.textContent = "없음";
            btnHangup.disabled = true;
            frameWrap?.classList.remove("is-open");

            await refresh();
        } catch (e) {
            alert("종료 중 오류: " + e.message);
            console.error(e);
        }
    });

    // =========================
    // init
    // =========================
    function init() {
        connectCallAgentWs();
        refresh().catch(console.error);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
