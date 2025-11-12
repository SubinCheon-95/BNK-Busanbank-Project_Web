// GNB 드롭다운 제어
document.querySelectorAll('.menu-item > a').forEach(menu => {
  menu.addEventListener('click', e => {
    e.preventDefault();

    const item = menu.parentElement;
    const isActive = item.classList.contains('active');

    // 모든 메뉴 닫기
    document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));

    // 클릭한 메뉴만 열기
    if (!isActive) item.classList.add('active');
  });
});
