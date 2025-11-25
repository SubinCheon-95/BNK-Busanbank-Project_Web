document.addEventListener('DOMContentLoaded', function () {
    const modal        = document.getElementById('chatModal');
    const openBtn      = document.getElementById('startChatBtn');
    const chatInput    = document.getElementById('chatInput');
    const chatMessages = document.getElementById('chatMessages');
    const chips        = modal ? modal.querySelectorAll('.chat-chips .chip') : [];
    const chatWindow   = modal ? modal.querySelector('.chat-window') : null;
    const chatHeader   = modal ? modal.querySelector('.chat-header') : null;
    let lastFocus      = null;

    // =========================
    // WebSocket / 세션 관련
    // =========================
    let ws        = null;
    let sessionId = null;

    // TODO: 로그인 연동 후 실제 userId 주입
    let userId       = 0;        // 지금은 임시값
    const senderType = 'USER';   // 고객 화면 기준

    // 템플릿에서 내려준 컨텍스트 경로 사용
    const contextPath = (window.CTX_PATH || '/').replace(/\/+$/, '/'); // 항상 끝에 / 하나만
    const wsScheme    = (location.protocol === 'https:') ? 'wss' : 'ws';
    const wsUrl       = `${wsScheme}://${location.host}${contextPath}ws/chat`;

    /* =========================
       ① 드래그 관련 변수 & 함수
       ========================= */
    let isDragging   = false;
    let dragStartX   = 0;
    let dragStartY   = 0;
    let windowStartX = 0;
    let windowStartY = 0;

    function onDragMouseDown(e) {
        if (!chatWindow) return;
        if (e.button !== 0) return; // 왼쪽 버튼만

        isDragging = true;
        const rect = chatWindow.getBoundingClientRect();

        dragStartX   = e.clientX;
        dragStartY   = e.clientY;
        windowStartX = rect.left;
        windowStartY = rect.top;

        chatWindow.style.left     = rect.left + 'px';
        chatWindow.style.top      = rect.top + 'px';
        chatWindow.style.right    = 'auto';
        chatWindow.style.bottom   = 'auto';
        chatWindow.style.position = 'fixed';

        document.addEventListener('mousemove', onDragMouseMove);
        document.addEventListener('mouseup', onDragMouseUp);
    }

    function onDragMouseMove(e) {
        if (!isDragging || !chatWindow) return;

        const dx = e.clientX - dragStartX;
        const dy = e.clientY - dragStartY;

        let newX = windowStartX + dx;
        let newY = windowStartY + dy;

        const maxX = window.innerWidth  - chatWindow.offsetWidth;
        const maxY = window.innerHeight - chatWindow.offsetHeight;

        if (newX < 0)    newX = 0;
        if (newY < 0)    newY = 0;
        if (newX > maxX) newX = maxX;
        if (newY > maxY) newY = maxY;

        chatWindow.style.left = newX + 'px';
        chatWindow.style.top  = newY + 'px';
    }

    function onDragMouseUp() {
        isDragging = false;
        document.removeEventListener('mousemove', onDragMouseMove);
        document.removeEventListener('mouseup', onDragMouseUp);
    }

    if (chatHeader && chatWindow) {
        chatHeader.addEventListener('mousedown', onDragMouseDown);
    }

    /* =========================
       모달 열기 / 닫기
       ========================= */
    function openModal(e) {
        if (e) e.preventDefault();
        if (!modal || !chatWindow) return;

        lastFocus = document.activeElement;

        chatWindow.style.right    = '24px';
        chatWindow.style.bottom   = '24px';
        chatWindow.style.left     = 'auto';
        chatWindow.style.top      = 'auto';
        chatWindow.style.position = 'absolute';

        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';

        const firstFocusable = modal.querySelector('.chip')
            || modal.querySelector('.icon-btn[data-chat-close]')
            || chatInput;
        if (firstFocusable) firstFocusable.focus();
    }

    function closeModal() {
        if (!modal) return;

        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';

        if (ws && ws.readyState === WebSocket.OPEN) {
            try {
                ws.close();
            } catch (e) {
                console.error(e);
            }
        }
        ws = null;
        sessionId = null;

        if (lastFocus) {
            lastFocus.focus();
            lastFocus = null;
        }
    }

    if (openBtn) {
        openBtn.addEventListener('click', openModal);
    }

    if (modal) {
        modal.addEventListener('click', function (e) {
            const closeBtn = e.target.closest('[data-chat-close]');
            if (closeBtn && closeBtn.classList.contains('icon-btn')) {
                closeModal();
            }
        });
    }

    window.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal && modal.classList.contains('is-open')) {
            closeModal();
        }
    });

    /* =========================
       말풍선 생성
       type: 'me' | 'agent' | 'system'
       ========================= */
    function appendMessage(text, type = 'me') {
        if (!text || !chatMessages) return;

        const row = document.createElement('div');
        row.classList.add('chat-row');

        if (type === 'me') {
            row.classList.add('me');
        }

        if (type === 'agent') {
            const avatar = document.createElement('img');
            avatar.className = 'chat-avatar';
            avatar.src = contextPath + 'images/cs/agent.png';
            avatar.alt = '상담원';
            row.appendChild(avatar);
        }

        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble';
        bubble.innerHTML = escapeHtml(text).replace(/\n/g, '<br>');
        row.appendChild(bubble);

        chatMessages.appendChild(row);

        requestAnimationFrame(() => {
            chatMessages.scrollTop = chatMessages.scrollHeight;
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    /* =========================
       WebSocket 연결
       ========================= */
    function connectWebSocket() {
        if (!sessionId) {
            console.error('sessionId가 없습니다. WebSocket 연결 불가');
            return;
        }

        ws = new WebSocket(wsUrl);

        ws.addEventListener('open', () => {
            const enterMsg = {
                type: 'ENTER',
                sessionId: sessionId,
                senderType: senderType,
                senderId: userId
            };
            ws.send(JSON.stringify(enterMsg));
        });

        ws.addEventListener('message', (event) => {
            const data = event.data;
            let msgObj;

            try {
                msgObj = JSON.parse(data);
            } catch (e) {
                appendMessage(data, 'agent');
                return;
            }

            // 다른 세션 메시지는 무시
            if (msgObj.sessionId && sessionId && msgObj.sessionId !== sessionId) {
                return;
            }

            if (msgObj.type === 'CHAT') {
                // 1) 내가 보낸 메시지가 다시 브로드캐스트된 경우 → 이미 화면에 찍었으니 무시
                if (msgObj.senderType === 'USER') {
                    // 로그인 붙이면 여기에서 senderId == userId 비교까지 가능
                    return;
                }

                // 2) 상담원이 보낸 메시지
                if (msgObj.senderType === 'AGENT') {
                    appendMessage(msgObj.message || '', 'agent');
                } else {
                    // 혹시 모르는 타입은 일단 agent 스타일로
                    appendMessage(msgObj.message || '', 'agent');
                }

            } else if (msgObj.type === 'END') {
                appendMessage('상담이 종료되었습니다.', 'system');
                if (ws) ws.close();
            } else if (msgObj.type === 'SYSTEM') {
                appendMessage(msgObj.message || '', 'system');
            }
        });

        ws.addEventListener('close', () => {
            console.log('WebSocket closed');
        });

        ws.addEventListener('error', (e) => {
            console.error('WebSocket error', e);
        });
    }

    /* =========================
       메시지 전송 공통 함수
       ========================= */
    function sendMessage(text) {
        const trimmed = text.trim();
        if (!trimmed) return;

        appendMessage(trimmed, 'me');

        if (ws && ws.readyState === WebSocket.OPEN && sessionId) {
            const chatMsg = {
                type: 'CHAT',
                sessionId: sessionId,
                senderType: senderType,
                senderId: userId,
                message: trimmed
            };
            ws.send(JSON.stringify(chatMsg));
        } else {
            console.warn('WebSocket이 열려있지 않아 서버로 전송하지 못했습니다.');
        }
    }

    /* =========================
       입력창: Enter 전송 / Shift+Enter 줄바꿈
       ========================= */
    if (chatInput) {
        chatInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage(chatInput.value);
                chatInput.value = '';
                chatInput.style.height = 'auto';
            }
        });

        chatInput.addEventListener('input', function () {
            this.style.height = 'auto';
            this.style.height = this.scrollHeight + 'px';
        });
    }

    /* =========================
       chips 클릭: inquiryType으로 세션 생성 + 첫 메시지
       ========================= */
    chips.forEach(function (chip) {
        chip.addEventListener('click', async function () {
            const inquiryType = chip.dataset.type || chip.textContent.trim();
            if (!inquiryType) return;

            try {
                if (!sessionId) {
                    const body = { inquiryType: inquiryType };

                    const res = await fetch(`${contextPath}cs/chat/start`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json;charset=UTF-8'
                        },
                        body: JSON.stringify(body)
                    });

                    // 디버그용 로그
                    console.log('[startChat] status=', res.status);

                    if (!res.ok) {
                        alert('상담 세션 생성에 실패했습니다.');
                        return;
                    }

                    const data = await res.json();
                    sessionId = data.sessionId;

                    connectWebSocket();
                }

                sendMessage(inquiryType);

            } catch (err) {
                console.error(err);
                alert('상담 시작 중 오류가 발생했습니다.');
            }
        });
    });
});
