package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.dto.VoiceWaitingSessionDTO;
import kr.co.busanbank.call.service.VoiceCallQueueService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/call/voice")
public class AgentVoiceCallController {

    private final VoiceCallQueueService service;

    public AgentVoiceCallController(VoiceCallQueueService service) {
        this.service = service;
    }

    /** 대기 리스트 */
    @GetMapping("/waiting")
    public List<VoiceWaitingSessionDTO> waiting() {
        return service.getWaitingList(50);
    }

    /** 수락 */
    @PostMapping("/{sessionId}/accept")
    public Map<String, Object> accept(@PathVariable String sessionId, Authentication authentication) {
        String consultantId = authentication.getName(); // ✅ 현재 로그인 아이디(유저ID)가 consultantId로 쓰일 수 있는지 확인 필요
        return service.accept(sessionId, consultantId);
    }

    /** 종료 */
    @PostMapping("/{sessionId}/end")
    public Map<String, Object> end(@PathVariable String sessionId, Authentication authentication) {
        String consultantId = authentication.getName();
        service.end(sessionId, consultantId);
        return Map.of("ok", true);
    }

    /** (테스트용) 대기열 enqueue — 운영에서는 막아도 됨 */
    @PostMapping("/enqueue/{sessionId}")
    public Map<String, Object> enqueue(@PathVariable String sessionId) {
        service.enqueue(sessionId);
        return Map.of("ok", true, "sessionId", sessionId);
    }
}
