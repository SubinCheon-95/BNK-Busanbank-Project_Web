<!--
    수정일 : 2025/11/29
    수정자 : 천수빈
    내용 : data-tab-target 우선 확인
-->

document.addEventListener('DOMContentLoaded', function(){
    function initTabs(containerSelector, tabCls, panelCls, underlineCls){
        document.querySelectorAll(containerSelector).forEach(root=>{
            const tabs = root.querySelectorAll(tabCls);
            const panels = root.querySelectorAll(panelCls);
            const underline = root.querySelector(underlineCls);

            if(tabs.length === 0) {
                console.log('탭을 찾을 수 없습니다:', containerSelector);
                return;
            }

            function activate(tab){
                tabs.forEach(t=>t.classList.remove('is-active'));
                panels.forEach(p=>p.classList.remove('is-active'));
                tab.classList.add('is-active');

                // 수정: data-tab-target 우선 확인
                const targetSelector = tab.dataset.tabTarget || '#' + tab.getAttribute('aria-controls');
                const target = document.querySelector(targetSelector);

                if (target) {
                    target.classList.add('is-active');
                } else {
                    console.log('패널을 찾을 수 없습니다:', targetSelector);
                }

                if (underline){
                    const rect = tab.getBoundingClientRect();
                    const parentRect = tab.parentElement.getBoundingClientRect();
                    underline.style.width = rect.width + 'px';
                    underline.style.transform = `translateX(${rect.left - parentRect.left}px)`;
                }
            }

            tabs.forEach(tab => tab.addEventListener('click', ()=> activate(tab)));
            const initTab = root.querySelector(`${tabCls}.is-active`) || tabs[0];
            if (initTab) activate(initTab);
        });
    }

    // 여기서 실행
    initTabs('.preferred', '.pf-tab', '.pf-panel', '.pf-underline');

    console.log('탭 초기화 완료');
});