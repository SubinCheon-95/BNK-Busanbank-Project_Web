package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.UserCouponDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BtcMapper {
    public List<UserCouponDTO> findById(int docId);
    public void updateEvent(int couponId);
}
