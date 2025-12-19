package kr.co.busanbank.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** *****************************************
 *  UserProduct DTO
 ****************************************** */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProductDTO {

    private int userId;             // 회원 ID

    @NotNull(message = "상품 번호는 필수입니다.")
    private int productNo;          // 상품번호

    @NotBlank(message = "가입일은 필수입니다.")
    private String startDate;       // 상품 가입일 (YYYY-MM-DD)
    private String status;          // 상태 (A: 유효, N: 해지)
    private String endDate;         // 상품 해지(종료)일 (YYYY-MM-DD)

    @NotNull(message = "적용 이율은 필수입니다.")
    @DecimalMin(value = "0.0", message = "이율은 0 이상이어야 합니다.")
    private BigDecimal applyRate;   // 가입시 최종 적용 이율(%)

    @NotNull(message = "계약 기간은 필수입니다.")
    @Min(value = 1, message = "계약 기간은 1개월 이상이어야 합니다.")
    private int contractTerm;       // 계약 기간(개월)

    @NotNull(message = "원금은 필수입니다.")
    @DecimalMin(value = "0.0", message = "원금은 0 이상이어야 합니다.")
    private BigDecimal principalAmount;     // 가입 원금/최초 납입액

    private String expectedEndDate; // 예상 만기일 (YYYY-MM-DD)

    @NotNull(message = "중도해지 이율은 필수입니다.")
    @DecimalMin(value = "0.0", message = "중도해지 이율은 0 이상이어야 합니다.")
    private BigDecimal contractEarlyRate;   // 고객별 확정된 중도해지 이율

    @NotBlank(message = "계좌 비밀번호는 필수입니다.")
    @Size(min = 4, max = 6, message = "계좌 비밀번호는 4~6자리여야 합니다.")
    private String accountPassword; // 회원 계좌 비밀번호

    private String createdAt;       // 생성일시
    private String updatedAt;       // 수정일시


    // 조회시 추가 정보
    private String productName;     // 상품명
    private String userName;        // 회원명
    private String email;           // 이메일
    private String hp;              // 휴대폰 번호
    private String userIdStr;       // 사용자 로그인 ID

    // ✅ STEP 2 추가 필드들
    private Integer branchId;                  // 권유지점 ID
    private Integer empId;                     // 권유직원 ID
    private String notificationSms;            // SMS 알림 (Y/N)
    private String notificationEmail;          // 이메일 알림 (Y/N)
    private String notificationHp;             // 알림 수신 휴대폰번호
    private String notificationEmailAddr;      // 알림 수신 이메일

    private Integer usedPoints;  // 사용한 포인트
}