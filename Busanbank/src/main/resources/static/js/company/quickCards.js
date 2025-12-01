/*
    수정일 : 2025/12/01
    수정자 : 천수빈
    내용 : 은행소개 메인 퀵 카드 애니메이션
*/

document.addEventListener('DOMContentLoaded', function() {
    const quickCards = document.querySelectorAll('.quick-card');

    quickCards.forEach(card => {
        const lottieContainer = card.querySelector('.lottie-icon');
        const animationPath = lottieContainer.dataset.animationPath;

        // Lottie 애니메이션 초기화
        const animation = lottie.loadAnimation({
            container: lottieContainer,
            renderer: 'svg',
            loop: true,
            autoplay: false,
            path: animationPath
        });

        // 호버 이벤트 - 마우스 진입
        card.addEventListener('mouseenter', function() {
            animation.goToAndPlay(0, true); // 처음부터 재생
        });

        // 호버 이벤트 - 마우스 이탈
        card.addEventListener('mouseleave', function() {
            animation.stop(); // 애니메이션 정지 및 초기화
        });
    });
});