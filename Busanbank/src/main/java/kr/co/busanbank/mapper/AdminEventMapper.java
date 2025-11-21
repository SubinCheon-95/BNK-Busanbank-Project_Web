package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminEventMapper {
    public BoardDTO findById(int id);

    public List<BoardDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO);

    public List<BoardDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCountTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertEvent(BoardDTO boardDTO);
    public void modifyEvent(BoardDTO boardDTO);

    public void singleDelete(@Param("id") int id);
    public void delete(@Param("list") List<Long> idList);
}
