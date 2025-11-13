/* 아이디 or 비밀번호 찾기 화면 전환 */
document.addEventListener("DOMContentLoaded", () => {
    const emailBtn = document.getElementById("emailBtn");
    const hpBtn = document.getElementById("hpBtn");
    const emailTable = document.getElementById("authEmailTable");
    const hpTable = document.getElementById("authHpTable");

    if (emailBtn && hpBtn && emailTable && hpTable) {

        // 함수: 특정 테이블의 input을 disabled on/off
        const setInputsDisabled = (table, disabled) => {
            table.querySelectorAll("input").forEach(input => {
                input.disabled = disabled;
            });
        };

        // 페이지 로드 시: 휴대폰 입력 비활성화
        setInputsDisabled(hpTable, true);

        // 이메일 인증 버튼 클릭
        emailBtn.addEventListener("click", () => {
            document.getElementById("authMethod").value = 1;

            emailBtn.classList.add("active");
            hpBtn.classList.remove("active");

            emailTable.style.display = "table";
            hpTable.style.display = "none";

            setInputsDisabled(emailTable, false);
            setInputsDisabled(hpTable, true);
        });

        // 휴대폰 인증 버튼 클릭
        hpBtn.addEventListener("click", () => {
            document.getElementById("authMethod").value = 2;

            hpBtn.classList.add("active");
            emailBtn.classList.remove("active");

            hpTable.style.display = "table";
            emailTable.style.display = "none";

            setInputsDisabled(hpTable, false);
            setInputsDisabled(emailTable, true);
        });
    }
});
