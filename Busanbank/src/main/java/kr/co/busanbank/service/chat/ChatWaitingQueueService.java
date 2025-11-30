package kr.co.busanbank.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWaitingQueueService {

    // ✅ ZSET 키 이름 (새로 사용)
    private static final String WAITING_ZSET_KEY = "chat:waitingZset";

    private final StringRedisTemplate redisTemplate;

    /**
     * 새 대기 세션을 ZSET에 등록
     * @param sessionId     세션 PK
     * @param priorityScore DB에 저장된 비즈니스 우선순위 점수
     */
    public void enqueue(int sessionId, int priorityScore) {
        long now = System.currentTimeMillis();
        // K 값은 상황에 따라 조정 (우선순위 한 단계당 얼마나 당길지)
        long factor = 1_000_000L;

        double score = now - (priorityScore * factor);

        redisTemplate.opsForZSet()
                .add(WAITING_ZSET_KEY, String.valueOf(sessionId), score);

        log.info("📥 ZSET 대기열 등록 - sessionId={}, priorityScore={}, score={}",
                sessionId, priorityScore, score);
    }

    /**
     * 다음 상담할 세션 하나 꺼내기 (우선순위가 가장 낮은 score = 가장 오래된 대기)
     * Redis 5+ / Spring Data Redis에서 popMin 지원
     */
    public Integer popNextSession() {
        ZSetOperations.TypedTuple<String> tuple =
                redisTemplate.opsForZSet().popMin(WAITING_ZSET_KEY);

        if (tuple == null) {
            log.info("ℹ️ ZSET 대기열이 비어 있습니다.");
            return null;
        }

        String value = tuple.getValue();
        try {
            Integer sessionId = Integer.valueOf(value);
            log.info("📤 ZSET 대기열에서 배정 - sessionId={}, score={}", sessionId, tuple.getScore());
            return sessionId;
        } catch (NumberFormatException e) {
            log.error("❌ 잘못된 sessionId 값(ZSET): {}", value, e);
            return null;
        }
    }

    /**
     * 현재 대기열 개수
     */
    public long waitingCount() {
        Long size = redisTemplate.opsForZSet().zCard(WAITING_ZSET_KEY);
        return size != null ? size : 0L;
    }

    /**
     * 필요시: 특정 세션을 대기열에서 강제로 제거
     */
    public void remove(int sessionId) {
        redisTemplate.opsForZSet()
                .remove(WAITING_ZSET_KEY, String.valueOf(sessionId));
    }
}
