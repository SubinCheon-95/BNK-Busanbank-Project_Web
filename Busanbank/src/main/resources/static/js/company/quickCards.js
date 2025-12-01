/*
    수정일 : 2025/12/01
    수정자 : 천수빈
    내용 : 은행소개 메인 퀵 카드 애니메이션
*/

document.addEventListener('DOMContentLoaded', function() {
    const quickCards = document.querySelectorAll('.quick-card');

    quickCards.forEach(card => {
        const lottieContainer = card.querySelector('.lottie-icon');
        if (!lottieContainer) return;

        const animationPath = lottieContainer.dataset.animationPath;

        const animation = lottie.loadAnimation({
            container: lottieContainer,
            renderer: 'svg',
            loop: true,
            autoplay: false,
            path: animationPath,
            rendererSettings: {
                preserveAspectRatio: 'xMidYMid slice'
            }
        });

        animation.addEventListener('DOMLoaded', () => {
            const svg = lottieContainer.querySelector('svg');
            if (!svg) return;

            const scale   = parseFloat(lottieContainer.dataset.scale   || '3');
            const offsetX = parseFloat(lottieContainer.dataset.offsetX || '0');
            const offsetY = parseFloat(lottieContainer.dataset.offsetY || '0');

            svg.style.transformOrigin = "center center";
            svg.style.transform =
                `translate(${offsetX}px, ${offsetY}px) scale(${scale})`;
        });

        card.addEventListener('mouseenter', () => {
            animation.goToAndPlay(0, true);
        });

        card.addEventListener('mouseleave', () => {
            animation.stop();
        });
    });
});
