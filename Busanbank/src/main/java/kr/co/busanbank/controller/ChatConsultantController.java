package kr.co.busanbank.controller;

import kr.co.busanbank.domain.ConsultantStatus;
import kr.co.busanbank.dto.chatting.ChatMessageDTO;
import kr.co.busanbank.dto.chatting.ChatSessionDTO;
import kr.co.busanbank.dto.chatting.ConsultantDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.chatting.ChatMessageService;
import kr.co.busanbank.service.chatting.ChatSessionService;
import kr.co.busanbank.service.chatting.ConsultantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/cs/chatting")
public class ChatConsultantController {

    private final ChatSessionService chatSessionService;
    private final ConsultantService consultantService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/consultant")
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

        return "cs/chatting/consultant";
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

}