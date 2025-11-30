document.addEventListener('DOMContentLoaded', function () {
    const CTX       = '/busanbank';
    const STATE_KEY = 'oilEventState';

    const modal      = document.getElementById('oilEventModal');

    // 이 페이지에 모달이 없으면 아무 것도 안 함
    if (!modal) return;

    const triggerBtn = document.querySelector('.oil-event-trigger');
    const closeBtn   = modal.querySelector('.oil-event-close');
    const gridEl     = modal.querySelector('.oil-grid');
    const couponBtn  = modal.querySelector('.oil-coupon-btn');
    const messageEl  = modal.querySelector('.oil-event-message');

    const gridSize   = parseInt(gridEl.dataset.gridSize || '3', 10);
    const totalCells = gridSize * gridSize;

    // 버튼 data-logged-in 으로 로그인 여부 판단
    const isLoggedIn = triggerBtn?.dataset.loggedIn === 'true';

    let answerIndex  = null;
    let clicked      = false;

    /* -----------------------------
       상태 저장 / 복원 유틸
       ----------------------------- */

    function getRelativePath() {
        let path = window.location.pathname; // 예: "/busanbank/cs/..."
        if (path.startsWith(CTX)) {
            path = path.substring(CTX.length); // "/cs/..."
        }
        return path || '/';
    }

    function saveWinState() {
        const state = {
            status: 'FOUND',
            gridSize,
            answerIndex,
            path: getRelativePath()
        };
        sessionStorage.setItem(STATE_KEY, JSON.stringify(state));
    }

    function clearWinState() {
        sessionStorage.removeItem(STATE_KEY);
    }

    function restoreIfNeeded() {
        const raw = sessionStorage.getItem(STATE_KEY);
        if (!raw) return;

        let state;
        try {
            state = JSON.parse(raw);
        } catch (e) {
            clearWinState();
            return;
        }

        if (state.status !== 'FOUND') {
            clearWinState();
            return;
        }

        // 다른 페이지에서 온 흔적이면 제거만 하고 무시
        if (state.path !== getRelativePath()) {
            clearWinState();
            return;
        }

        // 여기까지 왔으면: 이 페이지에서 정답을 맞힌 상태로 돌아온 것
        modal.classList.remove('is-hidden');

        gridEl.innerHTML = '';
        clicked = true;
        answerIndex = state.answerIndex ?? 0;

        for (let i = 0; i < totalCells; i++) {
            const cell = document.createElement('button');
            cell.type = 'button';
            cell.className = 'oil-cell';
            cell.dataset.index = i;

            if (i === answerIndex) {
                cell.classList.add('is-revealed', 'is-hit');
                cell.innerHTML = '<span class="oil-cell-drop">💧</span>';
            } else {
                // 다른 칸은 더 이상 못 누르게 비활성화
                cell.disabled = true;
            }

            gridEl.appendChild(cell);
        }

        messageEl.textContent = '🎉 축하합니다! 오일 방울을 찾으셨습니다.';
        messageEl.classList.add('is-show');

        activateCoupon();
    }

    /* -----------------------------
       모달 / 게임 로직
       ----------------------------- */

    function openModal() {
        modal.classList.remove('is-hidden');

        // 새 게임 시작 시 이전 상태 삭제
        clearWinState();
        answerIndex = Math.floor(Math.random() * totalCells);
        console.log("🛢 오일 위치(index): " + answerIndex + " / 총 " + totalCells + "칸 중");

        resetGame();
    }

    function closeModal() {
        modal.classList.add('is-hidden');
        // 사용자가 모달을 닫으면 상태도 지워줌
        clearWinState();
    }

    function resetGame() {
        gridEl.innerHTML = '';
        clicked = false;
        couponBtn.classList.remove('is-active');
        couponBtn.disabled = true;

        messageEl.textContent = '';
        messageEl.classList.remove('is-show');

        for (let i = 0; i < totalCells; i++) {
            const cell = document.createElement('button');
            cell.type = 'button';
            cell.className = 'oil-cell';
            cell.dataset.index = i;

            cell.addEventListener('click', onCellClick, { once: true });
            gridEl.appendChild(cell);
        }
    }

    function onCellClick(e) {
        if (clicked) return; // 1회 시도만 허용

        const cell = e.currentTarget;
        const idx  = parseInt(cell.dataset.index, 10);

        cell.classList.add('is-revealed');
        clicked = true;

        if (idx === answerIndex) {
            console.log(`🎉 HIT! 선택한 index=${idx} (정답)`);

            cell.classList.add('is-hit');
            cell.innerHTML = '<span class="oil-cell-drop">💧</span>';

            messageEl.textContent = '🎉 축하합니다! 오일 방울을 찾으셨습니다.';
            messageEl.classList.remove('is-show');
            void messageEl.offsetWidth;
            messageEl.classList.add('is-show');

            // 로그인 여부와 상관없이, 정답 맞춘 상태는 저장
            saveWinState();
            activateCoupon();
        } else {
            console.log(`❌ MISS! 선택한 index=${idx}, 정답은 ${answerIndex}`);

            cell.classList.add('is-miss');
            cell.textContent = 'X';

            messageEl.classList.remove('is-show');
            messageEl.textContent = '아쉽습니다. 다음에 다시 도전해주세요.';
        }
    }

    function activateCoupon() {
        couponBtn.disabled = false;
        couponBtn.classList.add('is-active');
    }

    /* -----------------------------
       쿠폰 발급
       ----------------------------- */
    async function issueCoupon() {
        if (couponBtn.disabled) return;

        // 1) 로그인 여부 선체크
        if (!isLoggedIn) {
            alert('로그인 후 쿠폰을 발급받을 수 있습니다.');

            const redirectTarget =
                encodeURIComponent(getRelativePath() + window.location.search);

            // 로그인 후 이 페이지로 다시 돌아오게
            window.location.href =
                `${CTX}/member/login?redirect_uri=${redirectTarget}`;
            return;
        }

        // 2) 이미 로그인된 경우 실제 발급 요청
        try {
            const res = await fetch(`${CTX}/my/coupon/register?couponCode=5`, {
                method: 'POST'
            });

            if (!res.ok) {
                const text = await res.text();
                console.error('쿠폰 발급 실패 응답', res.status, text);
                messageEl.classList.remove('is-show');
                messageEl.textContent = '쿠폰 발급에 실패했습니다. (서버 응답 오류)';
                return;
            }

            const data = await res.json();

            if (!data.success) {
                messageEl.classList.remove('is-show');
                messageEl.textContent = data.message || '쿠폰 발급에 실패했습니다.';
                return;
            }

            // ✅ 성공 메시지 (중앙 팝업)
            messageEl.classList.remove('is-show');
            void messageEl.offsetWidth;
            messageEl.textContent = '🎉 쿠폰이 발급되었습니다!';
            messageEl.classList.add('is-show');

            couponBtn.disabled = true;
            clearWinState(); // 이제 더 이상 복원 필요 없음

            // 2초 뒤 쿠폰 페이지로 이동
            setTimeout(() => {
                window.location.href = `${CTX}/my/coupon`;
            }, 2000);

        } catch (err) {
            console.error(err);
            messageEl.classList.remove('is-show');
            messageEl.textContent = '서버 오류로 쿠폰 발급에 실패했습니다.';
        }
    }

    /* -----------------------------
       이벤트 바인딩 & 초기 복원
       ----------------------------- */

    triggerBtn?.addEventListener('click', openModal);
    closeBtn?.addEventListener('click', closeModal);
    modal.querySelector('.oil-event-backdrop')
        ?.addEventListener('click', closeModal);
    couponBtn.addEventListener('click', issueCoupon);

    // 🔥 로그인 후 돌아온 경우라면, 이 시점에서 모달/정답 상태 복원
    restoreIfNeeded();
});
