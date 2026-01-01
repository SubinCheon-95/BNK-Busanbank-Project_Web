package kr.co.busanbank.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ChatMessageStreamWorker {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    @Value("${chat.redis.stream.key:chat:stream:messages}")
    private String streamKey;

    @Value("${chat.redis.stream.group:chat-msg-workers}")
    private String group;

    // 인스턴스별 consumer name (중복 방지)
    private final String consumer = "c-" + UUID.randomUUID();

    private static final String SEED_JSON = "{\"type\":\"__seed__\"}";

    // Polling 설정
    private static final Duration BLOCK = Duration.ofSeconds(2);
    private static final long SCHEDULE_DELAY_MS = 200L;
    private static final int COUNT = 50;

    public ChatMessageStreamWorker(StringRedisTemplate redis,
                                   ObjectMapper objectMapper,
                                   ChatMessageService chatMessageService) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.chatMessageService = chatMessageService;
    }

    @PostConstruct
    public void init() {
        ensureStreamAndGroup();
        log.info("✅ Stream worker ready. streamKey={}, group={}, consumer={}", streamKey, group, consumer);
    }

    /**
     * streamKey / consumer group 보장
     * - stream이 없으면 seed XADD로 키 생성
     * - group이 없으면 createGroup
     * - 이미 있으면 BUSYGROUP 무시
     *
     * ⚠️ 핵심: createGroup은 "stream이 존재"해야 하므로 seed 생성이 먼저여야 함
     */
    public void ensureStreamAndGroup() {
        try {
            // 1) stream 키 없으면 seed로 생성 (streamKey 생성용)
            if (Boolean.FALSE.equals(redis.hasKey(streamKey))) {
                redis.opsForStream().add(streamKey, Map.of("payload", SEED_JSON));
                log.info("✅ Stream key created by seed XADD. streamKey={}", streamKey);
            }

            // 2) group 생성 시도
            // - '$'부터 읽고 싶으면 ReadOffset.latest()
            // - 과거까지 전부 읽을거면 0-0
            // 현재 코드 의도(재기동 시 밀린 것까지 처리)를 유지하려면 0-0이 맞음
            redis.opsForStream().createGroup(streamKey, ReadOffset.from("0-0"), group);
            log.info("✅ Stream group created. streamKey={}, group={}", streamKey, group);

        } catch (Exception e) {
            if (isBusyGroup(e)) {
                log.info("ℹ️ Stream group already exists. streamKey={}, group={}", streamKey, group);
                return;
            }
            log.error("❌ ensureStreamAndGroup failed. streamKey={}, group={}", streamKey, group, e);
        }
    }

    private boolean isBusyGroup(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = String.valueOf(cur.getMessage());
            String name = cur.getClass().getName();
            if (msg.contains("BUSYGROUP") || name.contains("RedisBusyException")) return true;
            cur = cur.getCause();
        }
        return false;
    }

    private boolean isNoGroup(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = String.valueOf(cur.getMessage());
            if (msg.contains("NOGROUP")) return true;
            cur = cur.getCause();
        }
        return false;
    }

    @Scheduled(fixedDelay = SCHEDULE_DELAY_MS)
    public void consume() {
        try {
            StreamReadOptions options = StreamReadOptions.empty()
                    .block(BLOCK)
                    .count(COUNT);

            List<MapRecord<String, Object, Object>> records =
                    redis.opsForStream().read(
                            Consumer.from(group, consumer),
                            options,
                            StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                    );

            if (records == null || records.isEmpty()) return;

            for (MapRecord<String, Object, Object> r : records) {
                RecordId id = r.getId();
                Map<Object, Object> value = r.getValue();
                String payload = (value == null) ? null : (String.valueOf(value.get("payload")));

                // seed/blank 방어
                if (payload == null || payload.isBlank() || payload.contains("\"type\":\"__seed__\"") || "__seed__".equals(payload)) {
                    redis.opsForStream().acknowledge(streamKey, group, id);
                    continue;
                }

                try {
                    ChatMessageDTO dto = objectMapper.readValue(payload, ChatMessageDTO.class);

                    // ✅ 기존 메시지 저장 로직 호출 (그대로 유지)
                    chatMessageService.saveFromQueue(dto);

                    // ACK
                    redis.opsForStream().acknowledge(streamKey, group, id);

                } catch (Exception ex) {
                    // ACK 안 하면 PEL에 남음(재처리 가능)
                    log.error("❌ Stream msg 처리 실패. id={}, payload={}", id, payload, ex);
                }
            }

        } catch (Exception e) {
            // ✅ 지금 겪는 문제: stream/group이 없을 때 무한 에러 도배
            if (isNoGroup(e)) {
                // NOGROUP은 error가 아니라 복구 이벤트로 취급 (로그 최소화)
                log.warn("⚠️ NOGROUP detected. Recreating stream/group... streamKey={}, group={}", streamKey, group);
                ensureStreamAndGroup();
                return;
            }

            log.error("❌ Stream worker consume() error", e);
        }
    }
}
