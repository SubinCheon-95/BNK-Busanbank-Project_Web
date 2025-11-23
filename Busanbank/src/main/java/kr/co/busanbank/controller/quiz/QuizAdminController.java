package kr.co.busanbank.controller.quiz;

import kr.co.busanbank.dto.quiz.QuizDTO;
import kr.co.busanbank.service.quiz.QuizAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 퀴즈 관리자 컨트롤러
 * 작성자: 진원
 * 작성일: 2025-11-24
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/quiz")
public class QuizAdminController {

    private final QuizAdminService quizAdminService;

    /**
     * 퀴즈 관리 페이지
     */
    @GetMapping
    public String quizAdminPage() {
        return "admin/quiz/adminQuiz";
    }

    /**
     * 퀴즈 목록 조회 (페이징, 검색)
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getQuizList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<QuizDTO> quizPage = quizAdminService.getQuizList(
                    page - 1, size, searchKeyword, category, difficulty);

            response.put("success", true);
            response.put("data", quizPage.getContent());
            response.put("currentPage", quizPage.getNumber() + 1);
            response.put("totalPages", quizPage.getTotalPages());
            response.put("totalElements", quizPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("퀴즈 목록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "퀴즈 목록 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 퀴즈 상세 조회
     */
    @GetMapping("/{quizId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getQuiz(@PathVariable Long quizId) {
        Map<String, Object> response = new HashMap<>();

        try {
            QuizDTO quiz = quizAdminService.getQuizById(quizId);
            response.put("success", true);
            response.put("data", quiz);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("퀴즈 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "퀴즈 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 퀴즈 등록
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createQuiz(@RequestBody QuizDTO quizDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            QuizDTO createdQuiz = quizAdminService.createQuiz(quizDTO);
            response.put("success", true);
            response.put("data", createdQuiz);
            response.put("message", "퀴즈가 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("퀴즈 등록 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "퀴즈 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 퀴즈 수정
     */
    @PutMapping("/{quizId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizDTO quizDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            QuizDTO updatedQuiz = quizAdminService.updateQuiz(quizId, quizDTO);
            response.put("success", true);
            response.put("data", updatedQuiz);
            response.put("message", "퀴즈가 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("퀴즈 수정 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "퀴즈 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 퀴즈 삭제
     */
    @DeleteMapping("/{quizId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteQuiz(@PathVariable Long quizId) {
        Map<String, Object> response = new HashMap<>();

        try {
            quizAdminService.deleteQuiz(quizId);
            response.put("success", true);
            response.put("message", "퀴즈가 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("퀴즈 삭제 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "퀴즈 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
