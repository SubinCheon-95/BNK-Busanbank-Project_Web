document.addEventListener('DOMContentLoaded', () => {
    //체크박스 전체 선택
    const allSelect = document.querySelector(".all_select");
    const checkboxes = document.querySelectorAll("tbody input[type='checkbox']");

    allSelect.addEventListener("change", function() {
        checkboxes.forEach(cb => cb.checked = allSelect.checked);
    });
});