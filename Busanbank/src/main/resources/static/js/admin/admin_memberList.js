document.addEventListener('DOMContentLoaded', () => {

    const modal = document.getElementById("myModal");
    const detailBtns = document.querySelectorAll(".detailBtn");
    const closeBtn = modal.querySelector(".close");
    const form = document.getElementById("shopForm");

    detailBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const userNo = btn.dataset.userno;

            fetch(`/busanbank/admin/member/detail?userNo=${userNo}`)
                .then(res => res.json())
                .then(data => {

                    document.getElementById("modalUserNo").innerText = data.userNo;
                    document.getElementById("modalUserName").innerText = data.userName;
                    document.getElementById("modalUserId").innerText = data.userId;
                    document.getElementById("modalHp").innerText = data.hp;
                    document.getElementById("modalAccountNo").innerText = data.accountNo;
                    document.getElementById("modalBalance").innerText = data.balance;
                    document.getElementById("modalUpdatedAt").innerText = data.updatedAt;

                    if(data.status === "A") {
                        document.getElementById("modalStatus").innerText = "정상";
                    } else {
                        document.getElementById("modalStatus").innerText = "휴면";
                        document.getElementById("modalStatus").classList.remove("status-badge");
                        document.getElementById("modalStatus").classList.add("status-badge2");
                    }

                    document.getElementById("modalRegDate").innerText = data.regDate;
                    modal.style.display = "block";
                });
        });
    });

    closeBtn.addEventListener("click", () => {
        modal.style.display = "none"
        form.reset();
    });
    window.addEventListener("click", (e) => {
        if (e.target === modal) modal.style.display = "none";
    });

    const modalContent = modal.querySelector(".modal-content");
    modalContent.addEventListener("click", (e) => {
        e.stopPropagation(); // 클릭 이벤트가 부모로 전달되지 않도록 막음
    });
});