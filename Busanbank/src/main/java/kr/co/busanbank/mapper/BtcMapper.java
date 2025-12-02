package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.UserCouponDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BtcMapper {
    public List<UserCouponDTO> findById(int docId);
    public int existsUserCoupon(@Param("userNo") int userNo, @Param("couponId") int couponId);
    public void updateEvent(int couponId);
}
