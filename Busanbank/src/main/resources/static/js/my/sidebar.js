 // 페이지 로드 시 현재 페이지 메뉴 활성화
window.addEventListener('DOMContentLoaded', () => {
    const currentPath = window.location.pathname;

    document.querySelectorAll('.sub-link').forEach(link => {
        const href = link.getAttribute('href');

        if(href === currentPath || currentPath.startsWith(href + '/')){ //currentPath.startsWith(href + '/')를 추가하면서 검색 등을 할때 사이드바 활성화 유지 가능
        link.classList.add('active');

        const parent = link.closest('.side-item');
        parent.classList.add('active');
        parent.querySelector('.sub-menu').classList.add('active');
        parent.dataset.current = "true";
        }
    });
    });

    // 메인 메뉴 클릭 이벤트
    document.querySelectorAll('.main-menu').forEach(menu => {
    menu.addEventListener('click', function(e) {
        e.preventDefault();
        const parent = this.closest('.side-item');
        const subMenu = parent.querySelector('.sub-menu');

        // 다른 메뉴 닫기 (현재 페이지 메뉴는 유지)
        document.querySelectorAll('.side-item').forEach(item => {
        if(item !== parent && !item.dataset.current){
            item.querySelector('.sub-menu').classList.remove('active');
            item.classList.remove('active');
        }
        });


        subMenu.classList.toggle('active');
        parent.classList.toggle('active');

    });
});