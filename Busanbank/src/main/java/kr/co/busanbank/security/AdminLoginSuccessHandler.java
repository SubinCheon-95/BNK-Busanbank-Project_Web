package kr.co.busanbank.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.AdminDTO;
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
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        AdminUserDetails adminUserDetails = (AdminUserDetails) authentication.getPrincipal();
        AdminDTO admin = adminUserDetails.getAdminDTO();

        HttpSession session = request.getSession();

        session.setAttribute("userNo", admin.getAdminId());

        String loginId = admin.getLoginId();

        session.setAttribute("loginId", loginId);



        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();



        // 기본 리다이렉트
        redirectStrategy.sendRedirect(request, response, "/admin");


    }
}