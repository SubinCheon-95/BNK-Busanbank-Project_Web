package kr.co.busanbank.controller;

import kr.co.busanbank.dto.TranslationRequestDTO;
import kr.co.busanbank.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public ResponseEntity<Map<String, List<String>>> translate(@RequestBody TranslationRequestDTO request) {
        List<String> results = translationService.translateBatch(request.getTexts(), request.getTargetLang());
        return ResponseEntity.ok(Map.of("translated", results));
    }
}
