(function () {
    const CTX = (window.CTX_PATH || "/").replace(/\/$/, "");

    const listEl = document.getElementById("voiceWaitingList");
    const countEl = document.getElementById("voiceWaitingCount");
    const btnRefresh = document.getElementById("btnVoiceRefresh");

    const voiceFrame = document.getElementById("voiceFrame");
    const voiceLabel = document.getElementById("currentVoiceSessionLabel");
    const btnHangup = document.getElementById("btnVoiceHangup");

    async function api(path, options = {}) {
        const res = await fetch(CTX + path, {
            credentials: "same-origin",
            ...options,
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            }
        });
        if (!res.ok) throw new Error(await res.text());
        return res.json();
    }

    function render(list) {
        listEl.innerHTML = "";
        countEl.textContent = `${list.length}건`;

        if (!list.length) {
            const li = document.createElement("li");
            li.innerHTML = `<div class="agent-session-main"><span class="agent-session-meta">대기중 전화가 없습니다.</span></div>`;
            listEl.appendChild(li);
            return;
        }

        list.forEach((s) => {
            const li = document.createElement("li");
            li.dataset.sessionId = s.sessionId;
            li.innerHTML = `
        <div class="agent-session-main">
          <span class="agent-session-id">콜 #${s.sessionId}</span>
          <span class="agent-session-meta">${s.status}</span>
        </div>
        <button type="button" class="agent-btn agent-btn-primary" data-accept>수락</button>
      `;

            li.querySelector("[data-accept]").addEventListener("click", async () => {
                try {
                    const r = await api(`/api/call/voice/${encodeURIComponent(s.sessionId)}/accept`, { method: "POST" });

                    if (!r.ok) {
                        alert(`수락 실패: ${r.reason}`);
                        return;
                    }

                    voiceLabel.textContent = s.sessionId;
                    btnHangup.disabled = false;

                    // ✅ 전화 iframe 열기
                    document.getElementById("voiceFrameWrap")?.classList.add("is-open");

                    // ✅ 기존 Agora agent 페이지 재사용
                    voiceFrame.src = `${CTX}/voice/agent.html?sessionId=${encodeURIComponent(s.sessionId)}`;

                    await refresh();
                } catch (e) {
                    alert("수락 중 오류: " + e.message);
                    console.error(e);
                }
            });

            listEl.appendChild(li);
        });
    }

    async function refresh() {
        const data = await api("/api/call/voice/waiting", { method: "GET" });
        render(data);
    }

    btnRefresh?.addEventListener("click", refresh);

    btnHangup?.addEventListener("click", async () => {
        const sessionId = voiceLabel.textContent;
        if (!sessionId || sessionId === "없음") return;

        try {
            await api(`/api/call/voice/${encodeURIComponent(sessionId)}/end`, { method: "POST" });
            voiceFrame.src = "";
            voiceLabel.textContent = "없음";
            btnHangup.disabled = true;

            // ✅ 전화 iframe 닫기
            document.getElementById("voiceFrameWrap")?.classList.remove("is-open");

            await refresh();
        } catch (e) {
            alert("종료 중 오류: " + e.message);
            console.error(e);
        }
    });

    // 최초 로딩
    if (listEl && countEl) refresh().catch(console.error);
})();
