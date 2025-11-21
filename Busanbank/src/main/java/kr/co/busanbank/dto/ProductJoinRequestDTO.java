package kr.co.busanbank.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 날짜 : 2025/11/21
 * 이름 : 김수진
 * 내용 : 상품 가입 요청 DTO
 * 수정사항 : Validation Groups를 사용하여 단계별로 검증
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJoinRequestDTO {

    // ========================================
    // Validation Groups 정의
    // ========================================
    public interface Step1 {}  // STEP 1 검증 그룹
    public interface Step2 {}  // STEP 2 검증 그룹
    public interface Step3 {}  // STEP 3 검증 그룹
    public interface Step4 {}  // STEP 4 검증 그룹

    // ========================================
    // STEP 1: 약관 동의
    // ========================================
    @NotNull(message = "상품 번호는 필수입니다.", groups = Step1.class)
    private Integer productNo;                 // 상품 번호

    @NotEmpty(message = "약관 동의는 최소 1개 이상 필요합니다.", groups = Step1.class)
    private List<Integer> agreedTermIds;       // 동의한 약관 ID 목록

    // ========================================
    // STEP 2: 가입 정보 입력
    // ========================================
    @NotNull(message = "가입 금액은 필수입니다.", groups = Step2.class)
    @DecimalMin(value = "0.0", message = "가입 금액은 0 이상이어야 합니다.", groups = Step2.class)
    private BigDecimal principalAmount;         // 가입 금액

    @NotNull(message = "계약 기간은 필수입니다.", groups = Step2.class)
    @Min(value = 1, message = "계약 기간은 1개월 이상이어야 합니다.", groups = Step2.class)
    private Integer contractTerm;               // 계약 기간 (개월)

    @NotBlank(message = "계좌 비밀번호는 필수입니다.", groups = Step2.class)
    @Pattern(regexp = "^[0-9]{4,6}$", message = "비밀번호는 숫자 4~6자리여야 합니다.", groups = Step2.class)
    private String accountPassword;             // 계좌 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수입니다.", groups = Step2.class)
    private String accountPasswordConfirm;      // 계좌 비밀번호 확인

    // === 권유직원 정보 (선택 사항이므로 검증 없음) ===
    private Integer branchId;                   // 권유지점 ID
    private Integer empId;                      // 권유직원 ID

    // === 만기 알림 설정 (선택 사항) ===
    private String notificationSms;             // SMS 알림 (Y/N)
    private String notificationEmail;           // 이메일 알림 (Y/N)
    private String notificationHp;              // 알림 수신 휴대폰번호
    private String notificationEmailAddr;       // 알림 수신 이메일

    // === 인증 상태 (세션 전용) ===
    private Boolean smsVerified;                // SMS 인증 완료
    private Boolean emailVerified;              // EMAIL 인증 완료

    // ========================================
    // STEP 2 계산 결과 (Controller에서 자동 설정)
    // ========================================
    private String startDate;                   // 가입일 (Controller에서 설정)
    private String expectedEndDate;             // 예상 만기일 (Controller에서 설정)

    // ========================================
    // STEP 3: 금리 정보 (Controller에서 자동 설정)
    // ========================================
    private BigDecimal baseRate;                // 기본 금리
    private BigDecimal applyRate;               // 최종 적용 금리
    private BigDecimal earlyTerminateRate;      // 중도해지 금리
    private BigDecimal expectedInterest;        // 예상 이자
    private BigDecimal expectedTotal;           // 총 수령액

    // ========================================
    // STEP 4: 최종 확인 및 사용자 정보
    // ========================================
    private Integer userId;                     // 사용자 번호 (Controller에서 설정)
    private String userName;                    // 사용자 이름 (Controller에서 설정)
    private String productName;                 // 상품명 (Controller에서 설정)
    private String productType;                 // 상품 유형 (Controller에서 설정)

    @AssertTrue(message = "최종 가입 동의가 필요합니다.", groups = Step4.class)
    private Boolean finalAgree;                 // 최종 동의 여부
}