package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailCounselDTO {
    private int ecounselId;
    private int userId;
    private String emailType;
    private String title;
    private String content;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String response;
}
