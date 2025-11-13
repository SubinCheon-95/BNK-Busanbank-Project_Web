package kr.co.busanbank.project.controller;

import kr.co.busanbank.project.dto.quiz.*;
import kr.co.busanbank.project.service.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizApiController {

    private final QuizService quizService;

    /**
     * 오늘의 퀴즈 3개 조회
     * GET /api/quiz/today?userId=1
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getTodayQuizzes(@RequestParam Long userId) {
        try {
            List<QuizDTO> quizzes = quizService.getTodayQuizzes(userId);
            return ResponseEntity.ok(ApiResponse.success(quizzes));
        } catch (Exception e) {
            log.error("오늘의 퀴즈 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 퀴즈 조회
     * GET /api/quiz/1
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizDTO>> getQuiz(@PathVariable Long quizId) {
        try {
            QuizDTO quiz = quizService.getQuiz(quizId);
            return ResponseEntity.ok(ApiResponse.success(quiz));
        } catch (Exception e) {
            log.error("퀴즈 조회 실패: quizId={}", quizId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("퀴즈를 찾을 수 없습니다"));
        }
    }

    /**
     * 정답 제출
     * POST /api/quiz/submit
     * Body: {"userId": 1, "quizId": 1, "selectedAnswer": 2}
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResultDTO>> submitAnswer(@RequestBody QuizSubmitRequest request) {
        try {
            QuizResultDTO result = quizService.submitAnswer(
                    request.getUserId(),
                    request.getQuizId(),
                    request.getSelectedAnswer()
            );
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("정답 제출 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("정답 제출에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 상태 조회
     * GET /api/quiz/status?userId=1
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<UserStatusDTO>> getUserStatus(@RequestParam Long userId) {
        try {
            UserStatusDTO status = quizService.getUserStatus(userId);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("사용자 상태 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 상태 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 결과 조회
     * GET /api/quiz/result?userId=1
     */
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<ResultDTO>> getResult(@RequestParam Long userId) {
        try {
            ResultDTO result = quizService.getResult(userId);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("결과 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("결과 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}
