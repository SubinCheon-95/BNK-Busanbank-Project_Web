/*
    날짜 : 2025/11/26
    이름 : 오서정
    내용 : 챗봇 서비스 작성
 */

package kr.co.busanbank.service;

import kr.co.busanbank.dto.ChatbotDTO;
import kr.co.busanbank.mapper.ChatbotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotMapper chatbotMapper;

    public List<ChatbotDTO> findByKeywords(List<String> keywords) {

        if (keywords.isEmpty()) return List.of();

        return chatbotMapper.findRelatedContents(keywords);
    }
}
