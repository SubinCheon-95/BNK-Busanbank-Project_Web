package kr.co.busanbank.call.service;

import kr.co.busanbank.call.dto.VoiceWaitingSessionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VoiceCallQueueService {

    private final StringRedisTemplate redis;

    @Value("${chat.call.voice.waitingZset:call:voice:waiting}")
    private String voiceWaitingZset;

    public VoiceCallQueueService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ✅ Redis Keys (voiceWaitingZset 외에는 prefix만)
    private static final String LOCK_PREFIX  = "call:voice:lock:";                // lock:{sessionId}
    private static final String ACTIVE_PREFIX = "call:voice:active:";             // active:{sessionId} -> consultantId
    private static final String AGENT_ACTIVE_PREFIX = "call:voice:agent:active:"; // agent:active:{consultantId} -> sessionId

    // TTL 정책
    private static final Duration LOCK_TTL = Duration.ofSeconds(8);
    private static final Duration ACTIVE_TTL = Duration.ofMinutes(30);

    /** 대기 목록 조회 */
    public List<VoiceWaitingSessionDTO> getWaitingList(int limit) {
        Set<String> ids = redis.opsForZSet().range(voiceWaitingZset, 0, limit - 1);
        if (ids == null || ids.isEmpty()) return List.of();

        return ids.stream()
                .map(id -> new VoiceWaitingSessionDTO(id, "WAITING"))
                .toList();
    }

    /** 고객(또는 테스트)이 전화 요청을 대기열에 넣음 */
    public void enqueue(String sessionId) {
        redis.opsForZSet().add(voiceWaitingZset, sessionId, System.currentTimeMillis());
    }

    /** waiting에서 제거 */
    public void removeFromWaiting(String sessionId) {
        redis.opsForZSet().remove(voiceWaitingZset, sessionId);
    }

    /** 상담사 1콜 제한: 이미 진행중이면 수락 불가 */
    public boolean isAgentBusy(String consultantId) {
        return Boolean.TRUE.equals(redis.hasKey(AGENT_ACTIVE_PREFIX + consultantId));
    }

    /** 수락(accept): 원자적으로 대기열에서 제거 + 상담사 active 1개 할당 */
    public Map<String, Object> accept(String sessionId, String consultantId) {

        if (sessionId == null || sessionId.isBlank()) {
            return Map.of("ok", false, "reason", "SESSION_ID_REQUIRED");
        }
        if (consultantId == null || consultantId.isBlank()) {
            return Map.of("ok", false, "reason", "CONSULTANT_ID_REQUIRED");
        }

        // 0) 상담사 1콜 제한 체크
        if (isAgentBusy(consultantId)) {
            return Map.of("ok", false, "reason", "AGENT_BUSY");
        }

        // 1) 세션 락
        String lockKey = LOCK_PREFIX + sessionId;
        Boolean locked = redis.opsForValue().setIfAbsent(lockKey, consultantId, LOCK_TTL);
        if (Boolean.FALSE.equals(locked)) {
            return Map.of("ok", false, "reason", "LOCKED");
        }

        // 2) waiting에서 제거 (✅ 반드시 voiceWaitingZset 사용)
        Long removed = redis.opsForZSet().remove(voiceWaitingZset, sessionId);
        if (removed == null || removed == 0) {
            return Map.of("ok", false, "reason", "NOT_WAITING");
        }

        // 3) 상담사 active 키 선점(1콜 제한 강제)
        String agentActiveKey = AGENT_ACTIVE_PREFIX + consultantId;
        Boolean agentClaimed = redis.opsForValue().setIfAbsent(agentActiveKey, sessionId, ACTIVE_TTL);
        if (Boolean.FALSE.equals(agentClaimed)) {
            // 롤백: 다시 waiting에 넣기
            redis.opsForZSet().add(voiceWaitingZset, sessionId, System.currentTimeMillis());
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

    /** 종료(end): active 해제 + waiting 잔여 제거(보수적) */
    public void end(String sessionId, String consultantId) {
        if (sessionId == null || sessionId.isBlank()) return;

        String sessionActiveKey = ACTIVE_PREFIX + sessionId;
        String owner = redis.opsForValue().get(sessionActiveKey);

        // ✅ waiting에 남아있을 수도 있으니 무조건 제거(보수적)
        removeFromWaiting(sessionId);

        // active 자체가 없으면(이미 끝났거나 accept 전) 여기서 종료
        if (owner == null || owner.isBlank()) {
            // 상담사 키도 혹시 남았을 수 있으니 consultantId가 있으면 삭제 시도
            if (consultantId != null && !consultantId.isBlank()) {
                redis.delete(AGENT_ACTIVE_PREFIX + consultantId);
            }
            return;
        }

        // consultantId가 왔는데 owner랑 다르면 -> 다른 상담사 통화중이므로 종료 금지
        if (consultantId != null && !consultantId.isBlank() && !owner.equals(consultantId)) {
            return;
        }

        // 정상 종료: 둘 다 삭제
        redis.delete(sessionActiveKey);
        redis.delete(AGENT_ACTIVE_PREFIX + owner);
    }

    /** 상담사 현재 활성 통화 세션 조회(선택) */
    public String getAgentActiveSession(String consultantId) {
        return redis.opsForValue().get(AGENT_ACTIVE_PREFIX + consultantId);
    }

    // ✅ sessionId가 waiting zset에 있는지
    public boolean isWaiting(String sessionId) {
        Double score = redis.opsForZSet().score(voiceWaitingZset, sessionId);
        return score != null;
    }

    // ✅ accept되었으면 active:{sessionId} = consultantId
    public String getAcceptedConsultantId(String sessionId) {
        return redis.opsForValue().get(ACTIVE_PREFIX + sessionId);
    }
}