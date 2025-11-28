package kr.co.busanbank.service;

import kr.co.busanbank.dto.UserCouponDTO;
import kr.co.busanbank.mapper.UserCouponMapper;
import kr.co.busanbank.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-28
 * 설명: 사용자 쿠폰 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;

    /**
     * 쿠폰 코드로 쿠폰 등록
     */
    @Transactional
    public String registerCoupon(int userId, String couponCode) {
        try {
            // 1. 쿠폰 코드로 쿠폰 조회
            UserCouponDTO couponInfo = userCouponMapper.selectCouponByCode(couponCode);

            if (couponInfo == null) {
                return "존재하지 않는 쿠폰 코드입니다.";
            }

            // 2. 쿠폰 활성화 상태 확인
            if (!"Y".equals(couponInfo.getIsActive())) {
                return "사용 중지된 쿠폰입니다.";
            }

            // 3. 쿠폰 유효기간 확인
            Date now = new Date();
            if (couponInfo.getValidTo() != null && now.after(couponInfo.getValidTo())) {
                return "유효기간이 만료된 쿠폰입니다.";
            }

            // 4. 중복 등록 확인
            int duplicateCount = userCouponMapper.countUserCouponByUserIdAndCouponId(
                    userId, couponInfo.getCouponId()
            );
            if (duplicateCount > 0) {
                return "이미 등록된 쿠폰입니다.";
            }

            // 5. 쿠폰 사용 한도 확인
            if (couponInfo.getMaxUsageCount() > 0 &&
                    couponInfo.getCurrentUsageCount() >= couponInfo.getMaxUsageCount()) {
                return "쿠폰 등록 한도가 초과되었습니다.";
            }

            // 6. 사용자 쿠폰 등록
            UserCouponDTO userCoupon = UserCouponDTO.builder()
                    .userId(userId)
                    .couponId(couponInfo.getCouponId())
                    .status("UNUSED")
                    .build();

            int result = userCouponMapper.insertUserCoupon(userCoupon);

            if (result > 0) {
                // 7. 쿠폰 사용 횟수 증가
                couponMapper.incrementUsageCount(couponInfo.getCouponId());

                log.info("쿠폰 등록 성공 - userId: {}, couponCode: {}", userId, couponCode);
                return "SUCCESS";
            } else {
                return "쿠폰 등록에 실패했습니다.";
            }

        } catch (Exception e) {
            log.error("쿠폰 등록 실패: {}", e.getMessage(), e);
            throw new RuntimeException("쿠폰 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 보유 쿠폰 목록 조회
     */
    public List<UserCouponDTO> getUserCoupons(int userId) {
        try {
            return userCouponMapper.selectUserCouponsByUserId(userId);
        } catch (Exception e) {
            log.error("쿠폰 목록 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("쿠폰 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 사용 가능한 쿠폰 개수
     */
    public int getAvailableCouponCount(int userId) {
        try {
            return userCouponMapper.countAvailableCouponsByUserId(userId);
        } catch (Exception e) {
            log.error("사용 가능 쿠폰 개수 조회 실패: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 사용한 쿠폰 개수
     */
    public int getUsedCouponCount(int userId) {
        try {
            return userCouponMapper.countUsedCouponsByUserId(userId);
        } catch (Exception e) {
            log.error("사용한 쿠폰 개수 조회 실패: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 쿠폰 사용 처리
     */
    @Transactional
    public boolean useCoupon(int userCouponId, int productNo) {
        try {
            int result = userCouponMapper.updateUserCouponUsed(userCouponId, productNo);
            if (result > 0) {
                log.info("쿠폰 사용 성공 - userCouponId: {}, productNo: {}", userCouponId, productNo);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("쿠폰 사용 실패: {}", e.getMessage(), e);
            throw new RuntimeException("쿠폰 사용 처리에 실패했습니다.");
        }
    }
}
