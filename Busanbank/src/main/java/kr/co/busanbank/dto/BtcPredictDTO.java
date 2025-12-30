package kr.co.busanbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BtcPredictDTO {
    private int predictId;
    private int userNo;
    private String predict;
    private String result;
    private String isSuccess;
    private String rewardIssued;
    private Integer couponId;
    private Date predictDate;
    private Date createdAt;
}
