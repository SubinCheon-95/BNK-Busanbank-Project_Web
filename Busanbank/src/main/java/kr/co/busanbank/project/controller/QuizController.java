package kr.co.busanbank.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
