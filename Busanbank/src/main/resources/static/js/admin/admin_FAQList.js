document.addEventListener('DOMContentLoaded', () => {
    //체크박스 전체 선택
    const allSelect = document.querySelector(".all_select");
    const checkboxes = document.querySelectorAll("tbody input[type='checkbox']");

    allSelect.addEventListener("change", function() {
        checkboxes.forEach(cb => cb.checked = allSelect.checked);
    });

    //삭제 기능
    const deleteBtn = document.querySelector(".select_delete");
    deleteBtn.addEventListener("click", async () => {
       const selected = document.querySelectorAll("tbody .row-checkbox:checked");
       const idList = Array.from(selected).map(cb => cb.value);
       console.log("삭제 id리스트: ", idList);

        const response = await fetch("/busanbank/admin/faq/list", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(idList)
        });
        if(response.ok) {
            alert("삭제 완료!");
            location.reload();
        } else {
            alert("삭제 실패!");
        }
    });

    const menuBtns = document.querySelectorAll(".cs_menuBtn");
    menuBtns.forEach(menuBtn => {
        menuBtn.addEventListener("click", function() {
            menuBtns.forEach(btn => btn.classList.remove("active"));
            menuBtn.classList.add("active");
        });
    });
});