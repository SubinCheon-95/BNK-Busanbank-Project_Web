package kr.co.busanbank.service;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.dto.ProductJoinRequestDTO;
import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.mapper.ProductMapper;
import kr.co.busanbank.mapper.UserProductMapper;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.mapper.UserCouponMapper;  // ✅ 추가!
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 2025/11/25 김수진
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProductJoinService {

    private final ProductService productService;
    private final UserProductMapper userProductMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserCouponMapper userCouponMapper;  // ✅ 추가!
    // 작성자: 진원, 2025-11-29, 포인트 차감을 위해 PointService 추가
    private final PointService pointService;
    private final ProductMapper productMapper;

    /**
     * 금리 계산 (기본 금리 + 우대 금리)
     */
    public BigDecimal calculateApplyRate(int productNo) {
        ProductDTO product = productService.getProductById(productNo);
        // 실제로는 우대 조건에 따라 계산해야 하지만, 여기서는 만기우대금리를 사용
        return product.getMaturityRate();
    }

    /**
     * 예상 이자 계산
     * @param principalAmount 원금 (예금) 또는 월 납입액 (적금)
     * @param applyRate 적용 금리 (%)
     * @param contractTerm 계약 기간 (개월)
     * @param productType 상품 유형 (01: 예금, 02: 적금)
     * @return 예상 이자
     */
    public BigDecimal calculateExpectedInterest(
            BigDecimal principalAmount,
            BigDecimal applyRate,
            int contractTerm,
            String productType) {

        BigDecimal interest = BigDecimal.ZERO;

        if ("01".equals(productType)) {
            // 예금: 원금 × 금리 × (기간/12)
            interest = principalAmount
                    .multiply(applyRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(contractTerm).divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP));
        } else if ("02".equals(productType)) {
            // 적금: 월 납입액 × 기간 × (기간+1) / 24 × 금리
            BigDecimal totalMonths = BigDecimal.valueOf(contractTerm);
            BigDecimal totalDeposits = principalAmount.multiply(totalMonths);

            interest = totalDeposits
                    .multiply(totalMonths.add(BigDecimal.ONE))
                    .divide(BigDecimal.valueOf(24), 6, RoundingMode.HALF_UP)
                    .multiply(applyRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        }

        return interest.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * 예상 만기일 계산
     */
    public String calculateExpectedEndDate(String startDate, int contractTerm) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = start.plusMonths(contractTerm);
        return end.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }


    /**
     * ✅ 계좌 비밀번호 복호화 후 재암호화 (AES)
     *
     * 목적:
     * 1. USERS 테이블의 암호화된 비밀번호를 복호화
     * 2. 평문을 다시 AES로 암호화
     * 3. USERPRODUCT 테이블에 저장
     */
    private String encryptAccountPassword(String plainPassword) {
        try {
            if (plainPassword == null || plainPassword.isEmpty()) {
                throw new IllegalArgumentException("비밀번호가 비어있습니다.");
            }

            // ✅ 평문을 AES로 암호화
            String encrypted = AESUtil.encrypt(plainPassword);
            log.info("✅ 계좌 비밀번호 AES 암호화 완료 (평문 → AES)");

            return encrypted;

        } catch (Exception e) {
            log.error("❌ 계좌 비밀번호 암호화 실패", e);
            throw new RuntimeException("계좌 비밀번호 암호화 실패", e);
        }
    }

    /**
     * ✅ 휴대폰 번호 암호화 (평문 또는 AES → AES)
     */
    private String encryptPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        try {
            String plainPhone = phone;

            // ✅ 이미 암호화되어 있으면 복호화
            try {
                plainPhone = AESUtil.decrypt(phone);
                log.info("📌 휴대폰 번호 AES 복호화 → 재암호화");
            } catch (Exception e) {
                log.info("📌 휴대폰 번호가 평문 → 바로 암호화");
            }

            // ✅ AES 암호화
            String encrypted = AESUtil.encrypt(plainPhone);
            log.info("✅ 휴대폰 번호 AES 암호화 완료");

            return encrypted;

        } catch (Exception e) {
            log.error("❌ 휴대폰 번호 암호화 실패", e);
            throw new RuntimeException("휴대폰 번호 암호화 실패", e);
        }
    }


    /**
     * 최종 가입 처리
     */
    @Transactional
    public boolean processJoin(ProductJoinRequestDTO joinRequest) {

        // ========================================
        // 🔥 Flutter 가입 대응: 날짜 자동 세팅
        // ========================================
        LocalDate startDate;
        LocalDate expectedEndDate;

        // STARTDATE
        if (joinRequest.getStartDate() == null || joinRequest.getStartDate().isEmpty()) {
            startDate = LocalDate.now();
            joinRequest.setStartDate(startDate.toString());
            log.info("📅 startDate 자동 설정: {}", startDate);
        } else {
            startDate = LocalDate.parse(joinRequest.getStartDate());
        }

        // EXPECTEDENDDATE
        if (joinRequest.getExpectedEndDate() == null || joinRequest.getExpectedEndDate().isEmpty()) {
            expectedEndDate = startDate.plusMonths(joinRequest.getContractTerm());
            joinRequest.setExpectedEndDate(expectedEndDate.toString());
            log.info("📅 expectedEndDate 자동 설정: {}", expectedEndDate);
        }


        try {
            log.info("🚀 상품 가입 처리 시작");
            log.info("   userId: {}", joinRequest.getUserId());
            log.info("   productNo: {}", joinRequest.getProductNo());
            log.info("   usedPoints: {} P", joinRequest.getUsedPoints());
            log.info("   pointBonusRate: {}%", joinRequest.getPointBonusRate());
            log.info("   finalApplyRate: {}%", joinRequest.getApplyRate());
            log.info("   selectedCouponId: {}", joinRequest.getSelectedCouponId());  // ✅ 추가!
            log.info("   couponBonusRate: {}%", joinRequest.getCouponBonusRate());  // ✅ 추가!


            // ========================================
            // 🔥 Flutter 가입 대응: 중도해지금리 자동 세팅
            // (웹 로직 영향 없음)
            // ========================================
            if (joinRequest.getEarlyTerminateRate() == null) {

                BigDecimal earlyRate = productMapper.selectContractEarlyRate(
                        joinRequest.getProductNo().longValue()
                );

                if (earlyRate == null) {
                    log.warn("⚠️ 상품에 중도해지금리가 없음 → 0으로 설정");
                    earlyRate = BigDecimal.ZERO;
                }

                joinRequest.setEarlyTerminateRate(earlyRate);
                log.info("📌 contractEarlyRate 자동 설정: {}", earlyRate);
            }


            // ✅ 1. 원본 비밀번호 가져오기
            String plainPassword = joinRequest.getAccountPasswordOriginal();

            if (plainPassword == null || plainPassword.isEmpty()) {
                log.error("❌ 원본 비밀번호가 Session에 없습니다!");
                throw new IllegalStateException("원본 비밀번호가 없습니다.");
            }

            // ✅ 2. 계좌 비밀번호 암호화
            String encryptedPassword = encryptAccountPassword(plainPassword);
            log.info("🔐 계좌 비밀번호 AES 암호화 완료");

            // ✅ 3. 휴대폰 번호 암호화
            String encryptedPhone = encryptPhoneNumber(joinRequest.getNotificationHp());
            log.info("🔐 휴대폰 번호 AES 암호화 완료");

            // ✅ 4. UserProductDTO 생성
            UserProductDTO userProduct = UserProductDTO.builder()
                    .userId(joinRequest.getUserId())
                    .productNo(joinRequest.getProductNo())
                    .startDate(joinRequest.getStartDate())
                    .status("A")
                    .applyRate(joinRequest.getApplyRate())
                    .contractTerm(joinRequest.getContractTerm())
                    .principalAmount(joinRequest.getPrincipalAmount())
                    .expectedEndDate(joinRequest.getExpectedEndDate())
                    .contractEarlyRate(joinRequest.getEarlyTerminateRate())
                    // ✅ AES 암호화된 비밀번호 사용
                    .accountPassword(encryptedPassword)
                    // ✅ STEP 2 필드들
                    .branchId(joinRequest.getBranchId())
                    .empId(joinRequest.getEmpId())
                    .notificationSms(joinRequest.getNotificationSms())
                    .notificationEmail(joinRequest.getNotificationEmail())
                    // ✅ AES 암호화된 휴대폰 번호 사용
                    .notificationHp(encryptedPhone)
                    .notificationEmailAddr(joinRequest.getNotificationEmailAddr())
                    .usedPoints(joinRequest.getUsedPoints())  // ✅ 사용한 포인트 추가
                    .build();

            log.info("📋 DB INSERT 준비 완료");
            log.info("DB INSERT 값 " + userProduct.toString());


            // ✅ 5. DB INSERT
            int result = userProductMapper.insertUserProduct(userProduct);

            if (result > 0) {
                log.info("✅ 상품 가입 완료!");
                log.info("   사용 포인트: {} P", joinRequest.getUsedPoints());
                //log.info("   포인트 금리: {}%", joinRequest.getPointBonusRate());
                if (joinRequest.getPointBonusRate() != null) {
                    log.info("   포인트 금리: {}%", joinRequest.getPointBonusRate());
                }

                // ========================================
                // 작성자: 진원, 2025-11-29, 포인트 차감 처리 추가
                // ========================================
                if (joinRequest.getUsedPoints() != null && joinRequest.getUsedPoints() > 0) {
                    try {
                        // 작성자: 진원, 2025-12-03 수정 - 상품번호 대신 상품명으로 표시
                        boolean pointDeducted = pointService.usePoints(
                                joinRequest.getUserId(),
                                joinRequest.getUsedPoints(),
                                "상품 가입 (" + joinRequest.getProductName() + ")"
                        );

                        if (pointDeducted) {
                            log.info("✅ 포인트 차감 완료: {} P", joinRequest.getUsedPoints());
                        } else {
                            log.warn("⚠️ 포인트 차감 실패: 잔액 부족 또는 오류");
                            // 포인트 차감 실패 시에도 상품 가입은 유지 (트랜잭션 롤백 안 함)
                        }
                    } catch (Exception e) {
                        log.error("❌ 포인트 차감 중 오류 발생", e);
                        // 포인트 차감 실패해도 가입은 완료 (트랜잭션 롤백 안 함)
                    }
                }

                // ========================================
                // ✅ 6. 쿠폰 사용 처리 (여기에 추가!)
                // ========================================
                if (joinRequest.getSelectedCouponId() != null) {
                    try {
                        int couponResult = userCouponMapper.updateUserCouponUsed(
                                joinRequest.getSelectedCouponId(),
                                joinRequest.getProductNo()  // ✅ 상품 번호 사용! // INSERT 후 생성된 ID
                        );

                        if (couponResult > 0) {
                            log.info("✅ 쿠폰 사용 처리 완료");
                            log.info("   쿠폰 ID: {}", joinRequest.getSelectedCouponId());
                            //log.info("   쿠폰 금리: {}%", joinRequest.getCouponBonusRate());
                            if (joinRequest.getCouponBonusRate() != null) {
                                log.info("   쿠폰 금리: {}%", joinRequest.getCouponBonusRate());
                            }
                            log.info("   상품 번호: {}", joinRequest.getProductNo());
                        } else {
                            log.warn("⚠️ 쿠폰 사용 처리 실패: 업데이트된 행 없음");
                        }
                    } catch (Exception e) {
                        log.error("❌ 쿠폰 사용 처리 실패", e);
                        // 쿠폰 처리 실패해도 가입은 완료 (트랜잭션 롤백 안 함)
                    }
                }

                log.info("   최종 금리: {}%", joinRequest.getApplyRate());
                return true;
            } else {
                log.error("❌ INSERT 실패");
                return false;
            }

        } catch (Exception e) {
            log.error("❌ 상품 가입 중 오류 발생" + e);
            throw e;
        }

    }



    /**
     * 중복 가입 체크
     */
    public boolean isDuplicateJoin(int userId, int productNo) {
        return false;
    }
}