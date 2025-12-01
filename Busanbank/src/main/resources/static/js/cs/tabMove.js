// /js/cs/tabMove.js

console.log('tabMove.js 로드됨');

function initTabs(containerSelector, tabCls, panelCls, underlineCls) {
    document.querySelectorAll(containerSelector).forEach(root => {
        const tabs      = root.querySelectorAll(tabCls);
        const panels    = root.querySelectorAll(panelCls);
        const underline = underlineCls ? root.querySelector(underlineCls) : null;

        if (tabs.length === 0) {
            console.log('탭을 찾을 수 없습니다:', containerSelector);
            return;
        }

        function activate(tab) {
            // 1) 탭 활성화 토글
            tabs.forEach(t => t.classList.remove('is-active'));
            panels.forEach(p => p.classList.remove('is-active'));
            tab.classList.add('is-active');

            // 2) 대상 패널 찾기
            //    - data-tab-target="#panel1"
            //    - data-target="panel1"
            //    - aria-controls="panel1"
            const targetSelector =
                tab.dataset.tabTarget
                || (tab.dataset.target ? ('#' + tab.dataset.target) : null)
                || (tab.getAttribute('aria-controls')
                    ? '#' + tab.getAttribute('aria-controls')
                    : null);

            if (!targetSelector) {
                console.log('타겟 셀렉터가 없습니다:', tab);
                return;
            }

            // root 아래에서만 패널 검색 (같은 id가 다른 곳에 있어도 안전)
            const target = root.querySelector(targetSelector) || document.querySelector(targetSelector);
            if (target) {
                target.classList.add('is-active');
            } else {
                console.log('패널을 찾을 수 없습니다:', targetSelector);
            }

            // 3) 언더라인 위치/길이 조정 (있는 경우만)
            if (underline) {
                const rect       = tab.getBoundingClientRect();
                const parentRect = tab.parentElement.getBoundingClientRect();

                underline.style.width     = rect.width + 'px';
                underline.style.transform = `translateX(${rect.left - parentRect.left}px)`;
            }
        }

        // 클릭 이벤트 바인딩
        tabs.forEach(tab => {
            tab.addEventListener('click', e => {
                // li/button 내부에 a 있으면 기본 이동 막기
                const link = tab.querySelector('a');
                if (link) e.preventDefault();

                activate(tab);
            });
        });

        // 초기 활성 탭
        const initTab = root.querySelector(`${tabCls}.is-active`) || tabs[0];
        if (initTab) {
            activate(initTab);
        }
    });
}

// DOM 준비 후 실행
document.addEventListener('DOMContentLoaded', function () {
    console.log('tabMove.js DOMContentLoaded');

    // 서비스 이용시간 탭(있으면)
    initTabs('.service-time', '.st-tab', '.st-panel', '.st-underline');

    // 고객우대서비스 탭(있으면)
    initTabs('.preferred', '.pf-tab', '.pf-panel', '.pf-underline');

    initTabs('.use-rate', '.use-rate-tabs li', '.use-rate-panel', null);

    console.log('탭 초기화 완료');
});
