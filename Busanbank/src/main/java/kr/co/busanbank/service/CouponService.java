package kr.co.busanbank.service;

import kr.co.busanbank.dto.CouponDTO;
import kr.co.busanbank.dto.CouponCategoryDTO;
import kr.co.busanbank.mapper.CouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 쿠폰 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponMapper couponMapper;

    /**
     * 쿠폰 목록 조회
     */
    public List<CouponDTO> getCouponList(int page, int size, String searchKeyword, String isActive) {
        try {
            int offset = (page - 1) * size;
            return couponMapper.selectCouponList(offset, size, searchKeyword, isActive);
        } catch (Exception e) {
            log.error("쿠폰 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 목록 조회에 실패했습니다.");
        }
    }

    /**
     * 쿠폰 총 개수
     */
    public int getCouponCount(String searchKeyword, String isActive) {
        try {
            return couponMapper.countCoupons(searchKeyword, isActive);
        } catch (Exception e) {
            log.error("쿠폰 개수 조회 실패: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 쿠폰 상세 조회
     */
    public CouponDTO getCouponById(int couponId) {
        try {
            CouponDTO coupon = couponMapper.selectCouponById(couponId);
            if (coupon != null) {
                // 카테고리 목록 조회
                List<CouponCategoryDTO> categories = couponMapper.selectCouponCategories(couponId);
                List<Integer> categoryIds = categories.stream()
                        .map(CouponCategoryDTO::getCategoryId)
                        .toList();
                coupon.setCategoryIds(categoryIds);
            }
            return coupon;
        } catch (Exception e) {
            log.error("쿠폰 상세 조회 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 정보를 조회할 수 없습니다.");
        }
    }

    /**
     * 쿠폰 코드 중복 체크
     */
    public boolean checkCouponCodeDuplicate(String couponCode) {
        try {
            return couponMapper.countByCouponCode(couponCode) > 0;
        } catch (Exception e) {
            log.error("쿠폰 코드 중복 체크 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 쿠폰 등록
     */
    @Transactional
    public boolean createCoupon(CouponDTO couponDTO, List<Integer> categoryIds) {
        try {
            // 쿠폰 코드 중복 체크
            if (checkCouponCodeDuplicate(couponDTO.getCouponCode())) {
                log.warn("쿠폰 코드 중복: {}", couponDTO.getCouponCode());
                return false;
            }

            // 쿠폰 등록
            int result = couponMapper.insertCoupon(couponDTO);
            if (result <= 0) {
                return false;
            }

            // 카테고리 매핑 등록
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Integer categoryId : categoryIds) {
                    CouponCategoryDTO ccDTO = CouponCategoryDTO.builder()
                            .couponId(couponDTO.getCouponId())
                            .categoryId(categoryId)
                            .build();
                    couponMapper.insertCouponCategory(ccDTO);
                }
            }

            log.info("쿠폰 등록 성공: {}", couponDTO.getCouponCode());
            return true;
        } catch (Exception e) {
            log.error("쿠폰 등록 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 등록에 실패했습니다.");
        }
    }

    /**
     * 쿠폰 수정
     */
    @Transactional
    public boolean updateCoupon(CouponDTO couponDTO, List<Integer> categoryIds) {
        try {
            // 쿠폰 수정
            int result = couponMapper.updateCoupon(couponDTO);
            if (result <= 0) {
                return false;
            }

            // 기존 카테고리 매핑 삭제 후 재등록
            couponMapper.deleteCouponCategories(couponDTO.getCouponId());

            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Integer categoryId : categoryIds) {
                    CouponCategoryDTO ccDTO = CouponCategoryDTO.builder()
                            .couponId(couponDTO.getCouponId())
                            .categoryId(categoryId)
                            .build();
                    couponMapper.insertCouponCategory(ccDTO);
                }
            }

            log.info("쿠폰 수정 성공: {}", couponDTO.getCouponId());
            return true;
        } catch (Exception e) {
            log.error("쿠폰 수정 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 수정에 실패했습니다.");
        }
    }

    /**
     * 쿠폰 삭제 (Soft Delete)
     */
    @Transactional
    public boolean deleteCoupon(int couponId) {
        try {
            int result = couponMapper.deleteCoupon(couponId);
            if (result > 0) {
                log.info("쿠폰 삭제 성공: {}", couponId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("쿠폰 삭제 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 삭제에 실패했습니다.");
        }
    }

    /**
     * 쿠폰 활성화/비활성화
     */
    @Transactional
    public boolean toggleCouponActive(int couponId, String isActive) {
        try {
            int result = couponMapper.updateCouponActive(couponId, isActive);
            if (result > 0) {
                log.info("쿠폰 활성화 변경 성공: {} -> {}", couponId, isActive);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("쿠폰 활성화 변경 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 활성화 변경에 실패했습니다.");
        }
    }

    /**
     * 특정 카테고리에서 사용 가능한 쿠폰 조회
     */
    public List<CouponDTO> getAvailableCouponsByCategory(int categoryId) {
        try {
            return couponMapper.selectCouponsByCategoryId(categoryId);
        } catch (Exception e) {
            log.error("카테고리별 쿠폰 조회 실패: {}", e.getMessage());
            throw new RuntimeException("사용 가능한 쿠폰을 조회할 수 없습니다.");
        }
    }

    /**
     * 쿠폰 사용 처리
     */
    @Transactional
    public boolean useCoupon(int couponId) {
        try {
            CouponDTO coupon = couponMapper.selectCouponById(couponId);
            if (coupon == null) {
                log.warn("존재하지 않는 쿠폰: {}", couponId);
                return false;
            }

            // 사용 가능 여부 체크
            if (coupon.getMaxUsageCount() > 0 &&
                coupon.getCurrentUsageCount() >= coupon.getMaxUsageCount()) {
                log.warn("쿠폰 사용 한도 초과: {}", couponId);
                return false;
            }

            // 사용 횟수 증가
            int result = couponMapper.incrementUsageCount(couponId);
            if (result > 0) {
                log.info("쿠폰 사용 성공: {}", couponId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("쿠폰 사용 처리 실패: {}", e.getMessage());
            throw new RuntimeException("쿠폰 사용 처리에 실패했습니다.");
        }
    }
}
