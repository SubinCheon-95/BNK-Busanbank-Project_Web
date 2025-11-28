package kr.co.busanbank.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWaitingQueueService {

    private static final String WAITING_QUEUE_KEY = "chat:waitingQueue";

    private final StringRedisTemplate redisTemplate;

    /**
     * 새로 생성된 대기 세션을 큐에 넣기
     */
    public void enqueue(int sessionId) {
        redisTemplate.opsForList().leftPush(WAITING_QUEUE_KEY, String.valueOf(sessionId));
        log.info("📥 대기열 등록 - sessionId={}", sessionId);
    }

    /**
     * 다음 상담할 세션 하나 꺼내기 (없으면 null)
     */
    public Integer popNextSession() {
        String value = redisTemplate.opsForList().rightPop(WAITING_QUEUE_KEY);
        if (value == null) {
            log.info("ℹ️ 대기열이 비어 있습니다.");
            return null;
        }
        try {
            Integer sessionId = Integer.valueOf(value);
            log.info("📤 대기열에서 배정 - sessionId={}", sessionId);
            return sessionId;
        } catch (NumberFormatException e) {
            log.error("❌ 잘못된 sessionId 값: {}", value, e);
            return null;
        }
    }

    /**
     * 현재 대기열 개수
     */
    public long waitingCount() {
        Long size = redisTemplate.opsForList().size(WAITING_QUEUE_KEY);
        return size != null ? size : 0;
    }
}
