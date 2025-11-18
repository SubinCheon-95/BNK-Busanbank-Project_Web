package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminFaqMapper {

    public FaqDTO findById(int faqId);

    public List<FaqDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode, @Param("faqCategory") String faqCategory);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO, @Param("groupCode") String groupCode,  @Param("faqCategory") String faqCategory);

    public List<FaqDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCount(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertFaq(FaqDTO faqDTO);

    public void modifyFaq(FaqDTO faqDTO);

    public void singleDelete(@Param("faqId") int faqId);
    public void delete(@Param("list") List<Long> idList);
}
