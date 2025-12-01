package kr.co.busanbank.controller;

import kr.co.busanbank.domain.ConsultantStatus;
import kr.co.busanbank.dto.CategoryDTO;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.dto.chat.ConsultantDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.CategoryService;
import kr.co.busanbank.service.chat.ChatMessageService;
import kr.co.busanbank.service.chat.ChatSessionService;
import kr.co.busanbank.service.chat.ConsultantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/*
    이름 : 우지희
    날짜 :
    내용 : 채팅(상담사) 컨트롤러
 */

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cs/chat/consultant")
public class ChatConsultantController {

    private final CategoryService categoryService;
    private final ChatSessionService chatSessionService;
    private final ConsultantService consultantService;
    private final ChatMessageService chatMessageService;

    @ModelAttribute("csHeaderCategories")
    public Map<String, Object> getCsHeaderCategories() {
        Map<String, Object> headerData = new HashMap<>();

        try {
            // 고객상담 (CATEGORYID=30의 하위)
            List<CategoryDTO> customerSupport = categoryService.getCategoriesByParentId(30);
            headerData.put("customerSupport", customerSupport);

            log.info("Test " + customerSupport.toString());

            // 이용안내 (CATEGORYID=35의 하위)
            List<CategoryDTO> usageGuide = categoryService.getCategoriesByParentId(35);
            headerData.put("usageGuide", usageGuide);

            // 금융소비자보호 (CATEGORYID=43의 하위)
            List<CategoryDTO> consumerProtection = categoryService.getCategoriesByParentId(43);
            headerData.put("consumerProtection", consumerProtection);

            // 상품공시실 (CATEGORYID=58의 하위)
            List<CategoryDTO> productDisclosure = categoryService.getCategoriesByParentId(58);
            headerData.put("productDisclosure", productDisclosure);

            // 서식/약관/자료실 (CATEGORYID=67의 하위)
            List<CategoryDTO> archives = categoryService.getCategoriesByParentId(67);
            headerData.put("archives", archives);

            log.info("고객센터 헤더 카테고리 로드 - 고객상담:{}, 이용안내:{}, 소비자보호:{}, 상품공시:{}, 서식자료:{}",
                    customerSupport.size(), usageGuide.size(),
                    consumerProtection.size(), productDisclosure.size(), archives.size());

        } catch (Exception e) {
            log.error("고객센터 헤더 카테고리 로드 실패: {}", e.getMessage());
            headerData.put("customerSupport", new ArrayList<>());
            headerData.put("usageGuide", new ArrayList<>());
            headerData.put("consumerProtection", new ArrayList<>());
            headerData.put("productDisclosure", new ArrayList<>());
            headerData.put("archives", new ArrayList<>());
        }

        return headerData;
    }

    @GetMapping
    public String agentConsole(@AuthenticationPrincipal MyUserDetails principal,
                               Model model) {

        if (principal == null) {
            return "redirect:/member/login";
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);

        if (consultant == null) {
            // 상담원 정보 없으면 접근 막기
            return "redirect:/member/login?noConsultant";
        }

        int consultantId = consultant.getConsultantId();

        List<ChatSessionDTO> waitingList  = chatSessionService.getWaitingSessions();
        List<ChatSessionDTO> chattingList = chatSessionService.getChattingSessions(consultantId);

        model.addAttribute("consultant", consultant);
        model.addAttribute("waitingList", waitingList);
        model.addAttribute("chattingList", chattingList);

        return "cs/chat/consultant";
    }

