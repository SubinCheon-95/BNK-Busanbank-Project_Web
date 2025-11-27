package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CsPDFDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminArchiveMapper {

    public CsPDFDTO findById(int id);

    public List<CsPDFDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode, @Param("archiveCategory") String archiveCategory);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode,  @Param("archiveCategory") String archiveCategory);

    public List<CsPDFDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCount(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertPDF(CsPDFDTO csPDFDTO);
    public void modifyArchive(CsPDFDTO csPDFDTO);

    public void singleDelete(@Param("id") int id);
    public void delete(@Param("list") List<Long> idList);
}