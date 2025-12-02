/*
    수정일 : 2025/12/03
    수정자 : 천수빈
    내용 : 상품 메인 인피니티 카운셀 기능
*/

let currentIndex = 0; // 자산전문예금(1번)부터 시작
const track = document.getElementById('zigzagTrack');
const items = document.querySelectorAll('.carousel-item');
const totalItems = items.length;
const cardWidth = 410; // 카드 너비(380px) + 간격(30px)

// 트랙의 시작과 끝에 클론 추가 (무한 루프용)
function createClones() {
    // 뒤쪽에 처음 3개 추가 (무한 루프용)
    for (let i = 0; i < 3; i++) {
        const clone = items[i].cloneNode(true);
        clone.classList.add('clone');
        track.appendChild(clone);
    }

    // 앞쪽에 마지막 3개 추가 (7, 6, 5번 순서로)
    for (let i = totalItems - 1; i >= totalItems - 3; i--) {
        const clone = items[i].cloneNode(true);
        clone.classList.add('clone');
        track.insertBefore(clone, track.firstChild);
    }
}

// 초기 위치 설정
function updateCarousel(transition = true) {
    if (transition) {
        track.style.transition = 'transform 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
    } else {
        track.style.transition = 'none';
    }

    // 클론 3개 + 현재 인덱스를 고려한 offset 계산
    const actualIndex = currentIndex + 3;
    const offset = -(actualIndex * cardWidth) + (window.innerWidth / 2) - (cardWidth * 0.5);
    track.style.transform = `translateX(${offset}px)`;
}

function moveCarousel(direction) {
    // 3개씩 이동
    currentIndex += (direction * 3);
    updateCarousel(true);

    // 무한 루프 처리
    setTimeout(() => {
        if (currentIndex < 0) {
            currentIndex = totalItems + currentIndex;
            updateCarousel(false);
        } else if (currentIndex >= totalItems) {
            currentIndex = currentIndex - totalItems;
            updateCarousel(false);
        }
    }, 600);
}

// 초기 설정
createClones();
window.addEventListener('load', () => updateCarousel(false));
window.addEventListener('resize', () => updateCarousel(false));