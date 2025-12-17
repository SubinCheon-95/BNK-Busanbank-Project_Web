package kr.co.busanbank.controller;

import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.dto.chat.ChatStartRequest;
import kr.co.busanbank.dto.chat.ChatStartResponse;
import kr.co.busanbank.service.chat.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatSessionService chatSessionService;

    @PostMapping("/start")
    public ResponseEntity<ChatStartResponse> startChat(@RequestBody ChatStartRequest req) {

        ChatStartResponse res = new ChatStartResponse();

        try {
            log.info("📥 /api/chat/start 호출 - userId={}, inquiryType={}",
                    req.getUserId(), req.getInquiryType());

            // 로그인 안 된 경우(0 또는 null) → DB에는 null로 저장
            Integer userId = req.getUserId();
            if (userId != null && userId <= 0) {
                userId = null;
            }

            int priorityScore = chatSessionService.calcPriorityScore("BASIC", req.getInquiryType());

            ChatSessionDTO session = chatSessionService.createChatSession(
                    userId,
                    req.getInquiryType(),
                    priorityScore
            );

            res.setSessionId(session.getSessionId());
            res.setStatus("SUCCESS");
            res.setMessage("상담 세션이 생성되었습니다.");

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            log.error("❌ /api/chat/start 처리 중 예외", e);
            res.setStatus("FAIL");
            res.setMessage("상담 세션 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }
}
