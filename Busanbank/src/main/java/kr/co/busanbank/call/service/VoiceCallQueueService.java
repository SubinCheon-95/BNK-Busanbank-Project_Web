package kr.co.busanbank.call.service;

import kr.co.busanbank.call.dto.VoiceWaitingSessionDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VoiceCallQueueService {

    private final StringRedisTemplate redis;

    // ✅ Redis Keys
    private static final String WAITING_ZSET = "call:voice:waiting";                 // ZSET(sessionId -> score)
    private static final String LOCK_PREFIX  = "call:voice:lock:";                  // lock:sessionId
    private static final String ACTIVE_PREFIX = "call:voice:active:";               // active:sessionId -> consultantId
    private static final String AGENT_ACTIVE_PREFIX = "call:voice:agent:active:";   // agent:active:consultantId -> sessionId

    // TTL 정책
    private static final Duration LOCK_TTL = Duration.ofSeconds(8);
    private static final Duration ACTIVE_TTL = Duration.ofMinutes(30);

    public VoiceCallQueueService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 대기 목록 조회 */
    public List<VoiceWaitingSessionDTO> getWaitingList(int limit) {
        Set<String> ids = redis.opsForZSet().range(WAITING_ZSET, 0, Math.max(0, limit - 1));
        if (ids == null || ids.isEmpty()) return List.of();
        return ids.stream()
                .map(id -> new VoiceWaitingSessionDTO(id, "WAITING"))
                .collect(Collectors.toList());
    }

    /** 고객(또는 테스트)이 전화 요청을 대기열에 넣음 */
    public void enqueue(String sessionId) {
        redis.opsForZSet().add(WAITING_ZSET, sessionId, System.currentTimeMillis());
    }

    /** 상담사 1콜 제한: 이미 진행중이면 수락 불가 */
    public boolean isAgentBusy(String consultantId) {
        String key = AGENT_ACTIVE_PREFIX + consultantId;
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    /** 수락(accept): 원자적으로 대기열에서 제거 + 상담사 active 1개 할당 */
    public Map<String, Object> accept(String sessionId, String consultantId) {
        // 0) 상담사 1콜 제한 체크 (먼저)
        if (isAgentBusy(consultantId)) {
            return Map.of("ok", false, "reason", "AGENT_BUSY");
        }

        // 1) 세션 락
        String lockKey = LOCK_PREFIX + sessionId;
        Boolean locked = redis.opsForValue().setIfAbsent(lockKey, consultantId, LOCK_TTL);
        if (Boolean.FALSE.equals(locked)) {
            return Map.of("ok", false, "reason", "LOCKED");
        }

        // 2) waiting에서 제거 (없으면 이미 누군가 가져감)
        Long removed = redis.opsForZSet().remove(WAITING_ZSET, sessionId);
        if (removed == null || removed == 0) {
            return Map.of("ok", false, "reason", "NOT_WAITING");
        }

        // 3) 상담사 active 키를 선점(1콜 제한을 강제)
        String agentActiveKey = AGENT_ACTIVE_PREFIX + consultantId;
        Boolean agentClaimed = redis.opsForValue().setIfAbsent(agentActiveKey, sessionId, ACTIVE_TTL);
        if (Boolean.FALSE.equals(agentClaimed)) {
            // 상담사가 방금 다른 콜을 잡았으면 롤백: 다시 waiting에 넣어줌
            redis.opsForZSet().add(WAITING_ZSET, sessionId, System.currentTimeMillis());
            return Map.of("ok", false, "reason", "AGENT_BUSY");
        }

        // 4) session active 기록
        String sessionActiveKey = ACTIVE_PREFIX + sessionId;
        redis.opsForValue().set(sessionActiveKey, consultantId, ACTIVE_TTL);

        return Map.of(
                "ok", true,
                "sessionId", sessionId,
                "consultantId", consultantId,
                "acceptedAt", Instant.now().toString()
        );
    }

    /** 종료(end): active 해제 */
    public void end(String sessionId, String consultantId) {
        // sessionId가 상담사에게 할당된 상태인지 확인 후 정리(보수적으로)
        String sessionActiveKey = ACTIVE_PREFIX + sessionId;
        String owner = redis.opsForValue().get(sessionActiveKey);
        if (owner != null && owner.equals(consultantId)) {
            redis.delete(sessionActiveKey);
            redis.delete(AGENT_ACTIVE_PREFIX + consultantId);
        } else {
            // 소유가 다르거나 없으면 최소한 세션 active는 삭제하지 않음(다른 상담사 통화중일 수 있음)
            // 필요하면 로그만 남기세요.
        }
    }

    /** 상담사 현재 활성 통화 세션 조회(선택) */
    public String getAgentActiveSession(String consultantId) {
        return redis.opsForValue().get(AGENT_ACTIVE_PREFIX + consultantId);
    }
}
