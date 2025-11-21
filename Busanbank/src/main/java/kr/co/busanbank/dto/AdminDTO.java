package kr.co.busanbank.dto;


import lombok.*;

/**
 * 수정일: 2025-11-20 (로그인 실패 제한 필드 추가 - 진원)
 */
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

    // 로그인 실패 제한 관련 필드 (작성자: 진원, 2025-11-20)
    private Integer loginFailCount;
    private String accountLocked;
    private String lastFailedLogin;
    private String lockedDate;

}