package kr.co.busanbank.call.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class CallAgentAvailabilityService {

    private final StringRedisTemplate redis;

    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String consultantReadyZset;

    @Value("${chat.redis.consultant.statusPrefix:chat:consultant:status:}")
    private String consultantStatusPrefix;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String consultantLoadZset;

    public CallAgentAvailabilityService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** 상담사 READY 등록 */
    public void markReady(String consultantId) {
        if (consultantId == null || consultantId.isBlank()) return;

        long now = Instant.now().toEpochMilli();

        // READY 큐 등록(점수는 now로: 오래 대기한 상담사가 먼저 배정되도록)
        redis.opsForZSet().add(consultantReadyZset, consultantId, now);

        // 상태 표시
        redis.opsForValue().set(consultantStatusPrefix + consultantId, "READY");

        // loadZset 없으면 0으로 초기화(있으면 유지)
        Double cur = redis.opsForZSet().score(consultantLoadZset, consultantId);
        if (cur == null) {
            redis.opsForZSet().add(consultantLoadZset, consultantId, 0.0);
        }

        log.info("✅ consultant READY. consultantId={}", consultantId);
    }

    /** 상담사 READY 해제(오프라인) */
    public void markOffline(String consultantId) {
        if (consultantId == null || consultantId.isBlank()) return;

        // READY 큐에서 제거
        redis.opsForZSet().remove(consultantReadyZset, consultantId);

        // 상태 표시
        redis.opsForValue().set(consultantStatusPrefix + consultantId, "OFFLINE");

        log.info("🔌 consultant OFFLINE. consultantId={}", consultantId);
    }
}
