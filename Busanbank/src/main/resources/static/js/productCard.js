/*
    수정일 : 2025/11/26
    수정자 : 천수빈
    내용 : 예금 상품 카드 인터랙션
*/

document.addEventListener('DOMContentLoaded', () => {
    const cards = document.querySelectorAll('.deposit-card');
    const slider = document.querySelector('.deposit-slider');

    // hover 이벤트: 활성화된 카드 전환
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            // 모든 카드에서 active 클래스 제거
            cards.forEach(c => c.classList.remove('active'));

            // 현재 hover된 카드에 active 추가
            card.classList.add('active');
        });
    });

    // 슬라이더 영역 벗어나면 1번 카드 다시 활성화
    slider.addEventListener('mouseleave', () => {
        cards.forEach(c => c.classList.remove('active'));
        cards[0].classList.add('active');
    });

    // 클릭 이벤트: 상세페이지 이동
    cards.forEach(card => {
        card.addEventListener('click', () => {
            const link = card.dataset.link;
            if (link && link !== '#') {
                window.location.href = link;
            }
        });
    });
});