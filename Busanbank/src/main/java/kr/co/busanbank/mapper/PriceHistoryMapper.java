package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.PriceHistoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PriceHistoryMapper {

    void insertPrice(@Param("symbol") String symbol, @Param("price") Double price);

    List<PriceHistoryDTO> getBtc(@Param("symbol") String symbol);
    List<PriceHistoryDTO> getGold(@Param("symbol") String symbol);
    List<PriceHistoryDTO> getOil(@Param("symbol") String symbol);
}
