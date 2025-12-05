/*
    날짜 : 2025/11/24
    이름 : 오서정
    내용 : 마이페이지 mapper 수정 작성
 */
package kr.co.busanbank.mapper;


import kr.co.busanbank.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyMapper {

    int countUserItems(@Param("userId") String userId);

    String getProductRecentlyDate(@Param("userId") String userId);

    String getProductLastDate(@Param("userId") String userId);

    int  updateInfo(@Param("userId") String userId, @Param("email") String email, @Param("hp") String hp, @Param("zip") String zip, @Param("addr1") String addr1, @Param("addr2") String addr2);

    UsersDTO getUserById(@Param("userId") String userId);

    String getUserPwById(@Param("userId") String userId);

    String getUserAccountPwById(@Param("userId") String userId);


    void updatePw(@Param("userId") String userId, @Param("userPw") String userPw);

    void deleteUser(@Param("userId") String userId);

    List<UserProductDTO> getUserProducts(@Param("userId") String userId);

    List<UserProductDTO> getUserProductNames(@Param("userId") String userId);

    void deleteProduct(@Param("userId") String userId, @Param("productNo") String productNo);

    UserProductDTO getCancelProduct(@Param("userId") String userId, @Param("productNo") String productNo);

    int getUserNo(@Param("userId") String userId);

    List<EmailCounselDTO> getEmailList(@Param("userNo") int userNo);

    List<UserAccountDTO> getUserAccountList(@Param("userNo") int userNo);

    int getUserBalance(@Param("userNo") int userNo);

    CancelProductDTO getCancelProductData(@Param("userNo") int userNo, @Param("productNo") int productNo);

    List<UserAccountDTO> getUserDepositAccounts(@Param("userNo") int userNo);

    void depositToAccount(@Param("accountNo") String accountNo, @Param("amount") int amount);

    void terminateProduct(String userId, int productNo);

    void updateAccountStatusToN(String accountNo);

    void updateUserStatusToW(String userId);

    void updateUsersToD();

    int findTotalUsedPoints(int userId);

    void updateBalanceToZero(String accountNo);
}
