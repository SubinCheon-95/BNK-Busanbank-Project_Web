package kr.co.busanbank.service.chat;

import kr.co.busanbank.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionScheduler {

    private final ChatSessionMapper chatSessionMapper;

    private static final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 5분마다 대기/진행 세션 상태 정리
     */
    @Scheduled(cron = "0 */5 * * * *")   // 매 5분 0초
    public void cleanupInactiveSessions() {
        String now = LocalDateTime.now().format(dtf);

        // 1) 오래된 WAITING 세션 취소 (예: 10분 경과)
        int cancelled = chatSessionMapper.autoCancelOldWaitingSessions(10);
        // 2) 오래된 CHATTING 세션 종료 (예: 30분 경과)
        int closed    = chatSessionMapper.autoCloseOldChattingSessions(30);

        if (cancelled > 0 || closed > 0) {
            log.info("🧹 세션 정리 완료 - cancelled={}, closed={}", cancelled, closed);
        }
    }
}
