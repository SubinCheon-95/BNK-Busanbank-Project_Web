package kr.co.busanbank.call;

import java.util.EnumSet;
import java.util.Objects;

/**
 * 전화(Voice) 전용 상태 머신.
 * - 기존 채팅/세션 구조는 그대로 두고, session hash에 callStatus 필드로 얹는 용도
 * - 상태 전이를 엄격히 제한해서 중복 이벤트/재시도에도 안정적으로 동작하게 합니다.
 */
public enum CallStatus {
    NONE,
    CALL_WAITING,
    CALL_ASSIGNED,
    CALL_CONNECTED,
    CALL_ENDED;

    /** 안전 파싱 (null/이상값 -> NONE) */
    public static CallStatus from(String raw) {
        if (raw == null || raw.isBlank()) return NONE;
        try {
            return CallStatus.valueOf(raw.trim());
        } catch (Exception e) {
            return NONE;
        }
    }

    /** 단방향 전이만 허용 */
    public boolean canTransitTo(CallStatus next) {
        Objects.requireNonNull(next, "next");
        switch (this) {
            case NONE:
                return EnumSet.of(CALL_WAITING, CALL_ENDED).contains(next);
            case CALL_WAITING:
                return EnumSet.of(CALL_ASSIGNED, CALL_ENDED).contains(next);
            case CALL_ASSIGNED:
                return EnumSet.of(CALL_CONNECTED, CALL_WAITING, CALL_ENDED).contains(next); // 타임아웃 재큐잉 허용
            case CALL_CONNECTED:
                return EnumSet.of(CALL_ENDED).contains(next);
            case CALL_ENDED:
                return false;
            default:
                return false;
        }
    }

    /**
     * 상태 전이 검증 (불가하면 IllegalStateException)
     * - 서비스 계층에서 세션 상태 업데이트 직전에 호출 권장
     */
    public void assertTransitTo(CallStatus next) {
        if (!canTransitTo(next)) {
            throw new IllegalStateException("Invalid callStatus transition: " + this + " -> " + next);
        }
    }
}
