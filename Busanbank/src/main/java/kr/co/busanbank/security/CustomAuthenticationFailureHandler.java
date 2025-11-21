package kr.co.busanbank.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.dto.SecuritySettingDTO;
import kr.co.busanbank.mapper.AdminMapper;
import kr.co.busanbank.service.SecuritySettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 작성자: 진원
 * 작성일: 2025-11-20
 * 설명: 관리자 로그인 실패 처리 핸들러
 */
@Slf4j
@RequiredArgsConstructor
@Component("adminAuthenticationFailureHandler")
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AdminMapper adminMapper;
    private final SecuritySettingService securitySettingService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String loginId = request.getParameter("username");
        log.info("관리자 로그인 실패 - loginId: {}", loginId);

        try {
            // 로그인 실패 횟수 증가
            adminMapper.incrementLoginFailCount(loginId);

            // 관리자 정보 조회
            AdminDTO admin = adminMapper.selectAdminByLoginId(loginId);

            if (admin != null) {
                // DB에서 로그인 실패 제한 설정 조회
                SecuritySettingDTO setting = securitySettingService.getSettingByKey("LOGIN_FAIL_LIMIT");
                int failLimit = setting != null ? Integer.parseInt(setting.getSettingvalue()) : 5;

                int currentFailCount = admin.getLoginFailCount() != null ? admin.getLoginFailCount() : 0;

                log.info("현재 로그인 실패 횟수: {} / 제한: {}", currentFailCount, failLimit);

                // 실패 횟수가 제한을 초과하면 계정 잠금
                if (currentFailCount >= failLimit) {
                    adminMapper.lockAccount(loginId);
                    log.warn("계정 잠금 - loginId: {} ({}회 로그인 실패)", loginId, currentFailCount);
                    setDefaultFailureUrl("/busanbank/admin/login?error=locked");
                } else {
                    int remainingAttempts = failLimit - currentFailCount;
                    log.info("남은 로그인 시도 횟수: {}", remainingAttempts);
                    setDefaultFailureUrl("/busanbank/admin/login?error=true&remaining=" + remainingAttempts);
                }
            } else {
                setDefaultFailureUrl("/busanbank/admin/login?error=true");
            }

        } catch (Exception e) {
            log.error("로그인 실패 처리 중 오류: {}", e.getMessage());
            setDefaultFailureUrl("/busanbank/admin/login?error=true");
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}
