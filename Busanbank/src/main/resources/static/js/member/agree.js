const agreeBtn = document.querySelector('.agree-btn');

if (agreeBtn) {
  agreeBtn.addEventListener('click', function (e) {
    e.preventDefault();

    const basic = document.querySelector('input[name="basic"]:checked');
    const finance = document.querySelector('input[name="finance"]:checked');
    const privacy = document.querySelector('input[name="privacy"]:checked');

    if (!basic || !finance || !privacy) {
      alert("필수 약관에 모두 동의해야 합니다.");
      return;
    } else if (basic.value !== "yes" || finance.value !== "yes" || privacy.value !== "yes") {
      alert("필수 약관에 모두 동의해야 합니다.");
      return;
    }

    window.location.href = '/html/member/register.html';
  });
}
