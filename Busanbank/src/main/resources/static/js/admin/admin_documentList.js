document.addEventListener('DOMContentLoaded', () => {
    //체크박스 전체 선택
    const allSelect = document.querySelector(".all_select");
    const checkboxes = document.querySelectorAll("tbody input[type='checkbox']");

    allSelect.addEventListener("change", function() {
        checkboxes.forEach(cb => cb.checked = allSelect.checked);
    });

    //삭제 기능
    const deleteBtn = document.querySelector(".select_delete");

    const menuBtns = document.querySelectorAll(".cs_menuBtn");

    menuBtns.forEach(menuBtn => {
        menuBtn.addEventListener("click", function() {
            menuBtns.forEach(btn => btn.classList.remove("active"));
            menuBtn.classList.add("active");
        });
    });
});