/*
    수정일 : 2025/11/30
    수정자 : 천수빈
    내용 : 예금상품공시 탭-사이드바 연동
*/

(function initDepositTabs(){
    const table    = document.querySelector('.sd-table');
    if (!table) return;

    const panels   = [...table.querySelectorAll('.dp-panel')];
    const sideAnch = [...document.querySelectorAll('.sidebar .sidebar-menu > a')];
    const heading  = document.getElementById('pageHeading');

    const KEY_BY_TEXT = {
        '적립식예금':'saving',
        '거치식예금':'lump',
        '입출금자유로운예금':'demand',
        '주택청약관련예금':'housing',
        '외화예금':'fx',
        '예금금리조회':'rate'
    };

    // 사이드바 링크에 data-key 부여
    sideAnch.forEach(a => {
        const text = a.textContent.trim();
        const key  = KEY_BY_TEXT[text];
        if (key) {
            a.dataset.key = key;
        }
    });

    function updateHeadingByKey(key){
        const panel = panels.find(p => p.dataset.key === key);
        const title = panel?.dataset.title || '예금상품공시';
        if (heading) heading.textContent = title;
        document.title = `${title} - 예금상품공시`;
    }

    function setActive(key, {pushHash = true} = {}){
        panels.forEach(p =>
            p.classList.toggle('is-active', p.dataset.key === key)
        );
        sideAnch.forEach(a =>
            a.classList.toggle('is-active', a.dataset.key === key)
        );
        if (pushHash) {
            history.replaceState(null, '', '#' + key);
        }
        updateHeadingByKey(key);
    }

    // 사이드바 클릭 이벤트
    sideAnch.forEach(a => {
        a.addEventListener('click', e => {
            const key = a.dataset.key;
            if (!key) return;

            const hasPanel = panels.some(p => p.dataset.key === key);
            if (!hasPanel) {
                return;
            }
            e.preventDefault();
            setActive(key);
        });
    });

    // 초기 표시
    const initKey = location.hash.replace('#','');
    const valid   = panels.some(p => p.dataset.key === initKey);
    setActive(valid ? initKey : 'saving', {pushHash:false});

    window.addEventListener('hashchange', () => {
        const k = location.hash.replace('#','');
        if (k) {
            const ok = panels.some(p => p.dataset.key === k);
            if (ok) setActive(k, {pushHash:false});
        }
    });
})();