package kr.co.busanbank.controller.quiz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 작성자: 진원
 * 작성일: 2025-11-24
 * 설명: 퀴즈 페이지 컨트롤러 (View 반환)
 * - 퀴즈 대시보드, 퀴즈 풀기, 결과 페이지 등 렌더링
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/quiz")
public class QuizController {
    @GetMapping("/quizadmincomplete")
    public String quizadmincomplete(Model model) {
        return  "quiz/quizadmincomplete";
    }

    @GetMapping("/quizdashboardcomplete")
    public String quizdashboardcomplete(Model model) {
        return  "quiz/quizdashboardcomplete";
    }

    @GetMapping("/quizresultcomplete")
    public String quizresultcomplete(Model model) {
        return  "quiz/quizresultcomplete";
    }

    @GetMapping("/quizsolvecomplete")
    public String quizsolvecomplete(Model model) {
        return  "quiz/quizsolvecomplete";
    }

}