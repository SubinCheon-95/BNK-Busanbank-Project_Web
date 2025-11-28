package kr.co.busanbank.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
/*
    이름 : 우지희
    날짜 :
    내용 : 메시지를 큐에 넣는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageQueueService {

    private static final String QUEUE_KEY = "chat:messageQueue";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public void enqueue(ChatMessageDTO chatMessageDTO) {
        log.info("➡️ Redis 큐 적재 시도: {}", chatMessageDTO);
        try {
            String json = objectMapper.writeValueAsString(chatMessageDTO);
            redis.opsForList().rightPush(QUEUE_KEY, json);
            log.info("✅ Redis 큐 적재 성공: {}", json);
        } catch (JsonProcessingException e) {
            log.error("❌ ChatMessageDTO → JSON 변환 실패", e);
        }catch (Exception e){
            log.error("❌ Redis 큐 적재 실패", e);
        }
    }
}
