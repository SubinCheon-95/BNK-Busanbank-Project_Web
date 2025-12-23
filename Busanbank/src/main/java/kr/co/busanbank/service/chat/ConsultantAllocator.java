package kr.co.busanbank.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultantAllocator {
    private final StringRedisTemplate redis;

    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String readyZset;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String loadZset;

    @Value("${chat.redis.consultant.lockPrefix:chat:consultant:lock:}")
    private String lockPrefix;

    private final DefaultRedisScript<String> pickConsultantScript = new DefaultRedisScript<>("""
            -- KEYS[1] = readyZset
            -- KEYS[2] = loadZset
            -- ARGV[1] = lockPrefix
            -- ARGV[2] = lockTtlMs
            -- ARGV[3] = candidateCount

            local readyKey = KEYS[1]
            local loadKey  = KEYS[2]
            local lockPrefix = ARGV[1]
            local lockTtl = tonumber(ARGV[2])
            local n = tonumber(ARGV[3])

            -- 후보 n명 (readyZset의 앞쪽) 가져오기
            local candidates = redis.call('ZRANGE', readyKey, 0, n - 1)
            if (#candidates == 0) then
              return nil
            end

            local bestId = nil
            local bestLoad = nil

            -- 후보들 중 load 최저 찾기
            for i=1,#candidates do
              local id = candidates[i]
              local load = redis.call('ZSCORE', loadKey, id)
              if (load == false or load == nil) then
                load = 0
              else
                load = tonumber(load)
              end

              if (bestLoad == nil or load < bestLoad) then
                bestLoad = load
                bestId = id
              end
            end

            -- bestId에 lock 걸기 (경쟁 방지)
            -- lock 실패 시, 후보들을 load 오름차순으로 재시도(간단히 한 번 더 순회)
            local function tryLock(id)
              local lockKey = lockPrefix .. id
              local ok = redis.call('SET', lockKey, '1', 'NX', 'PX', lockTtl)
              if ok then
                return id
              end
              return nil
            end

            local locked = tryLock(bestId)
            if locked then
              return locked
            end

            -- best lock 실패 시 나머지 후보도 순회하며 lock 가능한 사람 반환
            for i=1,#candidates do
              local id = candidates[i]
              if id ~= bestId then
                local locked2 = tryLock(id)
                if locked2 then
                  return locked2
                end
              end
            end

            return nil
            """, String.class);

    /**
     * READY 상담원 1명 선택 (원자)
     */
    public Integer pickReadyConsultant(int candidateCount, Duration lockTtl) {
        String id = redis.execute(
                pickConsultantScript,
                List.of(readyZset, loadZset),
                lockPrefix,
                String.valueOf(lockTtl.toMillis()),
                String.valueOf(candidateCount)
        );

        if (id == null) return null;
        try {
            return Integer.valueOf(id);
        } catch (NumberFormatException e) {
            log.error("❌ invalid consultant id from lua: {}", id, e);
            return null;
        }
    }

    public void releaseLock(int consultantId) {
        redis.delete(lockPrefix + consultantId);
    }
}
