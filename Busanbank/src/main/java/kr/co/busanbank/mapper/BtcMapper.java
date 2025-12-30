package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.BtcPredictDTO;
import kr.co.busanbank.dto.UserCouponDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BtcMapper {
    public List<UserCouponDTO> findById(int userId);
    public List<BtcPredictDTO> findByYesterdayPredict();

    public void markUserParticipated(@Param("userNo") int userNo, @Param("couponId") int couponId);
    public void insertPredict(@Param("userNo") int userNo, @Param("prediction") String prediction);
    public void updateEvent(int couponId);
    public void updateCouponId(@Param("userNo") int userNo, int couponId);
    void updatePredictResult(@Param("predictId") int predictId, @Param("result") String result, @Param("isSuccess") String isSuccess);
}
