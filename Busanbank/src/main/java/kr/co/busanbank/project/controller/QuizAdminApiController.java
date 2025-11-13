package kr.co.busanbank.project.controller;

import kr.co.busanbank.project.dto.quiz.*;
import kr.co.busanbank.project.entity.quiz.Quiz;
import kr.co.busanbank.project.service.quiz.QuizAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz/admin")
@RequiredArgsConstructor
@Slf4j
public class QuizAdminApiController {

    private final QuizAdminService quizAdminService;

    /**
     * 퀴즈 추가
     * POST /api/quiz/admin
     * Body: {"question": "...", "options": [...], "correctAnswer": 2, ...}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Quiz>> addQuiz(@RequestBody QuizAddRequest request) {
        try {
            Quiz quiz = quizAdminService.addQuiz(request);
            return ResponseEntity.ok(ApiResponse.success(quiz));
        } catch (Exception e) {
            log.error("퀴즈 추가 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈 추가에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 퀴즈 수정
     * PUT /api/quiz/admin
     * Body: {"quizId": 1, "question": "...", ...}
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Quiz>> updateQuiz(@RequestBody QuizUpdateRequest request) {
        try {
            Quiz quiz = quizAdminService.updateQuiz(request);
            return ResponseEntity.ok(ApiResponse.success(quiz));
        } catch (Exception e) {
            log.error("퀴즈 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 퀴즈 삭제
     * DELETE /api/quiz/admin/1
     */
    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<String>> deleteQuiz(@PathVariable Long quizId) {
        try {
            quizAdminService.deleteQuiz(quizId);
            return ResponseEntity.ok(ApiResponse.success("퀴즈가 삭제되었습니다"));
        } catch (Exception e) {
            log.error("퀴즈 삭제 실패: quizId={}", quizId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 모든 퀴즈 조회
     * GET /api/quiz/admin
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getAllQuizzes() {
        try {
            List<QuizDTO> quizzes = quizAdminService.getAllQuizzes();
            return ResponseEntity.ok(ApiResponse.success(quizzes));
        } catch (Exception e) {
            log.error("퀴즈 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 퀴즈 조회
     * GET /api/quiz/admin/1
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizDTO>> getQuiz(@PathVariable Long quizId) {
        try {
            QuizDTO quiz = quizAdminService.getQuiz(quizId);
            return ResponseEntity.ok(ApiResponse.success(quiz));
        } catch (Exception e) {
            log.error("퀴즈 조회 실패: quizId={}", quizId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈를 찾을 수 없습니다"));
        }
    }

    /**
     * 통계 조회
     * GET /api/quiz/admin/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<StatisticsDTO>> getStatistics() {
        try {
            StatisticsDTO statistics = quizAdminService.getStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("통계 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}
