package kr.co.busanbank.service.chat;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWaitingQueueService {

    private final StringRedisTemplate redisTemplate;

    @Value("${chat.redis.waitingZset:chat:waitingZset}")
    private String waitingZsetKey;

    @Value("${chat.redis.assigningZset:chat:queue:assigning}")
    private String assigningZsetKey;

    private DefaultRedisScript<List> claimScript;

    @PostConstruct
    public void init() {
        claimScript = new DefaultRedisScript<>();
        claimScript.setScriptText("""
        local r = redis.call('ZPOPMIN', KEYS[1], tonumber(ARGV[1]))
        if (r == nil or #r == 0) then return nil end
        local member = r[1]
        local score  = r[2]
        redis.call('ZADD', KEYS[2], score, member)
        return {member, score}
    """);
        claimScript.setResultType(List.class);
    }

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
                .add(waitingZsetKey, String.valueOf(sessionId), score);

        log.info("📥 ZSET 대기열 등록 - key={}, sessionId={}, priorityScore={}, score={}",
                waitingZsetKey, sessionId, priorityScore, score);
    }

    /**
     * ✅ 다음 세션 “Claim” (waiting에서 꺼내 assigning으로 이동) — 유실 방지 핵심
     */
    public ClaimResult claimNext() {
        List res = redisTemplate.execute(
                claimScript,
                List.of(waitingZsetKey, assigningZsetKey),
                "1"
        );

        if (res == null || res.size() < 2) {
            log.info("ℹ️ 대기열 비어있음. key={}", waitingZsetKey);
            return null;
        }

        String member = String.valueOf(res.get(0));
        double score = Double.parseDouble(String.valueOf(res.get(1)));

        try {
            int sessionId = Integer.parseInt(member);
            log.info("📤 claim 성공 - sessionId={}, score={}, waitingKey={}, assigningKey={}",
                    sessionId, score, waitingZsetKey, assigningZsetKey);
            return new ClaimResult(sessionId, score);
        } catch (NumberFormatException e) {
            log.error("❌ claim 결과 sessionId 파싱 실패. member={}", member, e);
            // 이상 값이면 assigning에서 제거
            redisTemplate.opsForZSet().remove(assigningZsetKey, member);
            return null;
        }
    }

    /**
     * ✅ 배정 성공 → assigning에서 제거
     */
    public void ackClaim(int sessionId) {
        redisTemplate.opsForZSet().remove(assigningZsetKey, String.valueOf(sessionId));
        log.info("✅ ackClaim - sessionId={}, assigningKey={}", sessionId, assigningZsetKey);
    }


    /**
     * ✅ 배정 실패/스킵 → assigning에서 제거하고 waiting으로 되돌림(원복)
     */
    public void releaseClaim(int sessionId, double score) {
        String member = String.valueOf(sessionId);
        redisTemplate.opsForZSet().remove(assigningZsetKey, member);
        redisTemplate.opsForZSet().add(waitingZsetKey, member, score);
        log.info("↩ releaseClaim - sessionId={}, score={}, assigningKey={}, waitingKey={}",
                sessionId, score, assigningZsetKey, waitingZsetKey);
    }

    /**
     * 종료/취소 시: waiting+assigning에서 모두 제거(안전)
     */
    public void removeEverywhere(int sessionId) {
        String member = String.valueOf(sessionId);
        redisTemplate.opsForZSet().remove(waitingZsetKey, member);
        redisTemplate.opsForZSet().remove(assigningZsetKey, member);
        log.info("🗑 removeEverywhere - sessionId={}", sessionId);
    }

    public record ClaimResult(int sessionId, double score) {}
}
