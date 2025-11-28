package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CouponDTO;
import kr.co.busanbank.dto.CouponCategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-27
 * 설명: 쿠폰 Mapper
 */
@Mapper
public interface CouponMapper {

    // 쿠폰 목록 조회 (페이징, 검색)
    List<CouponDTO> selectCouponList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("searchKeyword") String searchKeyword,
            @Param("isActive") String isActive
    );

    // 쿠폰 총 개수
    int countCoupons(
            @Param("searchKeyword") String searchKeyword,
            @Param("isActive") String isActive
    );

    // 쿠폰 상세 조회
    CouponDTO selectCouponById(@Param("couponId") int couponId);

    // 쿠폰 코드 중복 체크
    int countByCouponCode(@Param("couponCode") String couponCode);

    // 쿠폰 등록
    int insertCoupon(CouponDTO couponDTO);

    // 쿠폰 수정
    int updateCoupon(CouponDTO couponDTO);

    // 쿠폰 삭제 (Soft Delete)
    int deleteCoupon(@Param("couponId") int couponId);

    // 쿠폰 활성화/비활성화
    int updateCouponActive(
            @Param("couponId") int couponId,
            @Param("isActive") String isActive
    );

    // 쿠폰 사용 횟수 증가
    int incrementUsageCount(@Param("couponId") int couponId);

    // ========== 쿠폰-카테고리 매핑 ==========

    // 쿠폰의 카테고리 목록 조회
    List<CouponCategoryDTO> selectCouponCategories(@Param("couponId") int couponId);

    // 쿠폰-카테고리 매핑 등록
    int insertCouponCategory(CouponCategoryDTO couponCategoryDTO);

    // 쿠폰의 모든 카테고리 매핑 삭제
    int deleteCouponCategories(@Param("couponId") int couponId);

    // 특정 카테고리에서 사용 가능한 쿠폰 조회
    List<CouponDTO> selectCouponsByCategoryId(@Param("categoryId") int categoryId);
}
