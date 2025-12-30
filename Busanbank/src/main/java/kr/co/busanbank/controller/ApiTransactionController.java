/*
    날짜 : 2025/12/29
    이름 : 진원
    내용 : Flutter 앱용 거래 API Controller - 계좌이체 및 거래내역 조회
 */
package kr.co.busanbank.controller;

import kr.co.busanbank.dto.TransactionHistoryDTO;
import kr.co.busanbank.dto.AccountDTO;
import kr.co.busanbank.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transaction")
public class ApiTransactionController {

    private final TransactionService transactionService;

    /**
     * 계좌간 이체 API (Flutter 앱용)
     * POST /api/transaction/transfer
     * 2025/12/29 - 작성자: 진원
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> transferRequest) {
        try {
            int userId = Integer.parseInt(transferRequest.get("userId").toString());
            String fromAccountNo = transferRequest.get("fromAccountNo").toString();
            String toAccountNo = transferRequest.get("toAccountNo").toString();
            Long amount = Long.parseLong(transferRequest.get("amount").toString());
            String description = transferRequest.getOrDefault("description", "계좌이체").toString();

            log.info("📱 [Flutter] 계좌이체 요청 - userId: {}, from: {}, to: {}, amount: {}",
                    userId, fromAccountNo, toAccountNo, amount);

            // 이체 처리
            transactionService.transferBetweenAccounts(userId, fromAccountNo, toAccountNo, amount, description);

            // 이체 후 잔액 조회
            Long balanceAfter = transactionService.getAccountBalance(fromAccountNo);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "이체가 완료되었습니다.");
            result.put("balanceAfter", balanceAfter);

            log.info("✅ [Flutter] 계좌이체 완료 - userId: {}, 거래후 잔액: {}", userId, balanceAfter);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("❌ [Flutter] 계좌이체 실패 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ [Flutter] 계좌이체 처리 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 사용자 거래내역 조회 API (Flutter 앱용)
     * GET /api/transaction/history/{userId}
     * 2025/12/29 - 작성자: 진원
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getTransactionHistory(@PathVariable int userId) {
        try {
            log.info("📱 [Flutter] 거래내역 조회 요청 - userId: {}", userId);

            List<TransactionHistoryDTO> transactions = transactionService.getUserTransactionHistory(userId);

            log.info("✅ [Flutter] 거래내역 조회 완료 - userId: {}, 거래내역 수: {}", userId, transactions.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactions", transactions
            ));

        } catch (Exception e) {
            log.error("❌ [Flutter] 거래내역 조회 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 계좌별 거래내역 조회 API (Flutter 앱용)
     * GET /api/transaction/account/{accountNo}
     * 2025/12/29 - 작성자: 진원
     */
    @GetMapping("/account/{accountNo}")
    public ResponseEntity<?> getAccountTransactionHistory(@PathVariable String accountNo) {
        try {
            log.info("📱 [Flutter] 계좌별 거래내역 조회 요청 - accountNo: {}", accountNo);

            List<TransactionHistoryDTO> transactions = transactionService.getAccountTransactionHistory(accountNo);

            log.info("✅ [Flutter] 계좌별 거래내역 조회 완료 - accountNo: {}, 거래내역 수: {}",
                    accountNo, transactions.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactions", transactions
            ));

        } catch (Exception e) {
            log.error("❌ [Flutter] 계좌별 거래내역 조회 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 계좌 잔액 조회 API (Flutter 앱용)
     * GET /api/transaction/balance/{accountNo}
     * 2025/12/29 - 작성자: 진원
     */
    @GetMapping("/balance/{accountNo}")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNo) {
        try {
            log.info("📱 [Flutter] 계좌 잔액 조회 요청 - accountNo: {}", accountNo);

            Long balance = transactionService.getAccountBalance(accountNo);

            log.info("✅ [Flutter] 계좌 잔액 조회 완료 - accountNo: {}, balance: {}", accountNo, balance);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "balance", balance
            ));

        } catch (Exception e) {
            log.error("❌ [Flutter] 계좌 잔액 조회 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 거래내역 상세 조회 API (Flutter 앱용)
     * GET /api/transaction/detail/{transactionId}
     * 2025/12/29 - 작성자: 진원
     */
    @GetMapping("/detail/{transactionId}")
    public ResponseEntity<?> getTransactionDetail(@PathVariable Long transactionId) {
        try {
            log.info("📱 [Flutter] 거래내역 상세 조회 요청 - transactionId: {}", transactionId);

            TransactionHistoryDTO transaction = transactionService.getTransactionDetail(transactionId);

            if (transaction == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "error", "거래내역을 찾을 수 없습니다."
                ));
            }

            log.info("✅ [Flutter] 거래내역 상세 조회 완료 - transactionId: {}", transactionId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transaction", transaction
            ));

        } catch (Exception e) {
            log.error("❌ [Flutter] 거래내역 상세 조회 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 사용자 계좌 목록 조회 API (Flutter 앱용)
     * GET /api/transaction/accounts/{userId}
     * 2025/12/29 - 작성자: 진원
     */
    @GetMapping("/accounts/{userId}")
    public ResponseEntity<?> getUserAccounts(@PathVariable int userId) {
        try {
            log.info("📱 [Flutter] 사용자 계좌 목록 조회 요청 - userId: {}", userId);

            List<AccountDTO> accounts = transactionService.getUserAccounts(userId);

            log.info("✅ [Flutter] 계좌 목록 조회 완료 - userId: {}, 계좌 수: {}", userId, accounts.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accounts", accounts
            ));

        } catch (Exception e) {
            log.error("❌ [Flutter] 계좌 목록 조회 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "서버 오류가 발생했습니다."
            ));
        }
    }
}
