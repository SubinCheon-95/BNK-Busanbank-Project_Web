/*
    날짜 : 2025/11/24
    이름 : 오서정
    내용 : 회원 로그인 시 상태 처리 로직 추가
*/
package kr.co.busanbank.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component("userAuthenticationFailureHandler")
@RequiredArgsConstructor
@Slf4j
public class UserAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MemberMapper memberMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String userId = request.getParameter("userId");
        log.info("회원 로그인 실패 - userId: {}", userId);

        // 원본 예외 또는 cause가 LockedException 인 경우 (W)
        if (exception instanceof LockedException ||
                exception.getCause() instanceof LockedException) {

            response.sendRedirect(request.getContextPath() + "/member/login?error=withdrawPending");
            return;
        }

        // 원본 예외 또는 cause가 DisabledException 인 경우 (D)
        if (exception instanceof DisabledException ||
                exception.getCause() instanceof DisabledException) {

            response.sendRedirect(request.getContextPath() + "/member/login?error=withdrawComplete");
            return;
        }

        // 그 외 일반 실패
        response.sendRedirect(request.getContextPath() +  "/member/login?error=true");
    }
}