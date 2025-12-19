package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.CsService;
import kr.co.busanbank.service.chat.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService service;
    private final CsService csService;

    private int resolveUserNo(Authentication authentication) throws Exception {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED");
        }

        Object principal = authentication.getPrincipal();

        // 1) 현재 로그 기준: principal이 UsersDTO로 들어옴
        if (principal instanceof UsersDTO u) {
            // userNo가 제대로 있으면 바로 사용
            if (u.getUserNo() != 0) return u.getUserNo();

            // userNo가 0이면 userId로 DB 조회해서 보정
            // ⚠️ u.getUserId() 타입이 String이면 그대로, 숫자면 String.valueOf로
            String userId = String.valueOf(u.getUserId());
            UsersDTO loginUser = csService.getUserById(userId);
            if (loginUser == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND");
            }
            return loginUser.getUserNo();
        }

        // 2) 혹시 principal이 String(username)로 들어오는 경우까지 방어
        if (principal instanceof String userId) {
            UsersDTO loginUser = csService.getUserById(userId);
            if (loginUser == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND");
            }
            return loginUser.getUserNo();
        }

        // 3) 그 외 타입은 인증구조가 예상과 달라서 거절
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_PRINCIPAL");
    }

    @GetMapping("/sessions")
    public Map<String, Object> sessions(
            Authentication authentication,
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "20") int size
    ) throws Exception {

        int userNo = resolveUserNo(authentication);
        return service.getSessions(userNo, cursor, size);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Map<String, Object> messages(
            Authentication authentication,
            @PathVariable int sessionId,
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "50") int size
    ) throws Exception {

        int userNo = resolveUserNo(authentication);
        return service.getMessages(userNo, sessionId, cursor, size);
    }
}
