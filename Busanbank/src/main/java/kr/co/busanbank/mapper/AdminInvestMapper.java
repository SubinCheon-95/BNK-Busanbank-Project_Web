package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.InvestDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminInvestMapper {

    public InvestDTO findById(int id);

    public List<InvestDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO, @Param("investType") String investType);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO, @Param("investType") String investType);

    public List<InvestDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCount(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertPDF(InvestDTO investDTO);

    public void modifyInvest(InvestDTO investDTO);

    public void singleDelete(@Param("id") int id);
    public void delete(@Param("list") List<Long> idList);
}
