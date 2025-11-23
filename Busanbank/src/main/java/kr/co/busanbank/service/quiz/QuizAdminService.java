package kr.co.busanbank.service.quiz;

import kr.co.busanbank.dto.quiz.*;
import kr.co.busanbank.entity.quiz.Quiz;
import kr.co.busanbank.repository.quiz.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * 퀴즈 목록 조회 (페이징, 검색) - 관리자용
     * 작성자: 진원, 2025-11-24
     */
    public Page<QuizDTO> getQuizList(int page, int size, String searchKeyword, String category, Integer difficulty) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<Quiz> quizPage;

        // 검색 조건에 따라 조회
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // 검색어가 있을 때
            if (category != null && !category.isEmpty() && difficulty != null) {
                quizPage = quizRepository.findByQuestionContainingAndCategoryAndDifficulty(
                        searchKeyword, category, difficulty, pageable);
            } else if (category != null && !category.isEmpty()) {
                quizPage = quizRepository.findByQuestionContainingAndCategory(
                        searchKeyword, category, pageable);
            } else if (difficulty != null) {
                quizPage = quizRepository.findByQuestionContainingAndDifficulty(
                        searchKeyword, difficulty, pageable);
            } else {
                quizPage = quizRepository.findByQuestionContaining(searchKeyword, pageable);
            }
        } else {
            // 검색어 없을 때
            if (category != null && !category.isEmpty() && difficulty != null) {
                quizPage = quizRepository.findByCategoryAndDifficulty(category, difficulty, pageable);
            } else if (category != null && !category.isEmpty()) {
                quizPage = quizRepository.findByCategory(category, pageable);
            } else if (difficulty != null) {
                quizPage = quizRepository.findByDifficulty(difficulty, pageable);
            } else {
                quizPage = quizRepository.findAll(pageable);
            }
        }

        return quizPage.map(this::convertToDTO);
    }

    /**
     * 퀴즈 상세 조회 (ID로) - 관리자용
     * 작성자: 진원, 2025-11-24
     */
    public QuizDTO getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));
        return convertToDTO(quiz);
    }

    /**
     * 퀴즈 등록 (QuizDTO 사용) - 관리자용
     * 작성자: 진원, 2025-11-24
     */
    public QuizDTO createQuiz(QuizDTO quizDTO) {
        Quiz quiz = Quiz.builder()
                .question(quizDTO.getQuestion())
                .correctAnswer(quizDTO.getCorrectAnswer())
                .explanation(quizDTO.getExplanation())
                .category(quizDTO.getCategory())
                .difficulty(quizDTO.getDifficulty())
                .build();

        // 선택지 설정
        quiz.setOptions(quizDTO.getOptions());

        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToDTO(savedQuiz);
    }

    /**
     * 퀴즈 수정 (QuizDTO 사용) - 관리자용
     * 작성자: 진원, 2025-11-24
     */
    public QuizDTO updateQuiz(Long quizId, QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));

        quiz.setQuestion(quizDTO.getQuestion());
        quiz.setOptions(quizDTO.getOptions());
        quiz.setCorrectAnswer(quizDTO.getCorrectAnswer());
        quiz.setExplanation(quizDTO.getExplanation());
        quiz.setCategory(quizDTO.getCategory());
        quiz.setDifficulty(quizDTO.getDifficulty());

        Quiz updatedQuiz = quizRepository.save(quiz);
        return convertToDTO(updatedQuiz);
    }

    /**
     * QuizDTO로 변환
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