package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chatting.ChatSessionDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.CsService;
import kr.co.busanbank.service.chatting.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/cs/chat")
public class ChatController {

    private final ChatSessionService chatSessionService;

    /** 상담 시작 (세션 생성) */
    @PostMapping("/start")
    public ResponseEntity<?> startChat(@AuthenticationPrincipal MyUserDetails principal,
                                        @RequestBody Map<String, String> req
    ) throws Exception {

        // 1) 로그인 확인
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }
        // 2) 로그인 사용자 정보
        String loginId = principal.getUsername();
        UsersDTO loginUser = chatSessionService.getUserByLoginId(loginId);

        int realUserNo = loginUser.getUserNo();
        String inquiryType = req.get("inquiryType");

        // 3) 채팅 세션 생성
        ChatSessionDTO session = chatSessionService.createChatSession(realUserNo, inquiryType);
        return ResponseEntity.ok(Map.of("sessionId", session.getSessionId()));
    }


}