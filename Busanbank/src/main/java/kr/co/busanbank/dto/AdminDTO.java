package kr.co.busanbank.dto;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDTO {
    private int adminId;
    private String adminName;
    private String loginId;
    private String password;

    private String adminRole;

    private String createdAt;
    private String updatedAt;

    private String status;

}