    /** 상담원 → 세션 배정 */
    @PostMapping("/assign")
    @ResponseBody
    public ResponseEntity<?> assign(
            @AuthenticationPrincipal MyUserDetails principal,
            @RequestParam("sessionId") int sessionId
    ) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);

        if (consultant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "상담원 권한이 없습니다."));
        }

        int consultantId = consultant.getConsultantId();
        log.info("배정 요청 - sessionId={}, consultantId={}, loginId={}",
                sessionId, consultantId, loginId);

        // 1) 세션에 상담원 배정
        int updated = chatSessionService.assignConsultant(sessionId, consultantId);

        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "유효하지 않은 sessionId", "sessionId", sessionId));
        }

        // 2) 상담원 상태 BUSY로 변경
        consultantService.updateStatus(consultantId, ConsultantStatus.BUSY);

        return ResponseEntity.ok(Map.of(
                "result", "OK",
                "sessionId", sessionId,
                "consultantId", consultantId
        ));
    }

    @PostMapping("/assignNext")
    @ResponseBody
    public ResponseEntity<?> assignNext(@AuthenticationPrincipal MyUserDetails principal) {

        if (principal == null) {
            // 세션 만료 등으로 인증이 끊어진 상태
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);
        if (consultant == null) {
            // 로그인은 되었지만 상담원 정보가 없는 계정
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("NO_CONSULTANT");
        }
        int consultantId = consultant.getConsultantId();

        // Redis 대기열 기반으로 다음 세션 배정
        ChatSessionDTO session = chatSessionService.assignNextWaitingSession(consultantId);

        if (session == null) {
            return ResponseEntity.ok(Map.of(
                "result", "NO_WAITING"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "result", "OK",
                "sessionId", session.getSessionId(),
                "session", session
        ));
    }

    /** 상담원 콘솔용 대기/진행 세션 리스트 조회 (AJAX) */
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal MyUserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);
        if (consultant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "상담원 권한이 없습니다."));
        }

        int consultantId = consultant.getConsultantId();

        List<ChatSessionDTO> waitingList  = chatSessionService.getWaitingSessions();
        List<ChatSessionDTO> chattingList = chatSessionService.getChattingSessions(consultantId);

        // 진행중 세션에 대해, 상담원 기준 미읽음 개수 계산
        List<Map<String, Object>> chattingWithUnread = chattingList.stream()
                .map(s -> {
                    int unread = chatMessageService.countUnread(s.getSessionId(), consultantId);

                    Map<String, Object> map = new HashMap<>();
                    map.put("sessionId", s.getSessionId());
                    map.put("inquiryType", s.getInquiryType());
                    map.put("status", s.getStatus());
                    map.put("unreadCount", unread);
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "waitingList",  waitingList,
                "chattingList", chattingWithUnread
        ));
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@RequestParam("sessionId") Integer sessionId,
                                         @AuthenticationPrincipal MyUserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);
        if (consultant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "상담원 권한이 없습니다."));
        }
        int consultantId = consultant.getConsultantId();

        // 1) 메시지 목록 조회
        List<ChatMessageDTO> list = chatMessageService.getMessageBySessionId(sessionId);

        // 2) 읽음 처리
        chatMessageService.markMessageAsRead(sessionId, consultantId);

        return ResponseEntity.ok(list);
    }

    // 상담원 기준 읽음 처리
    @PostMapping("/messages/read")
    @ResponseBody
    public ResponseEntity<?> markMessagesRead(@RequestParam("sessionId") Integer sessionId,
                                              @AuthenticationPrincipal MyUserDetails principal) {

        // 1) 인증 체크
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);
        if (consultant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "상담원 권한이 없습니다."));
        }

        int consultantId = consultant.getConsultantId();

        // 2) 읽음 처리 서비스 호출
        try {
            chatMessageService.markMessageAsRead(sessionId, consultantId);
            return ResponseEntity.ok(Map.of(
                    "result", "OK",
                    "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("메시지 읽음 처리 중 오류 - sessionId={}, consultantId={}", sessionId, consultantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "읽음 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/end")
    @ResponseBody
    public ResponseEntity<?> endSession(
            @AuthenticationPrincipal MyUserDetails principal,
            @RequestParam("sessionId") int sessionId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "UNAUTHORIZED"));
        }

        String loginId = principal.getUsername();
        ConsultantDTO consultant = consultantService.getConsultantByLoginId(loginId);
        if (consultant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("result", "NO_CONSULTANT"));
        }

        int consultantId = consultant.getConsultantId();
        log.info("🔚 상담 종료 요청 - sessionId={}, consultantId={}", sessionId, consultantId);

        // 1) 세션 상태 CLOSED 처리 (DB)
        int updated = chatSessionService.closeSession(sessionId);
        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("result", "INVALID_SESSION", "sessionId", sessionId));
        }

        // 2) 상담원 상태 변경 여부는 정책에 따라
        // consultantService.updateStatus(consultantId, ConsultantStatus.IDLE);

        return ResponseEntity.ok(Map.of(
                "result", "OK",
                "sessionId", sessionId
        ));
    }
}


