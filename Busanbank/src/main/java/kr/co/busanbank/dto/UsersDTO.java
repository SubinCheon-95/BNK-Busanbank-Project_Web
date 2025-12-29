package kr.co.busanbank.dto;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersDTO {
    private int userNo;
    private String userName;
    private String userId;
    private String userPw;
    private String email;
    private String hp;
    private String zip;
    private String addr1;
    private String addr2;
    private String role;
    private String regDate;
    private String createdAt;
    private String updatedAt;
    private String status;
    private String accountPassword;
    private String rrn;
    private String userPriority;

    private String birth;  // 기능구현용
    private String gender; // 기능구현용
    private Long regDays;

    // 2025/12/23 - 프로필 기능 추가 - 작성자: 진원
    private String nickname;      // 닉네임
    private String avatarImage;   // 아바타 이미지 경로

    // 2025/12/29 - 이체한도 컬럼 추가 - 작성자: 오서정
    private Long onceLimit;
    private Long dailyLimit;
}
