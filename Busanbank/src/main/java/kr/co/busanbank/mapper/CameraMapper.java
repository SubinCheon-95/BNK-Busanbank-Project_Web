package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.PointHistoryDTO;
import kr.co.busanbank.dto.UserPointDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CameraMapper {

    UserPointDTO selectUserPointByUserId(@Param("userId") int userId);

    int countTodayReward(@Param("userId") int userId);

    int insertCameraReward(@Param("userId") int userId,
                           @Param("point") int point);

    int updateUserPointAfterEarn(@Param("userId") int userId,
                                 @Param("amount") int amount);

    int insertPointHistory(PointHistoryDTO history);
}
