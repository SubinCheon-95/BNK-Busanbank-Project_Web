/*
    날짜 : 2025/11/26
    이름 : 오서정
    내용 : 챗봇 매퍼 작성
 */

package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ChatbotDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatbotMapper {

    public List<ChatbotDTO> findRelatedContents(@Param("keywords") List<String> keywords);

}
