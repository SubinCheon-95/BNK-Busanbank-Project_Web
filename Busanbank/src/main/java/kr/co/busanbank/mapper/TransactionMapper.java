/*
    날짜 : 2025/12/29
    이름 : 진원
    내용 : 거래 내역 Mapper 생성 - Flutter 앱 연동용
 */
package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.TransactionHistoryDTO;
import kr.co.busanbank.dto.AccountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {

    /**
     * 거래 내역 저장
     */
    void insertTransaction(TransactionHistoryDTO transaction);

    /**
     * 계좌별 거래 내역 조회
     */
    List<TransactionHistoryDTO> selectTransactionsByAccountNo(@Param("accountNo") String accountNo);

    /**
     * 사용자별 모든 거래 내역 조회 (userNo 기준)
     */
    List<TransactionHistoryDTO> selectTransactionsByUserId(@Param("userId") int userId);

    /**
     * 특정 거래 내역 조회
     */
    TransactionHistoryDTO selectTransactionById(@Param("transactionId") Long transactionId);

    /**
     * 계좌 잔액 업데이트 (출금)
     */
    int updateAccountBalanceWithdraw(@Param("accountNo") String accountNo, @Param("amount") Long amount);

    /**
     * 계좌 잔액 업데이트 (입금)
     */
    int updateAccountBalanceDeposit(@Param("accountNo") String accountNo, @Param("amount") Long amount);

    /**
     * 계좌 잔액 조회
     */
    Long selectAccountBalance(@Param("accountNo") String accountNo);

    /**
     * 계좌 소유자 확인
     */
    int verifyAccountOwner(@Param("accountNo") String accountNo, @Param("userId") int userId);

    /**
     * 사용자 계좌 목록 조회 (2025/12/29 - 작성자: 진원)
     */
    List<AccountDTO> selectUserAccounts(@Param("userId") int userId);

    /**
     * 계좌 소유자 ID 조회 (2025/12/30 - 작성자: 진원)
     */
    Integer selectAccountOwnerId(@Param("accountNo") String accountNo);
}
