/*
    날짜 : 2025/11/24
    이름 : 오서정
    내용 : 마이페이지 서비스 수정 작성
 */

package kr.co.busanbank.service;


import kr.co.busanbank.dto.*;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.mapper.MyMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyService {
    private final MyMapper myMapper;
    private final PasswordEncoder passwordEncoder;

    public int countUserItems(String userId) {
        return myMapper.countUserItems(userId);
    }

    public String findProductRecentlyDate(String userId) {
        return myMapper.getProductRecentlyDate(userId);
    }

    public String findProductLastDate(String userId) {
        return myMapper.getProductLastDate(userId);
    }





    public boolean modifyInfo(String userId, String email, String hp, String zip, String addr1, String addr2) throws Exception {
        String encryptedEmail = AESUtil.encrypt(email);
        String encryptedHp = AESUtil.encrypt(hp);

        int updatedRows = myMapper.updateInfo(userId, encryptedEmail, encryptedHp, zip, addr1, addr2);
        return updatedRows > 0;

    }

    public UsersDTO getUserById(String userId) {
        return myMapper.getUserById(userId);
    }


    public Boolean findUserPw(String userId, String pw) {
        // DB에서 암호화된 비밀번호 가져오기
        String dbEncodedPw = myMapper.getUserPwById(userId);
        // passwordEncoder.matches(raw, encoded) 사용
        return passwordEncoder.matches(pw, dbEncodedPw);
    }

    public void withdrawUser(String userId) {
        myMapper.deleteUser(userId);
    }

    public void modifyPw(String userId, String userPw){
        String encodedPass = passwordEncoder.encode(userPw);
        myMapper.updatePw(userId, encodedPass);
    }

    public List<UserProductDTO> findUserProducts(String userId) {
        return myMapper.getUserProducts(userId);
    }

    public List<UserProductDTO> findUserProductNames(String userId) {
        return myMapper.getUserProductNames(userId);
    }

    public void removeProduct(String userId, String productNo){
        myMapper.deleteProduct(userId, productNo);
    }

    public UserProductDTO findCancelProduct(String userId, String productNo){
        return myMapper.getCancelProduct(userId, productNo);
    }

    public int findUserNo(String userId){
        return myMapper.getUserNo(userId);
    }

    public List<EmailCounselDTO> findEmailCounseList(int userNo){
        return myMapper.getEmailList(userNo);
    }

    public List<UserAccountDTO> findUserAcount(int userNo){return myMapper.getUserAccountList(userNo);}

    public int findUserBalance(int userNo){return myMapper.getUserBalance(userNo);}

    public CancelProductDTO findCancelProductData(int userNo, int productNo){
        return myMapper.getCancelProductData(userNo, productNo);
    }

    private static final double TAX_RATE = 0.154;

    // 단리 기준
    public CancelProductDTO calculate(UserProductDTO upDto, ProductDTO pDto, LocalDate actualEndDate) {
        CancelProductDTO result = new CancelProductDTO();

        // 기본 정보 세팅
        result.setProductNo(upDto.getProductNo());
        result.setProductName(pDto.getProductName());
        result.setPrincipalAmount(upDto.getPrincipalAmount()); // ① 원금
        result.setStartDate(upDto.getStartDate());
        result.setExpectedEndDate(upDto.getExpectedEndDate());

        // 1️⃣ 적용 금리 선택
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(upDto.getStartDate()), actualEndDate);
        double rate;
        if (daysBetween >= upDto.getContractTerm() * 30) { // 만기
            rate = upDto.getApplyRate().doubleValue();
        } else { // 조기 해지
            rate = upDto.getContractEarlyRate().doubleValue();
        }
        result.setApplyRate(rate);
        result.setEarlyTerminateRate(upDto.getContractEarlyRate().doubleValue());

        // 2️⃣ 해지이자 (조기 해지 또는 만기 적용 이자)
        double earlyInterest = upDto.getPrincipalAmount().doubleValue() * rate / 100 * daysBetween / 365.0;
        result.setEarlyInterest(BigDecimal.valueOf(Math.floor(earlyInterest)));

        // 3️⃣ 만기후이자 (예시로 만기 후 발생한 추가 이자 계산)
        double maturityInterest = 0;
        if (daysBetween >= upDto.getContractTerm() * 30) {
            maturityInterest = earlyInterest * 0.01; // 예시: 1% 추가 이자
        }
        result.setMaturityInterest(BigDecimal.valueOf(Math.floor(maturityInterest)));

        // 4️⃣ 환입이자 (이미 지급된 이자)
        double refundInterest = 0; // 필요시 DB에서 가져오거나 계산
        result.setRefundInterest(BigDecimal.valueOf(refundInterest));

        // 5️⃣ 세금 계산 (해지이자 + 만기후이자 - 환입이자)
        double tax = (earlyInterest + maturityInterest - refundInterest) * TAX_RATE;
        result.setTaxAmount(BigDecimal.valueOf(Math.floor(tax)));

        // 6️⃣ 차감지급액 계산 (①+②+③-④-⑤)
        double netPayment = upDto.getPrincipalAmount().doubleValue() + earlyInterest + maturityInterest - refundInterest - tax;
        result.setNetPayment(BigDecimal.valueOf(Math.floor(netPayment)));

        // 7️⃣ 실입금금액 (원금 + netPayment)
        result.setFinalAmount(BigDecimal.valueOf(Math.floor(netPayment))); // 원금 포함 여부에 따라 조정 가능

        return result;
    }


}
