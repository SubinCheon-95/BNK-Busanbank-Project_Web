document.addEventListener('DOMContentLoaded', () => {
    const buttons = document.querySelectorAll('.title_btn');

    buttons.forEach(btn => {
        btn.addEventListener('click', () => {
        // 모든 버튼 비활성화
        buttons.forEach(b => b.classList.remove('active'));

        // 클릭된 버튼만 활성화
        btn.classList.add('active');
        });
    });
});