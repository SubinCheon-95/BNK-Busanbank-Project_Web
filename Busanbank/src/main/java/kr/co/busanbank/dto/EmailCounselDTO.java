package kr.co.busanbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailCounselDTO {
    private int ecounselId;
    private int userId;

    private String groupCode;
    @NotBlank(message = "문의분야를 선택해주세요.")
    private String csCategory;

    private String csCategoryName;  // ★ 화면용

    private String title;
    private String content;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String response;

}
