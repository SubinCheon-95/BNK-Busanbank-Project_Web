package kr.co.busanbank.controller;

import jakarta.validation.Valid;
import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.service.UserProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** ***********************************************************
 * UserProductController 여기 이걸 다 할 필요는 없는데 일단 넣어봄
 ************************************************************* */
@RestController
@RequestMapping("/api/user-products")
@RequiredArgsConstructor
@Slf4j
public class UserProductController {

    private final UserProductService userProductService;

    /**
     * 상품 가입 등록
     * POST /api/user-products
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> registerProduct(@Valid @RequestBody UserProductDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = userProductService.registerProduct(dto);

            if (result) {
                response.put("success", true);
                response.put("message", "상품 가입이 완료되었습니다.");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("success", false);
                response.put("message", "상품 가입에 실패했습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("상품 가입 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 정보 조회 (단건)
     * GET /api/user-products/{userId}/{productNo}?startDate=2025-11-17
     */
    @GetMapping("/{userId}/{productNo}")
    public ResponseEntity<Map<String, Object>> getProduct(
            @PathVariable int userId,
            @PathVariable int productNo,
            @RequestParam String startDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserProductDTO product = userProductService.getProduct(userId, productNo, startDate);

            if (product != null) {
                response.put("success", true);
                response.put("data", product);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "상품을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("상품 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 전체 가입 상품 목록 조회
     * GET /api/user-products/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getProductList(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UserProductDTO> products = userProductService.getProductList(userId);
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 활성 상품만 조회
     * GET /api/user-products/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Map<String, Object>> getActiveProducts(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UserProductDTO> products = userProductService.getActiveProducts(userId);
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("활성 상품 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 정보 수정
     * PUT /api/user-products
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProduct(@Valid @RequestBody UserProductDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = userProductService.updateProduct(dto);

            if (result) {
                response.put("success", true);
                response.put("message", "상품 정보가 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "수정할 상품을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("상품 수정 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 해지
     * PATCH /api/user-products/{userId}/{productNo}/terminate?startDate=2025-11-17
     */
    @PatchMapping("/{userId}/{productNo}/terminate")
    public ResponseEntity<Map<String, Object>> terminateProduct(
            @PathVariable int userId,
            @PathVariable int productNo,
            @RequestParam String startDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = userProductService.terminateProduct(userId, productNo, startDate);

            if (result) {
                response.put("success", true);
                response.put("message", "상품이 해지되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "해지할 상품을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("상품 해지 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 삭제
     * DELETE /api/user-products/{userId}/{productNo}?startDate=2025-11-17
     */
    @DeleteMapping("/{userId}/{productNo}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @PathVariable int userId,
            @PathVariable int productNo,
            @RequestParam String startDate) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = userProductService.deleteProduct(userId, productNo, startDate);

            if (result) {
                response.put("success", true);
                response.put("message", "상품이 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "삭제할 상품을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 만기 예정 상품 조회
     * GET /api/user-products/user/{userId}/maturity?days=30
     */
    @GetMapping("/user/{userId}/maturity")
    public ResponseEntity<Map<String, Object>> getMaturityProducts(
            @PathVariable int userId,
            @RequestParam(defaultValue = "30") int days) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<UserProductDTO> products = userProductService.getMaturityProducts(userId, days);
            response.put("success", true);
            response.put("data", products);
            response.put("count", products.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("만기 예정 상품 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 중복 가입 체크
     * GET /api/user-products/check-duplicate?userId=1&productNo=100
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Object>> checkDuplicateProduct(
            @RequestParam int userId,
            @RequestParam int productNo) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = userProductService.isDuplicateProduct(userId, productNo);
            response.put("success", true);
            response.put("isDuplicate", isDuplicate);
            response.put("message", isDuplicate ? "이미 가입된 상품입니다." : "가입 가능한 상품입니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("중복 체크 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}