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
     * 오늘의 3개 퀴즈 조회 (또는 생성)
     * 수정: 1분마다 새로운 퀴즈 가능 (작성자: 진원, 2025-11-24)
     */
    public List<QuizDTO> getTodayQuizzes(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        var dailyQuest = dailyQuestRepository
                .findByUserIdAndQuestDate(userId, today)
                .orElse(null);

        boolean needNewQuiz = false;

        if (dailyQuest == null) {
            needNewQuiz = true;
        } else if (dailyQuest.isCompleted()) {
            // 퀴즈 완료 후 1분이 지났는지 확인 (작성자: 진원, 2025-11-24)
            LocalDateTime lastCompleted = dailyQuest.getLastCompletedTime();
            if (lastCompleted != null && now.isAfter(lastCompleted.plusMinutes(1))) {
                // 1분 경과: 퀴즈 리셋 및 저장 (작성자: 진원, 2025-11-24)
                needNewQuiz = true;
                dailyQuest.setCompletedCount(0);
                dailyQuest.setLastCompletedTime(null);
                dailyQuestRepository.save(dailyQuest);
            } else {
                // 1분 미경과: 대기 필요
                throw new RuntimeException("다음 퀴즈까지 1분을 기다려주세요!");
            }
        }

        if (needNewQuiz || dailyQuest == null) {
            // 새로운 퀴즈 생성
            List<Quiz> randomQuizzes = quizRepository.findRandomQuizzes();
            List<Long> quizIds = randomQuizzes.stream()
                    .map(Quiz::getQuizId)
                    .collect(Collectors.toList());

            if (dailyQuest == null) {
                dailyQuest = DailyQuest.builder()
                        .userId(userId)
                        .questDate(today)
                        .completedCount(0)
                        .build();
            }

            dailyQuest.setQuizIds(quizIds);
            dailyQuestRepository.save(dailyQuest);
        }

        return dailyQuest.getQuizIds().stream()
                .map(quizId -> quizRepository.findById(quizId).orElse(null))
                .filter(quiz -> quiz != null)
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

        LocalDate today = LocalDate.now();
        var dailyQuest = dailyQuestRepository
                .findByUserIdAndQuestDate(userId, today)
                .orElse(null);

        if (dailyQuest != null) {
            dailyQuest.incrementCompleted();
            // 3개 완료 시 완료 시간 기록 (작성자: 진원, 2025-11-24)
            if (dailyQuest.isCompleted()) {
                dailyQuest.setLastCompletedTime(LocalDateTime.now());
            }
            dailyQuestRepository.save(dailyQuest);
        }

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
     * 수정자: 진원, 2025-11-24
     * 내용: 1분 쿨다운을 위한 마지막 완료 시간 추가 및 쿨다운 경과 시 자동 리셋
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

        // 오늘 퀴즈 완료 여부 및 마지막 완료 시간 확인 (작성자: 진원, 2025-11-24)
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        var dailyQuest = dailyQuestRepository
                .findByUserIdAndQuestDate(userId, today)
                .orElse(null);

        boolean todayQuestCompleted = false;
        LocalDateTime lastCompletedTime = null;

        if (dailyQuest != null && dailyQuest.isCompleted()) {
            // 1분 쿨다운 체크 (작성자: 진원, 2025-11-24)
            LocalDateTime lastCompleted = dailyQuest.getLastCompletedTime();

            // lastCompletedTime이 null이면 즉시 리셋 (데이터 불일치 해결)
            if (lastCompleted == null) {
                dailyQuest.setCompletedCount(0);
                dailyQuest.setLastCompletedTime(null);
                dailyQuestRepository.save(dailyQuest);
                todayQuestCompleted = false;
                lastCompletedTime = null;
            } else if (now.isAfter(lastCompleted.plusMinutes(1))) {
                // 1분 경과: 퀴즈 리셋
                dailyQuest.setCompletedCount(0);
                dailyQuest.setLastCompletedTime(null);
                dailyQuestRepository.save(dailyQuest);
                todayQuestCompleted = false;
                lastCompletedTime = null;
            } else {
                // 1분 미경과: 대기 상태
                todayQuestCompleted = true;
                lastCompletedTime = lastCompleted;
            }
        }

        return UserStatusDTO.builder()
                .userId(userId)
                .totalPoints(userLevel.getTotalPoints())
                .currentLevel(userLevel.getCurrentLevel())
                .tier(userLevel.getTier())
                .completedQuizzes(completedQuizzes)
                .correctRate(correctRate)
                .completedToday(completedToday)
                .todayQuestCompleted(todayQuestCompleted)
                .lastCompletedTime(lastCompletedTime)
                .build();
    }

    /**
     * 결과 조회
     * 수정자: 진원, 2025-11-24
     * 내용: null 체크 및 기본값 처리 강화
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

        Integer correctCount = progressRepository.countCorrectAnswers(userId);
        Integer totalCount = progressRepository.countTotalAttempts(userId);
        Integer correctRate = progressRepository.getCorrectRate(userId);
        Integer earnedToday = progressRepository.getTodayTotalPoints(userId);

        // null 체크 및 기본값 설정
        correctCount = correctCount != null ? correctCount : 0;
        totalCount = totalCount != null ? totalCount : 0;
        correctRate = correctRate != null ? correctRate : 0;
        earnedToday = earnedToday != null ? earnedToday : 0;

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
                .correctRate(correctRate)
                .earnedPoints(earnedToday)
                .totalPoints(userLevel.getTotalPoints())
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .timeSpent(timeSpent)
                .leveledUp(leveledUp)
                .newTier(userLevel.getTier())
                .levelUpMessage(levelUpMessage)
                .needMorePoints(needMorePoints)
                .pointsNeeded(pointsNeeded)
                .build();
    }

    /**
     * 오늘 퀴즈 소요 시간 계산
     * 작성자: 진원, 2025-11-24
     * 수정: LocalDate를 String으로 변환하여 Oracle 쿼리 호환
     */
    private String calculateTimeSpent(Long userId) {
        try {
            LocalDate today = LocalDate.now();

            // LocalDate를 String으로 변환 (Oracle TO_DATE 형식)
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            // 오늘 풀은 퀴즈들의 제출 시간 조회
            List<UserQuizProgress> todayProgress = progressRepository.findByUserIdAndSubmittedAtBetween(
                    userId, startOfDay, endOfDay);

            if (todayProgress == null || todayProgress.isEmpty()) {
                return "0분 0초";
            }

            // 첫 번째와 마지막 제출 시간 차이 계산
            LocalDateTime firstSubmit = todayProgress.stream()
                    .map(UserQuizProgress::getSubmittedAt)
                    .filter(java.util.Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());

            LocalDateTime lastSubmit = todayProgress.stream()
                    .map(UserQuizProgress::getSubmittedAt)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.now());

            long seconds = java.time.Duration.between(firstSubmit, lastSubmit).getSeconds();

            // 음수 방지
            if (seconds < 0) seconds = 0;

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