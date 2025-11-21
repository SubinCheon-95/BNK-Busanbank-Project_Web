package kr.co.busanbank.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantDTO {

    private int consultantId;
    private String consultantName;
    private String specialty;
    private String status;
    private String createdAt;
    private String updatedAt;

}
