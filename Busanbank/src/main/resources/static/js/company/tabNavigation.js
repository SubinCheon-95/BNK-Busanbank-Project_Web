/*
    수정일 : 2025/12/02
    수정자 : 천수빈
    내용 : 은행소개 탭 메뉴 구현
*/

// Tab switching functionality
const tabs = document.querySelectorAll('.tab');
const boardContents = document.querySelectorAll('.board-content');

tabs.forEach(tab => {
    tab.addEventListener('click', () => {
        // Remove active class from all tabs
        tabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        // Get the target content
        const targetTab = tab.getAttribute('data-tab');

        // Hide all content sections
        boardContents.forEach(content => {
            content.classList.remove('active');
        });

        // Show the target content
        const targetContent = document.querySelector(`[data-content="${targetTab}"]`);
        if (targetContent) {
            targetContent.classList.add('active');

            // Animate items if it's a list type
            if (targetContent.classList.contains('content-list')) {
                const boardItems = targetContent.querySelectorAll('.board-item');
                boardItems.forEach((item, index) => {
                    item.style.opacity = '0';
                    item.style.transform = 'translateY(20px)';

                    setTimeout(() => {
                        item.style.transition = 'all 0.4s ease';
                        item.style.opacity = '1';
                        item.style.transform = 'translateY(0)';
                    }, index * 100);
                });
            }

            // Animate items if it's a detail type
            if (targetContent.classList.contains('content-detail')) {
                const detailItems = targetContent.querySelectorAll('.detail-item');
                detailItems.forEach((item, index) => {
                    item.style.opacity = '0';
                    item.style.transform = 'translateX(-20px)';

                    setTimeout(() => {
                        item.style.transition = 'all 0.5s ease';
                        item.style.opacity = '1';
                        item.style.transform = 'translateX(0)';
                    }, index * 150);
                });
            }
        }
    });
});

// Hover effect for cards
const cards = document.querySelectorAll('.value-card');

cards.forEach(card => {
    card.addEventListener('mouseenter', function() {
        cards.forEach(c => {
            if (c !== this) {
                c.style.opacity = '0.6';
            }
        });
    });

    card.addEventListener('mouseleave', function() {
        cards.forEach(c => {
            c.style.opacity = '1';
        });
    });
});