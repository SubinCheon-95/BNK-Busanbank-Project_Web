package kr.co.busanbank.service;

import kr.co.busanbank.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryCleanupScheduler {

    private final ChatSessionMapper chatSessionMapper;

    /**
     * 매일 새벽 3시에, 90일 지난 종료 세션 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void deleteOldClosedSessions() {
        int days = 90;
        int deleted = chatSessionMapper.deleteOldClosedSessions(days);
        if (deleted > 0) {
            log.info("🗑 오래된 종료 세션 삭제 - {} rows ({}일 초과)", deleted, days);
        }
    }
}
