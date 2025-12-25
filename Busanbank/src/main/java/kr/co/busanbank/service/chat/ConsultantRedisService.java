package kr.co.busanbank.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultantRedisService {

    private final StringRedisTemplate redis;

    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String readyZset;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String loadZset;

    @Value("${chat.redis.consultant.statusPrefix:chat:consultant:status:}")
    private String statusPrefix;

    @Value("${chat.redis.consultant.lockPrefix:chat:consultant:lock:}")
    private String lockPrefix;

    public void setReady(int consultantId) {
        String id = String.valueOf(consultantId);
        redis.opsForZSet().add(readyZset, id, System.currentTimeMillis()); // tie-break 용
        redis.opsForValue().set(statusPrefix + id, "READY", Duration.ofHours(12));
        // load 없으면 0으로 초기화
        redis.opsForZSet().add(loadZset, id, getLoad(consultantId));
        log.info("✅ consultant READY. id={}", consultantId);
    }

    public void setBusy(int consultantId) {
        String id = String.valueOf(consultantId);
        redis.opsForZSet().remove(readyZset, id);
        redis.opsForValue().set(statusPrefix + id, "BUSY", Duration.ofHours(12));
        log.info("✅ consultant BUSY. id={}", consultantId);
    }

    public void setOffline(int consultantId) {
        String id = String.valueOf(consultantId);
        redis.opsForZSet().remove(readyZset, id);
        redis.opsForValue().set(statusPrefix + id, "OFFLINE", Duration.ofHours(12));
        log.info("✅ consultant OFFLINE. id={}", consultantId);
    }

    public long getLoad(int consultantId) {
        String id = String.valueOf(consultantId);
        Double score = redis.opsForZSet().score(loadZset, id);
        return score == null ? 0L : score.longValue();
    }

    public void incLoad(int consultantId) {
        String id = String.valueOf(consultantId);
        redis.opsForZSet().incrementScore(loadZset, id, 1.0);
    }

    public void decLoad(int consultantId) {
        String id = String.valueOf(consultantId);
        redis.opsForZSet().incrementScore(loadZset, id, -1.0);
    }

    public String lockKey(int consultantId) {
        return lockPrefix + consultantId;
    }
}
