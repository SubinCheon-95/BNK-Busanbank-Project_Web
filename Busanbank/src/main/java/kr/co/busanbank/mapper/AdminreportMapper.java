package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminreportMapper {
    public BoardDTO findById(int id);

    public List<BoardDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO);

    void insertReport(BoardDTO boardDTO);
    public void modifyReport(BoardDTO boardDTO);

    public void singleDelete(@Param("id") int id);
    public void delete(@Param("list") List<Long> idList);
}
