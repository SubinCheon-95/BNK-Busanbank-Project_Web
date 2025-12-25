package kr.co.busanbank.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageStreamProducer {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Value("${chat.redis.stream.key:chat:stream:messages}")
    private String streamKey;

    /**
     * 기존 LIST enqueue 대신 Streams에 적재 (유실/중복 처리에 강함)
     */
    public RecordId enqueue(ChatMessageDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            Map<String, String> body = new HashMap<>();
            body.put("payload", json);
            body.put("sessionId", String.valueOf(dto.getSessionId())); // dto에 sessionId가 있어야 함

            MapRecord<String, String, String> record = MapRecord.create(streamKey, body);
            RecordId id = redis.opsForStream().add(record);

            log.info("✅ Stream enqueue success. id={}, sessionId={}", id, dto.getSessionId());
            return id;

        } catch (JsonProcessingException e) {
            log.error("❌ DTO -> JSON 변환 실패", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("❌ Stream enqueue 실패", e);
            throw new RuntimeException(e);
        }
    }
}
