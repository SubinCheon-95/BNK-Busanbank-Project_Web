/*
    날짜 : 2025/11/23
    이름 : 오서정
    내용 : 챗봇 스크립트 작성 (외부 JS 파일)
*/
document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("chat-form");
    const input = document.getElementById("chat-input");
    const chatBox = document.getElementById("chat-box");

    if (!form || !input || !chatBox) {
        console.error("챗봇 요소를 찾을 수 없습니다. HTML 구조를 확인하세요.");
        return;
    }

    window.chatForm = form;
    window.chatInput = input;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const userMsg = input.value.trim();
        if (!userMsg) return;

        appendMessage("나", userMsg);
        input.value = "";

        try {
            const res = await fetch("/busanbank/member/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(userMsg)
            });

            let reply = "(응답 형식이 이상합니다)";
            try {
                const json = await res.json();
                const text = json.candidates?.[0]?.content?.parts?.[0]?.text;
                if (text) reply = text;
            } catch (err) {
                reply = "(JSON 파싱 실패)";
            }

            appendMessage("Gemini", reply);
        } catch (err) {
            appendMessage("Gemini", "(에러 발생: " + err.message + ")");
        }
    });

    function appendMessage(sender, message) {
        const msgBox = document.createElement("div");

        if (sender === "나") {
            msgBox.className = "user-msg";
        } else {
            msgBox.className = "bot-msg";
        }

        msgBox.textContent = message;
        chatBox.appendChild(msgBox);
        chatBox.scrollTop = chatBox.scrollHeight;
    }
});

document.addEventListener("click", (e) => {
    if (e.target.classList.contains("keyword-btn")) {

        const text = e.target.getAttribute("data-text");

        // 메시지 입력
        window.chatInput.value = text;

        // 자동 전송
        window.chatForm.dispatchEvent(new Event("submit"));
    }
});
