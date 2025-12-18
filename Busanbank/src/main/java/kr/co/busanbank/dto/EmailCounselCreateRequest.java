package kr.co.busanbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailCounselCreateRequest {

    @NotBlank(message = "문의분야를 선택해주세요.")
    private String csCategory;

    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @NotBlank
    private String contactEmail;
}
