package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.DocumentsDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminDocMapper {

    public DocumentsDTO findById(int docId);

    public List<DocumentsDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode, @Param("docCategory") String docCategory);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode,  @Param("docCategory") String docCategory);

    public List<DocumentsDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCount(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertDoc(DocumentsDTO documentsDTO);

    public void modifyDoc(DocumentsDTO documentsDTO);

    public void singleDelete(@Param("docId") int docId);
    public void delete(@Param("list") List<Long> idList);
}
