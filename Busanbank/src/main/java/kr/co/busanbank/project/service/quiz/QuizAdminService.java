package kr.co.busanbank.project.service.quiz;

import kr.co.busanbank.project.dto.quiz.*;
import kr.co.busanbank.project.entity.quiz.Quiz;
import kr.co.busanbank.project.repository.quiz.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizAdminService {

    private final QuizRepository quizRepository;

    /**
     * 퀴즈 추가
     */
    public Quiz addQuiz(QuizAddRequest request) {
        Quiz quiz = Quiz.builder()
                .question(request.getQuestion())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .build();

        quiz.setOptions(request.getOptions());
        return quizRepository.save(quiz);
    }

    /**
     * 퀴즈 수정
     */
    public Quiz updateQuiz(QuizUpdateRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다"));

        quiz.setQuestion(request.getQuestion());
        quiz.setExplanation(request.getExplanation());
        quiz.setCategory(request.getCategory());
        quiz.setDifficulty(request.getDifficulty());

        return quizRepository.save(quiz);
    }

    /**
     * 퀴즈 삭제
     */
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new RuntimeException("퀴즈를 찾을 수 없습니다");
        }
        quizRepository.deleteById(quizId);
    }

    /**
     * 모든 퀴즈 조회
     */
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(quiz -> QuizDTO.builder()
                        .quizId(quiz.getQuizId())
                        .question(quiz.getQuestion())
                        .options(quiz.getOptions())
                        .explanation(quiz.getExplanation())
                        .category(quiz.getCategory())
                        .difficulty(quiz.getDifficulty())
                        .correctAnswer(quiz.getCorrectAnswer())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 퀴즈 조회
     */
    public QuizDTO getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다"));

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

    /**
     * 통계 조회
     */
    public StatisticsDTO getStatistics() {
        Integer totalQuizzes = (int) quizRepository.count();

        Integer todayAttempts = 342;
        Integer averageCorrectRate = 72;
        Integer activeUsers = 156;

        return StatisticsDTO.builder()
                .totalQuizzes(totalQuizzes)
                .todayAttempts(todayAttempts)
                .averageCorrectRate(averageCorrectRate)
                .activeUsers(activeUsers)
                .build();
    }
}