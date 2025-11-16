package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.AdminMemberDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMemberMapper {
    public List<AdminMemberDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO);

    public AdminMemberDTO findByUserNo(@Param("userNo") int userNo);
}
