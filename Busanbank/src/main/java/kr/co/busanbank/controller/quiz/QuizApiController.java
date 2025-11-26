package kr.co.busanbank.controller.quiz;

import kr.co.busanbank.dto.quiz.*;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 작성자: 진원
 * 작성일: 2025-11-24
 * 설명: 사용자용 퀴즈 REST API 컨트롤러
 * - 일일 퀴즈 조회 및 제출
 * - 사용자 진행도 및 결과 조회
 * - 실시간 랭킹 업데이트 (WebSocket 연동)
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizApiController {

    private final QuizService quizService;
    private final kr.co.busanbank.websocket.RankingWebSocketHandler rankingWebSocketHandler;

    /**
     * 오늘의 퀴즈 3개 조회
     * GET /api/quiz/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<QuizDTO>>> getTodayQuizzes(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            Long userId = (long) userDetails.getUsersDTO().getUserNo();
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
     * Body: {"quizId": 1, "selectedAnswer": 2}
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResultDTO>> submitAnswer(
            @RequestBody QuizSubmitRequest request,
            @AuthenticationPrincipal MyUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            Long userId = (long) userDetails.getUsersDTO().getUserNo();
            QuizResultDTO result = quizService.submitAnswer(
                    userId,
                    request.getQuizId(),
                    request.getSelectedAnswer()
            );

            // 랭킹 실시간 업데이트 브로드캐스트
            rankingWebSocketHandler.broadcastRanking();

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("정답 제출 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("정답 제출에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 상태 조회
     * GET /api/quiz/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<UserStatusDTO>> getUserStatus(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            Long userId = (long) userDetails.getUsersDTO().getUserNo();
            UserStatusDTO status = quizService.getUserStatus(userId);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("사용자 상태 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 상태 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 결과 조회
     * GET /api/quiz/result
     */
    @GetMapping("/result")
    public ResponseEntity<ApiResponse<ResultDTO>> getResult(
            @AuthenticationPrincipal MyUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("로그인이 필요합니다."));
            }

            Long userId = (long) userDetails.getUsersDTO().getUserNo();
            ResultDTO result = quizService.getResult(userId);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("결과 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("결과 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 실시간 랭킹 조회
     * GET /api/quiz/ranking
     * 작성자: 진원, 2025-11-25
     */
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getRanking() {
        try {
            List<java.util.Map<String, Object>> ranking = quizService.getTopRanking(10);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            log.error("랭킹 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("랭킹 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}