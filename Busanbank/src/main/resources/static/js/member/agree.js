const agreeBtn = document.querySelector('.agree-btn');

if (agreeBtn) {
  agreeBtn.addEventListener('click', function (e) {
    e.preventDefault();

    const basic = document.querySelector('input[name="basic"]:checked');
    const finance = document.querySelector('input[name="finance"]:checked');

    if (!basic || !finance) {
      alert("필수 약관에 모두 동의해야 합니다.");
      return;
    } else if (basic.value !== "yes" || finance.value !== "yes") {
      alert("필수 약관에 모두 동의해야 합니다.");
      return;
    }

    window.location.href = '/busanbank/member/register';
  });
}
