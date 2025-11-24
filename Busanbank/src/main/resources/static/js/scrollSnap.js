/*
    수정일 : 2025/11/24
    수정자 : 천수빈
    내용 : 스크롤 스냅 기능 구현
*/

const dots = document.querySelectorAll('.nav-dot');

dots.forEach(dot => {
    dot.addEventListener('click', () => {
        const target = document.getElementById(dot.dataset.target);
        target.scrollIntoView({ behavior: 'smooth' });
    });
});

const sections = document.querySelectorAll('.snap-section');
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const id = entry.target.id;

            dots.forEach(dot => {
                dot.classList.toggle('active', dot.dataset.target === id);
            });
        }
    });
}, { threshold: 0.5 });

sections.forEach(sec => observer.observe(sec));
