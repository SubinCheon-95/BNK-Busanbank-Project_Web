package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.AdminDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminLoginMapper {

    AdminDTO findByLoginId(@Param("loginId") String loginId);
}