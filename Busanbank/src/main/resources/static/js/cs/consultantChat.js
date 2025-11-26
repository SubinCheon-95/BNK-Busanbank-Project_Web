document.addEventListener('DOMContentLoaded', function () {

    // ===== 필수 DOM =====
    const agentConsole   = document.getElementById('chatAgentConsole');
    if (!agentConsole) return; // 상담원 화면이 아닐 때 방어

    const waitingList    = document.getElementById('waitingList');    // 대기중 세션 목록
    const chattingList   = document.getElementById('chattingList');   // 진행중 세션 목록
    const chatMessages   = document.getElementById('agentChatMessages');
    const chatInput      = document.getElementById('agentChatInput');
    const currentSessionLabel = document.getElementById('currentSessionLabel');

    // 상담원 ID (템플릿에서 data-consultant-id로 내려줌)
    const consultantId = parseInt(agentConsole.dataset.consultantId || '0', 10);
    if (!consultantId) {
        console.warn('consultantId가 설정되어 있지 않습니다. data-consultant-id를 확인하세요.');
    }

    // ===== WebSocket 공통 설정 =====
    const contextPath = (window.CTX_PATH || '/').replace(/\/+$/, '/'); // 항상 마지막에 / 하나
    const wsScheme    = (location.protocol === 'https:') ? 'wss' : 'ws';
    const wsUrl       = `${wsScheme}://${location.host}${contextPath}ws/chat`;

    const senderType = 'AGENT'; // 상담원
    let ws = null;
    let currentSessionId = null;
    let activeSessionLi = null; // 좌측 목록에서 선택된 li

    // =========================
    // 말풍선 생성
    // type: 'me' | 'user' | 'system'
    // =========================
    function appendMessage(text, type) {
        if (!text || !chatMessages) return;

        const row = document.createElement('div');
        row.classList.add('chat-row');

        if (type === 'me') {
            row.classList.add('me');      // 상담원 (오른쪽)
        } else if (type === 'system') {
            row.classList.add('system');  // 안내 메시지
        } else {
            row.classList.add('user');    // 고객
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

    // =========================
    // WebSocket 연결/해제
    // =========================
    function connectWebSocket() {
        if (!currentSessionId) {
            console.error('currentSessionId가 없습니다. WebSocket 연결 불가');
            return;
        }
        if (!consultantId) {
            console.error('consultantId가 없습니다. WebSocket 연결 불가');
            return;
        }

        // 기존 연결 정리
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.close();
        }

        ws = new WebSocket(wsUrl);

        ws.addEventListener('open', () => {
            console.log('Agent WebSocket opened:', wsUrl);

            // 상담원 세션 참가 알림
            const enterMsg = {
                type: 'ENTER',
                sessionId: currentSessionId,
                senderType: senderType,
                senderId: consultantId
            };
            ws.send(JSON.stringify(enterMsg));
        });

        ws.addEventListener('message', (event) => {
            const data = event.data;
            let msgObj;

            try {
                msgObj = JSON.parse(data);
            } catch (e) {
                // 문자열로만 온 경우 일단 고객 메시지로 처리
                appendMessage(data, 'user');
                return;
            }

            // 다른 세션 메시기는 무시
            if (msgObj.sessionId && currentSessionId && msgObj.sessionId !== currentSessionId) {
                return;
            }

            if (msgObj.type === 'CHAT') {

                // 1) 내가 보낸 메시지가 브로드캐스트로 다시 온 경우 → 이미 appendMessage('me') 했으니 무시
                if (msgObj.senderType === 'AGENT' && msgObj.senderId === consultantId) {
                    return;
                }

                // 2) 고객이 보낸 메시지
                if (msgObj.senderType === 'USER') {
                    appendMessage(msgObj.message || '', 'user');
                    return;
                }

                // 3) 그 외는 시스템처럼
                appendMessage(msgObj.message || '', 'system');

            } else if (msgObj.type === 'END') {
                // 서버에서 상담 종료 브로드캐스트
                appendMessage('상담이 종료되었습니다.', 'system');
                if (chatInput) {
                    chatInput.disabled = true;
                }
                if (ws) ws.close();

            } else if (msgObj.type === 'SYSTEM') {
                appendMessage(msgObj.message || '', 'system');
            }
        });

        ws.addEventListener('close', () => {
            console.log('Agent WebSocket closed');
        });

        ws.addEventListener('error', (e) => {
            console.error('Agent WebSocket error', e);
        });
    }

    // =========================
    // 메시지 전송
    // =========================
    function sendMessage(text) {
        const trimmed = text.trim();
        if (!trimmed) return;
        if (!currentSessionId) {
            alert('선택된 세션이 없습니다.');
            return;
        }

        // 내 말풍선
        appendMessage(trimmed, 'me');

        // 서버로 전송
        if (ws && ws.readyState === WebSocket.OPEN) {
            const msg = {
                type: 'CHAT',
                sessionId: currentSessionId,
                senderType: senderType,
                senderId: consultantId,
                message: trimmed
            };
            ws.send(JSON.stringify(msg));
        } else {
            console.warn('WebSocket이 열려있지 않아 서버로 전송하지 못했습니다.');
        }
    }

    // =========================
    // 입력창: Enter 전송 / Shift+Enter 줄바꿈
    // =========================
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

    // =========================
    // 세션 선택 / 배정 관련
    // =========================

    function updateCurrentSessionLabel() {
        if (!currentSessionLabel) return;
        if (currentSessionId) {
            currentSessionLabel.textContent = '세션 #' + currentSessionId;
        } else {
            currentSessionLabel.textContent = '없음';
        }
    }

    function highlightSessionLi(li) {
        if (!li) return;
        if (activeSessionLi) {
            activeSessionLi.classList.remove('is-active');
        }
        li.classList.add('is-active');
        activeSessionLi = li;
    }

    /** 세션을 현재 상담 세션으로 선택하고, 채팅창 초기화 + WebSocket 연결 */
    function selectSession(sessionId, li) {
        if (!sessionId) return;

        currentSessionId = sessionId;
        updateCurrentSessionLabel();
        highlightSessionLi(li);

        // 기존 메시지 초기화
        if (chatMessages) {
            chatMessages.innerHTML = '';
        }
        if (chatInput) {
            chatInput.disabled = false;
        }

        // 과거 메시지 먼저 로딩
        const url = `${contextPath}cs/chat/messages?sessionId=${sessionId}`;

        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(res => res.json())
            .then(list => {
                list.forEach(m => {
                    const type = (m.senderType === 'AGENT') ? 'me' : 'user';
                    appendMessage(m.messageText, type);
                });

                appendMessage(`세션 #${sessionId} 상담을 시작합니다.`, 'system');
                // 상담원 기준 읽음 처리
                markMessagesRead(sessionId);
                // 그 다음 WebSocket 연결
                connectWebSocket();
            })
            .catch(err => {
                console.error(err);
                appendMessage('이전 대화 내용을 불러오지 못했습니다.', 'system');
                connectWebSocket();
            });

    }

    /** 대기목록에서 배정 버튼 클릭 -> 서버에 배정 요청 후 진행 목록으로 이동 */
    if (waitingList) {
        waitingList.addEventListener('click', function (e) {
            const btn = e.target.closest('.assign-btn');
            if (!btn) return;

            const li = btn.closest('li');
            if (!li) return;

            const sessionId = parseInt(li.dataset.sessionId || '0', 10);
            if (!sessionId) return;

            const url = `${contextPath}cs/chatting/assign?sessionId=${sessionId}`;
            console.log('[assign] url =', url);

            fetch(url, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(res => {
                    if (!res.ok) {
                        throw new Error('배정 실패');
                    }
                    return res.json();
                })
                .then(data => {
                    // UI 상에서 대기목록 -> 진행목록으로 이동
                    if (chattingList) {
                        const cloned = li.cloneNode(true);
                        const clonedBtn = cloned.querySelector('.assign-btn');
                        if (clonedBtn) clonedBtn.remove();
                        chattingList.appendChild(cloned);
                    }
                    li.remove();

                    // 방금 배정한 세션을 현재 세션으로 선택
                    const lastLi = chattingList
                        ? chattingList.querySelector(`li[data-session-id="${sessionId}"]`)
                        : null;
                    selectSession(sessionId, lastLi);
                })
                .catch(err => {
                    console.error(err);
                    alert('세션 배정 중 오류가 발생했습니다.');
                });
        });
    }

    /** 진행중 세션 목록에서 다른 세션 클릭 시, 그 세션으로 전환 */
    if (chattingList) {
        chattingList.addEventListener('click', function (e) {
            const li = e.target.closest('li[data-session-id]');
            if (!li) return;

            const sessionId = parseInt(li.dataset.sessionId || '0', 10);
            if (!sessionId) return;

            selectSession(sessionId, li);
        });
    }
    // 읽음 처리 API
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

    // =========================
    // 세션 리스트 렌더링
    // =========================
    function renderSessionLists(data) {
        if (!data) return;

        // --- 대기 목록 ---
        if (waitingList && Array.isArray(data.waitingList)) {
            waitingList.innerHTML = '';

            data.waitingList.forEach(s => {
                const li = document.createElement('li');
                li.dataset.sessionId = s.sessionId;

                li.innerHTML = `
                    <div class="agent-session-main">
                        <span class="agent-session-id">세션 #${s.sessionId}</span>
                        <span class="agent-session-meta">
                            ${escapeHtml(s.inquiryType || '')} · ${escapeHtml(s.status || '')}</span>
                            ${s.unreadCount > 0 ? `<span class="unread-badge">${s.unreadCount}</span>` : ''}
                    </div>
                    <button type="button" class="assign-btn">배정</button>
                `;
                waitingList.appendChild(li);
            });
        }

        // --- 진행 목록 ---
        if (chattingList && Array.isArray(data.chattingList)) {
            chattingList.innerHTML = '';

            data.chattingList.forEach(s => {
                const li = document.createElement('li');
                li.dataset.sessionId = s.sessionId;

                const unreadHtml =
                    s.unreadCount && s.unreadCount > 0
                        ? `<span class="unread-badge">${s.unreadCount}</span>`
                        : '';

                li.innerHTML = `
                    <div class="agent-session-main">
                        <span class="agent-session-id">세션 #${s.sessionId}</span>
                        <span class="agent-session-meta">
                            ${escapeHtml(s.inquiryType || '')} · ${escapeHtml(s.status || '')}</span>
                            ${s.unreadCount > 0 ? `<span class="unread-badge">${s.unreadCount}</span>` : ''}
                    </div>
                `;

                // 이미 선택된 세션이면 강조 유지
                if (currentSessionId && Number(currentSessionId) === s.sessionId) {
                    li.classList.add('is-active');
                    activeSessionLi = li;
                }

                chattingList.appendChild(li);
            });
        }
    }

    function fetchSessionStatus() {
        const url = `${contextPath}cs/chatting/status`;

        fetch(url, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(res => {
                if (!res.ok) {
                    throw new Error('status 조회 실패');
                }
                return res.json();
            })
            .then(data => {
                renderSessionLists(data);
            })
            .catch(err => {
                console.error('[status] error', err);
            });
    }

    // =========================
    // 상담 종료 버튼
    // =========================
    const endBtn = document.querySelector('[data-agent-chat-end]');

    if (endBtn) {
        endBtn.addEventListener('click', function (e) {
            e.preventDefault();

            if (!currentSessionId) {
                alert('종료할 세션이 선택되어 있지 않습니다.');
                return;
            }

            if (ws && ws.readyState === WebSocket.OPEN) {
                const msg = {
                    type: 'END',
                    sessionId: currentSessionId,
                    senderType: 'AGENT',
                    senderId: consultantId
                };
                ws.send(JSON.stringify(msg));
            }

            appendMessage('상담을 종료했습니다.', 'system');
            if (chatInput) {
                chatInput.disabled = true;
            }
        });
    }


    // =========================
    // 자동 갱신 설정
    // =========================
    setInterval(fetchSessionStatus, 3000);
    fetchSessionStatus();

});
