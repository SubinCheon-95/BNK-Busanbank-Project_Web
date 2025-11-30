
document.addEventListener('DOMContentLoaded', function () {
    const modal      = document.getElementById('oilEventModal');

    // 이 페이지에는 오일 이벤트 모달이 없으면, 스크립트 그냥 종료
    if (!modal) {return;}

    const triggerBtn = document.querySelector('.oil-event-trigger');
    const closeBtn   = modal.querySelector('.oil-event-close');
    const gridEl     = modal.querySelector('.oil-grid');
    const couponBtn  = modal.querySelector('.oil-coupon-btn');
    const messageEl  = modal.querySelector('.oil-event-message');

    const gridSize   = parseInt(gridEl.dataset.gridSize || '3', 10);
    const totalCells = gridSize * gridSize;

    let answerIndex  = null;
    let clicked      = false;

    function openModal() {
        modal.classList.remove('is-hidden');

        // 정답 생성
        answerIndex = Math.floor(Math.random() * totalCells);
        console.log("🛢 오일 위치(index): " + answerIndex + " / 총 " + totalCells + "칸 중");

        resetGame();
    }

    function closeModal() {
        modal.classList.add('is-hidden');
    }

    function resetGame() {
        gridEl.innerHTML = '';
        clicked = false;
        couponBtn.classList.remove('is-active');
        couponBtn.disabled = true;
        messageEl.textContent = '';

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
        if (clicked) return; // 1회 시도만 허용할 경우

        const cell = e.currentTarget;
        const idx  = parseInt(cell.dataset.index, 10);

        cell.classList.add('is-revealed');
        clicked = true;

        if (idx === answerIndex) {
            console.log(`🎉 HIT! 선택한 index=${idx} (정답)`);

            cell.classList.add('is-hit');
            cell.innerHTML = '<span class="oil-cell-drop">💧</span>';
            messageEl.textContent = '축하합니다! 오일 방울을 찾으셨습니다.';
            activateCoupon();
        } else {
            console.log(`❌ MISS! 선택한 index=${idx}, 정답은 ${answerIndex}`);

            cell.classList.add('is-miss');
            cell.textContent = 'X';
            messageEl.textContent = '아쉽습니다. 다음에 다시 도전해주세요.';
        }
    }

    function activateCoupon() {
        couponBtn.disabled = false;
        couponBtn.classList.add('is-active');
    }

    async function issueCoupon() {
        if (couponBtn.disabled) return;

        try {
            // TODO: 실제 쿠폰 발급 API 엔드포인트로 변경
            const res = await fetch('/event/oil/coupon', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ eventType: 'OIL_FIELD' })
            });

            if (!res.ok) throw new Error('쿠폰 발급 실패');

            const data = await res.json();
            messageEl.textContent = `쿠폰이 발급되었습니다. 코드: ${data.couponCode || '발급 완료'}`;
            couponBtn.disabled = true;
        } catch (err) {
            console.error(err);
            messageEl.textContent = '일시적인 오류로 쿠폰 발급에 실패했습니다. 다시 시도해주세요.';
        }
    }

    // 이벤트 바인딩
    triggerBtn?.addEventListener('click', openModal);
    closeBtn?.addEventListener('click', () => modal.classList.add('is-hidden'));
    modal.querySelector('.oil-event-backdrop')
        ?.addEventListener('click', () => modal.classList.add('is-hidden'));
    couponBtn.addEventListener('click', issueCoupon);
});
