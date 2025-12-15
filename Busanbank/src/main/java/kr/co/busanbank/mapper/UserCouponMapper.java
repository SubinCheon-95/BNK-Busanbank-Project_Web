package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.UserCouponDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-28
 * 설명: 사용자 쿠폰 Mapper
 */
@Mapper
public interface UserCouponMapper {

    /**
     * 사용자가 쿠폰 코드로 쿠폰 등록
     */
    int insertUserCoupon(UserCouponDTO userCouponDTO);

    /**
     * 사용자가 보유한 쿠폰 목록 조회
     */
    List<UserCouponDTO> selectUserCouponsByUserId(@Param("userId") int userId);

    /**
     * 사용자의 특정 쿠폰 조회
     */
    UserCouponDTO selectUserCouponById(@Param("userCouponId") int userCouponId);

    /**
     * 쿠폰 코드로 쿠폰 정보 조회 (유효성 검증용)
     */
    UserCouponDTO selectCouponByCode(@Param("couponCode") String couponCode);

    /**
     * 사용자가 이미 해당 쿠폰을 등록했는지 확인
     */
    int countUserCouponByUserIdAndCouponId(
            @Param("userId") int userId,
            @Param("couponId") int couponId
    );

    /**
     * 쿠폰 사용 처리
     */
    int updateUserCouponUsed(
            @Param("userCouponId") int userCouponId,
            @Param("productNo") int productNo
    );

    /**
     * 사용 가능한 쿠폰 개수 조회
     */
    int countAvailableCouponsByUserId(@Param("userId") int userId);

    /**
     * 사용한 쿠폰 개수 조회
     */
    int countUsedCouponsByUserId(@Param("userId") int userId);

    /**
     * 상품 가입용, 2025/11/28, 수진
     */
    List<UserCouponDTO> selectAvailableCouponsByCategory(
            @Param("userId") int userId,
            @Param("categoryId") int categoryId
    );

    /**
     * 🔥 Flutter API용: 사용 가능한 쿠폰 조회
     *
     * @param userNo 사용자 번호
     * @return 사용 가능한 쿠폰 목록
     */
    List<UserCouponDTO> selectAvailableCoupons(@Param("userNo") Long userNo);
}
