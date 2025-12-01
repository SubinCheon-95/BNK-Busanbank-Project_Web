
document.addEventListener('DOMContentLoaded', function () {
    console.log('tabMove.js DOMContentLoaded');

    // ===================== [A] 예금관련수수료 탭 =====================
    (function initUseRateTabs() {
        const container = document.querySelector('.use-rate');
        if (!container) {
            console.log('use-rate 컨테이너 없음 (이 페이지는 예금관련수수료 페이지가 아님)');
            return;
        }

        const tabs   = [...container.querySelectorAll('.use-rate-tabs li')];
        const panels = [...container.querySelectorAll('.use-rate-panel')];

        if (tabs.length === 0 || panels.length === 0) {
            console.log('use-rate 탭 또는 패널이 없음');
            return;
        }

        function setActive(targetId) {
            if (!targetId) return;

            // 탭 활성화
            tabs.forEach(li => {
                li.classList.toggle('is-active', li.dataset.target === targetId);
            });

            // 패널 활성화
            panels.forEach(panel => {
                const active = (panel.id === targetId);
                panel.classList.toggle('is-active', active);
                panel.style.display = active ? 'block' : 'none';
            });
        }

        // 탭 클릭 이벤트
        tabs.forEach(li => {
            li.addEventListener('click', function (e) {
                // li 안에 a 태그 있으면 링크 이동 막기
                const link = this.querySelector('a');
                if (link) {
                    e.preventDefault();
                }

                const targetId = this.dataset.target;
                setActive(targetId);
            });
        });

        // 최초 진입시: is-active 달린 탭 기준 초기화
        const initTab = tabs.find(li => li.classList.contains('is-active')) || tabs[0];
        if (initTab) {
            setActive(initTab.dataset.target);
        }

        console.log('use-rate 탭 초기화 완료');
    })();

    // ===================== [B] 전자금융 탭 (상품약관/상품설명서/서식자료실) =====================
    (function initEfTabsPage() {
        const container = document.querySelector('.e-finance');
        if (!container) {
            console.log('e-finance 컨테이너 없음 (이 페이지는 전자금융 페이지가 아님)');
            return;
        }

        // .ef-tabs 와 .use-rate-tabs 둘 다 지원하도록 한 경우라면 이렇게:
        const tabs   = [...container.querySelectorAll('.ef-tabs li, .use-rate-tabs li')];
        const panels = [...container.querySelectorAll('.ef-panel')];

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

        efSideAnch.forEach(a => {
            const key = TEXT_TO_PANEL[a.textContent.trim()];
            a.dataset.panelId = key;
        });

        function setActive(targetId, opts = { scroll: false }) {
            if (!targetId) return;

            // 상단 탭 활성화
            tabs.forEach(li => {
                li.classList.toggle('is-active', li.dataset.target === targetId);
            });

            // 패널 활성화
            panels.forEach(panel => {
                const active = (panel.id === targetId);
                panel.classList.toggle('is-active', active);
                panel.style.display = active ? 'block' : 'none';
            });

            // 사이드바 메뉴 활성화
            efSideAnch.forEach(a => {
                a.classList.toggle('is-active', a.dataset.panelId === targetId);
            });

            if (opts.scroll) {
                container.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        }

        // 상단 탭 클릭
        tabs.forEach(li => {
            li.addEventListener('click', function (e) {
                const link = this.querySelector('a');
                if (link) e.preventDefault();

                const targetId = this.dataset.target;
                if (!targetId) return;

                setActive(targetId, { scroll: false });
            });
        });

        // 사이드바 클릭
        efSideAnch.forEach(a => {
            a.addEventListener('click', function (e) {
                const panelId = this.dataset.panelId;
                if (!panelId) return;

                e.preventDefault();
                setActive(panelId, { scroll: false });
            });
        });

        const initTab = tabs.find(li => li.classList.contains('is-active')) || tabs[0];
        if (initTab) {
            setActive(initTab.dataset.target, { scroll: false });
        }

        console.log('e-finance 탭 초기화 완료');
    })();
});
