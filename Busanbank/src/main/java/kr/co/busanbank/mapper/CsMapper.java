package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CodeDetailDTO;
import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CsMapper {

    // 목록조회 (페이징 + 검사)
    List<FaqDTO> selectFaqList(PageRequestDTO pageRequestDTO);

    // 전체 건수
    int selectFaqTotal(PageRequestDTO pageRequestDTO);

    // FAQ 카테고리 목록 (코드테이블)
    List<CodeDetailDTO> selectFaqCategories();
}
