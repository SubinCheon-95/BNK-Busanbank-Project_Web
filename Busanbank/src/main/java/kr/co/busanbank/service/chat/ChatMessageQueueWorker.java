package kr.co.busanbank.service.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.busanbank.dto.chat.ChatMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
/*
    이름 : 우지희
    날짜 :
    내용 : 큐를 소비하는 worker 서비스
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageQueueWorker {

    private static final String QUEUE_KEY = "chat:messageQueue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    /**
     * 일정 주기로 Redis 큐에서 메시지를 꺼내 DB에 저장
     */
    @Scheduled(fixedDelay = 50) // 0.05초마다 실행 (상황에 따라 조정 가능)
    public void consume() {
        try {
            while (true) {
                String json = redisTemplate.opsForList().leftPop(QUEUE_KEY);
                if (json == null) {
                    // 큐가 비었으면 루프 종료
                    break;
                }

                try {
                    // JSON → ChatMessageDTO 역직렬화
                    ChatMessageDTO dto = objectMapper.readValue(json, ChatMessageDTO.class);

                    // DB 저장 (아래에서 메서드 예시 설명)
                    chatMessageService.saveFromQueue(dto);

                    log.debug("✅ 큐 메시지 DB 저장 완료: {}", dto);

                } catch (Exception e) {
                    log.error("❌ 큐 메시지 처리 실패. json={}", json, e);
                    // 상황에 따라 실패한 json을 다른 리스트로 옮기거나 로그만 남기거나 정책 결정 가능
                }
            }
        } catch (Exception e) {
            log.error("❌ ChatMessageQueueConsumer consume() 실행 중 오류", e);
        }
    }
}
