document.addEventListener("DOMContentLoaded", () => {
    const emailBtn = document.getElementById("emailBtn");
    const hpBtn = document.getElementById("hpBtn");
    const emailTable = document.getElementById("authEmailTable");
    const hpTable = document.getElementById("authHpTable");

    // 이메일 인증 버튼 클릭
    emailBtn.addEventListener("click", () => {
        emailBtn.classList.add("active");
        hpBtn.classList.remove("active");
        emailTable.style.display = "table"; 
        hpTable.style.display = "none"; 
    });

    // 휴대폰 인증 버튼 클릭
    hpBtn.addEventListener("click", () => {
        hpBtn.classList.add("active");
        emailBtn.classList.remove("active");
        hpTable.style.display = "table";
        emailTable.style.display = "none";
    });
});