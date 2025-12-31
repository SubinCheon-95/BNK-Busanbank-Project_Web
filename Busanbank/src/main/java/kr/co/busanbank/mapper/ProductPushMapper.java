package kr.co.busanbank.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductPushMapper {
    public String findByUserName(@Param("userNo") int userNo);
}
