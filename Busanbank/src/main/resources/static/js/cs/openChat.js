
    document.addEventListener('DOMContentLoaded', function () {
    const modal        = document.getElementById('chatModal');
    const openBtn      = document.getElementById('startChatBtn');
    const chatInput    = document.getElementById('chatInput');
    const chatMessages = document.getElementById('chatMessages');
    const chips        = modal ? modal.querySelectorAll('.chat-chips .chip') : [];
    const chatWindow   = modal ? modal.querySelector('.chat-window') : null;   // ★ 추가
    const chatHeader   = modal ? modal.querySelector('.chat-header') : null;   // ★ 추가
    let lastFocus      = null;

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
    // 왼쪽 버튼만
    if (e.button !== 0) return;

    isDragging = true;
    const rect = chatWindow.getBoundingClientRect();

    dragStartX   = e.clientX;
    dragStartY   = e.clientY;
    windowStartX = rect.left;
    windowStartY = rect.top;

    // 기존 right/bottom 기반 위치를 left/top 기반으로 전환
    chatWindow.style.left    = rect.left + 'px';
    chatWindow.style.top     = rect.top + 'px';
    chatWindow.style.right   = 'auto';
    chatWindow.style.bottom  = 'auto';
    chatWindow.style.position = 'fixed'; // 뷰포트 기준으로 드래그

    document.addEventListener('mousemove', onDragMouseMove);
    document.addEventListener('mouseup', onDragMouseUp);
}

    function onDragMouseMove(e) {
    if (!isDragging || !chatWindow) return;

    const dx = e.clientX - dragStartX;
    const dy = e.clientY - dragStartY;

    let newX = windowStartX + dx;
    let newY = windowStartY + dy;

    // 화면 밖으로 못 나가게 제한
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

    // 헤더에 드래그 이벤트 연결
    if (chatHeader && chatWindow) {
    chatHeader.addEventListener('mousedown', onDragMouseDown);
}

    /* =========================
       모달 열기 / 닫기
       ========================= */
    function openModal(e){
    if (e) e.preventDefault();
    if (!modal) return;

    lastFocus = document.activeElement;

    // 처음 열 때 위치를 기본값으로 초기화(원하면 유지해도 됨)
    chatWindow.style.right   = '24px';
    chatWindow.style.bottom  = '24px';
    chatWindow.style.left    = 'auto';
    chatWindow.style.top     = 'auto';
    chatWindow.style.position = 'absolute';

    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden','false');
    document.body.style.overflow = 'hidden';

    const firstFocusable = modal.querySelector('.chip')
    || modal.querySelector('.icon-btn[data-chat-close]')
    || chatInput;
    if (firstFocusable) firstFocusable.focus();
}

    function closeModal(){
    if (!modal) return;
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden','true');
    document.body.style.overflow = '';

    if (lastFocus) {
    lastFocus.focus();
    lastFocus = null;
}
}

    // 열기 버튼
    if (openBtn) {
    openBtn.addEventListener('click', openModal);
}

    // X 버튼으로만 닫기 (배경 클릭은 무시)
    if (modal) {
    modal.addEventListener('click', function (e) {
    const closeBtn = e.target.closest('[data-chat-close]');
    if (closeBtn && closeBtn.classList.contains('icon-btn')) {
    closeModal();
}
});
}

    // ESC 키로 닫기
    window.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && modal && modal.classList.contains('is-open')) {
    closeModal();
}
});

    /* =========================
       채팅 메시지(말풍선) 생성
       ========================= */
    function appendMessage(text, type = 'me') {
    if (!text || !chatMessages) return;

    const row = document.createElement('div');
    row.classList.add('chat-row');
    if (type === 'me') {
    row.classList.add('me');
}

    if (type !== 'me') {
    const avatar = document.createElement('img');
    avatar.className = 'chat-avatar';
    avatar.src = '/src/main/resources/static/images/main/agent.png';
    avatar.alt = '상담원';
    row.appendChild(avatar);
}

    const bubble = document.createElement('div');
    bubble.className = 'chat-bubble';
    bubble.innerHTML = text.replace(/\n/g, '<br>');
    row.appendChild(bubble);

    chatMessages.appendChild(row);

    requestAnimationFrame(() => {
    chatMessages.scrollTop = chatMessages.scrollHeight;
});
}

    function sendMyMessage() {
    if (!chatInput) return;
    const text = chatInput.value.trim();
    if (!text) return;

    appendMessage(text, 'me');
    chatInput.value = '';
    chatInput.style.height = 'auto';
}

    /* =========================
       입력창: Enter 전송 / Shift+Enter 줄바꿈
       ========================= */
    if (chatInput) {
    chatInput.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendMyMessage();
}
});

    chatInput.addEventListener('input', function () {
    this.style.height = 'auto';
    this.style.height = this.scrollHeight + 'px';
});
}

    /* =========================
       칩 클릭 시 내 말풍선으로 추가
       ========================= */
    chips.forEach(function (chip) {
    chip.addEventListener('click', function () {
    const text = chip.textContent.trim();
    if (text) {
    appendMessage(text, 'me');
}
});
});
});
