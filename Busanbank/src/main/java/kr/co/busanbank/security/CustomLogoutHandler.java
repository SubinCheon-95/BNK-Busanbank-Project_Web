package kr.co.busanbank.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.service.LoginTimePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * 작성자: 진원
 * 작성일: 2025-12-04
 * 설명: 로그아웃 시 로그인 시간 포인트 세션 제거 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final LoginTimePointService loginTimePointService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Integer userNo = (Integer) session.getAttribute("userNo");

            if (userNo != null) {
                // 로그인 시간 포인트 세션 제거 (작성자: 진원, 2025-12-04)
                loginTimePointService.removeLoginSession(userNo);
                log.info("🚪 로그아웃 - 세션 제거 완료, userId: {}", userNo);
            }
        }
    }
}
