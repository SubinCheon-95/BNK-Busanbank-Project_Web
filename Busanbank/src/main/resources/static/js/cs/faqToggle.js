document.addEventListener('DOMContentLoaded', function () {
    const faqSection = document.querySelector('section.question');
    if (!faqSection) return;

    const faqList = faqSection.querySelector('ul');
    if (!faqList) return;

    // 이벤트 위임 방식: ul 하나에만 리스너 달기
    faqList.addEventListener('click', function (event) {
        const questionDiv = event.target.closest('.faq-question');
        if (!questionDiv) return; // 질문 영역이 아니면 무시

        const li = questionDiv.closest('li');
        if (!li) return;

        const answerDiv = li.querySelector('.faq-answer');
        if (!answerDiv) return;

        const isActive = answerDiv.classList.contains('active');

        // 다른 답변들은 모두 닫기
        faqList.querySelectorAll('.faq-answer.active').forEach(function (el) {
            el.classList.remove('active');
        });

        // 지금 클릭한 것만 토글
        if (!isActive) {
            answerDiv.classList.add('active');
        }
    });
});

/* 검색 선택창 화살표 회전 25.11.30 수빈 */
document.addEventListener("DOMContentLoaded", () => {
    const selectEl = document.querySelector(".select-wrapper select");
    selectEl.addEventListener("change", () => {
        selectEl.blur();   // 선택 후 select에서 focus 제거
    });
});

