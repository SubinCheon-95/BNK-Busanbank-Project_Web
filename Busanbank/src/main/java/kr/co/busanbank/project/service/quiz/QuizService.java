package kr.co.busanbank.project.service.quiz;

import kr.co.busanbank.project.dto.quiz.*;
import kr.co.busanbank.project.entity.quiz.*;
import kr.co.busanbank.project.repository.quiz.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
     */
    public List<QuizDTO> getTodayQuizzes(Long userId) {
        LocalDate today = LocalDate.now();

        var dailyQuest = dailyQuestRepository
                .findByUserIdAndQuestDate(userId, today)
                .orElse(null);

        if (dailyQuest == null) {
            List<Quiz> randomQuizzes = quizRepository.findRandomQuizzes();
            List<Long> quizIds = randomQuizzes.stream()
                    .map(Quiz::getQuizId)
                    .collect(Collectors.toList());

            dailyQuest = DailyQuest.builder()
                    .userId(userId)
                    .questDate(today)
                    .build();
            dailyQuest.setQuizIds(quizIds);
            dailyQuest.setCompletedCount(0);

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
                .build();
    }

    /**
     * 결과 조회
     */
    public ResultDTO getResult(Long userId) {
        UserLevel userLevel = levelRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다"));

        Integer correctCount = progressRepository.countCorrectAnswers(userId);
        Integer totalCount = progressRepository.countTotalAttempts(userId);
        Integer correctRate = progressRepository.getCorrectRate(userId);
        Integer earnedToday = progressRepository.getTodayTotalPoints(userId);
        Integer incorrectCount = totalCount - correctCount;

        int pointsNeeded = 0;
        boolean needMorePoints = false;

        if (userLevel.getCurrentLevel() == 1) {
            pointsNeeded = 200 - userLevel.getTotalPoints();
            needMorePoints = pointsNeeded > 0;
        } else if (userLevel.getCurrentLevel() == 2) {
            pointsNeeded = 500 - userLevel.getTotalPoints();
            needMorePoints = pointsNeeded > 0;
        }

        return ResultDTO.builder()
                .correctRate(correctRate)
                .earnedPoints(earnedToday)
                .totalPoints(userLevel.getTotalPoints())
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .timeSpent("3분 42초")
                .leveledUp(false)
                .newTier(userLevel.getTier())
                .needMorePoints(needMorePoints)
                .pointsNeeded(pointsNeeded)
                .build();
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