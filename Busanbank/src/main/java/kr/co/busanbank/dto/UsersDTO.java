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


    private String birth;  // 기능구현용
    private String gender; // 기능구현용
    private Long regDays;

}
