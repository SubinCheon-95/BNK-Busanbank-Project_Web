console.log('🔥🔥 OIL TEST LOG 🔥🔥');
console.log('[oil] script file loaded');

document.addEventListener('DOMContentLoaded', function () {
    console.log('[oil] DOMContentLoaded');

    const CTX       = '/busanbank';
    const STATE_KEY = 'oilEventState';

    // 1) DOM 요소들
    const modal      = document.getElementById('oilEventModal');
    const triggerBtn = document.querySelector('.oil-event-trigger');
    const closeBtn   = modal ? modal.querySelector('.oil-event-close') : null;
    const gridEl     = modal ? modal.querySelector('.oil-grid') : null;
    const couponBtn  = modal ? modal.querySelector('.oil-coupon-btn') : null;
    const messageEl  = modal ? modal.querySelector('.oil-event-message') : null;
    const lottieContainer = modal ? modal.querySelector('.oil-lottie-container') : null;
    const lottiePlayer = document.getElementById('oilLottieAnimation'); // 🎉 Lottie Player

    // 2) 요소 존재 여부 로그
    console.log('[oil] init elements', {
        modal: !!modal,
        triggerBtn: !!triggerBtn,
        gridEl: !!gridEl,
        couponBtn: !!couponBtn,
        messageEl: !!messageEl,
        lottieContainer: !!lottieContainer,
        lottiePlayer: !!lottiePlayer
    });

    // 3) 필수 요소 없으면 종료
    if (!modal || !triggerBtn || !gridEl || !couponBtn || !messageEl) {
        console.warn('[oil] 필수 요소를 찾지 못했습니다.');
        return;
    }

    const gridSize   = parseInt(gridEl.dataset.gridSize || '3', 10);
    const totalCells = gridSize * gridSize;
    const isLoggedIn = triggerBtn.dataset.loggedIn === 'true';

    let answerIndex  = null;
    let clicked      = false;

    /* -----------------------------
       상태 저장 / 복원 유틸
       ----------------------------- */

    function getRelativePath() {
        let path = window.location.pathname;
        if (path.startsWith(CTX)) {
            path = path.substring(CTX.length);
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

        if (state.path !== getRelativePath()) {
            clearWinState();
            return;
        }

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
        console.log('[oil] openModal called');

        modal.classList.remove('is-hidden');

        clearWinState();
        answerIndex = Math.floor(Math.random() * totalCells);
        console.log("🛢 오일 위치(index): " + answerIndex + " / 총 " + totalCells + "칸 중");

        resetGame();
    }

    function closeModal() {
        modal.classList.add('is-hidden');
        clearWinState();
    }

    function resetGame() {
        gridEl.innerHTML = '';
        clicked = false;
        couponBtn.classList.remove('is-active');
        couponBtn.disabled = true;

        messageEl.textContent = '';
        messageEl.classList.remove('is-show');

        // 🎉 Lottie 숨기기 및 정지
        if (lottieContainer) {
            lottieContainer.classList.add('is-hidden');
        }
        if (lottiePlayer) {
            lottiePlayer.stop();
        }

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
        if (clicked) return;

        const cell = e.currentTarget;
        const idx  = parseInt(cell.dataset.index, 10);

        cell.classList.add('is-revealed');
        clicked = true;

        if (idx === answerIndex) {
            console.log(`🎉 HIT! 선택한 index=${idx} (정답)`);

            cell.classList.add('is-hit');
            cell.innerHTML = '<span class="oil-cell-drop">💧</span>';

            // 🎉 Lottie 애니메이션 재생
            if (lottiePlayer && lottieContainer) {
                console.log('[oil] Lottie 애니메이션 재생');
                lottieContainer.classList.remove('is-hidden');
                lottiePlayer.play();

                // 애니메이션 완료 후 숨기기
                lottiePlayer.addEventListener('complete', function() {
                    console.log('[oil] Lottie 애니메이션 완료');
                    setTimeout(() => {
                        lottieContainer.classList.add('is-hidden');
                    }, 500);
                }, { once: true });
            } else {
                console.warn('[oil] Lottie Player 없음');
            }

            messageEl.textContent = '🎉 축하합니다! 오일 방울을 찾으셨습니다.';
            messageEl.classList.remove('is-show');
            void messageEl.offsetWidth;
            messageEl.classList.add('is-show');

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

        if (!isLoggedIn) {
            alert('로그인 후 쿠폰을 발급받을 수 있습니다.');

            const redirectTarget =
                encodeURIComponent(getRelativePath() + window.location.search);

            window.location.href =
                `${CTX}/member/login?redirect_uri=${redirectTarget}`;
            return;
        }

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
                void messageEl.offsetWidth;

                if (data.message && data.message.indexOf('이미 등록된 쿠폰') !== -1) {
                    messageEl.textContent =
                        '이미 발급받은 쿠폰입니다.\n마이페이지 > 쿠폰에서 확인해 주세요.';
                    couponBtn.disabled = true;
                } else {
                    messageEl.textContent =
                        data.message || '쿠폰 발급에 실패했습니다.';
                }

                messageEl.classList.add('is-show');
                return;
            }

            messageEl.classList.remove('is-show');
            void messageEl.offsetWidth;
            messageEl.textContent = '🎉 쿠폰이 발급되었습니다!';
            messageEl.classList.add('is-show');

            couponBtn.disabled = true;
            clearWinState();

            setTimeout(() => {
                window.location.href = `${CTX}/my/coupon`;
            }, 3000);

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

    restoreIfNeeded();
});