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
@Configuration
@EnableScheduling
@RequiredArgsConstructor
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

    @PostConstruct
    public void ensureGroup() {
        try {
            // 그룹이 없으면 생성 (streamKey가 없으면 mkstream 옵션이 필요)
            redis.opsForStream().createGroup(streamKey, ReadOffset.from("0-0"), group);
            log.info("✅ Stream group created. streamKey={}, group={}", streamKey, group);
        } catch (RedisSystemException e) {
            // 이미 존재하면 BUSYGROUP 에러가 나올 수 있음 → 무시
            log.info("ℹ️ Stream group already exists or stream missing. {}", e.getMessage());
        } catch (Exception e) {
            log.warn("⚠️ ensureGroup failed. streamKey={}, group={}", streamKey, group, e);
        }
    }

    /**
     * Streams는 fixedDelay를 길게 두고 BLOCK으로 대기하는 게 정석
     */
    @Scheduled(fixedDelay = 200) // 0.2초마다 깨어서 poll (BLOCK이 있어서 실제 CPU 거의 안씀)
    public void consume() {
        try {
            // 새 메시지(>)만 읽음
            StreamReadOptions options = StreamReadOptions.empty()
                    .block(Duration.ofSeconds(2))
                    .count(50);

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

                String payload = String.valueOf(value.get("payload"));

                try {
                    ChatMessageDTO dto = objectMapper.readValue(payload, ChatMessageDTO.class);

                    // ✅ DB 저장
                    chatMessageService.saveFromQueue(dto);

                    // ✅ ACK
                    redis.opsForStream().acknowledge(streamKey, group, id);
                    log.debug("✅ Stream msg saved & ack. id={}, sessionId={}", id, dto.getSessionId());

                } catch (Exception ex) {
                    log.error("❌ Stream msg 처리 실패. id={}, payload={}", id, payload, ex);
                    // ACK 하지 않으면 PEL에 남음 (재처리 가능)
                    // 운영에서는 DLQ로 옮기는 로직을 추가해도 됨
                }
            }

        } catch (Exception e) {
            log.error("❌ Stream worker consume() error", e);
        }
    }
}
