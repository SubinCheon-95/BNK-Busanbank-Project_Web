package kr.co.busanbank.service.chat;

import kr.co.busanbank.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionScheduler {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatWaitingQueueService chatWaitingQueueService;

    private static final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 5분마다 대기/진행 세션 상태 정리
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanupInactiveSessions() {

        // 1) 오래된 WAITING 세션 ID 목록 조회 (예: 10분 경과)
        List<Integer> cancelledSessionIds =
                chatSessionMapper.findOldWaitingSessionIds(10);

        int cancelled = 0;
        for (Integer sid : cancelledSessionIds) {
            if (sid == null) continue;

            // 상태를 CANCELLED 로 변경
            cancelled += chatSessionMapper.closeChatSession(sid, "CANCELLED");

            // Redis ZSet에서도 제거
            chatWaitingQueueService.remove(sid);
        }

        // 2) 오래된 CHATTING 세션 CLOSED 처리 (예: 30분 경과)
        int closed = chatSessionMapper.autoCloseOldChattingSessions(30);

        if (cancelled > 0 || closed > 0) {
            log.info("🧹 세션 정리 완료 - cancelled={}, closed={}", cancelled, closed);
        }
    }
}
