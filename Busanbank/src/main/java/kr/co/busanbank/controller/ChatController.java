package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.chat.ChatMessageService;
import kr.co.busanbank.service.chat.ChatSessionService;
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
                                       @SessionAttribute(name = "user", required = false) UsersDTO sessionUser,
                                       @RequestBody Map<String, String> req) {

        try {
            UsersDTO loginUser = null;
            String loginId = null;
            boolean fromSession   = false;
            boolean fromPrincipal = false;

            // 1) 먼저 세션의 user(@SessionAttributes("user")) 를 우선 사용 (상품가입/일반 고객)
            if (sessionUser != null && sessionUser.getUserNo() > 0) {
                loginUser = sessionUser;

                // 🔹 UsersDTO에는 loginId가 아니라 userId 필드가 있으므로 이걸 사용
                loginId = sessionUser.getUserId();  // ← 여기 핵심

                fromSession = true;
            }

            // 2) 세션 user 가 없을 때만 Security principal 사용 (상담원/관리자 등)
            if (!fromSession && principal != null) {
                String principalId = principal.getUsername();

                if (principalId != null && !principalId.isBlank()) {
                    loginId = principalId;
                    fromPrincipal = true;

                    // principal 기반으로 DB 조회
                    loginUser = chatSessionService.getUserByLoginId(loginId);
                }
            }

            log.info("💬 /cs/chat/start 호출 - fromSession={}, fromPrincipal={}, loginId={}",
                    fromSession, fromPrincipal, loginId);

            // 3) 로그인 사용자 정보 검증
            if (loginUser == null) {
                log.error("❌ startChat - 로그인 사용자 정보 없음 (loginUser=null, loginId={})", loginId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            // (선택) loginId 가 비어있으면 로그용으로만 email/hp 를 써도 됨
            if (loginId == null || loginId.isBlank()) {
                loginId = loginUser.getEmail() != null ? loginUser.getEmail() : ("USER-" + loginUser.getUserNo());
            }

            // 4) 문의 유형(inquiryType) 확인
            String inquiryType = req.get("inquiryType");
            if (inquiryType == null || inquiryType.isBlank()) {
                log.warn("❌ startChat - inquiryType 누락, req={}", req);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "문의 유형이 필요합니다."));
            }

            int realUserNo       = loginUser.getUserNo();
            String priorityLevel = loginUser.getUserPriority();  // BASIC / VIP 등

            // 5) 우선순위 점수 계산
            int priorityScore = chatSessionService.calcPriorityScore(priorityLevel, inquiryType);
            log.info("📌 priorityLevel={}, inquiryType={}, priorityScore={}",
                    priorityLevel, inquiryType, priorityScore);

            // 6) 채팅 세션 생성
            ChatSessionDTO session =
                    chatSessionService.findOrCreateSession(realUserNo, inquiryType, priorityScore);

            log.info("✅ 채팅 세션 생성 완료 - sessionId={}, userNo={}, loginId={}",
                    session.getSessionId(), realUserNo, loginId);

            return ResponseEntity.ok(Map.of("sessionId", session.getSessionId()));

        } catch (Exception e) {
            log.error("❌ /cs/chat/start 처리 중 예외 발생, req={}", req, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
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

    @GetMapping("/active")
    public ResponseEntity<?> getActiveSession(@AuthenticationPrincipal MyUserDetails principal,
                                              @SessionAttribute(name="user", required=false) UsersDTO sessionUser) {
        UsersDTO loginUser = (sessionUser != null) ? sessionUser : (principal != null ? principal.getUsersDTO() : null);
        if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","로그인 필요"));

        ChatSessionDTO active = chatSessionService.getActiveSession(loginUser.getUserNo()); // mapper 호출
        if (active == null) return ResponseEntity.ok(Map.of("hasActive", false));

        return ResponseEntity.ok(Map.of("hasActive", true, "sessionId", active.getSessionId(), "inquiryType", active.getInquiryType()));
    }

}