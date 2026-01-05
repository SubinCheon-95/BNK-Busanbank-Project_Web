package kr.co.busanbank.call.service;

import kr.co.busanbank.call.CallStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class CallEndService {

    private final StringRedisTemplate redis;
    private final CallQueueKeys keys;

    // ✅ 추가: 상담사 WS 브로드캐스트(VOICE_ENDED)
    private final CallWsAssignNotifier agentWsNotifier;

    // ✅ 추가: 고객 WS push(VOICE_ENDED)
    private final CallCustomerWsNotifier customerWsNotifier;

    @Value("${chat.redis.consultant.readyZset:chat:consultant:ready}")
    private String consultantReadyZset;

    @Value("${chat.redis.consultant.loadZset:chat:consultant:load}")
    private String consultantLoadZset;

    @Value("${chat.redis.consultant.statusPrefix:chat:consultant:status:}")
    private String consultantStatusPrefix;

    @Value("${chat.call.voice.waitingZset:call:voice:waiting}")
    private String voiceWaitingZset;

    // ✅ 음성통화 active 키 prefix (AGENT_BUSY 원인)
    @Value("${chat.call.voice.activePrefix:call:voice:active:}")
    private String voiceActivePrefix; // call:voice:active:{sessionId}

    @Value("${chat.call.voice.agentActivePrefix:call:voice:agent:active:}")
    private String voiceAgentActivePrefix; // call:voice:agent:active:{agentId}


    public CallEndService(StringRedisTemplate redis,
                          CallQueueKeys keys,
                          CallWsAssignNotifier agentWsNotifier,
                          CallCustomerWsNotifier customerWsNotifier) {
        this.redis = redis;
        this.keys = keys;
        this.agentWsNotifier = agentWsNotifier;
        this.customerWsNotifier = customerWsNotifier;
    }

    /**
     * 통화 종료 (고객/상담사 공용)  ✅ “정답 종료 로직”
     * - AGENT_BUSY 재발 방지: voice active 키 2개를 반드시 DEL
     * - 상담사WS/고객WS 모두 VOICE_ENDED 송신
     */
    public void end(String sessionId, String endedBy, String reason) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }

        sessionId = sessionId.trim();
        String sKey = keys.sessionKey(sessionId);
        long now = Instant.now().toEpochMilli();

        // ✅ 세션 해시에서 상담사 id 확보 (세션 해시가 없더라도 voice active 키는 지워야 함)
        String agentId = null;
        try {
            agentId = (String) redis.opsForHash().get(sKey, "callAgentId");
        } catch (Exception ignore) {
        }

        // 1) 세션 해시 상태 종료로 마킹 (있을 때만)
        try {
            Boolean exists = redis.hasKey(sKey);
            if (Boolean.TRUE.equals(exists)) {
                CallStatus cur = CallStatus.from((String) redis.opsForHash().get(sKey, "callStatus"));

                if (cur != CallStatus.CALL_ENDED && cur != CallStatus.NONE) {
                    redis.opsForHash().putAll(sKey, Map.of(
                            "callStatus", CallStatus.CALL_ENDED.name(),
                            "callEndedAt", String.valueOf(now),
                            "callEndReason", reason == null ? "" : reason,
                            "callEndedBy", endedBy == null ? "" : endedBy
                    ));
                }

                // ✅ 종료 후 세션 해시는 5분 정도만 보관
                redis.expire(sKey, java.time.Duration.ofMinutes(5));
            }
        } catch (Exception e) {
            log.warn("⚠️ end: session hash update failed. sKey={}", sKey, e);
        }

        // 2) 큐/감시 정리
        cleanupQueues(sessionId);

        // 3) ✅ AGENT_BUSY 재발 방지: voice active 키 2개 “무조건 삭제”
        try {
            redis.delete(voiceActivePrefix + sessionId); // call:voice:active:{sid}
            if (agentId != null && !agentId.isBlank()) {
                redis.delete(voiceAgentActivePrefix + agentId); // call:voice:agent:active:{agentId}
            }
        } catch (Exception e) {
            log.warn("⚠️ end: voice active keys delete failed. sid={}, agentId={}", sessionId, agentId, e);
        }

        // 4) 상담사 복귀 처리(READY 복귀)
        if (agentId != null && !agentId.isBlank()) {
            try {
                String statusKey = consultantStatusPrefix + agentId;
                String curStatus = redis.opsForValue().get(statusKey);

                if (!"READY".equals(curStatus)) {
                    redis.opsForValue().set(statusKey, "READY");

                    Double curLoad = redis.opsForZSet().score(consultantLoadZset, agentId);
                    if (curLoad != null && curLoad > 0) {
                        redis.opsForZSet().incrementScore(consultantLoadZset, agentId, -1);
                    }

                    redis.opsForZSet().add(consultantReadyZset, agentId, 0.0);
                }
            } catch (Exception e) {
                log.warn("⚠️ end: consultant restore failed. agentId={}", agentId, e);
            }
        }

        // 5) ✅ 실시간 알림(상담사 WS + 고객 WS)
        try {
            // 상담사 전체에게 종료 알림(리스트 갱신 등)
            agentWsNotifier.notifyEnded(sessionId, endedBy == null ? "" : endedBy);
        } catch (Exception e) {
            log.warn("⚠️ end: agent ws notify failed. sid={}", sessionId, e);
        }

        try {
            // 고객에게 종료 알림(Flutter 화면 즉시 종료 처리용)
            customerWsNotifier.notifyEnded(sessionId, endedBy == null ? "" : endedBy);
        } catch (Exception e) {
            log.warn("⚠️ end: customer ws notify failed. sid={}", sessionId, e);
        }

        log.info("✅ call ended. sessionId={}, agentId={}, endedBy={}, reason={}",
                sessionId, agentId, endedBy, reason);
    }

    private void cleanupQueues(String sessionId) {
        try {
            redis.opsForZSet().remove(keys.assignedWatchZset(), sessionId);
            redis.opsForZSet().remove(keys.callQueue("default"), sessionId);
            redis.opsForZSet().remove(voiceWaitingZset, sessionId);
        } catch (Exception e) {
            log.warn("cleanupQueues failed. sessionId={}", sessionId, e);
        }
    }
}