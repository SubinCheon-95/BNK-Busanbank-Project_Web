/*
    수정일 : 2025/11/25
    수정자 : 천수빈
    내용 : GNB 드롭다운 제어 + 드롭다운 앞줄 정렬
*/

/*
    수정일 : 2025/11/25
    수정자 : 천수빈
    내용 : GNB 드롭다운 제어 + 드롭다운 앞줄 정렬
*/

// GNB가 여러 번 초기화되지 않도록 플래그 사용
function closeAllGnbMenus() {
    // 모든 메뉴 active 해제
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
    });

    // 모든 서브메뉴 닫기 (globalBtn.js에서 display를 건드리기 때문에 같이 관리)
    document.querySelectorAll('.submenu-wrap').forEach(s => {
        s.style.display = 'none';
    });
}

function initGnbEvents() {
    const gnbRoot = document.querySelector('.gnb');
    if (!gnbRoot) return;

    // 중복 바인딩 방지
    if (gnbRoot.dataset.bound === 'true') {
        return;
    }
    gnbRoot.dataset.bound = 'true';

    // ▼ 1) 클릭 이벤트 위임
    gnbRoot.addEventListener('click', function (e) {
        const link = e.target.closest('.menu-item > a');
        if (!link || !gnbRoot.contains(link)) return;

        const item = link.parentElement;
        const submenu = item.querySelector('.submenu-wrap');

        // 서브메뉴가 없는 단순 링크(예: 금융퀴즈)는 그대로 이동
        if (!submenu) {
            closeAllGnbMenus();
            // a 태그 기본 이동 허용
            return;
        }

        // 서브메뉴가 있는 경우: GNB 토글 동작
        e.preventDefault();

        const isActive = item.classList.contains('active');

        // 먼저 모두 닫고
        closeAllGnbMenus();

        // 클릭한 메뉴만 열기
        if (!isActive) {
            item.classList.add('active');
            submenu.style.display = 'block';
        }
    });

    // ▼ 2) mouseenter로 서브메뉴 위치 조정 (필요할 때만)
    gnbRoot.addEventListener('mouseenter', function (e) {
        const item = e.target.closest('.menu-item');
        if (!item || !gnbRoot.contains(item)) return;

        const submenu = item.querySelector('.submenu-wrap');
        const inner = item.querySelector('.submenu-inner');
        if (!submenu || !inner) return;

        const rect = item.getBoundingClientRect();
        inner.style.position = 'relative';
        inner.style.left = rect.left + 'px';
    }, true);
}

// 페이지 로드 시 1회 실행
document.addEventListener('DOMContentLoaded', initGnbEvents);

// 번역 완료 후 DOM이 갈아끼워졌을 수 있으니 다시 한 번만 바인딩
window.addEventListener('translationComplete', () => {
    const gnbRoot = document.querySelector('.gnb');
    if (gnbRoot) {
        delete gnbRoot.dataset.bound;
    }
    initGnbEvents();
});
