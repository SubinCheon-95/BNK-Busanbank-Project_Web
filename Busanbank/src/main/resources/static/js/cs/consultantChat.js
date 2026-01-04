document.addEventListener('DOMContentLoaded', function () {

    // ===== 필수 DOM =====
    const agentConsole   = document.getElementById('chatAgentConsole');
    if (!agentConsole) return; // 상담원 화면이 아닐 때 방어

    const waitingList    = document.getElementById('waitingList');    // 대기중 세션 목록
    const chattingList   = document.getElementById('chattingList');   // 진행중 세션 목록
    const chatMessages   = document.getElementById('agentChatMessages');
    const chatInput      = document.getElementById('agentChatInput');
    const currentSessionLabel = document.getElementById('currentSessionLabel');
    const btnAssignNext  = document.getElementById('btnAssignNext');

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
            if (msgObj.sessionId && currentSessionId && msgObj.sessionId !== currentSessionId) return;

            if (msgObj.type === 'TYPING') return;

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
    // 입력 중 출력 (TYPING 전송)
    // =========================
        let typing = false;
        let typingStopTimer = null;

        function sendTyping(isTyping) {
            if (!currentSessionId) return;
            if (!ws || ws.readyState !== WebSocket.OPEN) return;

            ws.send(JSON.stringify({
                type: 'TYPING',
                sessionId: currentSessionId,
                senderType: 'AGENT',
                senderId: consultantId,
                isTyping: !!isTyping
            }));
        }

    // =========================
    // 입력창 이벤트 (여기 1개만 유지)
    // =========================
        if (chatInput) {
            chatInput.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();

                    // 전송 직전 typing 종료 확정
                    if (typing) {
                        typing = false;
                        sendTyping(false);
                    }
                    clearTimeout(typingStopTimer);
                    typingStopTimer = null;

                    sendMessage(chatInput.value);

                    chatInput.value = '';
                    chatInput.style.height = 'auto';
                }
            });

            chatInput.addEventListener('input', function () {
                // 높이 자동 조절
                this.style.height = 'auto';
                this.style.height = this.scrollHeight + 'px';

                // 타이핑 시작 (처음 1회만)
                if (!typing) {
                    typing = true;
                    sendTyping(true);
                }

                // 입력 멈춤 감지 후 종료
                clearTimeout(typingStopTimer);
                typingStopTimer = setTimeout(() => {
                    typing = false;
                    sendTyping(false);
                }, 1200);
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
            sendTyping(false);
            typing = false;
            clearTimeout(typingStopTimer);
            typingStopTimer = null;
            ws.send(JSON.stringify(msg));
        } else {
            console.warn('WebSocket이 열려있지 않아 서버로 전송하지 못했습니다.');
        }
    }

    // // =========================
    // // 입력창: Enter 전송 / Shift+Enter 줄바꿈
    // // =========================
    // if (chatInput) {
    //     chatInput.addEventListener('keydown', function (e) {
    //         if (e.key === 'Enter' && !e.shiftKey) {
    //             e.preventDefault();
    //             // ✅ 전송 직전 typing 종료 확정
    //             if (typing) {
    //                 typing = false;
    //                 sendTyping(false);
    //             }
    //             clearTimeout(typingStopTimer);
    //             typingStopTimer = null;
    //
    //             sendMessage(chatInput.value);
    //
    //             chatInput.value = '';
    //             chatInput.style.height = 'auto';
    //         }
    //     });

    //     chatInput.addEventListener('input', function () {
    //         this.style.height = 'auto';
    //         this.style.height = this.scrollHeight + 'px';
    //
    //         // ✅ 타이핑 시작/멈춤 디바운스
    //         if (!typing) {
    //             typing = true;
    //             sendTyping(true);
    //         }
    //         clearTimeout(typingStopTimer);
    //         typingStopTimer = setTimeout(() => {
    //             typing = false;
    //             sendTyping(false);
    //         }, 1500);
    //     });
    // }

    // =========================
    // 자동 배정 버튼
    // =========================
    if (btnAssignNext) {
        btnAssignNext.addEventListener('click', function () {

            btnAssignNext.disabled = true;

            fetch(`${contextPath}cs/chat/consultant/assignNext`, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(res => res.json())
                .then(data => {

                    if (data === 'NO_WAITING') {
                        alert('대기 중인 상담이 없습니다.');
                        return;
                    }

                    // 서버에서 내려온 ChatSessionDTO 라고 가정
                    const sessionId = parseInt(data.sessionId || '0', 10);
                    if (!sessionId) {
                        console.warn('assignNext 응답에 sessionId가 없습니다.', data);
                        return;
                    }

                    // 1) 세션 상태 먼저 다시 가져오기 (Promise 리턴)
                    return fetchSessionStatus().then(() => {

                        // 2) 목록 갱신이 끝난 뒤, 진행중 목록에서 해당 세션 li 찾아서 선택
                        if (!chattingList) return;

                        const li = chattingList.querySelector(
                            `li[data-session-id="${sessionId}"]`
                        );
                        if (li) {
                            selectSession(sessionId, li);
                        } else {
                            console.warn('chattingList에서 sessionId에 해당하는 li를 찾지 못했습니다.', sessionId);
                        }
                    });
                })
                .catch(err => {
                    console.error('자동 배정 중 오류', err);
                    alert('자동 배정 중 오류가 발생했습니다.');
                })
                .finally(() => {
                    btnAssignNext.disabled = false;
                });
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
        // ✅ 이전 세션 typing 종료
        if (typing) {
            typing = false;
            sendTyping(false);
        }
        clearTimeout(typingStopTimer);
        typingStopTimer = null;
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
        const url = `${contextPath}cs/chat/consultant/messages?sessionId=${sessionId}`;

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

            const url = `${contextPath}cs/chat/consultant/assign?sessionId=${sessionId}`;
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
        const url = `${contextPath}cs/chat/consultant/messages/read?sessionId=${sessId}`;

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

        const waitingCountEl  = document.querySelector('.waiting-count');
        const chattingCountEl = document.querySelector('.chatting-count');

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
                        ${escapeHtml(s.inquiryType || '')} · ${escapeHtml(s.status || '')}
                    </span>
                </div>
            `;

                waitingList.appendChild(li);
            });

            // 🔹 대기 건수 갱신
            if (waitingCountEl) {
                waitingCountEl.textContent = data.waitingList.length + '건';
            }
        }

        // --- 진행 목록 ---
        if (chattingList && Array.isArray(data.chattingList)) {
            chattingList.innerHTML = '';

            data.chattingList.forEach(s => {
                const li = document.createElement('li');
                li.dataset.sessionId = s.sessionId;

                li.innerHTML = `
                <div class="agent-session-main">
                    <span class="agent-session-id">세션 #${s.sessionId}</span>
                    <span class="agent-session-meta">
                        ${escapeHtml(s.inquiryType || '')} · ${escapeHtml(s.status || '')}
                    </span>
                </div>
                ${s.unreadCount && s.unreadCount > 0
                    ? `<span class="unread-badge">${s.unreadCount}</span>`
                    : ''}
            `;

                // 이미 선택된 세션이면 강조 유지
                if (currentSessionId && Number(currentSessionId) === s.sessionId) {
                    li.classList.add('is-active');
                    activeSessionLi = li;
                }

                chattingList.appendChild(li);
            });

            // 🔹 진행 건수 갱신
            if (chattingCountEl) {
                chattingCountEl.textContent = data.chattingList.length + '건';
            }
        }
    }

    function fetchSessionStatus() {
        const url = `${contextPath}/cs/chat/consultant/status`;

        return fetch(url, {
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
                return data;
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

            // 1) WebSocket으로 END 알림 (고객/다른 참여자에게)
            if (ws && ws.readyState === WebSocket.OPEN) {
                const msg = {
                    type: 'END',
                    sessionId: currentSessionId,
                    senderType: 'AGENT',
                    senderId: consultantId
                };
                ws.send(JSON.stringify(msg));
            }

            // 2) 서버에 세션 종료 요청 (DB 상태 CLOSED)
            const endUrl = `${contextPath}cs/chat/consultant/end?sessionId=${currentSessionId}`;

            fetch(endUrl, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.result === 'OK') {
                        appendMessage('상담을 종료했습니다.', 'system');
                        if (chatInput) {
                            chatInput.disabled = true;
                        }

                        // 현재 선택 세션 초기화
                        currentSessionId = null;
                        updateCurrentSessionLabel();
                        if (activeSessionLi) {
                            activeSessionLi.classList.remove('is-active');
                            activeSessionLi = null;
                        }

                        // 3) 목록 즉시 다시 조회 (3초 기다리지 않고)
                        return fetchSessionStatus();
                    } else {
                        console.warn('세션 종료 응답 이상', data);
                        alert('상담 종료 처리 중 문제가 발생했습니다.');
                    }
                })
                .catch(err => {
                    console.error('세션 종료 요청 실패', err);
                    alert('상담 종료 요청에 실패했습니다.');
                });
        });
    }


    // =========================
    // 자동 갱신 설정
    // =========================
    setInterval(fetchSessionStatus, 3000);
    fetchSessionStatus();

});
