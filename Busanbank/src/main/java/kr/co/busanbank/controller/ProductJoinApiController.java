package kr.co.busanbank.controller;

import kr.co.busanbank.dto.ProductJoinRequestDTO;
import kr.co.busanbank.service.ProductJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/join")
@RequiredArgsConstructor
public class ProductJoinApiController {

    private final ProductJoinService productJoinService;

    /**
     * Flutter STEP4에서 보내는 가입 요청 처리
     *  - POST /api/join
     *  - Body: JSON → ProductJoinRequestDTO
     */
    @PostMapping
    public ResponseEntity<?> join(@RequestBody ProductJoinRequestDTO requestDTO) {

        log.info("📡 [API] 상품 가입 요청 수신");
        log.info("   userId: {}", requestDTO.getUserId());
        log.info("   productNo: {}", requestDTO.getProductNo());
        log.info("   principalAmount: {}", requestDTO.getPrincipalAmount());
        log.info("   usedPoints: {}", requestDTO.getUsedPoints());
        log.info("   pointBonusRate: {}", requestDTO.getPointBonusRate());
        log.info("   applyRate: {}", requestDTO.getApplyRate());

        try {
            boolean success = productJoinService.processJoin(requestDTO);

            if (success) {
                log.info("✅ [API] 상품 가입 처리 성공");
                // Flutter 쪽에서는 statusCode == 200 여부만 보면 됨
                return ResponseEntity.ok().build();
            } else {
                log.warn("❌ [API] 상품 가입 처리 실패 (service에서 false 반환)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("가입 처리에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("❌ [API] 가입 처리 중 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("가입 처리 중 서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
