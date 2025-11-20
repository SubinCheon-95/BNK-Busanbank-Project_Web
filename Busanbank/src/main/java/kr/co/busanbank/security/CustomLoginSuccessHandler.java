package kr.co.busanbank.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.UsersDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        UsersDTO user = userDetails.getUsersDTO();

        HttpSession session = request.getSession();

        session.setAttribute("userNo", user.getUserNo());
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("user", user);

        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        RequestCache requestCache = new HttpSessionRequestCache();

        // 1. 🔥 Spring Security가 저장한 원래 요청 URL 있는지 확인
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            log.info("🔄 [SavedRequest 존재] → {}", targetUrl);
            redirectStrategy.sendRedirect(request, response, targetUrl);
            return;
        }

        // 2. 🔥 세션에 저장해둔 redirect_uri 체크
        String redirectUri = (session != null) ? (String) session.getAttribute("redirect_uri") : null;

        if (redirectUri != null && !redirectUri.isBlank()) {
            log.info("🔄 [redirect_uri 감지] → {}", redirectUri);
            session.removeAttribute("redirect_uri"); // 일회성 사용
            redirectStrategy.sendRedirect(request, response, redirectUri);
            return;
        }

        // 3. 기본 리다이렉트 (마이페이지)
        log.info("🔄 redirect_uri 없음 → 기본 /my 이동");
        redirectStrategy.sendRedirect(request, response, "/my");
    }
}

