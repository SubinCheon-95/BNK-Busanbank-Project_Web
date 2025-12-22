package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private int id;
    private String title;
    private String content;
    private String createdAt;
    private String autoBtn;
    private String cronExpr;

    private String route; //추가사항
}