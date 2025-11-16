package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private int productNo;
    private String productName;
    private String productType;
    private int categoryId;
    private String description;

    private double baseRate;
    private double maturityRate;
    private double earlyTermInterRate;
    private double monthlyAmount;
    private int savingTerm;
    private double depositAmount;

    private String interestMethod;
    private String payCycle;
    private Date endDate;
    private int adminId;

    private Date createdAt;
    private Date updatedAt;
    private String status;
}
