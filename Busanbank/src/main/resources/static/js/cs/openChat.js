document.addEventListener('DOMContentLoaded', function () {
    const modal        = document.getElementById('chatModal');
    const openBtn      = document.getElementById('startChatBtn');
    const chatInput    = document.getElementById('chatInput');
    const chatMessages = document.getElementById('chatMessages');
    const initialChatHtml = chatMessages ? chatMessages.innerHTML : '';
    const chatWindow   = modal ? modal.querySelector('.chat-window') : null;
    const chatHeader   = modal ? modal.querySelector('.chat-header') : null;
    const endBtn       = modal ? modal.querySelector('[data-chat-end]') : null;
    const typingEl = document.getElementById("typingIndicator");
    const dotsEl = typingEl ? typingEl.querySelector(".dots") : null;

    // ✅ 상품 가입 STEP4 버튼 (지금 쓰고 있는 버튼)
    const productChatBtn = document.getElementById('productChatBtn');

    // ✅ 상품 메인 페이지용 “상담 신청하기” 버튼 (여러 개 있을 수 있음)
    const productMainChatBtns = document.querySelectorAll('.product-chat-open');

    let lastFocus      = null;

    // =========================
    // WebSocket / 세션 관련
    // =========================
    let ws        = null;
    let sessionId = null;

    // TODO: 로그인 연동 후 실제 userId 주입
    let userId       = 0;        // 지금은 임시값
    const senderType = 'USER';   // 고객 화면 기준

    // WebSocket이 열릴 때 서버로 보내줄 최초 메시지(칩/상품가입 버튼 등)
    let initialMessage = null;

    // 템플릿에서 내려준 컨텍스트 경로 사용
    // const contextPath = (window.CTX_PATH || '/').replace(/\/+$/, '/');
    const contextPath = '/busanbank/';
    const wsScheme    = (location.protocol === 'https:') ? 'wss' : 'ws';
    const wsUrl       = `${wsScheme}://${location.host}${contextPath}ws/chat`;

    // =========================
    // 타이핑 표시 코드
    // =========================
    let dotsTimer = null;
    let dotsStep = 0;
    let hideTimer = null;

    function showTyping() {
        if (!typingEl) return;
        typingEl.classList.remove("hidden");

        if (dotsEl && !dotsTimer) {
            dotsStep = 0;
            dotsTimer = setInterval(() => {
                dotsStep = (dotsStep + 1) % 4;
                dotsEl.textContent = ".".repeat(dotsStep);
            }, 350);
        }

        // ✅ 혹시 "stop" 신호를 못 받는 경우 대비(안전장치)
        clearTimeout(hideTimer);
        hideTimer = setTimeout(() => hideTyping(), 2500);
    }

    function hideTyping() {
        if (!typingEl) return;
        typingEl.classList.add("hidden");

        if (dotsTimer) {
            clearInterval(dotsTimer);
            dotsTimer = null;
        }
        if (dotsEl) dotsEl.textContent = "";
        clearTimeout(hideTimer);
        hideTimer = null;
    }

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

        // 🔹 새 상담창 열 때 입력창만 초기화
        if (chatInput) {
            chatInput.value = '';
            chatInput.style.height = 'auto';
        }

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

        // ✅ 추가 (닫을 때 무조건 숨김)
        hideTyping();

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
        sessionId = null;   // 세션 ID 리셋
        initialMessage = null;

        // 🔹 화면 말풍선/초기 안내 + chips 복원
        if (chatMessages) {
            chatMessages.innerHTML = initialChatHtml;
        }
        if (chatInput) {
            chatInput.value = '';
            chatInput.style.height = 'auto';
        }

        if (lastFocus) {
            lastFocus.focus();
            lastFocus = null;
        }
    }

    // CS 페이지에서 쓰는 기본 열기 버튼
    if (openBtn) {
        openBtn.addEventListener('click', openModal);
    }

    // ✅ 상품 메인 페이지: 상담 신청하기 버튼 → 모달만 열기 (chips 선택 후 세션 시작)
    if (productMainChatBtns && productMainChatBtns.length > 0) {
        productMainChatBtns.forEach(btn => {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                openModal();
                // 여기서는 startChatWithType 호출 안 함 (칩 클릭 시 시작)
            });
        });
    }

    // 닫기(X) 버튼 처리 + chip 클릭 위임
    if (modal) {
        modal.addEventListener('click', function (e) {
            // 닫기 버튼
            const closeBtn = e.target.closest('[data-chat-close]');
            if (closeBtn && closeBtn.classList.contains('icon-btn')) {
                closeModal();
                return;
            }

            // 🔹 chips 클릭 (이벤트 위임)
            const chip = e.target.closest('.chat-chips .chip');
            if (chip) {
                const inquiryType = chip.dataset.type || chip.textContent.trim();
                startChatWithType(inquiryType);
            }
        });
    }

    // =========================
    // 상담 종료 버튼 클릭
    // =========================
    if (endBtn) {
        endBtn.addEventListener('click', function (e) {
            e.preventDefault();

            if (!sessionId) {
                // 아직 세션이 없으면 그냥 닫기만
                closeModal();
                return;
            }

            // 서버에 END 메시지 전송
            try {
                if (ws && ws.readyState === WebSocket.OPEN) {
                    const endMsg = {
                        type: 'END',
                        sessionId: sessionId,
                        senderType: senderType, // 'USER'
                        senderId: userId
                    };
                    ws.send(JSON.stringify(endMsg));
                }
            } catch (err) {
                console.error('END 전송 중 오류', err);
            }

            // 화면에서는 모달 닫기
            closeModal();
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
       🔹 과거 메시지 로딩 함수
       ========================= */
    function loadPreviousMessages(sessId) {
        const url = `${contextPath}cs/chat/messages?sessionId=${sessId}`;

        return fetch(url, {
            method: "GET",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then(res => res.json())
            .then(list => {
                list.forEach(m => {
                    let type;
                    if (m.senderType === 'USER') {
                        type = 'me';
                    } else if (m.senderType === 'AGENT') {
                        type = 'agent';
                    } else {
                        type = 'system';
                    }
                    appendMessage(m.messageText, type);
                });
                // 과거 메시지 로딩 후 읽음 처리
                markMessagesRead(sessId);
            })
            .catch(err => {
                console.error('이전 메시지 불러오기 실패', err);
                appendMessage("이전 대화를 불러오지 못했습니다.", "system");
            });
    }

    // 🔹 읽음 처리 API 호출
    function markMessagesRead(sessId) {
        const url = `${contextPath}cs/chat/messages/read?sessionId=${sessId}`;

        fetch(url, {
            method: "POST",
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        }).catch(err => {
            console.error('읽음 처리 실패', err);
        });
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

            // 🔹 초기 메시지 있으면, open 된 뒤에 전송
            if (initialMessage) {
                const chatMsg = {
                    type: 'CHAT',
                    sessionId: sessionId,
                    senderType: senderType,
                    senderId: userId,
                    message: initialMessage
                };
                ws.send(JSON.stringify(chatMsg));
                initialMessage = null; // 한 번 전송 후 초기화
            }
        });

        ws.addEventListener('message', (event) => {
            console.log('[WS IN]', event.data);
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
            if (msgObj.type === 'TYPING' && msgObj.senderType === 'AGENT') {
                if (msgObj.isTyping) showTyping();
                else hideTyping();
                return;
            }

            if (msgObj.type === 'CHAT') {
                // 상담원 메시지 오면 typing 자동 종료
                if (msgObj.senderType === 'AGENT') hideTyping();

                if (msgObj.senderType === 'USER') return;
                appendMessage(msgObj.message || '', 'agent');

            } else if (msgObj.type === 'END') {
                hideTyping();
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
       공통: 특정 inquiryType으로 상담 시작
       ========================= */
    async function startChatWithType(inquiryType) {
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

                console.log('[startChat] status=', res.status);

                if (res.status === 401) {
                    alert('로그인이 필요합니다. 로그인 후 다시 상담을 신청해 주세요.');
                    // 필요하면 로그인 페이지로 이동
                    // window.location.href = contextPath + 'member/login';
                    return;
                }

                if (!res.ok) {
                    alert('상담 세션 생성에 실패했습니다.');
                    return;
                }

                const data = await res.json();
                sessionId = data.sessionId;

                // 1) 과거 메시지 먼저 로딩
                await loadPreviousMessages(sessionId);

                // 2) 화면에 내 말풍선 먼저 찍어주고
                appendMessage(inquiryType, 'me');

                // 3) WebSocket 연결하면서, open 된 뒤 서버로 첫 메시지 전송
                initialMessage = inquiryType;
                connectWebSocket();

            } else {
                // 이미 세션/웹소켓 있는 상태면 기존 sendMessage 그대로 사용
                sendMessage(inquiryType);
            }

        } catch (err) {
            console.error(err);
            alert('상담 시작 중 오류가 발생했습니다.');
        }
    }

    /* =========================
       상품 가입 step4: 상담하기 버튼
       ========================= */
    if (productChatBtn) {
        productChatBtn.addEventListener('click', function (e) {
            e.preventDefault();

            const inquiryType = productChatBtn.dataset.inquiryType || '상품 가입';

            // 1) 모달 열기
            openModal();

            // 2) 지정 타입으로 바로 상담 시작
            startChatWithType(inquiryType);
        });
    }
});
