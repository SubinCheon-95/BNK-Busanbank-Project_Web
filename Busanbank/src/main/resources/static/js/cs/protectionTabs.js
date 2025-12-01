/*
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : 금융소비자보호 탭-사이드바 연동
*/

(function initProtectionTabs(){
    const container = document.querySelector('.protection');
    if (!container) return;

    const tabs     = [...container.querySelectorAll('.prot-tab')];
    const panels   = [...document.querySelectorAll('.prot-panel')];
    const sideAnch = [...document.querySelectorAll('.sidebar .side-submenu a')];

    // 사이드바 텍스트 → key 매핑
    const KEY_BY_TEXT = {
        '금융소비자보호헌장': 'charter',
        '소비자보호 조직도': 'org',
        '민원접수 및 처리프로세스': 'complaint',
        '상품개발 프로세스': 'dev',
        '상품판매 준칙': 'sales',
        '고령 금융소비자보호 준칙': 'senior',
        '영업행위 윤리 준칙': 'ethics',
        '소비자보호 필수 안내사항': 'notice',
        '금융소비자 권리행사 안내': 'rights'
    };

    // 사이드바 링크에 data-key 부여
    sideAnch.forEach(a => {
        const key = KEY_BY_TEXT[a.textContent.trim()];
        if (key) a.dataset.key = key;
    });

    function setActive(key, opts = { scroll: true }) {
        // 탭/패널 상태
        tabs.forEach(t => t.classList.toggle('is-active', t.dataset.key === key));
        panels.forEach(p => p.classList.toggle('is-active', p.dataset.key === key));

        // 사이드바 강조
        sideAnch.forEach(a => a.classList.toggle('is-active', a.dataset.key === key));

        // 해시 반영 (직링크/새로고침 대응)
        history.replaceState(null, '', '#' + key);

        // 필요 시 상단 탭 위치로 스크롤
        // if (opts.scroll) {
        // document.querySelector('.prot-tabs-wrap')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        // }
    }

    // 탭 클릭
    tabs.forEach(tab => {
        tab.addEventListener('click', () => setActive(tab.dataset.key));
        tab.addEventListener('keydown', e => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                setActive(tab.dataset.key);
            }
        });
    });

    // 사이드바 클릭
    sideAnch.forEach(a => {
        a.addEventListener('click', e => {
            const key = a.dataset.key;
            if (!key) return;
            e.preventDefault();
            setActive(key);
        });
    });

    // 초기 활성 (해시가 유효하면 사용, 아니면 charter)
    const initKey = location.hash.replace('#','');
    const valid = panels.some(p => p.dataset.key === initKey);
    setActive(valid ? initKey : 'charter', { scroll: false });
})();