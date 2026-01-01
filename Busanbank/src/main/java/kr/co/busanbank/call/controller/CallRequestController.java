package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.service.CallRequestService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/call")
public class CallRequestController {

    private final CallRequestService callRequestService;

    public CallRequestController(CallRequestService callRequestService) {
        this.callRequestService = callRequestService;
    }

    /**
     * 고객 전화 요청
     * 예) POST /api/call/{sessionId}/request
     */
    @PostMapping("/{sessionId}/request")
    public ResponseEntity<?> requestCall(@PathVariable String sessionId,
                                         @RequestBody(required = false) CallRequestBody body,
                                         Authentication authentication) {

        // 고객 식별이 필요하면 authentication에서 꺼내 기록만 해두세요(필수 아님)
        // String customerId = authentication != null ? authentication.getName() : "";

        // ✅ 초기 안정화: inquiryType은 무시하고 default로 enqueue 하려면
        // CallRequestService 쪽에서 qKey를 keys.callQueue("default")로 고정해둔 상태여야 합니다.
        String inquiryType = (body == null) ? null : body.getInquiryType();

        callRequestService.enqueueCall(sessionId, inquiryType);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "sessionId", sessionId,
                "status", "CALL_WAITING"
        ));
    }

    @Data
    public static class CallRequestBody {
        /**
         * 선택: 예금/대출 등 (초기에는 null로 보내도 됨)
         */
        private String inquiryType;
    }
}
