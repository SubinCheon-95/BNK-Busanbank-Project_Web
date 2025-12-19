package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.dto.chat.ChatStartRequest;
import kr.co.busanbank.dto.chat.ChatStartResponse;
import kr.co.busanbank.service.CsService;
import kr.co.busanbank.service.chat.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CsService csService; // ✅ userId로 DB 조회해서 userNo 얻기

    @PostMapping("/start")
    public ResponseEntity<ChatStartResponse> startChat(@RequestBody ChatStartRequest req) {

        ChatStartResponse res = new ChatStartResponse();

        try {
            // ✅ 1) JWT 인증 사용자 꺼내기 (JwtProvider.getAuthentication()에서 principal = UsersDTO)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
                res.setStatus("FAIL");
                res.setMessage("로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            UsersDTO principal = (UsersDTO) auth.getPrincipal();
            String loginUserId = principal.getUserId(); // JWT claim username -> UsersDTO.userId
            if (loginUserId == null || loginUserId.isBlank()) {
                res.setStatus("FAIL");
                res.setMessage("로그인 사용자 정보를 확인할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            // ✅ 2) loginUserId로 DB에서 userNo(PK) 조회
            UsersDTO user = csService.getUserById(loginUserId);
            if (user == null) {
                res.setStatus("FAIL");
                res.setMessage("사용자 정보를 조회할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            int userNo = user.getUserNo(); // 먼저 꺼내고

            if (userNo <= 0) {             // 그 다음 검증
                res.setStatus("FAIL");
                res.setMessage("사용자 번호가 올바르지 않습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }

            log.info("📥 /api/chat/start 호출 - loginUserId={}, userNo={}, inquiryType={}",
                    loginUserId, userNo, req.getInquiryType());

            // ✅ 3) 우선순위 점수 계산
            // principal.getRole()이 "USER" 같은 값이라면 등급으로 쓰기 애매하니 지금은 BASIC 유지
            int priorityScore = chatSessionService.calcPriorityScore("BASIC", req.getInquiryType());

            // ✅ 4) 세션 생성 (DB userNo로 저장)
            ChatSessionDTO session = chatSessionService.createChatSession(
                    userNo,
                    req.getInquiryType(),
                    priorityScore
            );

            res.setSessionId(session.getSessionId());
            res.setStatus("SUCCESS");
            res.setMessage("상담 세션이 생성되었습니다.");
            return ResponseEntity.ok(res);

        } catch (ClassCastException e) {
            // principal 타입이 UsersDTO가 아닌 경우 방어
            log.error("❌ JWT principal 타입 캐스팅 실패", e);
            res.setStatus("FAIL");
            res.setMessage("인증 정보 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);

        } catch (Exception e) {
            log.error("❌ /api/chat/start 처리 중 예외", e);
            res.setStatus("FAIL");
            res.setMessage("상담 세션 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }
}