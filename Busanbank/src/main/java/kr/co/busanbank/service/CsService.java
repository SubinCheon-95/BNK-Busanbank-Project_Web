package kr.co.busanbank.service;

import kr.co.busanbank.dto.CodeDetailDTO;
import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.CsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CsService {

    private final CsMapper csMapper;

    public PageResponseDTO<FaqDTO> getFaqList(PageRequestDTO pageRequestDTO) {

        List<FaqDTO> dtoList = csMapper.selectFaqList(pageRequestDTO);
        int total = csMapper.selectFaqTotal(pageRequestDTO);

        return PageResponseDTO.<FaqDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public List<CodeDetailDTO> getFaqCategories() {
        return csMapper.selectFaqCategories();
    }
}
