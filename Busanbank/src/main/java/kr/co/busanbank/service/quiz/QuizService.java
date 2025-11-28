package kr.co.busanbank.service.quiz;

import kr.co.busanbank.dto.UserPointDTO;
import kr.co.busanbank.dto.quiz.*;
import kr.co.busanbank.entity.quiz.Quiz;
import kr.co.busanbank.entity.quiz.UserQuizProgress;
import kr.co.busanbank.repository.quiz.QuizRepository;
import kr.co.busanbank.repository.quiz.UserQuizProgressRepository;
import kr.co.busanbank.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 작성자: 진원
 * 작성일: 2025-11-24
 * 수정일: 2025-11-28
 * 설명: 퀴즈 게임화 시스템 서비스
 * - 일일 퀴즈 생성 및 제공
 * - 퀴즈 정답 제출 및 점수 계산
 * - 새로운 통합 포인트 시스템 사용 (USERPOINT 테이블)
 * - 포인트 시스템 (정답당 10점)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserQuizProgressRepository progressRepository;
    private final PointService pointService;

    private static final Integer CORRECT_POINTS = 10;

    /**
     * 매번 새로운 랜덤 퀴즈 3개 조회
     * 수정: 새로운 포인트 시스템 사용 (작성자: 진원, 2025-11-28)
     */
    public List<QuizDTO> getTodayQuizzes(Long userId) {
        // 사용자 포인트 정보 조회 (작성자: 진원, 2025-11-28)
        UserPointDTO userPoint = pointService.getUserPoint(userId.intValue());

        // 레벨에 맞는 난이도의 퀴즈 선택 (작성자: 진원, 2025-11-28)
        Integer difficulty = userPoint.getUserLevel(); // 1=쉬움, 2=보통, 3=어려움
        List<Quiz> randomQuizzes = quizRepository.findRandomQuizzesByDifficulty(difficulty);

        // 해당 난이도의 퀴즈가 부족하면 모든 난이도에서 선택
        if (randomQuizzes.size() < 3) {
            log.warn("⚠️ 난이도 {} 퀴즈 부족 ({}/3) - 전체 퀴즈에서 선택", difficulty, randomQuizzes.size());
            randomQuizzes = quizRepository.findRandomQuizzes();
        }

        log.info("🎲 새 랜덤 퀴즈 생성 - User: {}, Level: {}, Difficulty: {}, QuizIds: {}",
                userId, userPoint.getUserLevel(), difficulty,
                randomQuizzes.stream().map(Quiz::getQuizId).collect(Collectors.toList()));

        return randomQuizzes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 퀴즈 조회
     */
    public QuizDTO getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다"));
        return convertToDTO(quiz);
    }

    /**
     * 정답 제출 및 채점
     * 수정자: 진원, 2025-11-28
     * 내용: 새로운 통합 포인트 시스템 사용 (기존 UserLevel 엔티티 제거)
     */
    public QuizResultDTO submitAnswer(Long userId, Long quizId, Integer selectedAnswer) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다"));

        boolean isCorrect = quiz.getCorrectAnswer().equals(selectedAnswer);
        int earnedPoints = isCorrect ? CORRECT_POINTS : 0;

        UserQuizProgress progress = UserQuizProgress.builder()
                .userId(userId)
                .quiz(quiz)
                .isCorrect(isCorrect)
                .earnedPoints(earnedPoints)
                .build();

        progressRepository.save(progress);

        // 새로운 통합 포인트 시스템 사용 (작성자: 진원, 2025-11-28)
        UserPointDTO beforePoint = pointService.getUserPoint(userId.intValue());
        Integer previousLevel = beforePoint.getUserLevel();

        if (isCorrect && earnedPoints > 0) {
            pointService.earnPoints(userId.intValue(), earnedPoints, "퀴즈 정답");
        }

        // 포인트 적립 후 레벨 확인
        UserPointDTO afterPoint = pointService.getUserPoint(userId.intValue());
        boolean leveledUp = !previousLevel.equals(afterPoint.getUserLevel());
        Integer totalEarnedToday = progressRepository.getTodayTotalPoints(userId);

        return QuizResultDTO.builder()
                .isCorrect(isCorrect)
                .earnedPoints(earnedPoints)
                .explanation(quiz.getExplanation())
                .newTotalPoints(afterPoint.getTotalEarned())
                .totalEarnedToday(totalEarnedToday)
                .leveledUp(leveledUp)
                .newTier(afterPoint.getLevelName())
                .levelUpMessage(leveledUp
                        ? afterPoint.getLevelName() + " 레벨에 도달했습니다!"
                        : null)
                .build();
    }

    /**
     * 사용자 상태 조회
     * 수정: 새로운 통합 포인트 시스템 사용 (작성자: 진원, 2025-11-28)
     */
    public UserStatusDTO getUserStatus(Long userId) {
        UserPointDTO userPoint = pointService.getUserPoint(userId.intValue());

        Integer completedQuizzes = progressRepository.countTotalAttempts(userId);
        Integer correctRate = progressRepository.getCorrectRate(userId);
        Integer completedToday = progressRepository.countTodayQuizzes(userId);

        return UserStatusDTO.builder()
                .userId(userId)
                .totalPoints(userPoint.getTotalEarned())
                .currentLevel(userPoint.getUserLevel())
                .tier(userPoint.getLevelName() != null ? userPoint.getLevelName() : "새싹")
                .completedQuizzes(completedQuizzes)
                .correctRate(correctRate)
                .completedToday(completedToday)
                .todayQuestCompleted(false) // 쿨다운 없음, 언제든지 퀴즈 가능 (작성자: 진원, 2025-11-26)
                .lastCompletedTime(null) // 쿨다운 없음 (작성자: 진원, 2025-11-26)
                .build();
    }

    /**
     * 결과 조회
     * 수정자: 진원, 2025-11-28
     * 내용: 새로운 통합 포인트 시스템 사용 (기존 레벨 로직 제거)
     */
    public ResultDTO getResult(Long userId) {
        log.info("=== getResult 호출 - userId: {} ===", userId);

        // 사용자 포인트 정보 조회 (작성자: 진원, 2025-11-28)
        UserPointDTO userPoint = pointService.getUserPoint(userId.intValue());
        log.info("사용자 포인트: {}", userPoint);

        // 오늘의 통계 (작성자: 진원, 2025-11-25)
        Integer todayCorrectCount = progressRepository.countTodayCorrectAnswers(userId);
        Integer todayIncorrectCount = progressRepository.countTodayIncorrectAnswers(userId);
        Integer todayCorrectRate = progressRepository.getTodayCorrectRate(userId);
        Integer earnedToday = progressRepository.getTodayTotalPoints(userId);

        log.info("오늘의 통계 (raw) - 정답: {}, 오답: {}, 정답률: {}, 포인트: {}",
                todayCorrectCount, todayIncorrectCount, todayCorrectRate, earnedToday);

        // 누적 통계 (작성자: 진원, 2025-11-25)
        Integer correctCount = progressRepository.countCorrectAnswers(userId);
        Integer totalCount = progressRepository.countTotalAttempts(userId);
        Integer correctRate = progressRepository.getCorrectRate(userId);

        // null 체크 및 기본값 설정
        todayCorrectCount = todayCorrectCount != null ? todayCorrectCount : 0;
        todayIncorrectCount = todayIncorrectCount != null ? todayIncorrectCount : 0;
        todayCorrectRate = todayCorrectRate != null ? todayCorrectRate : 0;
        earnedToday = earnedToday != null ? earnedToday : 0;

        correctCount = correctCount != null ? correctCount : 0;
        totalCount = totalCount != null ? totalCount : 0;
        correctRate = correctRate != null ? correctRate : 0;

        Integer incorrectCount = totalCount - correctCount;

        log.info("오늘의 통계 (처리후) - 정답: {}, 오답: {}, 정답률: {}, 포인트: {}",
                todayCorrectCount, todayIncorrectCount, todayCorrectRate, earnedToday);

        // 소요 시간 계산 (오늘 제출한 퀴즈 기준) (작성자: 진원, 2025-11-24)
        String timeSpent = calculateTimeSpent(userId);

        // 다음 레벨까지 필요한 포인트 계산
        Integer currentPoints = userPoint.getTotalEarned() != null ? userPoint.getTotalEarned() : 0;
        Integer requiredForNextLevel = userPoint.getRequiredPoints() != null ? userPoint.getRequiredPoints() : 100;
        int pointsNeeded = requiredForNextLevel - currentPoints;
        boolean needMorePoints = pointsNeeded > 0;

        ResultDTO result = ResultDTO.builder()
                // 오늘의 통계
                .todayCorrectCount(todayCorrectCount)
                .todayIncorrectCount(todayIncorrectCount)
                .todayCorrectRate(todayCorrectRate)
                .earnedPoints(earnedToday)
                .timeSpent(timeSpent)
                // 누적 통계
                .totalPoints(currentPoints)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .correctRate(correctRate)
                // 레벨 정보 (새로운 시스템에서는 레벨업 없음, 항상 false)
                .leveledUp(false)
                .newTier(userPoint.getLevelName() != null ? userPoint.getLevelName() : "새싹")
                .levelUpMessage(null)
                .needMorePoints(needMorePoints)
                .pointsNeeded(pointsNeeded > 0 ? pointsNeeded : 0)
                .build();

        log.info("반환할 ResultDTO: {}", result);
        return result;
    }

    /**
     * 오늘 퀴즈 소요 시간 계산
     * 작성자: 진원, 2025-11-25
     * 수정: 가장 최근 퀴즈 세션(최대 3개)의 소요 시간만 계산
     */
    private String calculateTimeSpent(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            // 오늘 풀은 퀴즈들의 제출 시간 조회
            List<UserQuizProgress> todayProgress = progressRepository.findByUserIdAndSubmittedAtBetween(
                    userId, startOfDay, endOfDay);

            if (todayProgress == null || todayProgress.isEmpty()) {
                return "0분 0초";
            }

            // 제출 시간 기준 내림차순 정렬 (최신순)
            List<UserQuizProgress> sortedProgress = todayProgress.stream()
                    .filter(p -> p.getSubmittedAt() != null)
                    .sorted((p1, p2) -> p2.getSubmittedAt().compareTo(p1.getSubmittedAt()))
                    .collect(Collectors.toList());

            if (sortedProgress.isEmpty()) {
                return "0분 0초";
            }

            // 가장 최근 퀴즈 세션 (최대 3개) 추출
            int sessionSize = Math.min(3, sortedProgress.size());
            List<UserQuizProgress> recentSession = sortedProgress.subList(0, sessionSize);

            // 세션의 첫 번째(가장 최근)와 마지막(가장 오래된) 제출 시간
            LocalDateTime sessionStart = recentSession.get(sessionSize - 1).getSubmittedAt();
            LocalDateTime sessionEnd = recentSession.get(0).getSubmittedAt();

            long seconds = java.time.Duration.between(sessionStart, sessionEnd).getSeconds();

            // 음수 방지 및 1개만 풀었을 경우 처리
            if (seconds < 0) seconds = 0;

            // 1개만 풀었을 경우 평균 30초로 계산
            if (sessionSize == 1) {
                seconds = 30;
            }

            long minutes = seconds / 60;
            seconds = seconds % 60;

            return String.format("%d분 %d초", minutes, seconds);
        } catch (Exception e) {
            // 오류 발생 시 기본값 반환
            return "0분 0초";
        }
    }

    /**
     * 상위 랭킹 조회 (실시간 랭킹용)
     * 수정자: 진원, 2025-11-28
     * 내용: 새로운 통합 포인트 시스템 사용
     * 참고: RankingService를 사용하도록 변경 권장
     */
    public List<java.util.Map<String, Object>> getTopRanking(int limit) {
        // 퀴즈 전용 랭킹이 아닌 통합 랭킹을 사용하므로 이 메서드는 deprecated
        // RankingService의 getTotalRanking() 사용 권장
        return new java.util.ArrayList<>();
    }

    /**
     * QuizDTO로 변환 (정답 제외)
     */
    private QuizDTO convertToDTO(Quiz quiz) {
        return QuizDTO.builder()
                .quizId(quiz.getQuizId())
                .question(quiz.getQuestion())
                .options(quiz.getOptions())
                .explanation(quiz.getExplanation())
                .category(quiz.getCategory())
                .difficulty(quiz.getDifficulty())
                .correctAnswer(quiz.getCorrectAnswer())
                .build();
    }
}