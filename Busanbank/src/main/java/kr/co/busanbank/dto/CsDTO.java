package kr.co.busanbank.dto;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsDTO {
    private int csNo;
    private int userId;
    private int cateId;
    private String title;
    private String content;
    private String status;
    private String createdAt;
    private String updatedAt;

}
