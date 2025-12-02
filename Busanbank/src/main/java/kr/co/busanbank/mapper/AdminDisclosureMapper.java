package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.DisclosureDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminDisclosureMapper {

    public DisclosureDTO findById(int id);

    public List<DisclosureDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode, @Param("disclosureCategory") String disclosureCategory);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode,  @Param("disclosureCategory") String disclosureCategory);

    public List<DisclosureDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCount(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertPDF(DisclosureDTO disclosureDTO);

    public void modifyDisclosure(DisclosureDTO disclosureDTO);

    public void singleDelete(@Param("id") int id);
    public void delete(@Param("list") List<Long> idList);
}
