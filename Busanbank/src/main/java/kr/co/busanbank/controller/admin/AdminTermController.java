package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.service.AdminTermService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 작성자: 진원
 * 작성일: 2025-11-17
 * 설명: 정책약관 관리 컨트롤러
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/term")
@Controller
public class AdminTermController {

    private final AdminTermService adminTermService;

    /**
     * 약관 목록 페이지
     */
    @GetMapping("/list")
    public String list(Model model) {
        log.info("약관 목록 페이지 접근");
        return "admin/term/adminTermList";
    }

    /**
     * 약관 작성 페이지
     */
    @GetMapping("/write")
    public String write(Model model) {
        log.info("약관 작성 페이지 접근");
        return "admin/term/adminTermWrite";
    }

    /**
     * 약관 수정 페이지
     */
    @GetMapping("/modify")
    public String modify(@RequestParam int termNo, Model model) {
        log.info("약관 수정 페이지 접근 - termNo: {}", termNo);
        model.addAttribute("termNo", termNo);
        return "admin/term/adminTermModify";
    }

    /**
     * 약관 상세 페이지
     */
    @GetMapping("/view")
    public String view(@RequestParam int termNo, Model model) {
        log.info("약관 상세 페이지 접근 - termNo: {}", termNo);
        model.addAttribute("termNo", termNo);
        return "admin/term/adminTermView";
    }

    /**
     * 약관 목록 조회 API
     */
    @GetMapping("/terms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTermList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String termType
    ) {
        log.info("약관 목록 조회 - page: {}, size: {}, keyword: {}, termType: {}", page, size, searchKeyword, termType);

        Map<String, Object> response = new HashMap<>();

        try {
            List<TermDTO> termList = adminTermService.getTermList(page, size, searchKeyword, termType);
            int totalCount = adminTermService.getTotalCount(searchKeyword, termType);
            int totalPages = (int) Math.ceil((double) totalCount / size);

            response.put("success", true);
            response.put("data", termList);
            response.put("totalCount", totalCount);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약관 목록 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 약관 상세 조회 API
     */
    @GetMapping("/terms/{termNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTerm(@PathVariable int termNo) {
        log.info("약관 상세 조회 - termNo: {}", termNo);

        Map<String, Object> response = new HashMap<>();

        try {
            TermDTO term = adminTermService.getTermById(termNo);

            if (term != null) {
                response.put("success", true);
                response.put("data", term);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("약관 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 약관 추가 API
     */
    @PostMapping("/terms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createTerm(@RequestBody TermDTO termDTO) {
        log.info("약관 추가 - termTitle: {}", termDTO.getTermTitle());

        Map<String, Object> response = new HashMap<>();

        try {
            // 약관 제목 중복 체크
            if (adminTermService.isTermTitleDuplicate(termDTO.getTermTitle())) {
                response.put("success", false);
                response.put("message", "이미 존재하는 약관 제목입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean result = adminTermService.createTerm(termDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "약관이 성공적으로 추가되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관 추가에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("약관 추가 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 추가에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 약관 수정 API
     */
    @PutMapping("/terms/{termNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTerm(
            @PathVariable int termNo,
            @RequestBody TermDTO termDTO
    ) {
        log.info("약관 수정 - termNo: {}", termNo);

        Map<String, Object> response = new HashMap<>();

        try {
            termDTO.setTermNo(termNo);
            boolean result = adminTermService.updateTerm(termDTO);

            if (result) {
                response.put("success", true);
                response.put("message", "약관이 성공적으로 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관 수정에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("약관 수정 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 수정에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 약관 삭제 API
     */
    @DeleteMapping("/terms/{termNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTerm(@PathVariable int termNo) {
        log.info("약관 삭제 - termNo: {}", termNo);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = adminTermService.deleteTerm(termNo);

            if (result) {
                response.put("success", true);
                response.put("message", "약관이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "약관 삭제에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("약관 삭제 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 삭제에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 약관 제목 중복 체크 API
     */
    @GetMapping("/terms/check-title")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkTermTitle(@RequestParam String termTitle) {
        log.info("약관 제목 중복 체크 - termTitle: {}", termTitle);

        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = adminTermService.isTermTitleDuplicate(termTitle);
            response.put("success", true);
            response.put("isDuplicate", isDuplicate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("약관 제목 중복 체크 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "중복 체크에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 최신 버전 약관 조회 API
     */
    @GetMapping("/terms/latest/{termType}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLatestTerm(@PathVariable String termType) {
        log.info("최신 약관 조회 - termType: {}", termType);

        Map<String, Object> response = new HashMap<>();

        try {
            TermDTO term = adminTermService.getLatestTermByType(termType);

            if (term != null) {
                response.put("success", true);
                response.put("data", term);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "해당 유형의 약관이 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("최신 약관 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "약관 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
