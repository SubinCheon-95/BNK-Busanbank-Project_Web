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

        const loadingMsg = appendMessage("Gemini", "");
        loadingMsg.classList.add("loading-dots");

        loadingMsg.innerHTML = '<span>.</span><span>.</span><span>.</span>';

        try {
            const res = await fetch("/busanbank/chatbot/ask", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ message: userMsg })
            });
            let reply = "(응답 형식이 이상합니다)";
            try {
                const json = await res.json();
                reply = json.answer || "(응답 없음)";
            } catch (err) {
                reply = "(JSON 파싱 실패)";
            }

            // 로딩 점 제거하고 실제 답변 표시
            loadingMsg.classList.remove("loading-dots");
            loadingMsg.textContent = reply;
            chatBox.scrollTop = chatBox.scrollHeight;

        } catch (err) {
            appendMessage("Gemini", "(에러 발생: " + err.message + ")");
        }
    });

    function appendMessage(sender, message) {
        const msgBox = document.createElement("div");

        if (sender === "나") {
            msgBox.className = "user-msg";
            msgBox.textContent = message;
        } else {
            msgBox.className = "bot-msg";

            // 챗봇 이미지 + 말풍선
            const container = document.createElement("div");
            container.className = "bot-msg-container"; // flex로 이미지 + 말풍선

            const profile = document.createElement("img");
            profile.src = "/busanbank/images/member/chatIcon.png"; // 실제 웹 경로
            profile.className = "bot-profile";

            const bubble = document.createElement("div");
            bubble.className = "bot-msg";
            if (message === "") {
                bubble.classList.add("loading-dots");
                bubble.innerHTML = '<span>.</span><span>.</span><span>.</span>';
            } else {
                bubble.textContent = message;
            }

            container.appendChild(profile);
            container.appendChild(bubble);

            chatBox.appendChild(container);
            chatBox.scrollTop = chatBox.scrollHeight;
            return bubble;
        }

        chatBox.appendChild(msgBox);
        chatBox.scrollTop = chatBox.scrollHeight;
        return msgBox;
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
