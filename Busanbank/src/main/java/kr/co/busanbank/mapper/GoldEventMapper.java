/*
    날짜 : 2025/12/01
    이름 : 오서정
    내용 : 금 이벤트 매퍼 작성
*/
package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.GoldEventLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoldEventMapper {
    int countTodayByUser(int userNo);

    List<GoldEventLogDTO> findAllWait();

    void insertEvent(GoldEventLogDTO dto);

    void updateEvent(GoldEventLogDTO dto);

    Double findLatestPrice(String symbol);

    GoldEventLogDTO findTodayEvent(int userNo);

    GoldEventLogDTO findLastEvent(int userNo);

}
