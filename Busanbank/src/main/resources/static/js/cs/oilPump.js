/*
    수정일 : 2025/12/02
    수정자 : 천수빈
    내용 : 오일 펌프 이벤트
*/

document.addEventListener('DOMContentLoaded', function() {
    const oilCard = document.querySelector('.oil-event-trigger');
    const lottieContainer = oilCard.querySelector('.oil-lottie');
    const animationPath = lottieContainer.dataset.animationPath;

    const animation = lottie.loadAnimation({
        container: lottieContainer,
        renderer: 'svg',
        loop: true,
        autoplay: false,
        path: animationPath
    });

    oilCard.addEventListener('mouseenter', () => {
        animation.goToAndPlay(0, true);
    });

    oilCard.addEventListener('mouseleave', () => {
        animation.stop();
    });
});