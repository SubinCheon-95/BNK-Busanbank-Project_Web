package kr.co.busanbank.service;

import jakarta.transaction.Transactional;
import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminFaqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminFaqService {
    private final AdminFaqMapper adminFaqMapper;

    public FaqDTO findById(int faqId) {return adminFaqMapper.findById(faqId);}

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO, String groupCode, String faqCategory) {
        List<FaqDTO> dtoList = adminFaqMapper.findAll(pageRequestDTO, groupCode, faqCategory);
        int total = adminFaqMapper.selectCount(pageRequestDTO, groupCode, faqCategory);

        return PageResponseDTO.<FaqDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<FaqDTO> dtoList =  adminFaqMapper.searchAll(pageRequestDTO);
        int total = adminFaqMapper.searchCount(pageRequestDTO);

        return PageResponseDTO.<FaqDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    @Transactional
    public void insertFaq(FaqDTO faqDTO) {
        log.info("faqDTO = {}",  faqDTO);
        adminFaqMapper.insertFaq(faqDTO);
    }

    public void modifyFaq(FaqDTO faqDTO) {
        adminFaqMapper.modifyFaq(faqDTO);
    }

    public void singleDelete(int faqId) {adminFaqMapper.singleDelete(faqId);}

    public void delete(List<Long> idList) {adminFaqMapper.delete(idList);}
}
