package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.dto.VoiceWaitingSessionDTO;

import kr.co.busanbank.call.service.VoiceCallQueueService;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cs/call/voice")
@RequiredArgsConstructor
public class AgentVoiceCallController {

    private final VoiceCallQueueService service;
    private final MemberMapper memberMapper;

    @Value("${call.acceptAllowQueryId:false}")
    private boolean acceptAllowQueryId;

    @GetMapping("/waiting")
    public List<VoiceWaitingSessionDTO> waiting(Authentication authentication) {
        String consultantId = resolveConsultantId(authentication, null);
        log.info("[VOICE] waiting called by={}", consultantId);
        List<VoiceWaitingSessionDTO> list = service.getWaitingList(50);
        log.info("[VOICE] waiting size={}", list.size());
        return list;
    }

    /** ✅ 고객 enqueue: JWT 기반(Flutter) */
    @PostMapping("/enqueue/{sessionId}")
    public ResponseEntity<?> enqueue(@PathVariable String sessionId,
                                     Authentication authentication) {

        if (!StringUtils.hasText(sessionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionId is required");
        }

        // ✅ JWT principal은 userId/role만 있으니, 여기서 userId로 DB 조회해서 full info 확보
        String userId = extractUserId(authentication);
        UsersDTO full = (userId == null) ? null : memberMapper.findByUserId(userId);

        // ✅ 로그는 필요한 값만 (객체 통째로 찍지 않기)
        log.info("[VOICE] enqueue sessionId={} userId={} userNo={} userName={} role={}",
                sessionId.trim(),
                userId,
                full != null ? full.getUserNo() : null,
                full != null ? full.getUserName() : null,
                full != null ? full.getRole() : extractRole(authentication)
        );

        service.enqueue(sessionId.trim());

        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId.trim()));
    }

    /** 수락 */
    @PostMapping("/{sessionId}/accept")
    public ResponseEntity<?> accept(@PathVariable String sessionId,
                                    @RequestParam(required = false) String consultantId,
                                    Authentication authentication) {

        String cid = resolveConsultantId(authentication, consultantId);
        if (cid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "reason", "UNAUTHORIZED"));
        }

        // ✅ 상담사도 필요하면 이름/번호 조회 가능
        UsersDTO consultant = memberMapper.findByUserId(cid);

        log.info("[VOICE] accept sessionId={} cid={} consultantNo={} consultantName={}",
                sessionId, cid,
                consultant != null ? consultant.getUserNo() : null,
                consultant != null ? consultant.getUserName() : null
        );

        return ResponseEntity.ok(service.accept(sessionId, cid));
    }

    /** 종료 */
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<?> end(@PathVariable String sessionId,
                                 @RequestParam(required = false) String consultantId,
                                 Authentication authentication) {

        String cid = resolveConsultantId(authentication, consultantId);
        if (cid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("ok", false, "reason", "UNAUTHORIZED"));
        }

        log.info("[VOICE] end sessionId={} cid={}", sessionId, cid);

        service.end(sessionId, cid);
        return ResponseEntity.ok(Map.of("ok", true, "sessionId", sessionId));
    }

    private String resolveConsultantId(Authentication authentication, String consultantId) {
        if (acceptAllowQueryId && StringUtils.hasText(consultantId)) {
            return consultantId.trim();
        }
        if (authentication == null || !authentication.isAuthenticated()) return null;

        // memberSecurity(formLogin) 쪽이면 authentication.getName()이 userId(15)로 잘 들어옴
        return authentication.getName();
    }

    // ✅ JWT/세션 공통으로 userId 뽑기
    private String extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object p = authentication.getPrincipal();
        if (p instanceof UsersDTO u) {
            return u.getUserId(); // JWT principal: userId만 있음(10)
        }
        // 세션로그인 principal이 UserDetails인 경우도 대비
        if (p instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            return ud.getUsername();
        }
        // fallback
        return authentication.getName();
    }

    private String extractRole(Authentication authentication) {
        if (authentication == null) return null;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }
}
