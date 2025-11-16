/**
 *  폼 유효성 검사 자바스크립트
 */
// 유효성 검사에 사용할 정규표현식

const rePass  = /^(?=.*[a-zA-z])(?=.*[0-9])(?=.*[$`~!@$!%*#^?&\\(\\)\-_=+]).{5,16}$/;

// 유효성 검사 상태 변수
let isPassOk = false;


document.addEventListener('DOMContentLoaded', function(){
    const checkPw = document.getElementById('checkPw');
    const passResult = document.getElementsByClassName('passResult')[0];

    const form = document.getElementsByTagName('form')[0];


    //////////////////////////////////////////////////////////
    // 비밀번호 검사
    //////////////////////////////////////////////////////////
    form.userPw2.addEventListener('focusout', function(e){

        const userPw1 = form.userPw.value;
        const userPw2 = form.userPw2.value;

        // 비밀번호 유효성 검사
        if(!userPw1.match(rePass)){
            passResult.innerText = '비밀번호가 유효하지 않습니다.';
            passResult.style.color = 'red';
            isPassOk = false;
            return;
        }

        // 비밀번호 2회 일치 여부
        if(userPw1 == userPw2){
            passResult.innerText = '비밀번호가 일치합니다.';
            passResult.style.color = 'green';
            isPassOk = true;
        }else{
            passResult.innerText = '비밀번호가 일치하지 않습니다.';
            passResult.style.color = 'red';
            isPassOk = false;
        }
    });


    // 최종 폼 전송 처리
    form.addEventListener('submit', function(e){
        e.preventDefault(); // 기본 폼전송 해제

        if(passResult&&!isPassOk){
            alert('비밀번호를 확인하세요.');
            return;
        }


        // 최종 폼 전송 실행
        form.submit();
    });

}); // DOMContentLoaded 끝