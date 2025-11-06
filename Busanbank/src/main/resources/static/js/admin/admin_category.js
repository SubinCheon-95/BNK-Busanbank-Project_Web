document.addEventListener('DOMContentLoaded', () => {
    const categories = [
        { button: '.category1', menu: '.category1-ul' },
        { button: '.category2', menu: '.category2-ul' },
        { button: '.category3', menu: '.category3-ul' },
        { button: '.category4', menu: '.category4-ul' },
        { button: '.category5', menu: '.category5-ul' }
    ];

    const initialTops = {
        category2: 295,
        category3: 360,
        category4: 425,
        category5: 490
    };

    const getHeight = (el) => el.offsetHeight || 0;

    // 모든 메뉴 닫기 함수
    const closeAll = () => {
        categories.forEach(({ menu }) => {
            document.querySelector(menu).style.display = 'none';
        });
    };

    // top 위치 초기화 함수
    const resetTops = () => {
        document.querySelector('.category2').style.top = initialTops.category2 + 'px';
        document.querySelector('.category3').style.top = initialTops.category3 + 'px';
        document.querySelector('.category4').style.top = initialTops.category4 + 'px';
        document.querySelector('.category5').style.top = initialTops.category5 + 'px';
    };

    categories.forEach(({ button, menu }, index) => {
        const btn = document.querySelector(button);
        const submenu = document.querySelector(menu);

        btn.addEventListener('click', () => {
            const isOpen = submenu.style.display === 'block';

            // 모든 메뉴 닫고 top 초기화
            closeAll();
            resetTops();

            if (!isOpen) {
                // 현재 클릭한 메뉴만 열기
                submenu.style.display = 'block';

                // 현재 인덱스 이후 카테고리들의 위치 조정
                const submenuHeight = getHeight(submenu);
                for (let i = index + 1; i < categories.length; i++) {
                    const next = document.querySelector(categories[i].button);
                    const nextKey = `category${i + 1}`;
                    next.style.top = (initialTops[nextKey] + submenuHeight) + 'px';
                }
            }
        });
    });
});
