/*
 * 수정일 : 2025/11/30
 * 수정자 : 천수빈
 * 기능 : 전자금융 페이지 탭 시스템 - 상단 탭과 사이드바 메뉴 양방향 동기화
 */

(function initEfTabsPage(){
    const container = document.querySelector('.e-finance');
    if (!container) return;

    const tabs   = [...container.querySelectorAll('.ef-tabs li')];
    const panels = [...container.querySelectorAll('.ef-panel')];

    // 🔹 사이드바 전체에서 전자금융 관련 3개 메뉴를 "텍스트"로 찾기
    const TEXT_TO_PANEL = {
        '상품약관':   'ef-panel1',
        '상품설명서': 'ef-panel2',
        '서식자료실': 'ef-panel3'
    };

    const allSideLinks = [...document.querySelectorAll('.sidebar a')];

    const efSideAnch = allSideLinks.filter(a => {
        const txt = a.textContent.trim();
        return TEXT_TO_PANEL[txt] !== undefined;
    });

    // 찾은 사이드바 링크에 panelId 부여
    efSideAnch.forEach(a => {
        const key = TEXT_TO_PANEL[a.textContent.trim()];
        a.dataset.panelId = key;
    });

    function setActive(targetId, opts = { scroll: false }) {
        if (!targetId) return;

        // 1) 상단 탭 활성화
        tabs.forEach(li => {
            li.classList.toggle('is-active', li.dataset.target === targetId);
        });

        // 2) 패널 활성화
        panels.forEach(panel => {
            const active = (panel.id === targetId);
            panel.classList.toggle('is-active', active);
            panel.style.display = active ? 'block' : 'none';
        });

        // 3) 사이드바 3개 메뉴 활성화
        efSideAnch.forEach(a => {
            a.classList.toggle('is-active', a.dataset.panelId === targetId);
        });

        if (opts.scroll) {
            container.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    // ▷ 상단 탭 클릭 → 패널 + 사이드바 동기화
    tabs.forEach(li => {
        li.addEventListener('click', function(e) {
            const link = this.querySelector('a');
            if (link) {
                // li 안 a의 페이지 이동(#, 동일 URL 등)은 막고
                // 이 페이지 안에서 패널만 전환
                e.preventDefault();
            }

            const targetId = this.dataset.target;
            if (!targetId) return;

            setActive(targetId, { scroll: false });
        });
    });

    // ▷ 사이드바(상품약관/상품설명서/서식자료실) 클릭 → 탭/패널 동기화
    efSideAnch.forEach(a => {
        a.addEventListener('click', function(e) {
            const panelId = this.dataset.panelId;
            if (!panelId) return;

            e.preventDefault(); // library로 튀지 말고, ef 패널 전환만
            setActive(panelId, { scroll: false });
        });
    });

    // ▷ 최초 진입 시: is-active 달린 탭 기준으로 초기화
    const initTab = tabs.find(li => li.classList.contains('is-active')) || tabs[0];
    if (initTab) {
        setActive(initTab.dataset.target, { scroll: false });
    }
})();