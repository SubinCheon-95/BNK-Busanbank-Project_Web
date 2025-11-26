package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chatting.ChatMessageDTO;
import kr.co.busanbank.dto.chatting.ChatSessionDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.CsService;
import kr.co.busanbank.service.chatting.ChatMessageService;
import kr.co.busanbank.service.chatting.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/*
    이름 : 우지희
    날짜 :
    내용 : 채팅(유저) 컨트롤러
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/cs/chat")
public class ChatController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

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
    /** 특정 세션의 메시지 이력 조회 */
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@RequestParam("sessionId") Integer sessionId,
                                         @AuthenticationPrincipal MyUserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        // TODO: 접근권한 체크(해당 세션의 사용자/상담원인지) 필요하면 여기서 추가

        List<ChatMessageDTO> list = chatMessageService.getMessageBySessionId(sessionId);

        // TODO: 나중에 읽음 처리 쓸 거면 여기에서
        // UsersDTO user = chatSessionService.getUserByLoginId(principal.getUsername());
        // chatMessageService.markMessageAsRead(sessionId, user.getUserNo());

        return ResponseEntity.ok(list);
    }

    // 읽음 처리
    @PostMapping("/messages/read")
    public ResponseEntity<?> markRead(@RequestParam("sessionId") Integer sessionId,
                                      @AuthenticationPrincipal MyUserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        UsersDTO user = principal.getUsersDTO();
        int updated = chatMessageService.markMessageAsRead(sessionId, user.getUserNo());

        return ResponseEntity.ok(Map.of("updated", updated));
    }

}