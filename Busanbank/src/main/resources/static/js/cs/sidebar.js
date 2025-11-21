// /static/js/cs/sidebar.js

(function () {
    'use strict';

    /**
     * 개별 sidebar 요소에 대해 토글/접근성 처리
     * @param {HTMLElement} sidebarEl
     */
    function wireSidebar(sidebarEl) {
        if (!sidebarEl) return;

        // 2차 메뉴 앵커들만 대상으로
        const items = sidebarEl.querySelectorAll('.sidebar-menu > a');

        items.forEach((btn, idx) => {
            // 접근성 속성
            btn.setAttribute('role', 'button');
            btn.setAttribute('aria-expanded', 'false');

            const submenu = btn.nextElementSibling;
            if (submenu && submenu.classList.contains('side-submenu')) {
                const id = submenu.id || ('side-submenu-' + idx);
                submenu.id = id;
                btn.setAttribute('aria-controls', id);
            }

            btn.addEventListener('click', function (e) {
                const submenuEl = btn.nextElementSibling;
                const hasSub = submenuEl && submenuEl.classList.contains('side-submenu');

                const href = btn.getAttribute('href') || '';
                const isRealLink =
                    href !== '' &&
                    href !== '#' &&
                    !href.startsWith('javascript:');

                // === 1) 3차 없는 2차: 그냥 링크로 이동 ===
                if (!hasSub) {
                    // href가 제대로 있으면 브라우저 기본 동작(이동)
                    if (isRealLink) {
                        return;
                    }
                    // 링크도 없으면 아무 동작 안 함
                    e.preventDefault();
                    return;
                }

                // === 2) 3차가 있는 2차: 토글 전용 버튼 ===
                // 이 경우에는 펼치기/접기만 하고 페이지 이동은 막음
                e.preventDefault();

                const item = btn.parentElement;

                // 같은 sidebar 내 다른 메뉴 접기
                sidebarEl.querySelectorAll('.sidebar-menu.open').forEach(function (m) {
                    if (m !== item) {
                        m.classList.remove('open');
                        const a = m.querySelector(':scope > a');
                        if (a) {
                            a.setAttribute('aria-expanded', 'false');
                        }
                    }
                });

                // 현재 메뉴 열고/닫기
                const opened = item.classList.toggle('open');
                btn.setAttribute('aria-expanded', opened ? 'true' : 'false');
            });

            // 키보드 접근: Enter / Space 로 클릭과 동일하게 처리
            btn.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    btn.click();
                }
            });
        });
    }

    function wireAllSidebars() {
        const sidebars = document.querySelectorAll('.sidebar');
        sidebars.forEach(wireSidebar);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', wireAllSidebars);
    } else {
        wireAllSidebars();
    }
})();
