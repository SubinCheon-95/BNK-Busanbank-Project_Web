package kr.co.busanbank.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.busanbank.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 작성자: 진원
 * 작성일: 2025-11-20
 * 설명: 관리자 로그인 성공 처리 핸들러 (로그인 실패 카운트 리셋)
 */
@Slf4j
@RequiredArgsConstructor
@Component("adminAuthenticationSuccessHandler")
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AdminMapper adminMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String loginId = authentication.getName();
        log.info("관리자 로그인 성공 - loginId: {}", loginId);

        try {
            // 로그인 성공 시 실패 카운트 리셋
            adminMapper.resetLoginFailCount(loginId);
            log.info("로그인 실패 카운트 리셋 완료 - loginId: {}", loginId);

        } catch (Exception e) {
            log.error("로그인 성공 처리 중 오류: {}", e.getMessage());
        }

        // 기본 성공 URL로 리다이렉트
        setDefaultTargetUrl("/busanbank/admin");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
