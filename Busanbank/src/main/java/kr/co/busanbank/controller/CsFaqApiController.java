package kr.co.busanbank.controller;

import kr.co.busanbank.dto.CodeDetailDTO;
import kr.co.busanbank.dto.FaqDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cs")
public class CsFaqApiController {

    private final CsService csService;

    // ✅ FAQ 목록(페이징/검색/카테고리)
    @GetMapping("/faq")
    public PageResponseDTO<FaqDTO> faqList(PageRequestDTO pageRequestDTO) {

        // 웹에서 free 처리하던 로직 그대로
        if ("free".equals(pageRequestDTO.getCate())) {
            pageRequestDTO.setCate(null);
        }

        // TOP 모드: cate/keyword 없으면 xml에서 10개 처리되지만,
        // total은 selectFaqTotal 기준이므로 화면에서는 total이 의미 없을 수 있음.
        return csService.getFaqList(pageRequestDTO);
    }

    // ✅ FAQ 카테고리 목록
    @GetMapping("/faq/categories")
    public List<CodeDetailDTO> faqCategories() {
        return csService.getFaqCategories();
    }
}
