package kr.co.busanbank.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

 /** ***********************************************
 *               ProductJoinRequiestDTO
 ************************************************* */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductJoinRequestDTO {

    // === STEP 1: 약관 동의 ===
    private List<Integer> agreedTermIds;     // 동의한 약관 ID 목록

    // === STEP 2: 기본 정보 입력 ===
    @NotNull(message = "상품 번호는 필수입니다.")
    private Integer productNo;               // 상품번호

    @NotNull(message = "가입 금액은 필수입니다.")
    @DecimalMin(value = "0.0", message = "가입 금액은 0 이상이어야 합니다.")
    private BigDecimal principalAmount;      // 가입 원금 (예금) 또는 월 납입액 (적금)

    @NotNull(message = "계약 기간은 필수입니다.")
    @Min(value = 1, message = "계약 기간은 1개월 이상이어야 합니다.")
    private Integer contractTerm;            // 계약 기간(개월)

    @NotBlank(message = "계좌 비밀번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "계좌 비밀번호는 4~6자리 숫자여야 합니다.")
    private String accountPassword;          // 계좌 비밀번호

    @NotBlank(message = "계좌 비밀번호 확인은 필수입니다.")
    private String accountPasswordConfirm;   // 계좌 비밀번호 확인

    // === STEP 3: 금리 정보 확인 ===
    private BigDecimal baseRate;             // 기본 금리
    private BigDecimal applyRate;            // 최종 적용 금리
    private BigDecimal earlyTerminateRate;   // 중도해지 이율

    @NotBlank(message = "가입일은 필수입니다.")
    private String startDate;                // 가입일 (YYYY-MM-DD)

    private String expectedEndDate;          // 예상 만기일 (YYYY-MM-DD)

    // === STEP 4: 최종 확인 (모든 정보 표시) ===
    // 상품 정보
    private String productName;              // 상품명
    private String productType;              // 상품 유형 (01: 예금, 02: 적금)
    private String interestMethod;           // 이자 계산 방법

    // 사용자 정보
    private Integer userId;                  // 회원 ID (로그인 정보에서)
    private String userName;                 // 회원명

    // 계산된 정보
    private BigDecimal expectedInterest;     // 예상 이자
    private BigDecimal expectedTotal;        // 예상 만기 수령액

    // 최종 동의
    @AssertTrue(message = "최종 가입 동의가 필요합니다.")
    private Boolean finalAgree;              // 최종 가입 동의
}