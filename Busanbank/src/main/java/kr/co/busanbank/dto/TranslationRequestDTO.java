package kr.co.busanbank.dto;

import lombok.Data;

import java.util.List;

@Data
public class TranslationRequestDTO {

    private List<String> texts;
    private String targetLang;
}
