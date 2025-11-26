package kr.co.busanbank.service.quiz;

import kr.co.busanbank.dto.quiz.*;
import kr.co.busanbank.entity.quiz.DailyQuest;
import kr.co.busanbank.entity.quiz.Quiz;
import kr.co.busanbank.entity.quiz.UserLevel;
import kr.co.busanbank.entity.quiz.UserQuizProgress;
import kr.co.busanbank.repository.quiz.DailyQuestRepository;
import kr.co.busanbank.repository.quiz.QuizRepository;
import kr.co.busanbank.repository.quiz.UserLevelRepository;
import kr.co.busanbank.repository.quiz.UserQuizProgressRepository;
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
 * 설명: 퀴즈 게임화 시스템 서비스
 * - 일일 퀴즈 생성 및 제공
 * - 퀴즈 정답 제출 및 점수 계산
 * - 사용자 레벨 및 진행도 관리
 * - 포인트 시스템 (정답당 10점)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserQuizProgressRepository progressRepository;
    private final UserLevelRepository levelRepository;
    private final DailyQuestRepository dailyQuestRepository;

    private static final Integer CORRECT_POINTS = 10;

    /**
     * 매번 새로운 랜덤 퀴즈 3개 조회
     * 수정: DailyQuest 제거, 매번 완전히 새로운 랜덤 퀴즈 제공 (작성자: 진원, 2025-11-26)
     */
    public List<QuizDTO> getTodayQuizzes(Long userId) {
        // 사용자 레벨 조회 (작성자: 진원, 2025-11-26)
        UserLevel userLevel = levelRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserLevel newLevel = UserLevel.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .currentLevel(1)
                            .tier("Rookie")
                            .build();
                    return levelRepository.save(newLevel);
                });

        // 레벨에 맞는 난이도의 퀴즈 선택 (작성자: 진원, 2025-11-26)
        Integer difficulty = userLevel.getCurrentLevel(); // 1=쉬움, 2=보통, 3=어려움
        List<Quiz> randomQuizzes = quizRepository.findRandomQuizzesByDifficulty(difficulty);

        // 해당 난이도의 퀴즈가 부족하면 모든 난이도에서 선택
        if (randomQuizzes.size() < 3) {
            log.warn("⚠️ 난이도 {} 퀴즈 부족 ({}/3) - 전체 퀴즈에서 선택", difficulty, randomQuizzes.size());
            randomQuizzes = quizRepository.findRandomQuizzes();
        }

        log.info("🎲 새 랜덤 퀴즈 생성 - User: {}, Level: {}, Difficulty: {}, QuizIds: {}",
                userId, userLevel.getCurrentLevel(), difficulty,
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

        UserLevel userLevel = levelRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserLevel newLevel = UserLevel.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .currentLevel(1)
                            .tier("Rookie")
                            .build();
                    return levelRepository.save(newLevel);
                });

        String previousTier = userLevel.getTier();
        userLevel.addPoints(earnedPoints);
        levelRepository.save(userLevel);

        boolean leveledUp = !previousTier.equals(userLevel.getTier());
        Integer totalEarnedToday = progressRepository.getTodayTotalPoints(userId);

        return QuizResultDTO.builder()
                .isCorrect(isCorrect)
                .earnedPoints(earnedPoints)
                .explanation(quiz.getExplanation())
                .newTotalPoints(userLevel.getTotalPoints())
                .totalEarnedToday(totalEarnedToday)
                .leveledUp(leveledUp)
                .newTier(userLevel.getTier())
                .levelUpMessage(leveledUp
                        ? userLevel.getTier() + " 레벨에 도달했습니다! 예금이자 +"
                        + userLevel.getInterestBonus() + "% 혜택권 획득!"
                        : null)
                .build();
    }

    /**
     * 사용자 상태 조회
     * 수정: DailyQuest 제거, 쿨다운 제거 (작성자: 진원, 2025-11-26)
     */
    public UserStatusDTO getUserStatus(Long userId) {
        UserLevel userLevel = levelRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserLevel newLevel = UserLevel.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .currentLevel(1)
                            .tier("Rookie")
                            .build();
                    return levelRepository.save(newLevel);
                });

        Integer completedQuizzes = progressRepository.countTotalAttempts(userId);
        Integer correctRate = progressRepository.getCorrectRate(userId);
        Integer completedToday = progressRepository.countTodayQuizzes(userId);

        return UserStatusDTO.builder()
                .userId(userId)
                .totalPoints(userLevel.getTotalPoints())
                .currentLevel(userLevel.getCurrentLevel())
                .tier(userLevel.getTier())
                .completedQuizzes(completedQuizzes)
                .correctRate(correctRate)
                .completedToday(completedToday)
                .todayQuestCompleted(false) // 쿨다운 없음, 언제든지 퀴즈 가능 (작성자: 진원, 2025-11-26)
                .lastCompletedTime(null) // 쿨다운 없음 (작성자: 진원, 2025-11-26)
                .build();
    }

    /**
     * 결과 조회
     * 수정자: 진원, 2025-11-25
     * 내용: 오늘 통계와 누적 통계 분리
     */
    public ResultDTO getResult(Long userId) {
        // 사용자 레벨 정보 조회 또는 생성 (작성자: 진원, 2025-11-24)
        UserLevel userLevel = levelRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserLevel newLevel = UserLevel.builder()
                            .userId(userId)
                            .totalPoints(0)
                            .currentLevel(1)
                            .tier("Rookie")
                            .build();
                    return levelRepository.save(newLevel);
                });

        // 오늘의 통계 (작성자: 진원, 2025-11-25)
        Integer todayCorrectCount = progressRepository.countTodayCorrectAnswers(userId);
        Integer todayIncorrectCount = progressRepository.countTodayIncorrectAnswers(userId);
        Integer todayCorrectRate = progressRepository.getTodayCorrectRate(userId);
        Integer earnedToday = progressRepository.getTodayTotalPoints(userId);

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

        // 레벨업 체크 (작성자: 진원, 2025-11-24)
        int oldLevel = userLevel.getCurrentLevel();
        String oldTier = userLevel.getTier();
        boolean leveledUp = false;
        String levelUpMessage = null;

        // 레벨업 로직 체크
        if (userLevel.getTotalPoints() >= 500 && oldLevel < 3) {
            userLevel.setCurrentLevel(3);
            userLevel.setTier("Banker");
            leveledUp = true;
            levelUpMessage = "축하합니다! Banker 레벨로 승급했습니다!";
        } else if (userLevel.getTotalPoints() >= 200 && oldLevel < 2) {
            userLevel.setCurrentLevel(2);
            userLevel.setTier("Analyst");
            leveledUp = true;
            levelUpMessage = "축하합니다! Analyst 레벨로 승급했습니다!";
        }

        if (leveledUp) {
            levelRepository.save(userLevel);
        }

        int pointsNeeded = 0;
        boolean needMorePoints = false;

        if (userLevel.getCurrentLevel() == 1) {
            pointsNeeded = 200 - userLevel.getTotalPoints();
            needMorePoints = pointsNeeded > 0;
        } else if (userLevel.getCurrentLevel() == 2) {
            pointsNeeded = 500 - userLevel.getTotalPoints();
            needMorePoints = pointsNeeded > 0;
        }

        // 소요 시간 계산 (오늘 제출한 퀴즈 기준) (작성자: 진원, 2025-11-24)
        String timeSpent = calculateTimeSpent(userId);

        return ResultDTO.builder()
                // 오늘의 통계
                .todayCorrectCount(todayCorrectCount)
                .todayIncorrectCount(todayIncorrectCount)
                .todayCorrectRate(todayCorrectRate)
                .earnedPoints(earnedToday)
                .timeSpent(timeSpent)
                // 누적 통계
                .totalPoints(userLevel.getTotalPoints())
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .correctRate(correctRate)
                // 레벨 정보
                .leveledUp(leveledUp)
                .newTier(userLevel.getTier())
                .levelUpMessage(levelUpMessage)
                .needMorePoints(needMorePoints)
                .pointsNeeded(pointsNeeded)
                .build();
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
     */
    public List<java.util.Map<String, Object>> getTopRanking(int limit) {
        List<UserLevel> topUsers = levelRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC, "totalPoints"
                        ))
        ).getContent();

        return topUsers.stream()
                .map(user -> {
                    java.util.Map<String, Object> rankData = new java.util.HashMap<>();
                    rankData.put("userId", user.getUserId());
                    rankData.put("totalPoints", user.getTotalPoints());
                    rankData.put("tier", user.getTier());
                    rankData.put("currentLevel", user.getCurrentLevel());
                    return rankData;
                })
                .collect(Collectors.toList());
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