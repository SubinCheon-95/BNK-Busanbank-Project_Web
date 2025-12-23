package kr.co.busanbank.service.chat;

import kr.co.busanbank.dto.chat.ChatSessionDTO;
import kr.co.busanbank.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAssignmentService {
    private final ChatWaitingQueueService chatWaitingQueueService;
    private final ChatSessionMapper chatSessionMapper;
    private final ConsultantRedisService consultantRedisService; // ready/load/status 관리
    private final ChatSessionService chatSessionService;

    /**
     * ✅ 유실 방지 + "지금 로그인한 상담원(consultantId)"에게 다음 세션 배정
     * - waiting -> assigning claim
     * - DB 조건부 업데이트 성공 시 ack
     * - 실패 시 release(원복)
     */
    @Transactional
    public ChatSessionDTO assignNextToConsultant(int consultantId) {

        while (true) {
            // 1) waiting -> assigning claim(원자 이동)
            ChatWaitingQueueService.ClaimResult claim = chatWaitingQueueService.claimNext();
            if (claim == null) {
                log.info("ℹ️ 대기 세션 없음");
                return null;
            }

            int sessionId = claim.sessionId();
            double score = claim.score();

            try {

                ChatSessionDTO session = chatSessionMapper.selectChatSessionById(sessionId);

                if (session == null || !"WAITING".equals(session.getStatus())) {
                    log.info("⏭ claim skip - sessionId={}, status={}", sessionId,
                            session == null ? null : session.getStatus());
                    chatWaitingQueueService.ackClaim(sessionId);
                    continue;
                }

            // 3) DB 배정 (조건부 업데이트: status='WAITING'인 경우만 성공)
            int updated = chatSessionService.assignConsultantFromWaiting(sessionId, consultantId);

            if (updated <= 0) {
                log.warn("⚠ DB assign 실패. releaseClaim - sessionId={}, consultantId={}", sessionId, consultantId);
                chatWaitingQueueService.releaseClaim(sessionId, score);
                continue;
            }

            // 4) 성공 -> ack(확정)
            chatWaitingQueueService.ackClaim(sessionId);
            // ✅ 상담원 상태/로드 반영
            consultantRedisService.incLoad(consultantId);
            consultantRedisService.setBusy(consultantId);

            session.setConsultantId(consultantId);
            session.setStatus("CHATTING");

            log.info("✅ 배정 완료(수동) - consultantId={}, sessionId={}", consultantId, sessionId);
            return session;
        }catch (Exception e) {
                // ✅ 예외 시 유실 방지 원복
                chatWaitingQueueService.releaseClaim(sessionId, score);
                throw e;

            }
        }
    }
}
