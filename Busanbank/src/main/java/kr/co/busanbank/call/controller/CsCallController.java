package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.dto.CallTokenRequest;
import kr.co.busanbank.call.dto.CallTokenResponse;
import kr.co.busanbank.call.service.CallTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/cs/call")
@RequiredArgsConstructor
public class CsCallController {

    private final CallTokenService service;

    /**
     * ✅ 상담사(세션 로그인)용 토큰 발급
     * - 팝업(voice/agent.html)에서 호출
     * - /api/call/token(JWT용) 건드리지 않음
     */
    @PostMapping("/token")
    public CallTokenResponse token(@RequestBody CallTokenRequest request,
                                   Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        // ✅ 상담사 화면에서 role을 클라가 마음대로 보내는 건 위험하니 서버에서 강제
        // - Agora token 발급 로직이 role 문자열을 요구하면 CONSULTANT로 고정
        // - (서비스가 "PUBLISHER"/"SUBSCRIBER" 같은 값을 요구한다면 여기만 그 값으로 바꾸면 됨)
        String fixedRole = "CONSULTANT";

        return service.issue(request.getSessionId(), fixedRole);
    }
}
