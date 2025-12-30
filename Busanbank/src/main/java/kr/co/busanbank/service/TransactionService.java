/*
    날짜 : 2025/12/29
    이름 : 진원
    내용 : 거래 서비스 생성 - Flutter 앱 연동용 (계좌이체, 거래내역 조회)
 */
package kr.co.busanbank.service;

import kr.co.busanbank.dto.TransactionHistoryDTO;
import kr.co.busanbank.dto.AccountDTO;
import kr.co.busanbank.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {

    private final TransactionMapper transactionMapper;

    /**
     * 계좌간 이체 (Flutter 앱용)
     * 2025/12/29 - 작성자: 진원
     */
    @Transactional
    public void transferBetweenAccounts(
            int userId,
            String fromAccountNo,
            String toAccountNo,
            Long amount,
            String description
    ) {
        log.info("계좌간 이체 시작 - 사용자: {}, 출금계좌: {}, 입금계좌: {}, 금액: {}",
                userId, fromAccountNo, toAccountNo, amount);

        // 1. 계좌 소유자 확인
        int ownerCheck = transactionMapper.verifyAccountOwner(fromAccountNo, userId);
        if (ownerCheck == 0) {
            log.error("계좌 소유자 불일치 - userId: {}, accountNo: {}", userId, fromAccountNo);
            throw new IllegalArgumentException("본인 계좌가 아닙니다.");
        }

        // 2. 출금 계좌 잔액 확인
        Long fromBalance = transactionMapper.selectAccountBalance(fromAccountNo);
        if (fromBalance == null || fromBalance < amount) {
            log.error("잔액 부족 - 현재잔액: {}, 이체금액: {}", fromBalance, amount);
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        // 3. 출금 계좌에서 차감
        int withdrawResult = transactionMapper.updateAccountBalanceWithdraw(fromAccountNo, amount);
        if (withdrawResult == 0) {
            log.error("출금 실패 - accountNo: {}, amount: {}", fromAccountNo, amount);
            throw new RuntimeException("출금 처리에 실패했습니다.");
        }

        // 4. 입금 계좌에 추가
        int depositResult = transactionMapper.updateAccountBalanceDeposit(toAccountNo, amount);
        if (depositResult == 0) {
            log.error("입금 실패 - accountNo: {}, amount: {}", toAccountNo, amount);
            throw new RuntimeException("입금 처리에 실패했습니다.");
        }

        // 5. 출금 거래 내역 저장
        Long balanceAfterWithdraw = transactionMapper.selectAccountBalance(fromAccountNo);
        TransactionHistoryDTO withdrawTransaction = TransactionHistoryDTO.builder()
                .fromAccountNo(fromAccountNo)
                .toAccountNo(toAccountNo)
                .amount(amount)
                .balanceAfter(balanceAfterWithdraw)
                .transactionType("TRANSFER")
                .description(description != null ? description : "계좌이체")
                .userId(userId)
                .build();
        transactionMapper.insertTransaction(withdrawTransaction);

        // 6. 입금 거래 내역 저장 (입금받는 사람 계좌 기준)
        Long balanceAfterDeposit = transactionMapper.selectAccountBalance(toAccountNo);
        Integer toAccountOwnerId = transactionMapper.selectAccountOwnerId(toAccountNo);

        // 입금 계좌의 소유자 ID를 사용하여 거래 내역 저장
        TransactionHistoryDTO depositTransaction = TransactionHistoryDTO.builder()
                .fromAccountNo(fromAccountNo)
                .toAccountNo(toAccountNo)
                .amount(amount)
                .balanceAfter(balanceAfterDeposit)
                .transactionType("TRANSFER")
                .description(description != null ? description : "계좌이체 입금")
                .userId(toAccountOwnerId != null ? toAccountOwnerId : userId)
                .build();
        transactionMapper.insertTransaction(depositTransaction);

        log.info("계좌간 이체 완료 - 거래 후 잔액(출금계좌): {}, 거래 후 잔액(입금계좌): {}",
                balanceAfterWithdraw, balanceAfterDeposit);
    }

    /**
     * 사용자 거래내역 조회 (Flutter 앱용)
     * 2025/12/29 - 작성자: 진원
     */
    public List<TransactionHistoryDTO> getUserTransactionHistory(int userId) {
        log.info("사용자 거래내역 조회 - userId: {}", userId);
        return transactionMapper.selectTransactionsByUserId(userId);
    }

    /**
     * 계좌별 거래내역 조회 (Flutter 앱용)
     * 2025/12/29 - 작성자: 진원
     */
    public List<TransactionHistoryDTO> getAccountTransactionHistory(String accountNo) {
        log.info("계좌별 거래내역 조회 - accountNo: {}", accountNo);
        return transactionMapper.selectTransactionsByAccountNo(accountNo);
    }

    /**
     * 특정 거래내역 상세 조회
     * 2025/12/29 - 작성자: 진원
     */
    public TransactionHistoryDTO getTransactionDetail(Long transactionId) {
        log.info("거래내역 상세 조회 - transactionId: {}", transactionId);
        return transactionMapper.selectTransactionById(transactionId);
    }

    /**
     * 계좌 잔액 조회
     * 2025/12/29 - 작성자: 진원
     */
    public Long getAccountBalance(String accountNo) {
        log.info("계좌 잔액 조회 - accountNo: {}", accountNo);
        return transactionMapper.selectAccountBalance(accountNo);
    }

    /**
     * 사용자 계좌 목록 조회 (2025/12/29 - 작성자: 진원)
     */
    public List<AccountDTO> getUserAccounts(int userId) {
        log.info("사용자 계좌 목록 조회 - userId: {}", userId);
        return transactionMapper.selectUserAccounts(userId);
    }
}
