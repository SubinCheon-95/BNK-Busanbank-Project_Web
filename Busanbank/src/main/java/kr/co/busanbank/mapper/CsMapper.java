package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CodeDetailDTO;
import kr.co.busanbank.dto.EmailCounselDTO;
import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
    이름 : 우지희
    날짜 :
    내용 : 고객센터 FAQ, EMAILCOUNSEL 매퍼
 */

@Mapper
public interface CsMapper {

    // FAQ
    List<FaqDTO> selectFaqList(PageRequestDTO pageRequestDTO);
    int selectFaqTotal(PageRequestDTO pageRequestDTO);
    List<CodeDetailDTO> selectFaqCategories();

    // CODEDETAIL 공용
    List<CodeDetailDTO> selectCodeList(@Param("groupCode") String groupCode);

    // EMAILCOUNSEL
    int insertEmailCounsel(EmailCounselDTO emailCounselDTO);
    List<EmailCounselDTO> selectEmailCounselList(@Param("userId") int userId);
    EmailCounselDTO selectEmailCounselById(@Param("ecounselId") int ecounselId,
                                           @Param("userId") int userId);

}
