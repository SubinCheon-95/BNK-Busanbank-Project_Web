
(function(){
    function initTabs(containerSelector, tabCls, panelCls, underlineCls){
        document.querySelectorAll(containerSelector).forEach(root=>{
            const tabs = root.querySelectorAll(tabCls);
            const panels = root.querySelectorAll(panelCls);
            const underline = root.querySelector(underlineCls);

            function activate(tab){
                tabs.forEach(t=>t.classList.remove('is-active'));
                panels.forEach(p=>p.classList.remove('is-active'));
                tab.classList.add('is-active');

                const target = document.getElementById(tab.getAttribute('aria-controls'))
                    || document.querySelector(tab.dataset.tabTarget);
                if (target) target.classList.add('is-active');

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

    // 각 페이지의 탭을 한 번에 초기화
    initTabs('.service-time', '.st-tab', '.st-panel', '.st-underline');
    initTabs('.preferred', '.pf-tab', '.pf-panel', '.pf-underline');
})();

document.addEventListener("DOMContentLoaded", function () {
    const efTabs   = document.querySelectorAll(".ef-tabs li");
    const efPanels = document.querySelectorAll(".ef-panel");

    efTabs.forEach(tab => {
        tab.addEventListener("click", function (e) {

            if (tab.dataset.pageLink === 'true') {
                return;
            }

            const link = this.querySelector('a');
            if (link) e.preventDefault();

            efTabs.forEach(t => t.classList.remove("is-active"));
            this.classList.add("is-active");

            const target = this.dataset.target;

            efPanels.forEach(panel => {
                panel.classList.remove("is-active");
            });

            const activePanel = document.getElementById(target);
            if (activePanel) {
                activePanel.classList.add("is-active");
            }
        });
    });
});

document.addEventListener("DOMContentLoaded", function() {
    const tabs = document.querySelectorAll(".use-rate-tabs li");
    const panels = document.querySelectorAll(".use-rate-panel");

    tabs.forEach(tab => {
        tab.addEventListener("click", function() {

            // 1) 모든 탭 비활성화
            tabs.forEach(t => t.classList.remove("is-active"));

            // 2) 클릭한 탭 활성화
            this.classList.add("is-active");

            const target = this.dataset.target;

            // 3) 모든 패널 숨김
            panels.forEach(panel => {
                panel.classList.remove("is-active");
                panel.style.display = "none";
            });

            // 4) 해당 패널만 보이기
            const activePanel = document.getElementById(target);
            if (activePanel) {
                activePanel.classList.add("is-active");
                activePanel.style.display = "block";
            }
        });
    });
});